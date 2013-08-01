package com.appdelegates.solbrandam;



import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;



public class SettingsActivity extends Activity {

	RadioButton gs2radio;
	RadioButton gnradio;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setupmenu);
		
		gs2radio = (RadioButton)findViewById(R.id.radioButtonGS2);
		gnradio =  (RadioButton)findViewById(R.id.radioButtonGN);
		
		Boolean dot200 = getBooleanSetting("dot200");
		
		gs2radio.setChecked(dot200);
		gnradio.setChecked(!dot200);

	}
	
	@Override
	public void onBackPressed(){
		
		setBooleanSetting("dot200", gs2radio.isChecked());
		super.onBackPressed();
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


	

}
