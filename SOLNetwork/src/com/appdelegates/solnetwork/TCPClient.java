package com.appdelegates.solnetwork;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class TCPClient extends Thread{
	
	private static final String CNAME = "TCPClient";
			
	Socket clientSocket;
	InetAddress mInetAddress;
	int mPort;
	TCPClientListener mListener;
		
	private Handler mHandler;
	DataOutputStream outToServer;
	
	TCPClientRX rxThread;
	
	public TCPClient( InetAddress inetAddress, int port) {
		
		mInetAddress = inetAddress;
		mPort = port;
		mListener = null;
				
	}
	
	public TCPClient( String inetAddress, int port) throws UnknownHostException {
		
		mInetAddress = InetAddress.getByName(inetAddress);
		mPort = port;
		mListener = null;
				
	}
	
	public interface TCPClientListener {
		
		public void connectionEstablished(Handler handler);
		public void connectionDropped(InetAddress inetAddress, Exception e);
		public void connectionFailed(Exception e);
		public void receivedData(InetAddress inetAddress, String data);
		
	}

	public void setTCPClientListener(TCPClientListener listener){
		mListener = listener;
	}
	
	public void connect(){
		
		this.start();
				
	}
	
	public void close(){
		
		try {
			outToServer.close();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			Log.e(CNAME, "Error closing: "+e1.toString());
		}
		
		try {
			clientSocket.close();
			
		} catch (Exception e) {
			Log.e(CNAME, "Error closing: "+e.toString());
		}
		
		try {
			rxThread.interrupt();
			
		} catch (Exception e) {
			Log.e(CNAME, "Error closing rxthread: "+e.toString());
		}
		
		
	}

		
	private class TCPClientRX extends Thread {
		
		Boolean connectionAlive;
		
		@Override
		public void run(){
			
			connectionAlive = true;
			BufferedReader in = null;
			
			try {
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			 
			while (connectionAlive){
				
				try {
					String stuff = in.readLine();
					if (mListener!=null)
						mListener.receivedData(clientSocket.getInetAddress(), stuff);
					Log.i(CNAME, stuff);
				} catch (Exception e) {
					e.printStackTrace();
					Log.i(CNAME, "Connection presumed closed!");
					connectionAlive = false;
					if (mListener!=null)
						mListener.connectionDropped(clientSocket.getInetAddress(), e);
					try {
						in.close();
						clientSocket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}
			}
			
		}
		
	}
	
	
	@Override
	public void run(){
			
			Looper.prepare();				
			clientSocket = new Socket();
			
				
			try {
				clientSocket.connect(new InetSocketAddress(mInetAddress, mPort), 5000);
				
			} catch (IOException e1) {
				Log.e( CNAME, "Exception connecting: "+e1.toString() );
				try {
					clientSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				e1.printStackTrace();
				if (mListener !=null){
					mListener.connectionFailed(e1);
				}
				return;
			}

			
			try {
				outToServer = new DataOutputStream(clientSocket.getOutputStream());
			} catch (IOException e1) {
				Log.e( CNAME, "Exception creating DataOutputStream: "+e1.toString() );
				e1.printStackTrace();
				if (mListener !=null){
					mListener.connectionFailed(e1);
				}
				return;
			}
					
			mHandler = new Handler(){
						@Override
						public void handleMessage(Message msg){
							
							String smsg = (String)msg.obj;
							try {
								outToServer.writeBytes(smsg);
								outToServer.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
					};
					
			rxThread = new TCPClientRX();
			rxThread.start();
					
			if (mListener != null){
				
				mHandler.post(new Runnable(){

					@Override
					public void run() {
						mListener.connectionEstablished(mHandler);						
					}
					
				});
				
			}
			Looper.loop();
				
		}
		
	
	
	public void sendMessage(String message){

		while (mHandler==null)
			Log.i(CNAME, "Waiting to connect....");
		Message msg = mHandler.obtainMessage();
		msg.obj = message + "\n";
		mHandler.sendMessage(msg);
		
	}
	
	
	
}
