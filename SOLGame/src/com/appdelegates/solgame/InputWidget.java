package com.appdelegates.solgame;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.appdelegates.solnetwork.SOLMessage;
import com.appdelegates.solnetwork.UDPSendQueue2;

public class InputWidget {
	
	String mIpAddressString;
	UDPSendQueue2 mUdpSQ;
	InetAddress mInetAddr;
	long mTimeTag;
	
	public InputWidget(String ipAddress, UDPSendQueue2 udpsq) throws UnknownHostException{
		mIpAddressString = ipAddress;
		mUdpSQ = udpsq;
		mInetAddr = InetAddress.getByName(mIpAddressString);
	}

	public void setTimeTag(long timeTag){
		mTimeTag = timeTag;
	}
	
	public long getTimeTag(){
		return mTimeTag;
	}
	
	public String getIPShort(){
		int ips = 0xff & mInetAddr.getAddress()[3];
		return ""+ips;
	}
	
	public void hide(){
		
		mUdpSQ.postMessageFast(mInetAddr, 6464, SOLMessage.buildPayload(SOLMessage.HIDE_BUTTON, null), (byte)0);
	}
	
	public void show(){
		mUdpSQ.postMessageFast(mInetAddr, 6464, SOLMessage.buildPayload(SOLMessage.SHOW_BUTTON, null), (byte)0);
	}
	
	public void arm(){
		mUdpSQ.postMessageFast(mInetAddr, 6464, SOLMessage.buildPayload(SOLMessage.ARM_BUTTON, null), (byte)0);
	}
	
	public void armNegative(){
		mUdpSQ.postMessageFast(mInetAddr, 6464, SOLMessage.buildPayload(SOLMessage.ARM_NEGATIVE, null), (byte)0);
	}
	
	public void nextButton(){
		mUdpSQ.postMessageFast(mInetAddr, 6464, SOLMessage.buildPayload(SOLMessage.NEXT_BUTTON_IMAGE, null), (byte)0);
	}
	
	public void startButton(){
		mUdpSQ.postMessageFast(mInetAddr, 6464, SOLMessage.buildPayload(SOLMessage.START_BUTTON, null), (byte)2);
	}
	
	public void setPenalMode(Boolean isPenal){
		
		byte[] msg;
		if (isPenal)
			msg =  SOLMessage.buildPayload(SOLMessage.PENAL_MODE_ON, null);
		else
			msg = SOLMessage.buildPayload(SOLMessage.PENAL_MODE_OFF, null);
		
		mUdpSQ.postMessageFast(mInetAddr, 6464, msg, (byte)3);
	}
	
	public void gameMode(){
		
	}
	
	public void reset(){
		mUdpSQ.postMessageFast(mInetAddr, 6464, SOLMessage.buildPayload(SOLMessage.RESET, null), (byte)2);
	}
	
	public void playWarn(){
		// No need for an ACK, even just a few handsets are fine
		mUdpSQ.postMessageFast(mInetAddr, 6464, SOLMessage.buildPayload(SOLMessage.PLAY_WARN_SOUND, null), (byte)1);
	}
	
	public void gameOver(){
		// No need for an ACK, even just a few handsets are fine
		mUdpSQ.postMessageFast(mInetAddr, 6464, SOLMessage.buildPayload(SOLMessage.GAME_OVER, null), (byte)1);
	}
	
	public void countdown(){
		// No need for an ACK, even just a few handsets are fine
		mUdpSQ.postMessageFast(mInetAddr, 6464, SOLMessage.buildPayload(SOLMessage.COUNTDOWN, null), (byte)0);
	}
	
	public void pulse(){
		// No need for an ACK, even just a few handsets are fine
		mUdpSQ.postMessageFast(mInetAddr, 6464, SOLMessage.buildPayload(SOLMessage.PULSE, null), (byte)3);
	}

	public void idle(){
		// No need for an ACK, even just a few handsets are fine
		mUdpSQ.postMessageFast(mInetAddr, 6464, SOLMessage.buildPayload(SOLMessage.IDLE, null), (byte)2);
	}


}
