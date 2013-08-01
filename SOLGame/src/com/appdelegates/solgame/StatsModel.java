package com.appdelegates.solgame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.appdelegates.solnetwork.Gamer;
import com.appdelegates.solnetwork.SOLEvent;
import com.google.gson.Gson;

public class StatsModel {
	
	public static final String CNAME = "StatsModel";
	static Context mContext;
	DBOpenHelper mDBHelper;
	LocalTCPServer ltcps;
	
	
	public StatsModel(Context context){
		
		mContext = context;
		mDBHelper = new DBOpenHelper(context);
		ltcps = new LocalTCPServer();
		ltcps.start();
		
		try {
			backupDatabase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(CNAME, "Could not backup db on SD: "+e.toString());		
		}
		
		String j = getInterestingStats();
	}

	/**
	 * Convenience record method that sets game time to current system time.
	 * @param gamerTag
	 * @param email
	 * @param score
	 * @param gameType comes from GameEngine
	 */
	public synchronized void recordScore(String gamerTag, String email, int score, String gameType){

		recordScore(gamerTag, email, score, gameType, System.currentTimeMillis());
		
	}
	
	/**
	 * Records score to DB.
	 * @param gamerTag
	 * @param email
	 * @param score
	 * @param gameType comes from GameEngine
	 * @param time millesecond time
	 */
	public synchronized void recordScore(String gamerTag, String email, int score, String gameType, long time){
		
		ContentValues values = new ContentValues();
		
		try {
			
			values.put("gamertag", gamerTag);
			values.put("email", email);
			values.put("game_time",time );
			values.put("game_type", gameType);
			values.put("score", score);
						
		}
		catch (Exception e){
			Log.e("DB", "Failure inserting score: "+e);
		}
		
		SQLiteDatabase db = mDBHelper.getWritableDatabase();		
		db.insert(DBSchema.SCORE_TABLE_NAME, null, values);
		db.close();
		
	}
	
	
	public synchronized void recordEvent(String eventName, String eventNote, long startTime, long endTime){
		
		ContentValues values = new ContentValues();
		
		try {
			
			values.put("event_name", eventName);
			values.put("event_note", eventNote);
			values.put("event_start",startTime );
			values.put("event_end", endTime);
						
		}
		catch (Exception e){
			Log.e("DB", "Failure inserted event: "+e);
		}
		
		SQLiteDatabase db = mDBHelper.getWritableDatabase();		
		db.insert(DBSchema.EVENT_TABLE_NAME, null, values);
		db.close();   
		
	}
	
	public synchronized void log(String logEvent, int complexID, long logTime){
		
		ContentValues values = new ContentValues();
		
		try {
			
			values.put("log_complex_id", complexID);
			values.put("log_time", logTime);
			values.put("log_event",logEvent );
			
						
		}
		catch (Exception e){
			Log.e("DB", "Failure inserted log: "+e);
		}
		
		SQLiteDatabase db = mDBHelper.getWritableDatabase();		
		db.insert(DBSchema.LOG_TABLE_NAME, null, values);
		db.close();   
		
	}
	
	public SOLEvent getCurrentEvent(){
		
		long currentTime = System.currentTimeMillis();
		String sqlGetEventWindow = "SELECT event_start, event_end, event_name, event_note from event_table "+
				"where event_start < " + currentTime + " AND event_end > " + currentTime + " LIMIT 1";

		
		SQLiteDatabase db = mDBHelper.getReadableDatabase();		

		Cursor c = db.rawQuery(sqlGetEventWindow, null);

		if (c.getCount()==0)
			return null;
		
		c.moveToFirst();
		
		SOLEvent rval = new SOLEvent();
		
		rval.startTime = c.getInt(0);
		rval.endTime = c.getInt(1);
		rval.eventName = c.getString(2);
		rval.eventNote = c.getString(3);
		c.close();
		
		return rval;
		
	}
	
	public String getCurrentEventJSON(){
		
		SOLEvent e = getCurrentEvent();
		Gson gson = new Gson();
		return gson.toJson(e);
	}
	
	public SOLEvent getDefaultEvent(){
		SharedPreferences settings = mContext.getSharedPreferences("SETTINGS", 0);
		SOLEvent e = new SOLEvent();
		
		e.endTime = settings.getLong("endTime", -1);
		e.startTime = settings.getLong("startTime", -1);
		e.eventName = settings.getString("eventName", "");
		e.eventNote = settings.getString("eventNote", "");
		e.showOnLeaderboard = settings.getBoolean("showOnLB",false);
		return e;
	}
	
	public String getDefaultEventJSON(){
		
		SOLEvent e = getDefaultEvent();
		Gson gson = new Gson();
		return gson.toJson(e);
	}
	
	public String getTop10ForCurrentEvent(){
		
		SOLEvent e = getCurrentEvent();
		
		if (e==null){
			// no current event
			Gson g = new Gson();
			return (g.toJson(null));
		}
		
		return getTop10InWindow(e.startTime, e.endTime);
		
	}


	public synchronized String getTop10InWindow(long startTime, long endTime){
		
		/*
		 *  Algorithm:  use current system time to find out which event we are in.
		 *  From that event, we get the start/end time.
		 *  If no event, return null.
		 */

		
		String sqlGetTop10 = "SELECT email, gamertag, score, game_time from score_table "+
				"where game_time < " + endTime + " AND game_time > " + startTime + " ORDER BY score DESC LIMIT 10";

		ArrayList<Gamer> top10 = new ArrayList<Gamer>();

		SQLiteDatabase db = mDBHelper.getReadableDatabase();		
		Cursor c = db.rawQuery(sqlGetTop10, null);
		c.moveToFirst();
		
		for (int rows=0; rows < c.getCount(); rows++){
			
			Gamer g = new Gamer(c.getString(0), c.getString(1), c.getInt(2), c.getInt(3));
			top10.add( g );
			c.moveToNext();
		}
		
		c.close();		
		db.close();
		
		Gson gson = new Gson();
		String rjson = gson.toJson(top10);
		
		return rjson;
		
	}
	
	public synchronized String getTop10AllTime(){
		
		/*
		 *  Algorithm:  use current system time to find out which event we are in.
		 *  From that event, we get the start/end time.
		 *  If no event, return null.
		 */

		
		String sqlGetTop10 = "SELECT email, gamertag, score, game_time from score_table ORDER BY score DESC LIMIT 10";

		ArrayList<Gamer> top10 = new ArrayList<Gamer>();

		SQLiteDatabase db = mDBHelper.getReadableDatabase();		
		Cursor c = db.rawQuery(sqlGetTop10, null);
		c.moveToFirst();
		
		for (int rows=0; rows < c.getCount(); rows++){
			
			Gamer g = new Gamer(c.getString(0), c.getString(1), c.getInt(2), c.getInt(3));
			top10.add( g );
			c.moveToNext();
		}
		
		c.close();		
		db.close();
		
		Gson gson = new Gson();
		String rjson = gson.toJson(top10);
		
		return rjson;
		
	}
	
	public synchronized String getAllScores(){
		
		/*
		 *  Algorithm:  use current system time to find out which event we are in.
		 *  From that event, we get the start/end time.
		 *  If no event, return null.
		 */

		
		String sqlGetTop10 = "SELECT email, gamertag, score, game_time, _id from score_table ORDER BY score DESC";

		ArrayList<Gamer> all = new ArrayList<Gamer>();

		SQLiteDatabase db = mDBHelper.getReadableDatabase();		
		Cursor c = db.rawQuery(sqlGetTop10, null);
		c.moveToFirst();
		
		for (int rows=0; rows < c.getCount(); rows++){
			
			Gamer g = new Gamer(c.getString(0), c.getString(1), c.getInt(2), c.getInt(3));
			g.dbIdx = c.getInt(4);
			all.add( g );
			c.moveToNext();
		}
		
		c.close();		
		db.close();
		
		Gson gson = new Gson();
		String rjson = gson.toJson(all);
		
		return rjson;
		
	}
	
	public String getInterestingStats() {
		
		Hashtable<String, Object> results = new Hashtable<String, Object>();
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		String sqlGetGameCount = "SELECT COUNT(*) from score_table";
				
		Cursor c = db.rawQuery(sqlGetGameCount, null);
		c.moveToFirst();
		int games = Integer.valueOf(c.getInt(0));
		results.put("numberOfGames", games);
		results.put("totalTimePlayed", Float.valueOf( (float) (((float)games*40.0)/60.0)) );
		c.close();
		
		String sqlGetUniqueGamerCount = "SELECT COUNT(*) from score_table GROUP BY gamertag";		
	    c = db.rawQuery(sqlGetUniqueGamerCount, null);
		c.moveToFirst();
		int ugamers = Integer.valueOf(c.getInt(0));
		results.put("uniqueGamers", ugamers);
		c.close();
		
		String allTimeHS = "SELECT email, gamertag, score from score_table ORDER BY score DESC";
		c = db.rawQuery(allTimeHS, null);
		if (c.getCount()>0){
			c.moveToFirst();
			String aths = "" + c.getInt(2) + " by " + c.getString(1) + " (" + c.getString(0) + ")";
			results.put("allTimeHigh", aths);		
		} else
			results.put("allTimeHigh", 0);	
		
		c.close();
		
		SOLEvent thisEvent = getDefaultEvent();
		String eventTimeHS = "SELECT email, gamertag, score from score_table WHERE "+
				" game_time < " + thisEvent.endTime + " AND game_time > " + thisEvent.startTime +
				" ORDER BY score DESC LIMIT 1";
		c = db.rawQuery(eventTimeHS, null);
		if (c.getCount()>0){
			c.moveToFirst();
			String eths = "" + " by " + c.getString(1) + " (" + c.getString(0) + ")";
			results.put("eventTimeHigh", eths);	
		} else {
			results.put("eventTimeHigh", 0);	
		}
	
		c.close();
		
		db.close();
		
		Gson gson = new Gson();
		String rval = gson.toJson(results);
		return rval;
	}

	
	public void eraseScore(int scoreDBIdx) {
		
		String sqlNukeScore = "DELETE from score_table WHERE _idx = " + scoreDBIdx;
		SQLiteDatabase db = mDBHelper.getWritableDatabase();		
		db.execSQL(sqlNukeScore);
		
	}
	
	public void eraseScore(String gamertag, int score) {
		
		String sqlNukeScore = "DELETE from score_table WHERE gamertag = \"" + gamertag + 
				"\" AND score = "+score;
		SQLiteDatabase db = mDBHelper.getWritableDatabase();		
		db.execSQL(sqlNukeScore);
		
	}
 
	

	/**
	 * Inserts a bunch of dummy scores for testing
	 * @param time
	 */
	
	public synchronized void insertTestScoresAroundTime(long time, String tag){
		
		for (int i=0; i<2; i++){
			
			recordScore(tag + "TestOne", "gt1@test.com", new Random().nextInt(200), "testGame", time++);
			recordScore(tag + "TestTwo", "gt2@test.com", new Random().nextInt(200), "testGame", time++);
			recordScore(tag + "TestThree", "gt3@test.com", new Random().nextInt(200), "testGame", time++);
			recordScore(tag + "TestFour", "gt4@test.com", new Random().nextInt(200), "testGame", time++);
			recordScore(tag + "TestFive", "gt5@test.com", new Random().nextInt(200), "testGame", time++);
			
			recordScore(tag + "TestSix", "gt6@test.com", new Random().nextInt(200), "testGame", time++);
			recordScore(tag + "TestSeven", "gt7@test.com", new Random().nextInt(200), "testGame", time++);
			recordScore(tag + "TestEight", "gt8@test.com", new Random().nextInt(200), "testGame", time++);
			recordScore(tag + "TestNine", "gt9@test.com", new Random().nextInt(200), "testGame", time++);
			recordScore(tag + "TestTen", "gt10@test.com", new Random().nextInt(200), "testGame", time++);
		}
		
		
	}
	
	public void deleteAllTestScores(){
		
		String sqlNukeTestData = "DELETE from score_table WHERE game_type=\"testGame\"";
		SQLiteDatabase db = mDBHelper.getWritableDatabase();		
		db.execSQL(sqlNukeTestData);
		
	}
	
	public class LocalTCPServer extends Thread {
		 
	    public static final int SERVERPORT = 6565;
	    private boolean running = false;
	    private PrintWriter mOut;
	    Socket client;
	    BufferedReader in ;
	   
	 
	    /**
	     * Method to send the messages from server to client
	     * @param message the message sent by the server
	     */
	    public void sendMessage(String message){
	        if (mOut != null && !mOut.checkError()) {
	            mOut.println(message+"\n");
	            mOut.flush();
	        }
	    }
	 
	    @Override
	    public void run() {
	        super.run();
	 
	        running = true;
	 
	        try {
	            Log.i(CNAME, "Starting StatsModel TCP Server Thread");
	 
	            //create a server socket. A server socket waits for requests to come in over the network.
	            ServerSocket serverSocket = new ServerSocket(SERVERPORT);
	 
	            //create client socket... the method accept() listens for a connection to be made to this socket and accepts it.
	            
	            while (running) {
	            	
	            	client = serverSocket.accept();
		            
		            try {
		 
		                //sends the message to the client
		                mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
		 
		                //read the message received from client
		                in = new BufferedReader(new InputStreamReader(client.getInputStream(), "ISO-8859-15"));
		 
		                //in this while we wait to receive messages from client (it's an infinite loop)
		                //this while it's like a listener for messages
		                String message = in.readLine();
		 
		                if (message != null) {
		                        
		                    // Need to process message
		                	Log.i(CNAME, "StatsModel processing tcp message: "+message);
		                    	
		                   	String[] components = message.split("/");
		                    	
		                   	String command = "";
		                   	try {
		                   		command = components[0].trim();
		                   	} catch (Exception e){
		                   		Log.e(CNAME, "Bad TCP command, ignoring!");
		                   	}
		                    	
		                    	
		                    	
		                   	if (command.compareTo("getTop10ThisEvent")==0){
		                  		// send back the top 10 for this event
		                    		
		                   		sendMessage(getTop10ForCurrentEvent());
		                   	} else if (command.compareTo("getThisEvent")==0){
		                    		// get current event name
		                    		
		                   		sendMessage(getCurrentEventJSON());
		                    		
		                   	} else if (command.compareTo("getDefaultEvent")==0){
	                    		// get current event name
	                    		
		                   		sendMessage(getDefaultEventJSON());
	                    		
		                   	} else if (command.compareTo("getT10All")==0){
		                    		// send back the top 10 for all time
		                    		
		                   		sendMessage(getTop10AllTime());
		                    		
		                   	} else if (command.compareTo("getT10Default")==0){
	                    		// send back the top 10 for for the default event
	                    		SOLEvent e = getDefaultEvent();
	                    		if (e.startTime==-1)
	                    			sendMessage(getTop10AllTime());
	                    		else {
	                    			// we actually have a default event, yo
	                    			sendMessage(getTop10InWindow(e.startTime, e.endTime));
	                    		}
	                    		
		                   	} else if (command.compareTo("getAllScores")==0){
		                    		// send back the entire DB!
		                    	deleteAllTestScores();	
		                   		sendMessage(getAllScores());
		                    		
		                   	} else if (command.compareTo("setEvent")==0){
		                    		// set the event info based on url
		                    		
		                   		try {
	                    			String eventName = components[1];
		                    		String eventNote = components[2];			                    		
		                    		long startTime = Long.parseLong(components[2]);
			                    	long endTime = Long.parseLong(components[3]);
			                    	recordEvent(eventName, eventNote, startTime, endTime);
		                    	} 
		                    	catch (Exception e){
		                    		sendMessage("error/"+e.toString());
		                    	}
		                    		
		                    		
		                    } else if (command.compareTo("setDefaultEvent")==0){
	                    		// set the event info based on url
	                    		
		                    	try {
		                    		String eventName = components[1];
		                    		String eventNote = components[2];			                    		
		                    		long startTime = Long.parseLong(components[3]);
		                    		long endTime = Long.parseLong(components[4]);
		                    		recordEvent(eventName, eventNote, startTime, endTime);
		                    		
		                    		// Now add these to the SharedPrefs
		                    		SharedPreferences settings = mContext.getSharedPreferences("SETTINGS", 0);
		                    		SharedPreferences.Editor editor = settings.edit();
		                    	      
		                    		editor.putLong("startTime", startTime);
		                    		editor.putLong("endTime", endTime);
		                    		editor.putString("eventName", eventName);
		                    		editor.putString("eventNote", eventNote);
		                    		editor.putBoolean("showOnLB", components[5].equals("true"));
		                    		
		                    	    editor.commit();
		                    	    
		                    	    sendMessage("gotityo");
		                    	    
		                    	} 
		                    	catch (Exception e){
		                    		sendMessage("error/"+e.toString());
		                    	}
	                    		
	                    		
		                    }
		                   	
		                   	else if (command.compareTo("removeScoreByIdx")==0){
		                    	// set the event info based on url
		                    		
		                    	try {
		                    		int scoreDBIdx = Integer.parseInt(components[1]);
			                    	eraseScore(scoreDBIdx);
		                    	} 
		                    	catch (Exception e){
		                    		sendMessage("error/"+e.toString());
		                    	}
		                    		
		                    		
		                    } else if (command.compareTo("removeScore")==0){
		                    	// set the event info based on url
		                    		
		                    	try {
		                    		String gamertag = components[1];
		                    		int score = Integer.parseInt(components[2]);
		                    		eraseScore(gamertag, score);
		                    	} 
		                    	catch (Exception e){
		                    		sendMessage("error/"+e.toString());
		                    	}
		                    		
		                    		
		                    }// end of command parsing
		                   	
		                   	
		                    //client.close(); // see ya
		                }
		 
		            } catch (Exception e) {
		                Log.e(CNAME, "Error" + e.toString());
		                e.printStackTrace();
		            } finally {
		            	in.close();
		                client.close();
		                Log.i(CNAME, "Finally executing. Client closed.");
		            }
		 
	            } // while running
	 
	        } catch (Exception e){
	        	Log.e(CNAME, "Could not open socket server! Error: " + e.toString());
	        }
	    } // end of run()
	    
	    public void kill(){
	    	
	    	this.interrupt();
	    	
	    	if (mOut!=null)
	    		mOut.close();
	    	try {
				in.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	try {
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    }
	} // end of localTCPServer

	public void kill() {
		
		ltcps.kill();
	}
	
	public static void backupDatabase() throws IOException {
	    //Open your local db as the input stream
	    String inFileName = mContext.getDatabasePath(DBSchema.SOL_DATABASE_FILENAME).getPath();
	    File dbFile = new File(inFileName);
	    FileInputStream fis = new FileInputStream(dbFile);

	    String outFileName = Environment.getExternalStorageDirectory()+"/database.sqlite";
	    //Open the empty db as the output stream
	    OutputStream output = new FileOutputStream(outFileName);
	    //transfer bytes from the inputfile to the outputfile
	    byte[] buffer = new byte[1024];
	    int length;
	    while ((length = fis.read(buffer))>0){
	        output.write(buffer, 0, length);
	    }
	    //Close the streams
	    output.flush();
	    output.close();
	    fis.close();
	}
	
}
