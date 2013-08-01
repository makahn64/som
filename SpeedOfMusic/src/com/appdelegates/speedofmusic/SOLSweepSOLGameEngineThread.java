package com.appdelegates.speedofmusic;

import java.util.ArrayList;
import java.util.Collections;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.appdelegates.solnetwork.SOLMessage;
import com.appdelegates.solnetwork.HandsetState;




public class SOLSweepSOLGameEngineThread extends SOLGameEngineThread {
	
	public static Boolean DEBUG = false;
	
	public static int BUTTONS_LIT = 7;
	public static int NUM_BUTTONS = 15;
	public static int GAME_LENGTH = 30;
	public static int BUTTON_TIMEOUT = 3500;


	int mPlayerRate;
	long lastHitTime;
	long averageHitTime;
	int uiState;
	long currentGameTime;
	
	ArrayList<Handset> hiddenButtons; 
	ArrayList<Handset> visibleButtons;
	Boolean mIsRunning;
	
	enum State { START, THRESH1, THRESH2, THRESH3 };
	State mState;
	
	public SOLSweepSOLGameEngineThread(HandsetController handsetController) {
		super(handsetController);		
		
		hiddenButtons = new ArrayList<Handset>();
		visibleButtons = new ArrayList<Handset>();
		mHandsetController = handsetController;
		
		mHandsetController.addCommandListenerForCommand(SOLMessage.CLICK, this);
		mHandsetController.addCommandListenerForCommand(SOLMessage.CLICK_NEGATIVE, this);
		
	}
	
	@Override
	public void resetGame() {
		
		mPlayerRate = 1;
		currentGameTime = GAME_LENGTH;
		mScore = 0;		
		uiState = 0;
		mHandsetController.resetAllHandsets();  // reset all handsets
		mHandsetController.setPenalMode(true);
		mIsRunning = false;
		
		changeState(State.START);
	
		
		lastHitTime = System.currentTimeMillis();
		averageHitTime = 1000; // start them at 1 second to be fair
		
		hiddenButtons.clear();
		
		
		// Make all buttons hidden
		for(int i = 0; i < mHandsetController.handsets.size(); i++){
		    
		    hiddenButtons.add( mHandsetController.handsets.valueAt(i) );
		    
		}
		
		Collections.shuffle(hiddenButtons);
		
		for (Handset h: hiddenButtons){
			mHandsetController.setState(h.getInetAddress(), HandsetState.ARMED_NEGATIVE);
		}
		
		visibleButtons.clear();
		
		// Pull the number to light off the stack
		for (int i=0; i < BUTTONS_LIT; i++){
			Handset h = hiddenButtons.remove(0);
			mHandsetController.setState(h.getInetAddress(), HandsetState.ARMED);
			h.setTimeTag(currentGameTime*1000 - BUTTON_TIMEOUT);
			visibleButtons.add(h);						
		}
	
	}
	


	public void handleInboundMessage(Message rmsg) {
		
		// The only messages we get from upstream are time pulses
		// Let's sweep for guys that are lit, that are now timed out
			
		currentGameTime = (Long)rmsg.obj;
		handleTime(currentGameTime);

		// Get the oldest handset (I hope)
		Handset iw = visibleButtons.get(0);
		
		long tt = iw.getTimeTag();
		if ( tt > currentGameTime ) 
			recycleHandset(iw);
		
	}

		
	public synchronized void recycleHandset(Handset iw){
		
		Handset niw = hiddenButtons.remove(0);
		mHandsetController.setState(niw.getInetAddress(), HandsetState.ARMED);
		
		// Move it to the visible array and time tag it
		visibleButtons.add(niw);
		niw.setTimeTag(currentGameTime - BUTTON_TIMEOUT);
		
		// move the button just pushed from visible to hidden
		visibleButtons.remove(iw);
		hiddenButtons.add(iw);
		mHandsetController.setState(iw.getInetAddress(), HandsetState.ARMED_NEGATIVE);

		if (DEBUG)
			Log.i("RECYCLE", "" + niw.getIPShort() + "lit. " + iw.getIPShort() + " recycled");
	}

	@Override
	public Boolean isRunning() {
		
		return mIsRunning;
	}

	
	
	private void changeState(State newState){
		
		if ( newState == mState )
			return; //do nothing
		
		mState = newState;

		if ( mListener != null )
			mListener.stateChange(uiState);
				
		++uiState;
		
		switch ( newState ){
		
		case THRESH3:
			mHandsetController.playWarn();
		case THRESH2:
		case THRESH1:
			mHandsetController.nextButton();
			break;
		default:
			break;
		}
	}
	
	
			
	@Override
	public void handleTime(long time) {
		
		if ( ( time > 10000 ) && (time < 20000 ))
			changeState(State.THRESH1);
		else if ( ( time > 5000 ) && (time < 9999 ) )
			changeState(State.THRESH2);
		else if ( time < 5000 )
			changeState(State.THRESH3);
		
		
		
	}

	@Override
	public int getMusicResource() {
		
		return R.raw.mysticloop;
	}

	@Override
	public int getGameLengthInSeconds() {
		
		return GAME_LENGTH;
	}

	@Override
	public int getNumberOfButtons() {
		return NUM_BUTTONS;
	}

	@Override
	public synchronized int getMultiplier() {
		// TODO Auto-generated method stub
		return mPlayerRate;
	}

	@Override
	public int getNumberOfGameStates() {
		// TODO Auto-generated method stub
		return State.values().length;
	}

	@Override
	public synchronized int getCurrentGameState() {

		switch (mState){
		case START:
			return 0;
		case THRESH1:
			return 1;
		case THRESH2:
			return 2;
		case THRESH3:
			return 3;
		}
		
		return -1; // error
	}

	@Override
	public void run() {
		
		resetGame();
		
		Looper.prepare();
		
		mHandler = new Handler(){
			
			@Override
	    	public void handleMessage(Message msg){
	    		
				handleInboundMessage(msg);
			
			}
			
		};
		
		mIsRunning = true;
		Looper.loop();
		
		
	}

	@Override
	public void handsetMessageReceived(int shortIP, int row, int column,
			byte command) {
		
		// ignore everything unless we are running!!
		if (!mIsRunning)
			return;
		
		Handset clickedWidget = mHandsetController.handsets.get(shortIP);
		
		if ( command == SOLMessage.CLICK ) {
				
				// process a click
				
				// first check if this is a stray click (RTX) from a button that should be OFF
				if (hiddenButtons.contains(clickedWidget)){
					mHandsetController.setState(clickedWidget.getInetAddress(), HandsetState.ARMED_NEGATIVE);
					return;
				}
				
				recycleHandset(clickedWidget);
				
				long thisTime = System.currentTimeMillis();
				long deltaT = thisTime - lastHitTime;
				lastHitTime = thisTime;
				
				averageHitTime = (averageHitTime + deltaT)/2;
				
				if (averageHitTime < 333)
					mPlayerRate = 4;
				else if (averageHitTime < 500)
					mPlayerRate = 3;
				else if (averageHitTime < 750)
					mPlayerRate = 2;
				else
					mPlayerRate = 1;
				
				mScore = mScore + mPlayerRate;
				
			} else if ( command == SOLMessage.CLICK_NEGATIVE ) {
				if ( mScore > 0 ) {
					mScore--; // penalty
					mPlayerRate = 1;
				}
			}		
		
	}

	@Override
	public String getGameName() {
		return "SOL7x15SWEEPV1";
	}

	@Override
	public void kill() {
		
		mIsRunning = false;
		mHandler.getLooper().quit();
		
	}
	
			
}
