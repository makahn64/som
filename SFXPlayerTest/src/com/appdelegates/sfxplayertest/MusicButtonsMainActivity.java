package com.appdelegates.sfxplayertest;

import java.io.IOException;
import java.util.ArrayList;

import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.media.AudioManager;

public class MusicButtonsMainActivity extends Activity {
	// Doink!
	SoundPool audio;
	
	ArrayList<MediaPlayer> mediaPlayers = new ArrayList<MediaPlayer>(10);
	MediaPlayer mainTrack;
	
	int basetrack1ID;
	int fxtrack1ID;
	int fxtrack2ID;
	int fxtrack3ID;
	int fxtrack4ID;
	int fxtrack5ID;
	int track1ID;
	int track2ID;
	int track3ID;
	int track4ID;
	int track5ID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_buttons_main);
		
		Button button1 = (Button)findViewById(R.id.button1);
		Button button2 = (Button)findViewById(R.id.button2);
		Button button3 = (Button)findViewById(R.id.button3);
		Button button4 = (Button)findViewById(R.id.button4);
		Button button5 = (Button)findViewById(R.id.button5);
		Button button6 = (Button)findViewById(R.id.button6);
		Button button7 = (Button)findViewById(R.id.button7);
		Button button8 = (Button)findViewById(R.id.button8);
		Button button9 = (Button)findViewById(R.id.button9);
		Button button10 = (Button)findViewById(R.id.button10);
		
		Button startButton = (Button)findViewById(R.id.startButton);
		
		button1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				//audio.play(fxtrack1ID, 1, 1, 10, 0, 1);
				mediaPlayers.get(0).start();
				
			}
	    });
		 
		button2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				//audio.play(fxtrack2ID, 1, 1, 9, 0, 1);
				mediaPlayers.get(1).start();
				
			}
	    });
		
		button3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				//audio.play(fxtrack3ID, 1, 1, 8, 0, 1);
				mediaPlayers.get(2).start();
				
			}
	    });
		
		button4.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				//audio.play(fxtrack4ID, 1, 1, 7, 0, 1);
				mediaPlayers.get(3).start();
				
			}
	    });
		
		button5.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				//audio.play(fxtrack5ID, 1, 1, 6, 0, 1);
				mediaPlayers.get(4).start();
				
			}
	    });
		
		button6.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				//audio.play(track1ID, 1, 1, 5, 0, 1);
				mediaPlayers.get(5).start();
				
			}
	    });
		
		button7.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				//audio.play(track2ID, 1, 1, 4, 0, 1);
				mediaPlayers.get(6).start();
				
			}
	    });
		
		button8.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				//audio.play(track3ID, 1, 1, 3, 0, 1);
				mediaPlayers.get(7).start();
				
			}
	    });
		
		button9.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				//audio.play(track4ID, 1, 1, 2, 0, 1);
				mediaPlayers.get(8).start();
				
			}
	    });
		
		button10.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				//audio.play(track5ID, 1, 1, 1, 0, 1);
				mediaPlayers.get(9).start();
				
			}
	    });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.music_buttons_main, menu);
		return true;
	}
	
	@Override
	protected void onPause (){
		
		audio.release();
		audio = null;
		
	}
	
	@Override
	protected void onResume () {
		
		super.onResume();
		
		/*
		audio = new SoundPool(11, AudioManager.STREAM_MUSIC, 1);
		basetrack1ID = audio.load(this, R.raw.basebeat, 1);
		fxtrack1ID = audio.load(this, R.raw.effect1, 1);
		fxtrack2ID = audio.load(this, R.raw.effect2, 1);
		fxtrack3ID = audio.load(this, R.raw.effect3, 1);
		fxtrack4ID = audio.load(this, R.raw.effect4, 1);
		fxtrack5ID = audio.load(this, R.raw.effect5, 1);
		track1ID = audio.load(this, R.raw.musicaleffect1, 1);
		track2ID = audio.load(this, R.raw.musicaleffect2, 1);
		track3ID = audio.load(this, R.raw.musicaleffect3, 1);
		track4ID = audio.load(this, R.raw.musicaleffect4, 1);
		track5ID = audio.load(this, R.raw.musicaleffect5, 1);
		*/
		
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
		
		mediaPlayers.clear();
		
		for (int i=0; i<10; i++){
			
			MediaPlayer tmp = MediaPlayer.create(this, tracks[i]);
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
		
		mainTrack = MediaPlayer.create(this , R.raw.basebeat );
		mainTrack.setLooping(true);
		mainTrack.start();
		
	}
	
}
