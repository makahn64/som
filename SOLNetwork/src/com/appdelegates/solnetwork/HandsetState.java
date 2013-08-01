package com.appdelegates.solnetwork;

public enum HandsetState {
	OFF(0), ON(1), ARMED(2), ARMED_NEGATIVE(3), ATTRACT(4), COUNTDOWN(5), START_BUTTON(6), GAME_OVER(7), BARS(8) ;
    private final int value;

    private HandsetState(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
    
    public Boolean isOneShot() {
    	return ( (this==ATTRACT) || (this==COUNTDOWN) || (this==GAME_OVER));
    }
}

