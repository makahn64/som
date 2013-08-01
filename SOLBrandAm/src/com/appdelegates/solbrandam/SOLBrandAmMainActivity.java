package com.appdelegates.solbrandam;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.appdelegates.solnetwork.TCPClient;
import com.appdelegates.solnetwork.TCPClient.TCPClientListener;


public class SOLBrandAmMainActivity extends Activity {
	
	public static final Boolean USE_LAUNCH_CODE = false;
	public static final String CNAME = "BRANDAM";

	private Button goButton;
	
	EditText playerOneEmailET;
	EditText playerOneGamertagET;
	EditText codeET;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signinformlinear);
				
		playerOneEmailET = (EditText)findViewById(R.id.editTextEmail1);
		playerOneEmailET.setTextColor(Color.WHITE);
		
		playerOneGamertagET = (EditText)findViewById(R.id.editTextGamertag1);
		playerOneGamertagET.setTextColor(Color.WHITE);
				
		codeET = (EditText)findViewById(R.id.editTextCode);
		codeET.setTextColor(Color.WHITE);
		
		codeET.setVisibility(View.INVISIBLE);
		
	    
		goButton = (Button)findViewById(R.id.buttonGo);
		goButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				
				Boolean dot200 = getBooleanSetting("dot200");
				String targetIP = dot200 ? "192.168.1.200" : "192.168.1.100";
					
				String p1email = playerOneEmailET.getText().toString();
				String p1gt = playerOneGamertagET.getText().toString();
								
				String code = codeET.getText().toString();
				
				Boolean p1ok = (!p1gt.equals("")) && validEmail(p1email);
				Boolean codeLegit = code.equals("@tt");
				
				if (codeLegit || !USE_LAUNCH_CODE) {
					
					if ( p1ok ) {
						
						final String payload = "addGamer/"+p1email+"/"+p1gt;
						
						
						showOKDialog("Game On!", "Game Loading on: "+ (dot200?"Galaxy S2s.": "Galaxy Notes") );
						
						try {
							final TCPClient tcpc = new TCPClient(InetAddress.getByName(targetIP), 6466 );
							tcpc.setTCPClientListener(new TCPClientListener(){								


								@Override
								public void connectionEstablished(
										Handler handler) {
									tcpc.sendMessage(payload);
									
								}

								@Override
								public void connectionFailed(final Exception e) {
									codeET.post(new Runnable(){
										
										public void run(){
											showOKDialog("Error", "Error connecting: "+ e.toString() );
										}
										
									});									
								}
								
								@Override
								public void connectionDropped(InetAddress inetAddress, final Exception e) {
									Log.i(CNAME, "Connection dropped by other end");					
								}
					

								@Override
								public void receivedData(InetAddress inetAddress, final String data) {
									
									Log.i(CNAME, "Got back: "+data);
									if	(data.contains("error")){
										codeET.post(new Runnable(){
											
											public void run(){
												showOKDialog("Error", "Error connecting: "+data );
											}
											
										});
									}
								}
								
							});
							
							tcpc.connect();
							
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							toastMe("IP Address Conversion Exception");
						}
						
						//clearFields();

					}	
					else 	
						showOKDialog("Bad Info", "Bad email address or gamertag.");
						
					
				} else
					showOKDialog("Bad Code", "Bad launch code.");
			}	
			
		});
		

	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		if ( getBooleanSetting("useLaunchCode") )
			codeET.setVisibility(View.VISIBLE);
	}
	
	public Boolean getBooleanSetting(String key){
		
		SharedPreferences settings = getSharedPreferences("SETTINGS", 0);
		return settings.getBoolean(key, false);
		
	}
	
	public void setBooleanSetting(String key, Boolean val){
		
		SharedPreferences settings = getSharedPreferences("SETTINGS", 0);
		SharedPreferences.Editor editor = settings.edit();
	      
		editor.putBoolean(key, val);
	    editor.commit();
		
	}

	
	public void clearFields(){
		playerOneEmailET.setText("");
		playerOneGamertagET.setText("");
		codeET.setText("");
			
	}
	
	
	
	boolean validEmail(String email) {
		    // editing to make requirements listed
		   
		Boolean ok = email.contains("@");
		   return ok;
		   
		   //return true;
		   
		   // return email.matches("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}");
		    //return email.matches("[A-Z0-9._%+-][A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{3}");
		}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_solsign_in_main, menu);
		return true;
	}
	


	public void showOKDialog(String title, String message){
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);
 
			// set title
			alertDialogBuilder.setTitle(title);
 
			// set dialog message
			alertDialogBuilder
				.setMessage(message)
				.setCancelable(true)
				.setPositiveButton("OK",null)
				.setIcon(R.drawable.ic_launcher);
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_settings:
	            
	        	
	            Intent intent = new Intent(this, SettingsActivity.class);
	            startActivity(intent);
	           
	            return true;
	            
	        case R.id.menu_admin:
	        	
	        	Intent intent2 = new Intent(this, LeaderboardControlActivity.class);
	            startActivity(intent2);
	           
	            return true;
	            
	        case R.id.menu_stats:
	        	
	        	toastMe("Stats coming soon!");
	           
	            return true;
	        	
	        default:
	            return super.onOptionsItemSelected(item);
	    }
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

}
