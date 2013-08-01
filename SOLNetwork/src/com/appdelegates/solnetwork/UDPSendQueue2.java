package com.appdelegates.solnetwork;


import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class UDPSendQueue2 {
	
	private static Boolean DEBUG = false;
	
	int mAckPort;
	long mAgeoutMs;
	AckListenerThread ackListenerThread;
	SenderThread senderThread;
	Boolean listenerIsAlive;
	
	DatagramSocket ackSocket = null;

	private Boolean mUseAck;
	
	
	public UDPSendQueue2(Boolean useAck){
		
		 mUseAck = useAck;
		
		senderThread = new SenderThread();
		senderThread.start();
		
		if (useAck){
			ackListenerThread = new AckListenerThread(6565, senderThread.getHandler());
			ackListenerThread.start();
			mAgeoutMs = 1500;
		}

	
	}
	
	

	public void kill(){
		
		if (mUseAck){
			ackListenerThread.interrupt();
			if (ackSocket!=null)
				ackSocket.close();
		}
		
		senderThread.interrupt();
	}
	
	class SenderThread extends Thread {
		
		public Handler senderHandler;
		private int packetNumber;
		
		public Handler getHandler(){
			return senderHandler;
		}
		
		@Override
	    public void run(){
	           Looper.prepare();
	           
	           packetNumber = 0;
	           
	           senderHandler = new Handler() {
                   public void handleMessage(Message msg) {
                	   
                	   UDPMessage2 toSend = (UDPMessage2)msg.obj;
                	      					
    					/*
    					 * Protocol packet looks like this:
    					 * 
    					 * [ retries: 1 byte ][ packet number: 4 bytes][ <<< Payload >>>]
    					 * 
    					 * 
    					 */
                	   try {
                		   
    					DatagramSocket s = new DatagramSocket();
    					byte [] payload = toSend.getMessage();
    					
    					ByteBuffer bb = ByteBuffer.allocateDirect(payload.length + 9);
    					
    					if (mUseAck)
    						bb.put(toSend.getRetries());
    					else
    						bb.put((byte)0);
    					
    					bb.putInt(++packetNumber);
    					bb.put(payload);
    					bb.flip();
    					
    					byte [] message = new byte[bb.remaining()];
    					bb.get(message, 0, bb.remaining());
    					
    					int msg_length = message.length;
    					DatagramPacket p = new DatagramPacket(message, msg_length, 
    							toSend.getInetAddress() , toSend.getPort());	
    					
    					
							s.send(p);
						
    					
    					if (toSend.getRetries() > 0 && mUseAck){
    						// Packet needs an ack, so kick it off delayed
    						//Log.i("UDPSQ2A", "Adding packet to ack queue");
    						toSend.decrementTTL();
    						Message retry = senderHandler.obtainMessage(packetNumber);
    						retry.obj = toSend;
    						senderHandler.sendMessageDelayed(retry, mAgeoutMs);
    						
    					}
               
                	   } catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
                	   } // catch
       				} // handle
       					
       				
           }; // Handler
           
           Looper.loop();
	    } // run()
	} // class
	
	class AckListenerThread extends Thread {
		
		public Handler refToSenderHandler;
		int mListenerPort;
		private Boolean listenerIsAlive;
		
		public AckListenerThread(int listenerPort, Handler senderHandler){
			super();
			mListenerPort = listenerPort;
			refToSenderHandler = senderHandler;			
		}
		
		public void run(){
			
			DatagramPacket p = null;
			listenerIsAlive = true;
			
			int packetNumber;
		    
		    try {
		    	
		    	byte[] message = new byte[1500];
	        	p = new DatagramPacket(message, message.length);
	        	ackSocket = new DatagramSocket(mListenerPort);
		        //accept connections
		        while (listenerIsAlive){
		        		        	
		        	ackSocket.receive(p);
		        	
		        	ByteBuffer bb = ByteBuffer.wrap(p.getData());
		        	packetNumber = bb.getInt();
		        	
		        	if (DEBUG){
			        	Log.i("GOTACK", "Got accked, yo! ["+packetNumber+"]");
			        	String yn = refToSenderHandler.hasMessages(packetNumber)?"yes":"no";
			        	Log.i("GOTACK", "Has message? "+yn );
		        	}

		        	if (refToSenderHandler!=null)
		        		refToSenderHandler.removeMessages(packetNumber);
		        	else {
		        		Log.e("UDPSendQueue", "Ref to Sender Handler is null, motherfucka!");
		        	}
		        
		        }
		        
		        ackSocket.close();
		        
		    } catch (InterruptedIOException e) {
		        //if timeout occurs
		        e.printStackTrace();
		        ackSocket.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		        ackSocket.close();
		    } catch (Exception e) {
		    	e.printStackTrace();
		    } finally {
		        if (ackSocket != null) {
		        	ackSocket.close();
		        }
		    }
			
		} // run()
		
		public void killAckListener(){
			listenerIsAlive = false;
		}
		
	}
	

	private void putMessageToHandler(UDPMessage2 umsg){
		
		Message msg = senderThread.senderHandler.obtainMessage();
		msg.obj = umsg;
		senderThread.senderHandler.sendMessage(msg);
		
	}
	
	public void postMessage(String ipAddr, int port, String message, byte retries){
		
		try {
			InetAddress inetAddr = InetAddress.getByName(ipAddr);
			UDPMessage2 umsg = new UDPMessage2(inetAddr, port, message.getBytes(), retries);
			putMessageToHandler(umsg);
			
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
			return;
		}
				
	}
	
	public void postMessageRaw(String ipAddr, int port, byte[] message, byte retries){
		
		try {
			InetAddress inetAddr = InetAddress.getByName(ipAddr);
			UDPMessage2 umsg = new UDPMessage2(inetAddr, port, message, retries);
			putMessageToHandler(umsg);
			
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
			return;
		}
				
	}
	
	public void postMessageFast(InetAddress ipAddr, int port, byte[] message, byte retries){
		
		UDPMessage2 umsg = new UDPMessage2(ipAddr, port, message, retries);
		putMessageToHandler(umsg);
				
	}
	

	
}
