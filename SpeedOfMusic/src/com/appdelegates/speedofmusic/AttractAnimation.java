package com.appdelegates.speedofmusic;

/**
 * 
 * @author mkahn
 * This class is abstract which means it is never directly instantiated (i.e. new AttractAnimation() ).
 * It is "cartilage" for subclasses like AttractAnimationChase. It also means, a class that instantiates
 * a subclass of AttractAnim (like the main Game class), does not need to refer to each subclass by it's 
 * exact class name; it can use this class. Example:
 * 
 *   AttractAnim dotAnim = new AttractAnimDots();
 *   AttractAnim chaseAnim = new AttractAnimChase();
 *   
 *   The other thing you can do, and I do here, in abstract classes is implement stuff you know all child class
 *   will need, or must have.
 *
 */
public abstract class AttractAnimation {
	
	// is this animation currently running?
	Boolean running;
	
	// The Java Thread that runs in the background that does the animation
	Thread animationThread;
	
	// This is an object that implements the AttractAnimListener interface defined below.
	// What this means is the object referenced (pointed to) by this variable must implement at least
	// the mehtod(s) defined in the interface. Listeners are used all the time to communicate between objects.
	
	AttractAnimListener mAnimListener;
	
	// This class encapsulates all the nasty of talking to the individual phones.
	HandsetController mHandsetController;	
	
	// All AttractAnims have to have a reference to the handsetController, so pass it in the constructor
	public AttractAnimation(HandsetController handsetController){
		
		mHandsetController = handsetController;
		setupAnimationThread();
		
	}
	
	// Simple class that gives me a Row/Column type for the handsets
	protected class RowCol {
		public int row;
		public int col;
		
		public RowCol(int row, int col){
			
			// The "this" prefix is only needed to get rid of ambiguity over which variable named
			// row (the one in the class, or the parameter). I could just as easily named the class
			// variables mRow and mCol and then done mRow = row. 
			
			this.row = row;
			this.col = col;
		}
	}
	
	// Assign something useful to animationThread
	// Abstract methods must be implemented in the child class.
	public abstract void setupAnimationThread();
	
	
	// This defines the methods that will be used to communicate to any AttractAnimListener.
	// In this case, there is only one ("loopDone()") and it passes no info back to the listener.
	// loopDone() is used to indicate to the main game class, that this class has finished one loop of the
	// attract animation and that it is a good time to run any pending games.
	public interface AttractAnimListener {
		
		void loopDone();
	}
	
	/**
	 * Set the listener for this AttractAnimation. It is usually the main Game Activity.
	 * @param attractAnimListener
	 */
	public void setAttractAnimListener(AttractAnimListener attractAnimListener){
		mAnimListener = attractAnimListener;
	}
	
	// Start the AttractAnimation
	public void start(){
		
		running = true;
		// The start() method on a Thread type object calls the run() method in the Thread and moves ito
		// to a background process. This means it appears to be running simultaneously with other processes.
		animationThread.start();
	}
	
	// Stop the animation
	public void stop(){
		running = false;
		// Sends an interrupt message to the Thread object (this is built into Thread). The Thread uses this
		// signal to kill itself.
		animationThread.interrupt();
		// Put all the handsets in a consistent state for whatever object will use it next. Usuaully a GameEngine.
		mHandsetController.resetAllHandsets();
	}
	
	protected final void sendLoopDone(){
		
		// This is a utility method for subclasses to signal to a listener that an animation loop is complete.
		
		// Need to check to make sure some object has been registered as a listener before callinf loopDone().
		// Unless you enjoy null pointer exceptions!
		
		if (mAnimListener!=null)
			mAnimListener.loopDone();
	}



}
