package com.example.solleaderboard;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class RepeatingAnimation implements AnimationListener{
	
	int mCount;
	Animation mAnimation;
	View mView;
	AnimationListener mAnimListener = null;
	Boolean loopForever;
	private Boolean setEnd;  // End of an animation "<set>"

	public RepeatingAnimation(View v, Animation anim, int count){
		
		mView = v;
		mAnimation = anim;
		mCount =  count;
		loopForever = (count < 1);
		anim.setAnimationListener(this);		
		
	}
	
	public void setAnimationListener(AnimationListener listener){
		mAnimListener = listener;
	}
	
	public void start(){
		setEnd = false;
		mView.startAnimation(mAnimation);
	}
	
	public void cancel(){
		mAnimation.cancel();
		mAnimation.reset();
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		//Log.i("AAA", "End of anim");
		setEnd = !setEnd;
		if ( (loopForever || (mCount>1)) && setEnd ) {
			mCount--;
			mAnimation.reset();
			mView.setVisibility(View.INVISIBLE);
			mView.startAnimation(mAnimation);
			if ( mAnimListener != null){
				mAnimListener.onAnimationRepeat(null);
			}
				
		} else if ( (!loopForever || (mCount==0)) && setEnd ){
			if ( mAnimListener != null){
				mAnimListener.onAnimationEnd(null);
			}		
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		mView.setVisibility(View.VISIBLE);
		if ( mAnimListener != null){
			mAnimListener.onAnimationStart(null);
		}		
	}
}
