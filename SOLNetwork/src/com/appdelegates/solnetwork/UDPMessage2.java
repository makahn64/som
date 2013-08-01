package com.appdelegates.solnetwork;


import java.net.InetAddress;

public class UDPMessage2 {
	
	InetAddress mIpAddr;
	byte[] mMessage;
	int shortIP;
	
	// Port only used on send
	int mPort = 0;
	byte mRetries;

	public UDPMessage2(InetAddress ipAddr, int port, byte[] message, byte retries){
		mIpAddr = ipAddr;
		mMessage = message;
		byte [] ipParts = ipAddr.getAddress();
		shortIP = ipParts[3];
		mPort = port;
		mRetries = retries;
	}


	public Boolean isMessage(byte message){
		return mMessage[0]==message;
	}
	
	public byte[] getMessage(){
		return mMessage;
	}
	
	public InetAddress getInetAddress(){
		return mIpAddr;
	}
	
	public String getIPAdressString(){
		
		return mIpAddr.getHostAddress();
		
	}
	
	public int getPort(){
		
		return mPort;
		
	}
	
	public int getShortIP(){
		
		return shortIP;
		
	}
		
	public byte getRetries(){
		return mRetries;
	}
	
	synchronized public void decrementTTL(){
		mRetries--;
	}
	
	

}
