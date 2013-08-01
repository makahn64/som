package com.example.solleaderboard;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appdelegates.solnetwork.Gamer;
import com.example.solleaderboard.LeaderboardUpdateThread.LeaderboardUpdateListener;

public class SOLLeaderboardMainActivity extends Activity {
	
	public static final Boolean DEBUG = true;
	
	ImageView ball1;
	ImageView ball2;
	ImageView ball3;
	
	TextView reload;
	TextView eventInfo;
	
	RepeatingAnimation anim1, anim2, anim3;
	ArrayList<TextView> leadersTV;
	
	LeaderboardUpdateThread lut;
		

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_solleaderboard_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		reload = (TextView)findViewById(R.id.textViewReload);
		reload.setVisibility(View.INVISIBLE);
		
		eventInfo = (TextView)findViewById(R.id.textViewEvent);
		eventInfo.setVisibility(View.INVISIBLE);
		
		leadersTV = new ArrayList<TextView>(10);
		
		leadersTV.add( (TextView)findViewById(R.id.textViewLeader1) );
		leadersTV.add( (TextView)findViewById(R.id.textViewLeader2) );
		leadersTV.add( (TextView)findViewById(R.id.textViewLeader3) );
		leadersTV.add( (TextView)findViewById(R.id.textViewLeader4) );
		leadersTV.add( (TextView)findViewById(R.id.textViewLeader5) );
		
		leadersTV.add( (TextView)findViewById(R.id.textViewLeader6) );
		leadersTV.add( (TextView)findViewById(R.id.textViewLeader7) );
		leadersTV.add( (TextView)findViewById(R.id.textViewLeader8) );
		leadersTV.add( (TextView)findViewById(R.id.textViewLeader9) );
		leadersTV.add( (TextView)findViewById(R.id.textViewLeader10) );
		
		Typeface face=Typeface.createFromAsset(getAssets(),
                "fonts/omnesmed.otf");

		for (TextView t: leadersTV) {
			t.setTypeface(face);
			t.setText("loading...");
		}
		
		
		
		ball1 = (ImageView)findViewById(R.id.imageView1); // right
		ball3 = (ImageView)findViewById(R.id.imageView2);
		ball2 = (ImageView)findViewById(R.id.imageView4); // center sphere
		
		
		AnimatorListenerAdapter repeat = new AnimatorListenerAdapter() {
			 
			@Override
			public void onAnimationEnd(Animator animation) {
			    super.onAnimationEnd(animation);
			    animation.start();
			}
			 
		};
			
		
		AnimatorSet as = new AnimatorSet();
		as.playSequentially(ObjectAnimator.ofFloat(ball2, "translationX", 0, 50), // anim 1
		                    ObjectAnimator.ofFloat(ball2, "translationX", 50, -50), // anim 2
		                    ObjectAnimator.ofFloat(ball2, "translationX", -50, 0)); // anim 4
		as.setDuration(15000);
		as.addListener(repeat);
		as.start();
		
		AnimatorSet asl = new AnimatorSet();
		asl.playSequentially(ObjectAnimator.ofFloat(ball3, "translationY", 0, 20), // anim 1
		                    ObjectAnimator.ofFloat(ball3, "translationY", 20, 0)); // anim 4
		asl.setDuration(15000);
		asl.addListener(repeat);
		asl.start();
		
		AnimatorSet asr = new AnimatorSet();
		asr.playTogether(ObjectAnimator.ofFloat(ball1, "translationY", 20, 0), // anim 1
                ObjectAnimator.ofFloat(ball1, "translationX", 0, 15));
		
		asr.setDuration(15000);
		asr.addListener(repeat);
		asr.start();
		
		
    
	    
	}
	
	@Override
	protected void onResume(){
		
		super.onResume();
		
		lut = new LeaderboardUpdateThread();
		lut.setLeaderboardUpdateListener(new LeaderboardUpdateListener(){

			@Override
			public void leaderboardUpdate(final ArrayList<Gamer> top10) {
				// we just need any-old ui element to post the update on
				ball1.post(new Runnable(){

					@Override
					public void run() {
						updateUI(top10);
					}
					
				});
				
			}

			@Override
			public void leaderboardNetworkFail(final String msg) {
				ball1.post(new Runnable(){

					@Override
					public void run() {
						toastMe(msg);
					}
					
				});
				
			}

			@Override
			public void leaderboardUpdateEvent(final String eventName,
					final Boolean shouldShow) {
				ball1.post(new Runnable(){

					@Override
					public void run() {
						
						eventInfo.setVisibility(shouldShow ? View.VISIBLE : View.INVISIBLE);
						eventInfo.setText(eventName);
					}
					
				});
				
			}
			
		});
		
		lut.start();
		
	
	}
	
	@Override
	protected void onPause(){
		
		
		super.onPause();
		
	}

	public void updateUI(ArrayList<Gamer> gamers){
		
			int i = 0;
		
			for (TextView ltv: leadersTV){
				if (i < gamers.size()){
					Gamer g = gamers.get(i);
					ltv.setText(g.getGamertag()+" /  "+g.getScore());
				} else {
					ltv.setText("");
				}
				i++;
			}
					
		
		reload.setVisibility(View.INVISIBLE);

	}
	
	public void toastMe(String toastage){
		toastMe(toastage, false);
	}
			
	public void toastMe(String toastage, Boolean fast){
		
		Context context = getApplicationContext();
		
		int duration = fast? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, toastage, duration);
		toast.show();
	}
			
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_solleaderboard_main, menu);
		return true;
	}


}
