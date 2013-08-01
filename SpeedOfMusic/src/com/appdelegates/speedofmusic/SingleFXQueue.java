package com.appdelegates.speedofmusic;

import java.util.ArrayList;

import android.media.MediaPlayer;

public class SingleFXQueue extends Thread {
	
	ArrayList<MediaPlayer> queue = new ArrayList<MediaPlayer>();
	private long mBeat;
	private boolean mIsRunning = false;
	
	
	public SingleFXQueue(long tempo) {
		mBeat = tempo;
	}
	
	
	public void run() {
		
		mIsRunning = true;
		
		while(mIsRunning) {
			for (int i = 0; i < queue.size(); i++){
				queue.get(i).start();
			}
			
			queue.clear();
			
			try {
				Thread.sleep(mBeat);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public void push(MediaPlayer effect) {
		
		queue.add(effect);
		
	}
	
	
	public void kill() {
		
		mIsRunning = false;
		queue.clear();
		
	}
}
