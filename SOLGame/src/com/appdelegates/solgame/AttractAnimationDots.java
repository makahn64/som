package com.appdelegates.solgame;

import java.util.ArrayList;
import java.util.Collections;

import com.appdelegates.solgame.Handset.HandsetState;

/**
 * 
 * @author mkahn
 *
 *	Notice there are a lot of variables and methods that are used but never defined in here. That's because
 *	they are defined in the parent class: AttractAnimation.
 *
 */

public class AttractAnimationDots extends AttractAnimation {

	
	public AttractAnimationDots(HandsetController handsetController) {
		// We don't do anything special for initialization here, so just chuck the hadnsetController
		// reference to the superclass constructor to deal with.
		super(handsetController);
	}

	// @Override tells Eclipse that this function overrides one in a parent class. Eclipse checks to make
	// sure there really is a method that matches exactly in the parent and pukes if not. It makes it obvious if the method signatures 
	// don't match in which case you're not *really* overriding anything! For example, if I had accidentally typed:
	// protected void setupAnimationThread(), @Override would throw an error since such a function does not exist in the
	// superclass.
	
	@Override
	public void setupAnimationThread() {

		// Create the animation Thread. a Thread class takes a Runnable object as a parameter.
		// Feel free to Google Thread and Runnable to learn about the many ways (well at least 2) you can
		// create a Thread.
		
		animationThread = new Thread(new Runnable(){			
			
			// All Threads must have a run() method. It is the main() for a Thread.
			@Override
			public void run() {
				
				// Create a new ArrayList (array on steroids) of RowCol objects. It will be as big
				// as the number of handsets this version of the SOL app is using (15).
				ArrayList<RowCol> handsets = new ArrayList<RowCol>(mHandsetController.handsets.size());
				
				for (int row=0; row<mHandsetController.mNumRows; row++ )
					for (int col=0; col<mHandsetController.mNumCols; col++ )
						handsets.add(new RowCol(row, col));
				
				
				
				while (running){
					
					Collections.shuffle(handsets);
					for (int i=0; i < handsets.size(); i++){
						mHandsetController.setState(handsets.get(i).row, handsets.get(i).col, HandsetState.ATTRACT);
						try {
							Thread.sleep(250);
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
