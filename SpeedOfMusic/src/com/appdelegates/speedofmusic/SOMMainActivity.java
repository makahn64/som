package com.appdelegates.speedofmusic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appdelegates.solnetwork.Gamer;
import com.appdelegates.solnetwork.IPAddressHelper;
import com.appdelegates.solnetwork.SOLMessage;
import com.appdelegates.solnetwork.TCPServer;
import com.appdelegates.solnetwork.UDPListener2;
import com.appdelegates.solnetwork.UDPSendQueue2;
import com.appdelegates.speedofmusic.AttractAnimation.AttractAnimListener;
import com.appdelegates.speedofmusic.HandsetController.HandsetControllerListener;
import com.appdelegates.speedofmusic.SOLGameEngineThread.UiStateChangeListener;


public class SOMMainActivity extends Activity implements HandsetControllerListener {
	
	public static Boolean DEBUG = true;
	
	public static final int MSPF = 20; // millisecond per frame. 66 = 15fps.
	
	public static final String CNAME = "SOMMain";
	
	public enum State { ATTRACT, WAIT_FOR_START, COUNTDOWN, PLAYING, ENDING, GAMEOVER };
	public State mState;
	
	
	SOLGameEngineThread currentGameEngine;
	int chosenGameEngine;
	
	HandsetController handsetController;
		
	TextView scoreTV;
	TextView errorTV;
	TextView timeTV;
	TextView timeTV2;
	TextView gamerTag;
	TextView multLeft;
	ImageView dotView;
	TextView upNextTV;
	
	TextView countTV;
		
	Boolean showStart;
	
	LocalTCPServer tcpServer;
	
	Handler mUIHandler = null;
	
	int mIPOffset;
	
	Boolean gameRunning = false;
	
	Runnable frameTimer;
	CountDownTimer gameTimer;
	
	long gameClock;
	long mFrameNum = 0;
	
	DecimalFormat timeFormat;
	
	ArrayList<Bitmap> dotBitmaps = new ArrayList<Bitmap>();
	
	int [] dotImages = { R.drawable.score_1, R.drawable.score_2, R.drawable.score_3, R.drawable.score_4 };
	
	ArrayList<Gamer> gamersArray;
	Gamer currentGamer;

	
	AttractAnimation attractAnim;
	//StatsModel stats;
	private int gameEngineIndex = 0;
	

	@Override
	public void onBackPressed() {
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scoreboardatt);
		
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
						
		timeFormat = new DecimalFormat("00.0");
		
		Typeface face=Typeface.createFromAsset(getAssets(),
                 "fonts/omnesmed.otf");

		timeTV = (TextView)findViewById(R.id.textViewTime);
		timeTV.setTypeface(face);
		
		timeTV2 = (TextView)findViewById(R.id.textViewTimeHigh);
		timeTV2.setTypeface(face);
		timeTV2.setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				
				switch (gameEngineIndex){
				case 0:
					gameEngineIndex = 1;
					toastMe("Switching to Non-Sweep Game");
					break;
				case 1:
					gameEngineIndex = 0;
					toastMe("Switching to Sweep Game");
					break;
				}
				return true;
			}
			
		});
		
		gamerTag = (TextView)findViewById(R.id.textViewGamertag);
		gamerTag.setTypeface(face);
		gamerTag.setText("");
				
		upNextTV = (TextView)findViewById(R.id.textViewUpNext);
		upNextTV.setTypeface(face);
		upNextTV.setText("");
					
		scoreTV = (TextView)findViewById(R.id.textViewScore);
	    scoreTV.setTypeface(face);
	    
	    multLeft = (TextView)findViewById(R.id.textViewMultL);
	    multLeft.setTypeface(face);
	    multLeft.setText("");
	    	    
		errorTV = (TextView)findViewById(R.id.textViewError);	
		errorTV.setVisibility(View.VISIBLE);
		
		dotView = (ImageView)findViewById(R.id.imageViewDot);
		dotView.setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View arg0) {
				changeState(State.WAIT_FOR_START);
				return true;
			}
			
		});
		
	    checkIPAddress();
		
		
		for (int i=0; i<dotImages.length; i++)
			dotBitmaps.add(BitmapFactory.decodeResource(getResources(), dotImages[i]));
		
		dotView.setImageBitmap(dotBitmaps.get(0));
		
		gamersArray =new ArrayList<Gamer>();
					
		
		
		
		
	}
	
	@Override
	protected void onResume(){
		
		ArrayList<MusicEvent> mixQueueTest = new ArrayList<MusicEvent>();
		mixQueueTest.add(new MusicEvent(0, R.raw.basebeat, MusicEvent.EventType.START_LOOPING));
		//mixQueueTest.add(new MusicEvent(1000, R.raw.musicaleffect1, MusicEvent.EventType.SINGLE_PLAY));
		mixQueueTest.add(new MusicEvent(5000, R.raw.basebeat, MusicEvent.EventType.STOP_LOOPING));
		
		AudioMixer testMixer = new AudioMixer(this, 2, 44100, mixQueueTest);
		
		long startMix = System.currentTimeMillis();
		
		try {
			testMixer.mix();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long endMix = System.currentTimeMillis();
		
		int mixTime =(int) (endMix - startMix);
		
		gameClock = mixTime;
		
	    mUIHandler = new Handler();
	    
		handsetController = new HandsetController(mIPOffset, 5, 3);
		handsetController.addCommandListenerForCommand(SOLMessage.START_GAME, this);
		
		tcpServer = new LocalTCPServer();
		tcpServer.start();
		
		//stats = new StatsModel(getApplicationContext());
	

		// Have to force initial state to GAMEOVER to allow switch to ATTRACT
		
		mUIHandler.postDelayed(new Runnable(){

			@Override
			public void run() {
				mState = State.GAMEOVER;
				changeState(State.ATTRACT);
				
			}
			
		}, 500);
		
		
		updateUI();

		super.onResume();

		
	}
	
	@Override
	protected void onPause(){
		
		try {
			mUIHandler.removeCallbacksAndMessages(null);
			
			if (attractAnim!=null){
				attractAnim.stop();
				attractAnim = null;
			}
			
			if (currentGameEngine!=null){
				currentGameEngine.kill();
				currentGameEngine = null;
			}
			
			if (handsetController!=null){
				handsetController.kill();
				handsetController = null;
			}
			
			/*
			if (stats!=null){
				stats.kill();
				stats = null;
			}
			*/
			
			
			if (tcpServer!=null){
				tcpServer.kill();
				tcpServer = null;
			}
			
		} catch (Exception e){
			
			Log.e(CNAME, "Failure in onPause(): "+e.toString());
		}
		
		
		
		super.onPause();
		
	}
	
	public Boolean getBooleanSetting(String key){
		
		SharedPreferences settings = getSharedPreferences("SETTINGS", 0);
		return settings.getBoolean(key, false);
		
	}
	
	public void setBooleanSetting(String key, Boolean val){
		
		SharedPreferences settings = getSharedPreferences("SETTINGS", 0);
		SharedPreferences.Editor editor = settings.edit();
	      
		editor.putBoolean(key, val);
	    editor.commit();
		
	}
	
	
	public void recordScore(Gamer g){
		
		//stats.recordScore(g.getGamertag(), g.getEmail(), g.getScore(), currentGameEngine.getGameName());
		
	}

	
	public void executeStart(){
		
		changeState(State.COUNTDOWN);
		
	};
	
	public void changeState(State newState){
		
		
		switch( newState ){
		
		case ATTRACT:
			
			// Can only switch to attract from GAMEOVER, otherwise ignore message
			if ( mState == State.GAMEOVER ){
				Log.i(CNAME, "Switching to ATTRACT state.");
				mState = State.ATTRACT;
				enterAttractState();
			} else {
				Log.e(CNAME, "Illegal state change to Attract from: "+mState);
			}
			break;
			
		case WAIT_FOR_START:
			
			// Can only switch to prep from ATTRACT or GAMEOVER, otherwise ignore message
			if ( (mState == State.GAMEOVER) || (mState == State.ATTRACT) ){
				Log.i(CNAME, "Switching to PREP state.");
				mState = State.WAIT_FOR_START;
				enterWaitForStartState();
			} else {
				Log.e(CNAME, "Illegal state change to WAIT_FOR_START from: "+mState);
			}
			break;
			
		case COUNTDOWN:
			
			// Can only switch to attract from ATTRACT or GAMEOVER, otherwise ignore message
			if ( mState == State.GAMEOVER  || mState == State.ATTRACT  || mState == State.WAIT_FOR_START){
				Log.i(CNAME, "Switching to COUNTDOWN state.");
				mState = State.COUNTDOWN;
				countdown();
			} else {
				Log.e(CNAME, "Illegal state change to Countdown from: "+mState);
			}
			break;
			
		
		case PLAYING:
		
			// Can only switch to PLAYING from COUNTODWN, otherwise ignore message
			if ( mState == State.COUNTDOWN  ){
				Log.i(CNAME, "Switching to PLAYING state.");
				mState = State.PLAYING;
				startGame();
			} else {
				Log.e(CNAME, "Illegal state change to PLAYING from: "+mState);
			}
			break;
			
		case ENDING:
			
			// Can only switch to ENDING from PLAYING, otherwise ignore message
			if ( mState == State.PLAYING  ){
				Log.i(CNAME, "Switching to ENDING state.");
				mState = State.ENDING;
				enterEndGameState();
			} else {
				Log.e(CNAME, "Illegal state change to ENDING from: "+mState);
			}
			break;
			
		case GAMEOVER:
			
			if (currentGameEngine!=null){
				currentGamer.setScore(currentGameEngine.getScore());
				recordScore(currentGamer);
				// This is a common cleanup/reset state. Can enter from anywhere.
				Log.i(CNAME, "Switching to GAMEOVER state.");
			}
				
			
			mState = State.GAMEOVER;
			gameOver();
				
		
		} // end of Switch
			
	}
	

	
	public void enterAttractState(){
		
		Log.i(CNAME, "Entering Attract State.");
		attractAnim = new AttractAnimationVUMeter(handsetController);
		attractAnim.setAttractAnimListener(new AttractAnimListener(){

			@Override
			public void loopDone() {
				// check to see if there's anyone on deck
				
				if (gamersArray.size()>0){
					attractAnim.stop();
					// need to call from main thread!
					mUIHandler.post(new Runnable(){

						@Override
						public void run() {
							changeState(State.WAIT_FOR_START);
						}
						
					});
					
				}
				
				
			}
			
		});
		
		attractAnim.start();
	}
	
	public void enterWaitForStartState(){
		
		Log.i(CNAME, "Entering Wait for Start State.");
		// pull the head of the line
		attractAnim.stop();
		newPlayer();
		
		// The stop process on attract anim requires a bit of time to complete...
		
		mUIHandler.postDelayed(new Runnable(){

			@Override
			public void run() {
				handsetController.startButton();
				
			}
			
		}, 1500); 
		
		

		
	}
	
	private void countdown(){
		
		attractAnim.stop();
		
		handsetController.countdown();
		// Countdown should take about 5 seconds. Then we need to go to starting the game
		
		mUIHandler.postDelayed(new Runnable(){

			@Override
			public void run() {
				changeState(State.PLAYING);				
			}
			
		}, 5000);
		
	}

	public void startGame(){
		
		
		currentGameEngine = new SOMGameEngineThread(handsetController, this);			
		
		currentGameEngine.start();
		
		currentGameEngine.setUiStateChangeListener(new UiStateChangeListener(){

			@Override
			public void stateChange(final int state) {
				
				mUIHandler.post(new Runnable(){

					@Override
					public void run() {
						dotView.setImageBitmap(dotBitmaps.get(state));						
					}
				});
			}
		});
		
		gameClock = currentGameEngine.getGameLengthInSeconds();

		gameRunning = true;
		

		
		// The countdown timer isn't perfectly accurate so make it longer than 30 seconds
		long gameLengthMS =  currentGameEngine.getGameLengthInSeconds() * 1000;
		
		gameTimer = new CountDownTimer(gameLengthMS, 200){

			@Override
			public void onFinish() {
				gameClock = 0;
				updateUI();
				Log.i("*****", "Finished");
				changeState(State.ENDING);
				currentGameEngine.getGameEngineHandler().getLooper().quit();
			}

			@Override
			public void onTick(long timeLeft) {
				gameClock = timeLeft/100;		
				Handler cgem = currentGameEngine.getGameEngineHandler();
				Message hbm = cgem.obtainMessage();
				hbm.arg1 = 99;
				hbm.obj = Long.valueOf(timeLeft);
				currentGameEngine.getGameEngineHandler().sendMessage(hbm);
				//currentGameEngine.handleTime(timeLeft);
				updateUI();
				
			}
			
		};
		
		// give the game engine some time to spin up
		mUIHandler.postDelayed(new Runnable(){

			@Override
			public void run() {
				gameTimer.start();
				
			}
			
		}, 250);
		
				
	}
	
	
	private void gameOver(){
		
		
		
		handsetController.gameOver();
		///widgetMap.gameOver();
		///widgetMap.gameOver();
		
		
	mUIHandler.postDelayed(new Runnable(){

			@Override
			public void run() {
				changeState(State.ATTRACT);
			}
				
		}, 10000);

		
	}
	
	public void enterEndGameState(){
		
		Log.i(CNAME, "Entering Endgame State.");		
		
		
		// This is a little hackish right now. The idea is the EndGameState can do like a bonus or closing anim
		changeState(State.GAMEOVER);
		
	}
	
	public void updateUI(){
		
		//mFrameNum++;
		
		// Game clock is 10x time we want to display
		
		timeTV.setText(""+String.format("" + gameClock%10));
		timeTV2.setText(""+ gameClock/10+".");
		if (currentGameEngine!=null) {
			multLeft.setText("multiplier: "+currentGameEngine.getMultiplier()+"X");
			scoreTV.setText(""+currentGameEngine.getScore());
		} else {
			scoreTV.setText("0");
		}

		//errorTV.setText(""+mFrameNum);
		
		
	}
	
	private void newPlayer(){
		
		if ( gamersArray.size()>0 )
			currentGamer = gamersArray.remove(0);
		else if (currentGamer==null)
			currentGamer = new Gamer("att@att.att", "at&t4Glte!");
		
		updateGamertags();
	}
	
	private void updateGamertags(){
				
		if (currentGamer!=null)
			gamerTag.setText(currentGamer.getGamertag());
		
		if (gamersArray.size()>0)
			upNextTV.setText("Up next: "+gamersArray.get(0).getGamertag());
		else
			upNextTV.setText("");
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_solscore_main, menu);
		return true;
	}
	
	public void checkIPAddress(){
		
		String ip = IPAddressHelper.getLocalIpAddress(getApplicationContext());
		String function = IPAddressHelper.getFunctionForIp(ip);
		
		if (function.equals("Scoreboard A") || function.equals("Scoreboard B")){
			errorTV.setVisibility(View.INVISIBLE);
			
			// Used to calculate button numbers
			if ( function.equals("Scoreboard A") )
				mIPOffset = 110;
			else
				mIPOffset = 210;
			
		} else {
			errorTV.setText("Wrong IP for Scoreboard. This tablet is "+ip +" . Must be 192.168.1.100 or 192.168.1.200.");
		}
	}

	
	public void toastMe(String toastage){
		
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, toastage, duration);
		toast.show();
	}

	@Override
	public void handsetMessageReceived(int shortIP, int row, int column,
			byte command) {
		
		if ( command == SOLMessage.START_GAME ) {
			
			// This gets called from a background thread, so we need to move the work to this thread....
			mUIHandler.post(new Runnable(){
				
				@Override
				public void run() {
					executeStart();					
				}
			});
			
		}
		
	}

	public class LocalTCPServer extends Thread {
		 
	    public static final int SERVERPORT = 6466;
		private static final String CNAME = "MainActivityTCPServer";
	    private boolean running = false;
	    private PrintWriter mOut;
		private BufferedReader in;
		private Socket client;
	   
	 
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
	            Log.i(CNAME, "Starting MainActivity TCP Server Thread");
	 
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
		                	Log.i(CNAME, "MainActivity processing tcp message: "+message);
		                    	
		                   	String[] components = message.split("/");
		                    	
		                   	String command = "";
		                   	try {
		                   		command = components[0].trim();
		                   	} catch (Exception e){
		                   		Log.e(CNAME, "Bad TCP command, ignoring!");
		                   	}
		                    	
		                    	
		                    	
		                   	if (command.compareTo("addGamer")==0){
		                  		// expect /email/gamertag
		                   		
		                   		try {
		                   			gamersArray.add(new Gamer(components[1], components[2]));
		                   			gamerTag.post(new Runnable(){

										@Override
										public void run() {
											updateGamertags();
											
										}
		                   				
		                   			});
		                   		}
		                   		catch (Exception e){
		                   		
		                   			sendMessage("/error/"+e.toString());
		                    		
		                   		}
		                   		
		                    } // end of command parsing
		                   	
		                   	
		                   
		                }
		 
		            } catch (Exception e) {
		                Log.e(CNAME, "Error" + e.toString());
		                e.printStackTrace();
		            } finally {
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
	
	
			

	
}
