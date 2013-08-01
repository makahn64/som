package com.appdelegates.solnetwork;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class UDPAckSender {

	int mPort;
	int mTag;
	InetAddress mIpAddress;
	
	public UDPAckSender(InetAddress ipAddress, int port, int tag) throws UnknownHostException{
		mPort = port;
		mTag = tag;
		mIpAddress = ipAddress;
		
	}
	
	public static byte[] buildAckPayload(int tag){
		ByteBuffer bb =  ByteBuffer.allocateDirect(4);
		bb.putInt(tag);
		bb.flip();
		byte[] message = new byte[bb.remaining()];
		bb.get(message,0,4);
		return message;
	}
	
	public void send(){
		Thread sendThread = new Thread(new Runnable(){

			@Override
			public void run() {
				
				try {
					
					DatagramSocket s = new DatagramSocket();
					DatagramPacket p = new DatagramPacket(buildAckPayload(mTag), 8, mIpAddress , mPort);		
					s.send(p);
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		});
		
		sendThread.start();
	}
	
	
}
