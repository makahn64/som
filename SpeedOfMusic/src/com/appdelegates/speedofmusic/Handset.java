package com.appdelegates.speedofmusic;

import java.net.InetAddress;
import com.appdelegates.solnetwork.HandsetState;

import com.appdelegates.solnetwork.IPAddressHelper;

public class Handset {
	
	// Grrgh. Java enums don't work like C and we need a value for the messages passed in UDP

	
	private InetAddress mInetAddress;
	private HandsetState mState;
	public Boolean oneShotStateVerified;
	// used to temporarily mark this Handset as being processed by game engine to prevent race conditions
	public Boolean ignoreState; 
	public int errorCount;
	
	private long mTimeTag; // holdover from original for blonky games
	
	public Handset(InetAddress inetAddress, HandsetState initialState){
		
		setState(initialState);
		mInetAddress = inetAddress;
		
	}
	
	public Boolean oneShotVerified(){
		return (mState.isOneShot() && oneShotStateVerified);
	}
	
	public void setOneShotVerified(){
		oneShotStateVerified = true;
	}
	
	public synchronized void setState(HandsetState newState){
		
		mState = newState;
		ignoreState = false;
		errorCount = 2;
		if (mState.isOneShot())
			oneShotStateVerified = false;

	}
	
	public InetAddress getInetAddress(){
		return mInetAddress;
	}
	
	public int getIPShort(){
		return IPAddressHelper.getShortIP(mInetAddress);
	}
	
	public synchronized int getIntState(){
		
		return mState.getValue();
		
	}
	
	public synchronized HandsetState getState(){
		return mState;
	}
	
	public long getTimeTag(){
		return mTimeTag;
	}
	
	public synchronized void setTimeTag(long tag){
		mTimeTag = tag;
	}

}
