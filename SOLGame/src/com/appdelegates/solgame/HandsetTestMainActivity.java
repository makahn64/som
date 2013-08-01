package com.appdelegates.solgame;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.appdelegates.solgame.Handset.HandsetState;
import com.appdelegates.solgame.HandsetController.HandsetControllerListener;
import com.appdelegates.solnetwork.SOLMessage;

public class HandsetTestMainActivity extends Activity {
	
	public static Boolean DEBUG = false;
		
	public static final String CLASSNAME = "HandsetTest";
	
	
	TextView logTV;
	HandsetController hsc;
	
			
	Handler mUIHandler = null;
	

	@Override
	public void onBackPressed() {
	}
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.handsettest);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
						
		hsc = new HandsetController(110, 2, 3);
		HandsetControllerListener hcl = new HandsetControllerListener(){

			@Override
			public void handsetMessageReceived(final int shortIP, int row,
					int column, final byte bs) {
				
				if (bs==SOLMessage.CLICK)
					hsc.setState(row, column, HandsetState.ARMED_NEGATIVE);
				else if (bs==SOLMessage.CLICK_NEGATIVE)
					hsc.setState(row, column, HandsetState.ARMED);
				else if (bs==SOLMessage.START_GAME)
					toastMe("START");
				
				logTV.post(new Runnable(){

					@Override
					public void run() {
						logTV.setText("Msg: "+bs+ " from: "+ shortIP);
					}
					
				});
				
			}
		};
		
		hsc.addCommandListenerForCommand(SOLMessage.CLICK, hcl);
		hsc.addCommandListenerForCommand(SOLMessage.CLICK_NEGATIVE, hcl);
		hsc.addCommandListenerForCommand(SOLMessage.START_GAME, hcl);

		logTV = (TextView)findViewById(R.id.textViewLog);
		
		Button start = (Button)findViewById(R.id.buttonStart);
		start.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				
				hsc.setState(0,0,HandsetState.START_BUTTON);
				
			}
			
		});
		
		Button cd = (Button)findViewById(R.id.buttonCountdown);
		cd.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				
				hsc.setStatesDelayed(0,0,HandsetState.COUNTDOWN, 3750, HandsetState.OFF);
				
			}
			
		});
		
		Button id = (Button)findViewById(R.id.buttonIdle);
		id.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				
				hsc.setStatesDelayed(0,0,HandsetState.ATTRACT, 5000, HandsetState.OFF);
				
			}
			
		});
		
		Button on = (Button)findViewById(R.id.buttonOn);
		on.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				
				hsc.setState(0,0,HandsetState.ARMED);
				
			}
			
		});


			

	}
	
	
	public void toastMe(String toastage){
		
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, toastage, duration);
		toast.show();
	}

}
