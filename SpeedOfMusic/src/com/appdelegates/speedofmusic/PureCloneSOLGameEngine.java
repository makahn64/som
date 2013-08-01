package com.appdelegates.speedofmusic;

import java.util.ArrayList;
import java.util.Collections;

import android.os.Message;
import android.util.Log;

import com.appdelegates.solnetwork.SOLMessage;
import com.appdelegates.solnetwork.UDPMessageRX;





public class PureCloneSOLGameEngine extends SOLGameEngine {
	
	public static int BUTTONS_LIT = 7;
	public static int NUM_BUTTONS = 15;
	public static int GAME_LENGTH = 30;

	int mPlayerRate;
	long lastHitTime;
	long averageHitTime;
	int uiState;
	
	ArrayList<InputWidget> hiddenButtons; 
	ArrayList<InputWidget> visibleButtons;
	Boolean mIsRunning;
	
	enum State { START, THRESH1, THRESH2, THRESH3 };
	State mState;
	
	public PureCloneSOLGameEngine(SparseWidgetArray widgetArray) {
		super(widgetArray);		
		hiddenButtons = new ArrayList<InputWidget>();
		visibleButtons = new ArrayList<InputWidget>();
		mPlayerRate = 1;
	}

	public  synchronized void handleMessageR(UDPMessageRX msg) {
		
		
		try {
			InputWidget clickedWidget = mWidgetArray.get(msg.getShortIP());
			
			if ( msg.isMessage(SOLMessage.CLICK) ) {
				
				// process a click
				
				// forst check if this is a stray click (RTX) from a button that should be OFF
				if (hiddenButtons.contains(clickedWidget)){
					clickedWidget.armNegative();
					return;
				}
				
				InputWidget niw = hiddenButtons.remove(0);
				niw.arm();
				
				// Move it to the visible array
				visibleButtons.add(niw);
				
				// move the button just pushed from visible to hidden
				visibleButtons.remove(clickedWidget);
				hiddenButtons.add(clickedWidget);
				clickedWidget.armNegative(); // death here
				
				long thisTime = System.currentTimeMillis();
				long deltaT = thisTime - lastHitTime;
				lastHitTime = thisTime;
				
				averageHitTime = (averageHitTime + deltaT)/2;
				/*
				if ( averageHitTime <  1000 ) {
					mPlayerRate++;
					if (mPlayerRate > 6)
						mPlayerRate = 6;
				} else {
					if (mPlayerRate > 1)
						mPlayerRate--;
				}
				*/
				
				if (averageHitTime < 500)
					mPlayerRate = 4;
				else if (averageHitTime < 750)
					mPlayerRate = 3;
				else if (averageHitTime < 900)
					mPlayerRate = 2;
				else
					mPlayerRate = 1;
				
				mScore = mScore + mPlayerRate;
				
			} else if ( msg.isMessage(SOLMessage.CLICK_NEGATIVE) ) {
				if ( mScore > 0 ) {
					mScore--; // penalty
					mPlayerRate = 1;
				}
			}
			
			
		} catch (Exception e){
			Log.e("GameEngine", "Something is fucked "+e.toString());
		}


	}

	@Override
	public Boolean isRunning() {
		
		return mIsRunning;
	}

	@Override
	public void resetGame() {
		
		mScore = 0;		
		uiState = 0;
		mWidgetArray.reset();  // reset all handsets
		mWidgetArray.setPenalMode(true);
		
		changeState(State.START);
		
		mPlayerRate = 1;
		
		
		lastHitTime = System.currentTimeMillis();
		averageHitTime = 1000; // start them at 1 second to be fair
		
		hiddenButtons.clear();
		
		for(int i = 0; i < NUM_BUTTONS; i++){
		    
		    hiddenButtons.add( mWidgetArray.valueAt(i) );
		    
		}
		
		Collections.shuffle(hiddenButtons);
		
		for (InputWidget b: hiddenButtons){
			b.armNegative();
		}
		
		visibleButtons.clear();
		
		for (int i=0; i < BUTTONS_LIT; i++){
			InputWidget b = hiddenButtons.remove(0);
			b.arm();
			visibleButtons.add(b);			
			
		}
	
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
			mWidgetArray.playWarn();
		case THRESH2:
		case THRESH1:
			mWidgetArray.nextButton();
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
	public int getMultiplier() {
		// TODO Auto-generated method stub
		return mPlayerRate;
	}

	@Override
	public int getNumberOfGameStates() {
		// TODO Auto-generated method stub
		return State.values().length;
	}

	@Override
	public int getCurrentGameState() {

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
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}


}
