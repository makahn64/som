package com.appdelegates.solgame;

import java.util.ArrayList;

import android.util.SparseArray;

public class SparseWidgetArray extends SparseArray<InputWidget> {
	
	public void reset(){
		for(int i = 0; i < this.size(); i++)
			this.valueAt(i).reset();
	}
	
	public void playWarn(){
		for(int i = 0; i < this.size(); i++)
			this.valueAt(i).playWarn();
	}
	
	public void nextButton(){
		for(int i = 0; i < this.size(); i++)
			this.valueAt(i).nextButton();
	}
	
	public void countdown(){
		for(int i = 0; i < this.size(); i++)
			this.valueAt(i).countdown();
	}
	
	public void setPenalMode(Boolean isPenal){
		for(int i = 0; i < this.size(); i++)
			this.valueAt(i).setPenalMode(isPenal);
	}
	
	public void gameOver(){
		for(int i = 0; i < this.size(); i++)
			this.valueAt(i).gameOver();
	}
	
	public void idle(){
		for(int i = 0; i < this.size(); i++)
			this.valueAt(i).idle();
	}

	
	public void startButton(){
		for(int i = 0; i < this.size(); i++) {
			this.valueAt(i).startButton();
			this.valueAt(i).startButton();
		}
	}
	
	public ArrayList<InputWidget> getAsArrayList(){
		ArrayList<InputWidget> rlist = new ArrayList<InputWidget>(this.size());
		for(int i = 0; i < this.size(); i++)
			rlist.add(this.valueAt(i));
		return rlist;
		
		
	}

}
