package com.appdelegates.solgame;

import com.appdelegates.solgame.HandsetController.HandsetControllerListener;

import android.os.Handler;
import android.os.Message;


public abstract class SOLGameEngineThread extends Thread implements HandsetControllerListener {
	
	HandsetController mHandsetController;
	int mScore;
	Boolean mPlayMusic = false;
	UiStateChangeListener mListener;
	Handler mHandler;
	
	public SOLGameEngineThread(HandsetController handsetController){
		mHandsetController = handsetController;
		//this.resetGame();
	}
	
	public synchronized Handler getGameEngineHandler(){
		return mHandler;
	}
	
	public interface UiStateChangeListener {
		void stateChange(int state);
	}
	
	public final void setUiStateChangeListener(UiStateChangeListener listener){
		mListener = listener;
	}

	
	public synchronized final int getScore(){
		return mScore;
	}
	
	public void endGame() {
		
		mHandsetController.gameOver();
		
	}
	
	public abstract void run();
	
	public abstract void handleInboundMessage(Message msg);
	public abstract void handleTime(long time);
	public abstract int getMusicResource();
	public abstract int getGameLengthInSeconds();
	public abstract Boolean isRunning();
	public abstract void resetGame();
	public abstract void kill();

	public abstract int getNumberOfButtons();
	public abstract int getMultiplier();
	public abstract int getNumberOfGameStates();
	public abstract int getCurrentGameState();
	public abstract String getGameName();
	

}
