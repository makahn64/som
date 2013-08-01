package com.appdelegates.solgame;

import java.net.InetAddress;

import com.appdelegates.solnetwork.IPAddressHelper;

public class Handset {
	
	// Grrgh. Java enums don't work like C and we need a value for the messages passed in UDP
	public enum HandsetState {
		OFF(0), ON(1), ARMED(2), ARMED_NEGATIVE(3), ATTRACT(4), COUNTDOWN(5), START_BUTTON(6), GAME_OVER(7) ;
        private final int value;

        private HandsetState(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
        
        public Boolean isOneShot() {
        	return ( (this==ATTRACT) || (this==COUNTDOWN) || (this==GAME_OVER));
        }
    }
	
	
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
