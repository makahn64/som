package com.appdelegates.solnetwork;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TCPServerCrufty {

	private static final Boolean DEBUG = false;
	
	TCPServerListener mListener;
	
	Handler mHandler = null;
	public int mPort;
	Boolean listenerAlive = true;
	
	ServerSocket listenSocket = null;
	
	ServerFactoryThread factory;
	
	ArrayList<ListenerThread> listenerThreads = new ArrayList<ListenerThread>();
	
	public TCPServerCrufty(int port, Handler handler){
		
		mPort = port;
		mHandler = handler;
		
		factory = new ServerFactoryThread();
		
	}
	
	public void startServer(){
		factory.start();
	}
	
	public interface TCPServerListener {
		
		public void serverListenerEstablished();
		public void serverListenerFailed(Exception e);
		public void serverAcceptedNewConnection(InetAddress inetAddress);
		
	}
		
	public void kill() throws IOException{
		

	}

	class ServerFactoryThread extends Thread {
		
		
		
		@Override
		public void run(){
			
			try {
				listenSocket = new ServerSocket(mPort);
			} catch (IOException e1) {

				Log.e("TCPServer", "Failed connecting to ServerSocket");
				if (mListener!=null)
					mListener.serverListenerFailed(e1);
				e1.printStackTrace();
			}
			
			while (true){
				
				

				try {
					Socket connectionSocket = listenSocket.accept();
					ListenerThread lt = new ListenerThread(connectionSocket, mHandler);
					lt.start();
					listenerThreads.add(lt);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
		}
		
	}
	
	class ListenerThread extends Thread {
		
		Handler lHandler;
		Socket mSocket;
		
		public ListenerThread(Socket socket, Handler handler){
			lHandler = handler;
			mSocket = socket;
		}
		
		@Override
		public void run() {
			
			DataInputStream dis;
			DataOutputStream dos;
			
			while (true){
		    			
		    	try {
					dis = new DataInputStream(mSocket.getInputStream());
					dos = new DataOutputStream(mSocket.getOutputStream());
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
				    Message msg = lHandler.obtainMessage();
				    msg.obj = new UDPMessageRX( mSocket.getInetAddress(), payload );
				    lHandler.sendMessage(msg);
				    dos.writeChars("Got it, bitch");
				    dos.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				        
				    	
		    			
		    }
			    	
			        			    		    		
		}
		
	}
		    	
		        
}
