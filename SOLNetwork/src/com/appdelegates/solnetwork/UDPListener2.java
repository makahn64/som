package com.appdelegates.solnetwork;


import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Random;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class UDPListener2 {

	private static final Boolean DEBUG = false;
	Handler mHandler = null;
	public int mPort;
	Boolean listenerAlive = true;
	UDPSendQueue2 mAckQueue = null;
	DatagramSocket listenSocket = null;
	
	ListenerThread listenerThread;
	
	public UDPListener2(int port, Handler handler, UDPSendQueue2 ackQueue){
		
		mPort = port;
		mHandler = handler;
		mAckQueue = ackQueue;
		
		listenerThread = new ListenerThread();		
		listenerThread.start();
		
	}
	
	public UDPListener2(int port, Handler handler){
		
		this(port, handler, null);
		
	}
	
	public void kill(){
		
		listenerAlive = false;
		listenerThread.interrupt();
		if (listenSocket!=null)
			listenSocket.close();
	}

	
	class ListenerThread extends Thread {
		
		@Override
		public void run() {
			
			DatagramPacket p = null;
			
			int packetNumber;
		    
		    try {
		    	
		    	byte[] message = new byte[1500];
	        	p = new DatagramPacket(message, message.length);
	        	listenSocket = new DatagramSocket(mPort);
		        //accept connections
		        while (listenerAlive){
		        		        	
		        	listenSocket.receive(p);
		        	ByteBuffer bb = ByteBuffer.allocateDirect(p.getLength());
		        	bb.put(p.getData(), 0, p.getLength());
		        	bb.flip();
		        	byte retries = bb.get();
		        	packetNumber = bb.getInt();
		        	int remaining = bb.remaining();
		        	byte[] payload = new byte[remaining];
		        	bb.get(payload, 0, bb.remaining());
		        	
			        //Log.i("UDP Listener", "received: " + text);
			        Message msg = mHandler.obtainMessage();
			        msg.obj = new UDPMessageRX( p.getAddress(), payload );
			        mHandler.sendMessage(msg);
		        	
		        	if (DEBUG==true){
		        		// simulate lossing 33% of packets
		        		int bet1and10 = new Random().nextInt(10);
		        		if (bet1and10<3)
		        			retries = 0;
		        	}
		        	
			        if (retries != 0){
			        	// ack needed
			        	Log.i("ACKBACK", "Ack to: " + IPAddressHelper.getShortIP(p.getAddress())+ "Tag: "+ packetNumber+ " TTL: "+retries);
			        	if (mAckQueue==null){
			        		DatagramSocket as = new DatagramSocket();
			        		byte [] ackLoad = UDPAckSender.buildAckPayload(packetNumber);
			        		int len = ackLoad.length;
							DatagramPacket ap = new DatagramPacket(ackLoad, len, p.getAddress() , 6565);		
							as.send(ap);
			        	} else {
			        		// use queue
			        		mAckQueue.postMessageFast(p.getAddress(), 6565, UDPAckSender.buildAckPayload(packetNumber), (byte)0);	        		
			        	}
			        	
			        }
		        }
		        
		        listenSocket.close();
		        
		    } catch (InterruptedIOException e) {
		        //if timeout occurs
		        e.printStackTrace();
		        listenSocket.close();
		    } 
		    catch (IOException e) {
		        e.printStackTrace();
		        listenSocket.close();
		    } finally {
		        if (listenSocket != null) {
		        	listenSocket.close();
		        }
		    }
		} // run
		
		
		public void killListener(){
			listenerAlive = false;
			listenSocket.close();
		}
		
	} // class
	
	
	
	
	
}
