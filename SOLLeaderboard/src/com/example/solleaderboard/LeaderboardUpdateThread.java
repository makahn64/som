package com.example.solleaderboard;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

import android.os.Handler;
import android.util.Log;

import com.appdelegates.solnetwork.Gamer;
import com.appdelegates.solnetwork.SOLEvent;
import com.appdelegates.solnetwork.TCPClient;
import com.appdelegates.solnetwork.TCPClient.TCPClientListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class LeaderboardUpdateThread extends Thread {
	// Comment to check on git yo
	public static final String CNAME = "LeaderboardThread";
	
	TCPClient tcpClient100;
	TCPClient tcpClient200;
	TCPClient getEventClient;
	ArrayList<Gamer> top10;
	Boolean mRunning = false;
	TCPClientListener listener;
	Boolean firstFetchDone;
	Boolean secondFetchDone;
	SOLEvent currentEvent;

	private LeaderboardUpdateListener mListener;
	
	public interface LeaderboardUpdateListener {
		
		public void leaderboardUpdate(ArrayList<Gamer> top10);
		public void leaderboardUpdateEvent(String eventName, Boolean shouldShow);
		public void leaderboardNetworkFail(String msg);
		
	}
	
	public void setLeaderboardUpdateListener(LeaderboardUpdateListener listener){
		mListener = listener;
	}
	
	@Override
	public void run(){
		
		mRunning = true;
		
		TCPClientListener listener100 = new TCPClientListener(){

			@Override
			public void connectionEstablished(Handler handler) {
				tcpClient100.sendMessage("getT10Default");
				
			}

			@Override
			public void connectionFailed(Exception e) {
				if (mListener!=null)
					mListener.leaderboardNetworkFail("Failure connecting to 100: "+e.toString());
				tcpClient100.close();
				firstFetchDone = true;  // unblock thread	
				tcpClient200.connect();
			}

			@Override
			public void receivedData(InetAddress inetAddress, String data) {

				// data contains a JSON Array of Gamers
				
				
				Log.i(CNAME, "Recieving data on client 100");
				if	(data=="")
					return;
				
				Gson gson = new Gson();
				JsonParser parser = new JsonParser();
			    JsonArray array = parser.parse(data).getAsJsonArray();
			    
			    for (int i=0; i < array.size(); i++){
			    	Gamer g = gson.fromJson(array.get(i), Gamer.class);
			    	top10.add(g);
			    }
			    
			    firstFetchDone = true;
			    tcpClient100.close();
			    tcpClient200.connect();
			    
				
			}

			@Override
			public void connectionDropped(InetAddress inetAddress, Exception e) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		TCPClientListener listener200 = new TCPClientListener(){

			@Override
			public void connectionEstablished(Handler handler) {
				tcpClient200.sendMessage("getT10Default");
				
			}

			@Override
			public void connectionFailed(Exception e) {
				secondFetchDone = true;  // unblock thread	
				if (mListener!=null)
					mListener.leaderboardNetworkFail("Failure connecting to 200: "+e.toString());
				if (mListener!=null)
					mListener.leaderboardUpdate(top10);
				//tcpClient200.close();
				
				getEventClient.connect();
				
			}

			@Override
			public void receivedData(InetAddress inetAddress, String data) {
				
				Gson gson = new Gson();
				JsonParser parser = new JsonParser();
			    JsonArray array = parser.parse(data).getAsJsonArray();
			    
			    for (int i=0; i < array.size(); i++){
			    	Gamer g = gson.fromJson(array.get(i), Gamer.class);
			    	top10.add(g);
			    }
			    
			    secondFetchDone = true;
				tcpClient200.close();
				
				Collections.sort(top10);
				
				if (mListener!=null)
					mListener.leaderboardUpdate(top10);
				
				getEventClient.connect();
				
			}

			@Override
			public void connectionDropped(InetAddress inetAddress, Exception e) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		TCPClientListener getEventListener = new TCPClientListener() {
			
			@Override
			public void connectionEstablished(Handler handler) {
				getEventClient.sendMessage("getDefaultEvent");
				
			}

			@Override
			public void connectionDropped(InetAddress inetAddress,
					Exception e) {
				Log.i(CNAME, "Connection dropped!");
				
			}

			@Override
			public void connectionFailed(Exception e) {
				Log.e(CNAME, "Can't get events connection!");
				
			}

			@Override
			public void receivedData(InetAddress inetAddress, String data) {
				
				getEventClient.close(); 
				
				if (data.equals(""))
					return;
				
				Gson gson = new Gson();
				currentEvent = gson.fromJson(data, SOLEvent.class);
				
				if (mListener!=null){
					mListener.leaderboardUpdateEvent(currentEvent.eventName, currentEvent.showOnLeaderboard);
				}
				
			   	
				
			}
			
		};
		
		while (mRunning){
			
			// Establish a new connection every pass 
			try {
				tcpClient100 = new TCPClient("192.168.1.100", 6565);
				tcpClient200 = new TCPClient("192.168.1.200", 6565);
				getEventClient = new TCPClient("192.168.1.100", 6565);
			} catch (UnknownHostException e) {
				Log.e(CNAME, "Blew up creating TCP client. Error: " + e.toString());
				e.printStackTrace();
			}
			
			tcpClient100.setTCPClientListener(listener100);
			tcpClient200.setTCPClientListener(listener200);
			getEventClient.setTCPClientListener(getEventListener);
			
			firstFetchDone = secondFetchDone = false;
			
			top10 = new ArrayList<Gamer>();
			top10.clear();
			
			tcpClient100.connect();
			
			
			Log.i(CNAME, "Kicked of requests, taking a nap");
			
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} // Sleep of 30 seconds
			
		}		
		
		
		
	}

}
