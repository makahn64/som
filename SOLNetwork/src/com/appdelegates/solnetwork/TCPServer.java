package com.appdelegates.solnetwork;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.appdelegates.solnetwork.TCPServerInstanceThread.TCPServerInstanceListener;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

public class TCPServer {

	private static final Boolean DEBUG = false;

	public static final String CNAME = "TCPServer";
	
	TCPServerListener mListener;
	
	public int mPort;
	Boolean listenerAlive = true;
	
	ServerSocket listenSocket = null;
	
	ServerFactoryThread factory;
	
	SparseArray<TCPServerInstanceThread> instanceThreads = new SparseArray<TCPServerInstanceThread>();
	
	public TCPServer(int port){
		
		mPort = port;		
		factory = new ServerFactoryThread();
		mListener = null;
		
	}
	
	public void startServer(){
		factory.start();
	}
	
	public void setTCPServerListener(TCPServerListener listener){
		mListener = listener;
	}
	
	public interface TCPServerListener {
		
		public void serverConnectionListenerStarted();
		public void serverConnectionListenerFailed(Exception e);
		public void serverAcceptedNewConnection(InetAddress inetAddress);
		public void dataReceived(InetAddress inetAddress, String rxData);
		public void clientDropped(InetAddress inetAddress);
		
	}
		
	public void kill() {
		

	}
	
	public int getIntForInetAddress(InetAddress inetAddress){
		byte[] bytes = inetAddress.getAddress();
		
	    int intIPAddress =
		         ((bytes [0] & 0xFF) << (24)) +
		         ((bytes [1] & 0xFF) << (16)) +
		         ((bytes [2] & 0xFF) << (8)) +
		         (bytes [3] &  0xFF);
	    
	    return intIPAddress;
	}

	class ServerFactoryThread extends Thread implements TCPServerInstanceListener {
		
		
		
		@Override
		public void run(){
			
			try {
				listenSocket = new ServerSocket(mPort);
			} catch (IOException e1) {

				Log.e("TCPServer", "Failed connecting to ServerSocket");
				if (mListener!=null)
					mListener.serverConnectionListenerFailed(e1);
				e1.printStackTrace();
			}
			
			while (true){
				
				

				try {
					Socket connectionSocket = listenSocket.accept();
					
					TCPServerInstanceThread lt = new TCPServerInstanceThread(connectionSocket);
					lt.setTCPServerInstanceListener(this);
					lt.start();
					instanceThreads.append(getIntForInetAddress(connectionSocket.getInetAddress()), lt);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
		}

		@Override
		public void serverInstanceStarted(InetAddress inetAddress) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void serverInstanceFailed(InetAddress inetAddress, Exception e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dataReceived(InetAddress inetAddress, String rxData) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void clientDropped(InetAddress inetAddress) {
			// TODO Auto-generated method stub
			
		}

		
		
	}
	
	public void sendData(InetAddress client, String data){
		
		TCPServerInstanceThread tsi = instanceThreads.get(getIntForInetAddress(client));
		tsi.sendData(data);
		
	}
	
	// This class was used for testing as a replacement for ServerInstances
	class ListenerThread extends Thread {
		
		Handler lHandler;
		Socket mSocket;
		
		public ListenerThread(Socket socket){
			mSocket = socket;
		}
		
		@Override
		public void run() {
			
			BufferedReader inFromClient = null;
			try {
				inFromClient = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			DataOutputStream outToClient = null;
			try {
				outToClient = new DataOutputStream(mSocket.getOutputStream());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
                   
			
			while (true){
		    			
		    	try {
				    String line = inFromClient.readLine();
				    outToClient.writeBytes("Copy> "+line + "\n");
				    outToClient.flush();
				    Log.i(CNAME, "Got "+line);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				        
				    	
		    			
		    }
			    	
			        			    		    		
		}
		
	}
		    	
		        
}
