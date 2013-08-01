package com.appdelegates.solnetwork;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class TCPServerInstanceThread extends Thread {
	
	public static final String CNAME = "TCPServerInstance";

	private static final boolean DEBUG = true;
	
	Handler mHandler;
	Socket mSocket;
	Boolean running;
	
	BufferedReader inFromClient;
	
	TCPServerInstanceListener mListener;
	TCPServerInstanceTXThread mTxThread;

	private DataOutputStream outToClient;
	
	
	public TCPServerInstanceThread(Socket socket) throws IOException{
		mSocket = socket;
		mListener = null;
		
		inFromClient = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
		outToClient = new DataOutputStream(mSocket.getOutputStream());
		
	}
	
	
	
	public interface TCPServerInstanceListener {
		
		public void serverInstanceStarted(InetAddress inetAddress);
		public void serverInstanceFailed(InetAddress inetAddress, Exception e);
		public void dataReceived(InetAddress inetAddress, String rxData);
		public void clientDropped(InetAddress inetAddress);
		
	}
	
	public void setTCPServerInstanceListener(TCPServerInstanceListener listener){
		mListener = listener;
	}
	
	public void sendData(String data){
		
		Message msg = mHandler.obtainMessage();
		msg.obj = data;
		mHandler.sendMessage(msg);
	}
	
	@Override
	public void run() {
		
		// At this point we are cool, we can start up the send thread
		
		mTxThread = new TCPServerInstanceTXThread();
		mTxThread.start();
		running = true;
		
		while (running){
	    			
	    	try {
			    String line = inFromClient.readLine();
			    if (DEBUG)
			    	Log.i(CNAME, "Got "+line);
			    if (mListener != null) {
			    	mListener.dataReceived(mSocket.getInetAddress(), line);
			    }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				if (mListener!=null){					
					mListener.serverInstanceFailed(mSocket.getInetAddress(), e);
					mListener.clientDropped(mSocket.getInetAddress());
				}
				e.printStackTrace();
				running = false;
			}
			        			    	
	    			
	    }
		    	
		        			    		    		
	}
	
	public class TCPServerInstanceTXThread extends Thread {
		
		@Override
		public void run(){
			
			Looper.prepare();
			
			 
			mHandler = new Handler(){
					
				public void handleMessage(Message msg){
						
					// Message should only be a string
						
				String toSend = (String)msg.obj + "\n";
						try {
							outToClient.writeBytes(toSend);
							outToClient.flush();
						} catch (IOException e) {
							// This is probably pretty bad and this Socket should be shut
							if (mListener != null)
								mListener.serverInstanceFailed(mSocket.getInetAddress(), e);
							e.printStackTrace();
						}
						
						
					}
				};
				
				
			Looper.loop();
			
			
			
		}
	}
	

}
