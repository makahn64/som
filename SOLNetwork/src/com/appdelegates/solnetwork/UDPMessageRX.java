package com.appdelegates.solnetwork;


import java.net.InetAddress;
import java.nio.ByteBuffer;

public class UDPMessageRX {
	
	InetAddress mIpAddr;
	byte[] mMessage;
	int shortIP;

	public UDPMessageRX(InetAddress ipAddr, byte[] message){
		mIpAddr = ipAddr;
		mMessage = message;
		shortIP = ipAddr.getAddress()[3] & 0xff;
		
	
	}
	
	public void changeCommand(byte newCommand){
		mMessage[0] = newCommand;
	}
	
	public Boolean isMessage(byte message){
		return mMessage[0]==message;
	}
	
	public String getMessageAsString(){
		return new String(mMessage);
	}
	
	public String getPayloadAsString(){
		
		return new String(mMessage, 1, (mMessage.length-1));
		
	}
	
	
	public InetAddress getInetAddress(){
		return mIpAddr;
	}
	
	public String getIPAdressString(){
		
		return mIpAddr.getHostAddress();
		
	}
	
	public byte getCommand(){
		return mMessage[0];
	}
	
	public int getShortIP(){
		
		return shortIP;
		
	}
	
	public byte[] getMessage(){
		
		return mMessage;
	}
}
