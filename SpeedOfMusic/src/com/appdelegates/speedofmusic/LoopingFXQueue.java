package com.appdelegates.speedofmusic;

import java.util.ArrayList;

import android.media.MediaPlayer;

public class LoopingFXQueue extends Thread {
	
	ArrayList<MediaPlayer> queue = new ArrayList<MediaPlayer>();
	ArrayList<MediaPlayer> activeTracks = new ArrayList<MediaPlayer>();
	private long mBeat;
	private boolean mIsRunning = false;
	
	
	public LoopingFXQueue(long tempo) {
		mBeat = tempo;
	}
	
	
	public void run() {
		
		mIsRunning = true;
		
		while(mIsRunning) {
			for (int i = 0; i < queue.size(); i++){
				queue.get(i).setLooping(true);
				queue.get(i).start();
				activeTracks.add(queue.get(i));
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
	
	
	public void start(MediaPlayer effect) {
		
		queue.add(effect);
		
	}
	
	public void stop(MediaPlayer effect) {
		
		effect.stop();
		//activeTracks.get(effect);
		
	}
	
	
	public void kill() {
		
		mIsRunning = false;
		queue.clear();
		
	}
}
