package com.appdelegates.solnetwork;

import java.nio.ByteBuffer;

public class SOLMessage {
	
	public static final byte RETRY_5 = (byte)5;
	public static final byte RETRY_3 = (byte)3;
	public static final byte RETRY_0 = (byte)0;

	/*
	 * Speed of Light messages look like this:
	 * 
	 * [ UDP Header ] [ ack byte ] [ tag 8 bytes ] [ msg byte ] [ extra bytes ]
	 * 
	 */
	
	public static final byte RESET = 0;	
	
	public static final byte SHOW_BUTTON = 1;
	public static final byte HIDE_BUTTON = 2;
	public static final byte ARM_BUTTON = 3;
	
	public static final byte FRAME = 4;
	public static final byte CLICK = 5;
	public static final byte NEXT_BUTTON_IMAGE = 6;
	public static final byte FIRST_BUTTON_IMAGE = 7;
	
	public static final byte NEW_PLAYER = 8;
	public static final byte CLOCK_SYNC = 9;
	public static final byte START_GAME = 10;
	
	public static final byte PING =11;
	public static final byte PING_ACK = 12;
	public static final byte LOAD_GAME = 13;
	public static final byte GET_HIGH_SCORES = 14;
	public static final byte HIGH_SCORE_VALUE = 15;
	public static final byte PLAY_VIDEO = 16;
	public static final byte PLAY_WARN_SOUND = 17;
	public static final byte GAME_OVER = 18;
	public static final byte COUNTDOWN = 19;
	public static final byte ARM_NEGATIVE = 20;
	public static final byte CLICK_NEGATIVE = 21;
	public static final byte PENAL_MODE_ON = 22;
	public static final byte PENAL_MODE_OFF = 23;
	
	public static final byte START_BUTTON = 24;
	public static final byte PULSE = 25;
	public static final byte IDLE = 26;
	public static final byte CLEAR_HIGH_SCORES = 27;
	public static final byte END_OF_MSG = 28;
	
	// for SOL2
	public static final byte STATUS_UPDATE = 50;
	public static final byte SET_BUTTON_NUMBER = 55;
	
	// payload layout: [int: how many bars][int: max bars][byte: state]
	public static final byte SHOW_BARS = 60;
	
	
	public static byte[] buildPayload(byte message, byte[] extra){
		
		if (extra != null){
			
			int length = extra.length + 1;		
			byte[] payload = new byte[length];
			ByteBuffer bb = ByteBuffer.allocateDirect(length);
			bb.put(message);
			bb.put(extra);		
			//bb.putChar('\n');  // for using tcp + buffered reader
			bb.flip();
			bb.get(payload, 0, length);
			return payload;
			
		} else {
			
			byte[] payload = new byte[1];
			payload[0] =  message;
			return payload;
		}
						
	}
	
	public static String getPayloadString(byte [] message){
		
		byte [] payload = new byte[message.length-1];
		
		for (int i=0; i<payload.length; i++){
			payload[i] = message[i+1];
		}
		
		String pinfo = new String(payload);
		return pinfo;
		
	}
	
}
