package com.appdelegates.solgame;

import android.os.Message;

import com.appdelegates.solnetwork.UDPMessageRX;


public abstract class SOLGameEngine {
	
	SparseWidgetArray mWidgetArray;
	int mScore;
	Boolean mPlayMusic = false;
	UiStateChangeListener mListener;
	
	public SOLGameEngine(SparseWidgetArray widgetArray){
		mWidgetArray = widgetArray;
		//this.resetGame();
	}
	
	public interface UiStateChangeListener {
		void stateChange(int state);
	}
	
	public final void setUiStateChangeListener(UiStateChangeListener listener){
		mListener = listener;
	}
	
	public final int getScore(){
		return mScore;
	}
	
	public void endGame() {
		
		mWidgetArray.gameOver();
		
	}
	
	public abstract void handleMessage(Message msg);
	public abstract void handleTime(long time);
	public abstract int getMusicResource();
	public abstract int getGameLengthInSeconds();
	public abstract Boolean isRunning();
	public abstract void resetGame();

	public abstract int getNumberOfButtons();
	public abstract int getMultiplier();
	public abstract int getNumberOfGameStates();
	public abstract int getCurrentGameState();
	

}
