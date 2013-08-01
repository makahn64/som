package com.appdelegates.speedofmusic;

public class MusicEvent {
	
	public enum EventType {
		START_LOOPING,
		STOP_LOOPING,
		SINGLE_PLAY;
	}
	
	private long time;
	private int rawId;
	private EventType eventType;
	
	public MusicEvent (long millis, int rawEffectId, EventType type) {
		time = millis;
		rawId = rawEffectId;
		eventType = type;
	}
	
	public long time() {
		return time;
	}
	
	public int rawId() {
		return rawId;
	}
	
	public EventType type() {
		return eventType;
	}
	
}
