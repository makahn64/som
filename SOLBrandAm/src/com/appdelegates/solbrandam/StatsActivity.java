package com.appdelegates.solbrandam;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.appdelegates.solnetwork.Gamer;
import com.appdelegates.solnetwork.SOLEvent;
import com.appdelegates.solnetwork.TCPClient;
import com.appdelegates.solnetwork.TCPClient.TCPClientListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;



public class StatsActivity extends Activity {

	private static final String CNAME = "LBControl";

	public static Boolean SHOW_RAW = true;
	
	SOLEvent currentEvent;
	
	Button setFilterButton;
	TextView currentFilterName;
	EditText newFilterName;
	
	DatePicker startDatePicker;
	DatePicker endDatePicker;
	
	TimePicker startTimePicker;
	TimePicker endTimePicker;
	
	CheckBox allTimeCB;
	CheckBox showOnLBCB;
	
	ListView gamersLV;
	
	
	ArrayList<Gamer> allScores;
	GamerAdapter gamerAdapter;
	

	
	Handler mHandler;

	Thread highScoreThread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaderboardcontrol);
		
		mHandler = new Handler();
		
		allScores = new ArrayList<Gamer>();
		gamersLV = (ListView)findViewById(R.id.listViewScores);
		gamerAdapter = new GamerAdapter(getApplicationContext(), R.layout.gamer_cell, allScores);
		gamersLV.setAdapter(gamerAdapter);
		gamersLV.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View clickedView, int arg2,
					long arg3) {
				
				TextView score = (TextView)clickedView.findViewById(R.id.textViewScore);
				TextView gt = (TextView)clickedView.findViewById(R.id.textViewGamertag);
				int s = Integer.parseInt( score.getText().toString() );
				String gts = gt.getText().toString();
				Log.i(CNAME, "Erase score: "+s+" from "+gts);
				showConfirmWipeDialog("Delete Score?", 
						"Are you sure you want to delete the score of "+s+" by "+ gts+"?", gts, s);
			}
			
		});
		
		currentFilterName = (TextView)findViewById(R.id.textViewCurrentFilter);
		
		newFilterName = (EditText)findViewById(R.id.editTextFilterName);
		Date minDate = new Date(113, 1, 1);
		
		startDatePicker = (DatePicker)findViewById(R.id.datePickerStart);		
		startDatePicker.setMinDate(minDate.getTime());
		endDatePicker = (DatePicker)findViewById(R.id.datePickerEnd);
		endDatePicker.setMinDate(minDate.getTime());
		
		startTimePicker = (TimePicker)findViewById(R.id.timePickerStart);
		endTimePicker = (TimePicker)findViewById(R.id.timePickerEnd);

		allTimeCB = (CheckBox)findViewById(R.id.checkBoxAllTime);
		showOnLBCB = (CheckBox)findViewById(R.id.checkBoxShowNameOnLB);
		
		setFilterButton = (Button)findViewById(R.id.buttonSetFilter);
		setFilterButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				int sdom = startDatePicker.getDayOfMonth();
				int sy = startDatePicker.getYear();
				int sm = startDatePicker.getMonth();				
				int shour = startTimePicker.getCurrentHour();
				int smin = startTimePicker.getCurrentMinute();
				
				long startTime = dateTimeToMs(sy, sm, sdom, shour, smin);
				
				Log.i(CNAME, "Month: "+sm+" Day: "+sdom+" Year: "+sy);
				Log.i(CNAME, "Ms Date: "+ dateTimeToMs(sy, sm, sdom, shour, smin));
				Log.i(CNAME, "Ms System: "+ System.currentTimeMillis());
				
				int edom = endDatePicker.getDayOfMonth();
				int ey = endDatePicker.getYear();
				int em = endDatePicker.getMonth();				
				int ehour = endTimePicker.getCurrentHour();
				int emin = endTimePicker.getCurrentMinute();
				
				long endTime = dateTimeToMs(ey, em, edom, ehour, emin);
				
				
				Log.i(CNAME, "Ms End: "+ endTime );
				Log.i(CNAME, "Windows in seconds: "+ ((endTime - startTime)/1000));
				
				Boolean allTime = allTimeCB.isChecked();
				if (allTime){
					// -1 is a flag to the statsModel to get everything
					endTime = startTime = -1;
					Log.i(CNAME, "All time scores chosen");
				}
				
				String showOnLB = showOnLBCB.isChecked() ? "true" : "false";
				
				Log.i(CNAME, "Shown on screen: "+showOnLB);
				
				String eventName = newFilterName.getText().toString();
				
				final String cmd = "setDefaultEvent/" + eventName + "/Note/" + startTime + "/" +
						endTime + "/" + showOnLB;
				
				
				
				try {
					final TCPClient tcpc = new TCPClient("192.168.1.100", 6565);
					tcpc.setTCPClientListener(new TCPClientListener(){

						@Override
						public void connectionEstablished(Handler handler) {
							tcpc.sendMessage(cmd);
							mHandler.postDelayed(new Runnable(){

								@Override
								public void run() {
									fetchCurrentFilter();
								}
								
							}, 2000);
						}

						@Override
						public void connectionDropped(InetAddress inetAddress,
								Exception e) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void connectionFailed(Exception e) {
							toastMe("Could not update 192.168.1.100");
						}

						@Override
						public void receivedData(InetAddress inetAddress,
								String data) {
							// TODO Auto-generated method stub
							
							
						}
						
					});
					tcpc.connect();
					
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					final TCPClient tcpc = new TCPClient("192.168.1.200", 6565);
					tcpc.setTCPClientListener(new TCPClientListener(){

						@Override
						public void connectionEstablished(Handler handler) {
							tcpc.sendMessage(cmd);
							
						}

						@Override
						public void connectionDropped(InetAddress inetAddress,
								Exception e) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void connectionFailed(Exception e) {
							toastMe("Could not update @ 192.168.1.200");
						}

						@Override
						public void receivedData(InetAddress inetAddress,
								String data) {
							// TODO Auto-generated method stub
							
							
						}
						
					});
					tcpc.connect();
					
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
								
				
			}
			
		});
		
		
	
	}
	
	@Override
	public void onBackPressed(){
		
		
		super.onBackPressed();
	}
	
	
	@Override
	protected void onResume(){
		
		super.onResume();
		
		// OK, need to load current event info
		
		fetchCurrentFilter();
		
		fetchCurrentScores();
	
	}
	
	private void fetchCurrentScores() {
		
		
		try {
			final TCPClient tcpc = new TCPClient("192.168.1.100", 6565);
			tcpc.setTCPClientListener(new TCPClientListener(){

				@Override
				public void connectionEstablished(Handler handler) {
					tcpc.sendMessage("getT10All");
					
				}

				@Override
				public void connectionDropped(InetAddress inetAddress,
						Exception e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void connectionFailed(Exception e) {
					toastMe("Could not get scores from 192.168.1.100");
				}

				@Override
				public void receivedData(InetAddress inetAddress,
						String data) {
				
					if (data.equals(""))
						return;
					
					Gson gson = new Gson();
					JsonParser parser = new JsonParser();
				    JsonArray array = parser.parse(data).getAsJsonArray();
				    
				    allScores.clear();
				    for (int i=0; i < array.size(); i++){
				    	Gamer g = gson.fromJson(array.get(i), Gamer.class);
				    	allScores.add(g);
				    }
					
				    mHandler.post(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							gamerAdapter.notifyDataSetChanged();
							toastMe("Refreshing...");
						}
				    	
				    });
				    
					
				}
				
			});
			tcpc.connect();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	protected void onPause(){
		
		
		super.onPause();
		
		
	}
	
	public void updateUI(){
	
		if (currentEvent.eventName.equals("")){
			currentFilterName.setText("[NONE]");
		} else {
			currentFilterName.setText(currentEvent.eventName);
		}
			
		if (currentEvent.endTime>0){
			Date endDate = new Date(currentEvent.endTime);
			int ey = endDate.getYear();
			int em = endDate.getMonth();
			int ed =  endDate.getDate();
			endDatePicker.updateDate(ey+1900, em, ed);
			endTimePicker.setCurrentHour(endDate.getHours());
			endTimePicker.setCurrentMinute(endDate.getMinutes());
		}
			
		if (currentEvent.startTime>0){
			Date startDate = new Date(currentEvent.startTime);
			startDatePicker.updateDate(startDate.getYear()+1900, startDate.getMonth(), startDate.getDate());
			startTimePicker.setCurrentHour(startDate.getHours());
			startTimePicker.setCurrentMinute(startDate.getMinutes());
		}
			
			
			
		showOnLBCB.setChecked(currentEvent.showOnLeaderboard);
		allTimeCB.setChecked(currentEvent.startTime<0);
			
		

	}

	
	public long dateTimeToMs(int year, int month, int day, int hour, int minute){
		
		Date date = new Date(year-1900, month, day, hour, minute);
		return date.getTime();
		
		
	}

	
	public void fetchCurrentFilter(){
		try {
			final TCPClient tcpClient = new TCPClient("192.168.1.100", 6565);
			tcpClient.setTCPClientListener(new TCPClientListener(){

				

				@Override
				public void connectionEstablished(Handler handler) {
					tcpClient.sendMessage("getDefaultEvent");
					
				}

				@Override
				public void connectionDropped(InetAddress inetAddress,
						Exception e) {
					Log.i(CNAME, "Connection dropped!");
					
				}

				@Override
				public void connectionFailed(Exception e) {
					toastMe("Cannot connect to Game at 192.168.1.100");
					
				}

				@Override
				public void receivedData(InetAddress inetAddress, String data) {
					
					if (data.equals(""))
						return;
					
					Gson gson = new Gson();
					currentEvent = gson.fromJson(data, SOLEvent.class);
					mHandler.post(new Runnable(){

						@Override
						public void run() {
							toastMe("Receiving current settings.");
							updateUI();
						}
						
					});
				    	
					
				}
				
			});
			
			tcpClient.connect();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
			

	public void showConfirmWipeDialog(String title, String message, final String gtag, final int score){
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);
 
			// set title
			alertDialogBuilder.setTitle(title);
 
			// set dialog message
			alertDialogBuilder
				.setMessage(message)
				.setCancelable(true)
				.setPositiveButton("Nuke Away", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						
						mHandler.post(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								eraseScore(gtag, score, "192.168.1.100");
								eraseScore(gtag, score, "192.168.1.200");
								fetchCurrentScores();
							}
							
						});
						
						
						
					}
				})
				.setNegativeButton("CANCEL Nukage", null)
				.setIcon(R.drawable.ic_launcher);
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
	}
	
	public void eraseScore(final String gtag, final int score, final String ipAddr){
		
		try {
			final TCPClient eraseClient = new TCPClient(ipAddr, 6565);
			eraseClient.setTCPClientListener(new TCPClientListener(){

				@Override
				public void connectionEstablished(Handler handler) {
					eraseClient.sendMessage("removeScore/"+gtag+"/"+score);
					
				}

				@Override
				public void connectionDropped(InetAddress inetAddress,
						Exception e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void connectionFailed(Exception e) {
					toastMe("Could not connect to: "+ipAddr);
					
				}

				@Override
				public void receivedData(InetAddress inetAddress, String data) {
					// TODO Auto-generated method stub
					
				}
				
			});
			eraseClient.connect();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
