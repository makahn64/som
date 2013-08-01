package com.appdelegates.speedofmusic;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.appdelegates.solnetwork.HandsetState;
import com.appdelegates.solnetwork.SOLMessage;


public class SOMGameEngineThread extends SOLGameEngineThread {
	
	public static Boolean DEBUG = false;
	
	
	public static int NUM_BUTTONS = 5;
	public static int GAME_LENGTH = 30;
	
	ArrayList<MediaPlayer> mediaPlayers;
	LoadoutMap loadouts = new LoadoutMap();
	EventQueue oneOffQueue;
	MediaPlayer mainTrack;


	int uiState;
	long currentGameTime;
	
	ArrayList<Handset> controllers; 

	Boolean mIsRunning;
	
	enum State { START, THRESH1, THRESH2, THRESH3  };
	State mState;
	
	public SOMGameEngineThread(HandsetController handsetController, Context context) {
		super(handsetController, context);		
		
		controllers = new ArrayList<Handset>();

		mHandsetController = handsetController;
		
		mHandsetController.addCommandListenerForCommand(SOLMessage.CLICK, this);
		
		
	}
	
	@Override
	public void resetGame() {
		
		
		currentGameTime = GAME_LENGTH;
			
		uiState = 0;
		mHandsetController.resetAllHandsets();  // reset all handsets
		
		
		
		mIsRunning = false;
		
		changeState(State.START);
		
		
		// Make all buttons hidden
		for(int i = 0; i < mHandsetController.handsets.size(); i++){
		    
		   controllers.add( mHandsetController.handsets.valueAt(i) );
		   mHandsetController.setState(controllers.get(i).getInetAddress(), 
				   HandsetState.ARMED);
		    
		}
		
		int [] tracks = new int[10];
		tracks[0] = R.raw.effect1;
		tracks[1] = R.raw.effect2;
		tracks[2] = R.raw.effect3;
		tracks[3] = R.raw.effect4;
		tracks[4] = R.raw.effect5;
		tracks[5] = R.raw.musicaleffect1;
		tracks[6] = R.raw.musicaleffect2;
		tracks[7] = R.raw.musicaleffect3;
		tracks[8] = R.raw.musicaleffect4;
		tracks[9] = R.raw.musicaleffect5;
		
		mediaPlayers = new ArrayList<MediaPlayer>(10);
		
		for (int i=0; i<10; i++){
			
			MediaPlayer tmp = MediaPlayer.create(mContext, tracks[i]);
			try {
				tmp.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			mediaPlayers.add(tmp);
			
		}
		
		oneOffQueue = new EventQueue((long) 500);
		
		oneOffQueue.start();
		
		mainTrack = MediaPlayer.create(mContext , R.raw.basebeat );
		mainTrack.setLooping(true);
		mainTrack.start();
		
	}
	


	public void handleInboundMessage(Message rmsg) {
		
		// The only messages we get from upstream are time pulses
		// Let's sweep for guys that are lit, that are now timed out
			
		currentGameTime = (Long)rmsg.obj;
		handleTime(currentGameTime);

		
		
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
		// BS return value for testing
		return 2;
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
			mHandsetController.setState(clickedWidget.getInetAddress(), 
					   HandsetState.ARMED);
			
			switch (shortIP) {
			
			case 110:
				//mediaPlayers.get(5).start();
				oneOffQueue.push(mediaPlayers.get(5));
				break;
				
			case 111:
				//mediaPlayers.get(6).start();
				oneOffQueue.push(mediaPlayers.get(6));
				break;
				
			case 112:
				//mediaPlayers.get(7).start();
				oneOffQueue.push(mediaPlayers.get(7));
				break;
				
			case 113:
				//mediaPlayers.get(8).start();
				oneOffQueue.push(mediaPlayers.get(8));
				break;
				
			case 114:
				//mediaPlayers.get(9).start();
				oneOffQueue.push(mediaPlayers.get(9));
				break;
				
			}
		
		}
		
	}

	@Override
	public String getGameName() {
		return "SOMV0p1";
	}

	@Override
	public void kill() {
		
		mIsRunning = false;
		oneOffQueue.kill();
		mHandler.getLooper().quit();
		for (int i = 0; i < 10; i++) {			
			mediaPlayers.get(i).release();			
		}
		
		mediaPlayers.clear();
		mainTrack.release();
		mainTrack = null;
	}
	
			
}
