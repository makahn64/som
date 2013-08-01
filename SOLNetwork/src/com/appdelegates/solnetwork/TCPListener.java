package com.appdelegates.solnetwork;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import android.os.Handler;
import android.os.Message;

public class TCPListener {

	private static final Boolean DEBUG = false;
	Handler mHandler = null;
	public int mPort;
	Boolean listenerAlive = true;
	
	ServerSocket listenSocket = null;
	Socket connectionSocket;
	
	ListenerThread listenerThread;
	
	public TCPListener(int port, Handler handler){
		
		mPort = port;
		mHandler = handler;
		
		listenerThread = new ListenerThread();		
		listenerThread.start();
		
	}
	
		
	public void kill() throws IOException{
		
		listenerAlive = false;
		listenerThread.interrupt();
		if (listenSocket!=null)
			listenSocket.close();
	}

	
	class ListenerThread extends Thread {
		
		@Override
		public void run() {
			
			try {
				listenSocket = new ServerSocket(mPort);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		    
		    try {
		    	
		    	while (true){
		    		
		    		connectionSocket = listenSocket.accept();
			    				    	
		    		while (true){
		    			
		    			DataInputStream dis = new DataInputStream(connectionSocket.getInputStream());
				        
				    	
				    	int len = dis.readInt();
				    	byte[] packet = new byte[len];
				    	dis.readFully(packet);
				    	
				        ByteBuffer bb = ByteBuffer.allocateDirect(len);
				        bb.put(packet);
				        bb.flip();
				        byte retries = bb.get();
				        int packetNumber = bb.getInt();
				        int remaining = bb.remaining();
				        byte[] payload = new byte[remaining];
				        bb.get(payload, 0, bb.remaining());
				        	
					    //Log.i("UDP Listener", "received: " + text);
					    Message msg = mHandler.obtainMessage();
					    msg.obj = new UDPMessageRX( connectionSocket.getInetAddress(), payload );
					    mHandler.sendMessage(msg);
		    			
		    		}
			    	
			        			    		    		
		    	}
		    	
		        
		    } catch (Exception e) {
		        //if timeout occurs
		        e.printStackTrace();
		        try {
					connectionSocket.close();
					listenSocket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		    } 
		   
		} // run
		
		
		public void killListener(){
			listenerAlive = false;
			
		}
		
	} // class
	
	
	
	
	
}
