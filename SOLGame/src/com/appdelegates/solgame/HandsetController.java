package com.appdelegates.solgame;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import com.appdelegates.solgame.Handset.HandsetState;
import com.appdelegates.solnetwork.IPAddressHelper;
import com.appdelegates.solnetwork.SOLMessage;
import com.appdelegates.solnetwork.UDPListener2;
import com.appdelegates.solnetwork.UDPMessageRX;
import com.appdelegates.solnetwork.UDPSendQueue2;



public class HandsetController {
	
	public static final Boolean DEBUG = true;

	private static final String CNAME = "HsController";
	
	public SparseArray< Handset > handsets = new SparseArray< Handset >(15);
	UDPSendQueue2 udpSQ;
	UDPListener2 udpListener;
	int mBaseIP;
	int mNumRows;
	int mNumCols;
	
	Handler mRXHandler;
	
	HandsetListenerThread hslThread;
	//HandsetControllerListener mListener = null;
	
	SparseArray<HandsetControllerListener> commandListeners = new SparseArray<HandsetControllerListener>();
	
	public HandsetController(int baseIP, int numRows, int numCols){
		
		mBaseIP = baseIP;
		mNumRows = numRows;
		mNumCols = numCols;
		
		udpSQ = new UDPSendQueue2(false); // no rtx
		hslThread = new HandsetListenerThread();
		hslThread.start();
		
		//InitializeHandsetsThread iht = new InitializeHandsetsThread();
		//iht.start();
		
		
	}
	
	/*
	public void setHandsetControllerListener(HandsetControllerListener listener){
		mListener = listener;
	}
	*/
	
	/**
	 * Adds a listener for a specific SOL command. Only one listener per command.
	 * @param command
	 * @param listener registering for notificaiton of this command.
	 */
	
	public void addCommandListenerForCommand(byte command, HandsetControllerListener listener){
		
		int key = command & 0xff;
		commandListeners.append(key, listener);
		
	}
	
	
	public interface HandsetControllerListener {
		
		public void handsetMessageReceived(int shortIP, int row, int column, byte command);
	}
	
	public void kill(){
		
		udpSQ.kill();
		udpSQ = null; //release
		udpListener.kill();
		udpListener = null;
		
	}
	
	public int shortIPFromRC(int row, int column){
		
		int rval = mBaseIP + (row*3) + column;
		return rval;
	}
	
	private void processUpdate(UDPMessageRX umrx) {
		
		// We know this is a status update message, so we need the state info which is an int right after the message byte
		byte [] message = umrx.getMessage();
		ByteBuffer bb = ByteBuffer.allocateDirect(message.length);
		bb.put(message);
		bb.flip();
		bb.get();  // skip the message byte
		int rxState = bb.getInt(); // get the current state
		
		Handset h = handsets.get(umrx.getShortIP());
		
		if (h.ignoreState)
			return;
		
		// first, check if it this is a oneShot 
		
		if ( h.getState().isOneShot() ){
			
			if (h.oneShotVerified()){
				if (DEBUG)
					Log.i(CNAME, "One shot " + h.getIPShort() +" already verified: "+h.getState());
				return;
			}
				
			else {
				// one shot not verified, is it good now?
				if ( rxState == h.getIntState() ){
					if (DEBUG)
						Log.i(CNAME, "Verified 1 shot" + h.getIPShort() + " in the right state: "+h.getState());
					h.setOneShotVerified(); 
				}
					
				else {
					// hit it again!
					h.errorCount--;
					if (DEBUG)
						Log.e(CNAME, "1 shot "+h.getIPShort() + " in the wrong state: "+rxState+ ". Should be: " + h.getState()+ " EC= "+h.errorCount);
					if (h.errorCount==0)
						setState(umrx.getInetAddress(), h.getState());
				}
			}

		} 
		// This is not a one-shot state
		else {
			
			if ( rxState != h.getIntState() ){
				// we have a state mismatch so try again. FOrst check if we missed a click
				h.errorCount--;
				if (h.getState()==HandsetState.ARMED  && rxState==HandsetState.ARMED_NEGATIVE.getValue() ){
					// we missed a click, maybe
					
					if (DEBUG)
						Log.e(CNAME, "Missed a click on "+h.getIPShort() + ", propogating: "+h.getState() + " EC=" + h.errorCount);
					if (h.errorCount==0){
						h.setState(HandsetState.ARMED_NEGATIVE);
						umrx.changeCommand(SOLMessage.CLICK);
						sendMessageUpstream(umrx);
					}
					
				} else {
					// The handset missed a UDP message, resend it
					if (DEBUG)
						Log.e(CNAME, "Missed a message on "+h.getIPShort() + ", resending: "+h.getState()+ " EC=" + h.errorCount);
					if (h.errorCount==0)
						setState(umrx.getInetAddress(), h.getState());
				}
				
				
			}
			
			
		}
		
		
		
		// otherwise throw it on the floor...
	}
	
	public synchronized void setState(int row, int column, HandsetState newState) {
		
		String ipString = "192.168.1." + shortIPFromRC(row, column);
		try {
			InetAddress destIP = InetAddress.getByName(ipString);
			setState(destIP, newState);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
/**
 * Sets the current state immediately, then changes to the next state after a delay. Good for actions like COUNTDOWN that run for a few seconds
 * then switch to OFF.
 * @param row 0-based row of the handset
 * @param column 0-based column of the handset
 * @param newState State to switch to immediately
 * @param delay Delay in ms to next state
 * @param nextState Next state
 */
	public synchronized void setStatesDelayed(final int row, final int column, HandsetState newState, int delay, final HandsetState nextState) {
		
		String ipString = "192.168.1." + shortIPFromRC(row, column);
		
			InetAddress destIP;
			try {
				destIP = InetAddress.getByName(ipString);
				setStatesDelayed(destIP, newState, delay, nextState);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
	}
	
	public synchronized void setStatesDelayed(final InetAddress inetAddress, HandsetState newState, int delay, final HandsetState nextState) {
		
		
			setState(inetAddress, newState);
			
			mRXHandler.postDelayed(new Runnable(){

				@Override
				public void run() {
					setState(inetAddress, nextState);
					
				}
				
			}, delay);
			
		
	}
	
	
	public synchronized void setState(InetAddress inetAddress, HandsetState newState){
		
		byte stateMessage = SOLMessage.RESET;
		
		switch ( newState ){
		
		case OFF:
			stateMessage = SOLMessage.HIDE_BUTTON;
			break;
			
		case ON:
			stateMessage = SOLMessage.SHOW_BUTTON;
			break;	
			
		case ARMED:
			stateMessage = SOLMessage.ARM_BUTTON;
			break;	
			
		case ARMED_NEGATIVE:
			stateMessage = SOLMessage.ARM_NEGATIVE;
			break;	
			
		case COUNTDOWN:
			stateMessage = SOLMessage.COUNTDOWN;
			break;	
			
		case ATTRACT:
			stateMessage = SOLMessage.IDLE;
			break;
			
		case START_BUTTON:
			stateMessage = SOLMessage.START_BUTTON;
			break;
			
		}
		
		handsets.get( IPAddressHelper.getShortIP(inetAddress) ).setState(newState);
		udpSQ.postMessageFast(inetAddress, 6464, SOLMessage.buildPayload(stateMessage, null), SOLMessage.RETRY_0);
		
	}
	
	public void sendMessageUpstream(UDPMessageRX umrx){
		int key = umrx.getCommand() & 0xff;
		HandsetControllerListener hcl = commandListeners.get(key);
		if ( hcl != null) {
			
			
			int shortIP = umrx.getShortIP();
			handsets.get(shortIP).ignoreState = true;  // upstream will do something   
			int sip = shortIP - mBaseIP;
			int row = sip/3;
			int col = sip%3;
			hcl.handsetMessageReceived(shortIP, row, col, umrx.getCommand());
			
		}
	}
	
	private class HandsetListenerThread extends Thread {
		
		@Override
		public void run(){
			
			Looper.prepare();
			
			mRXHandler = new Handler(){
				
				public void handleMessage(Message msg){
					
					UDPMessageRX umrx = (UDPMessageRX)msg.obj;
					
					if ( umrx.isMessage(SOLMessage.STATUS_UPDATE) )
						processUpdate(umrx);
					else {
						// This is a click on nclick or other message. Propagate it up.
						sendMessageUpstream(umrx);
							
					}
					
				}

				
			};
			
			for ( int row=0; row<mNumRows; row++ ) {
				
				for ( int column=0; column<mNumCols; column++ ) {
					String stringIP = "192.168.1." + shortIPFromRC(row, column);
					try {
						InetAddress inet = InetAddress.getByName(stringIP);
						handsets.append( shortIPFromRC(row, column), new Handset(inet, HandsetState.OFF));
						// Hit it twice
						
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
				}
			}
			
			resetAllHandsets();
						
			udpListener = new UDPListener2(6464, mRXHandler);
			Looper.loop();
			
		}
	}
	
	public void resetAllHandsets(){
		
		for (int i=0; i<handsets.size(); i++ ){
			handsets.valueAt(i).setState(HandsetState.OFF);
			udpSQ.postMessageFast(handsets.valueAt(i).getInetAddress(), 6464, SOLMessage.buildPayload(SOLMessage.RESET, null), SOLMessage.RETRY_0);
			
		}
	}
	
	public void startButton(){
		
		for (int i=0; i<handsets.size(); i++ ){
			handsets.valueAt(i).setState(HandsetState.START_BUTTON);
			udpSQ.postMessageFast(handsets.valueAt(i).getInetAddress(), 6464, SOLMessage.buildPayload(SOLMessage.START_BUTTON, null), SOLMessage.RETRY_0);
			
		}
	}
	
	public void countdown(){
		
		for (int i=0; i<handsets.size(); i++ ){
			setStatesDelayed(handsets.valueAt(i).getInetAddress(), HandsetState.COUNTDOWN, 3750, HandsetState.OFF);
			udpSQ.postMessageFast(handsets.valueAt(i).getInetAddress(), 6464, SOLMessage.buildPayload(SOLMessage.COUNTDOWN, null), SOLMessage.RETRY_0);
			
		}
	}
	
	public void gameOver(){
		
		for (int i=0; i<handsets.size(); i++ ){
			setState(handsets.valueAt(i).getInetAddress(), HandsetState.GAME_OVER);
			udpSQ.postMessageFast(handsets.valueAt(i).getInetAddress(), 6464, SOLMessage.buildPayload(SOLMessage.GAME_OVER, null), SOLMessage.RETRY_0);
			
		}
	}
	
	public void setPenalMode(Boolean yesNo){
		
		for (int i=0; i<handsets.size(); i++ ){
			byte cmd = yesNo ? SOLMessage.PENAL_MODE_ON : SOLMessage.PENAL_MODE_OFF; 
			udpSQ.postMessageFast(handsets.valueAt(i).getInetAddress(), 6464, SOLMessage.buildPayload(cmd, null), SOLMessage.RETRY_0);
			
		}
	}
	
	public void playWarn(){
		
		for (int i=0; i<handsets.size(); i++ ){
			udpSQ.postMessageFast(handsets.valueAt(i).getInetAddress(), 6464, SOLMessage.buildPayload(SOLMessage.PLAY_WARN_SOUND, null), SOLMessage.RETRY_0);			
		}
	}
	
	public void nextButton(){
		
		for (int i=0; i<handsets.size(); i++ ){
			udpSQ.postMessageFast(handsets.valueAt(i).getInetAddress(), 6464, SOLMessage.buildPayload(SOLMessage.NEXT_BUTTON_IMAGE, null), SOLMessage.RETRY_0);			
		}
	}

	public void useButton(int buttonNum){
		
		byte [] extra = ByteBuffer.allocate(4).putInt(buttonNum).array();
		
		for (int i=0; i<handsets.size(); i++ ){
			udpSQ.postMessageFast(handsets.valueAt(i).getInetAddress(), 6464, SOLMessage.buildPayload(SOLMessage.SET_BUTTON_NUMBER, extra), SOLMessage.RETRY_0);			
		}
	}

	
	// Has to be in a thread or Android shitsz
	private class InitializeHandsetsThread extends Thread {
		
		@Override
		public void run(){
			
			for ( int row=0; row<5; row++ ) {
				
				for ( int column=0; column<3; column++ ) {
					String stringIP = "192.168.1." + shortIPFromRC(row, column);
					try {
						InetAddress inet = InetAddress.getByName(stringIP);
						handsets.append( shortIPFromRC(row, column), new Handset(inet, HandsetState.OFF));
						// Hit it twice
						
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
				}
			}
			
			resetAllHandsets();
			
			
		}
	}

}
