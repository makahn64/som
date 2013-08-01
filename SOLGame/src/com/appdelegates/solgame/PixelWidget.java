package com.appdelegates.solgame;

public class PixelWidget {
	
	InputWidget widget;
	Boolean isOn;
	
	public PixelWidget(InputWidget iw){
		widget = iw;
		widget.hide();
		isOn = false;
	}
	
	public void on(){
		
		//if (!isOn){
			widget.show();
		//	isOn = true;
		//}
	}
	
	public void off(){
		
		//if (isOn){
			widget.hide();
		//	isOn = false;
		//}
	}

}
