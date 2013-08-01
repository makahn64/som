package com.appdelegates.speedofmusic;

import java.util.ArrayList;
import java.util.Collections;

import com.appdelegates.solnetwork.HandsetState;

public class AttractAnimationChase extends AttractAnimation {

	public AttractAnimationChase(HandsetController handsetController) {
		super(handsetController);
	}


	
	@Override
	public void setupAnimationThread() {

		
		
		animationThread = new Thread(new Runnable(){			
			
			@Override
			public void run() {
				
				ArrayList<RowCol> handsets = new ArrayList<RowCol>(mHandsetController.handsets.size());
				
				for (int row=0; row<mHandsetController.mNumRows; row++ )
					for (int col=0; col<mHandsetController.mNumCols; col++ )
						handsets.add(new RowCol(row, col));
				
				
				
				while (running){
					
					for (int i=0; i < handsets.size(); i++){
						mHandsetController.setState(handsets.get(i).row, handsets.get(i).col, HandsetState.ON);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							running = false;
							break;
						}
					}
					
					for (int i=0; i < handsets.size(); i++){
						mHandsetController.setState(handsets.get(i).row, handsets.get(i).col, HandsetState.OFF);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							running = false;
							break;
						}
					}
					
					sendLoopDone();
					
				}
				
			
			}
			
		});
		
	}

}
