package com.appdelegates.solnetwork;

public class Gamer implements Comparable<Gamer> {
	
	// git test
	String mGamerTag;
	String mEmail;
	long mGameTime;
	int mScore;
	public int dbIdx;
	
	public Gamer(String email, String gamerTag){
		mGamerTag = gamerTag;
		mEmail = email;
		mScore = 0;
	}
	
	public Gamer(String email, String gamerTag, int score){
		mGamerTag = gamerTag;
		mEmail = email;
		setScore(score);
	}
	
	public Gamer(String email, String gamerTag, int score, long time){
		mGamerTag = gamerTag;
		mEmail = email;
		setScore(score);
		mGameTime = time;
	}


	
	public void setScore(int score){
		mScore = score;
		mGameTime = System.currentTimeMillis();
	}

	public int getScore(){
		return mScore;
	}
	
	public String getGamertag(){
		return mGamerTag;
	}
	
	public String getEmail(){
		return mEmail;
	}
	
	public long getTime(){
		return mGameTime;
	}

	
	@Override
	public int compareTo(Gamer another) {
		
		if ( another.mScore != this.mScore )
			return  ( another.mScore - this.mScore);
		else
			return this.getGamertag().compareTo(another.getGamertag());
	}

	
}
