package com.appdelegates.speedofmusic;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.Context;

public class MediaReader {
	
	//Constants
	@SuppressWarnings("unused")
	private static final String CNAME = "MediaReader";
	private static final int RIFF = 1380533830;
	private static final int WAVE = 1463899717;
	private static final int DATA = 1684108385;
	
	//File info
	private int mRawId;
	private int mDataStartIndex = 36;
	private int mChannels;
	private int mSampleRate;
	private int mChunkSize;
	private Context mContext;
	
	//the file
	private DataInputStream mFileData;
	
	//Read variables
	private boolean mIsLooping;
	private boolean mIsStopped;
	private int currentIndex;
	
	public MediaReader (Context context, int resourceId, boolean looping) throws IOException {
		
		mContext = context;
		mRawId = resourceId;
		mIsLooping = looping;
		mIsStopped = true;
		
		InputStream rawInput = mContext.getResources().openRawResource(resourceId);
		mFileData = new DataInputStream(new BufferedInputStream(rawInput));
		
		setup();
		
		
	}
	
	public MediaReader (Context context, String filePath, boolean looping) throws IOException {
		
		mContext = context;
		mRawId = -1;
		FileInputStream rawInput;
		mIsLooping = looping;
		mIsStopped = true;
		
		
		rawInput = new FileInputStream(filePath);
		mFileData = new DataInputStream(new BufferedInputStream(rawInput));
		
		setup();
		
	}

	protected void setup() throws IOException{
		
		fileCheck();
		setChannels();
		setSampleRate();
		setDataStart();
		mFileData.mark(mChunkSize);
		
	}
	
	protected void fileCheck() throws IOException{
		try {
			
			int riffInt = mFileData.readInt();
			Boolean isRiff = (riffInt == RIFF);
			
			mChunkSize = mFileData.readInt();
			
			int waveInt = mFileData.readInt();
			Boolean isWave = (waveInt == WAVE);
			
			if ( !(isWave && isRiff) ) {
				
				IOException notWave = new IOException("WAV signature mismatch!");
				throw notWave;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	protected void setChannels() throws IOException{
		
		mFileData.skipBytes(10);
		
		byte[] rawChannelBuffer = new byte[2];
		
		mFileData.read(rawChannelBuffer, 0, 2);
		ByteBuffer channelBuffer = ByteBuffer.wrap(rawChannelBuffer);
		channelBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		mChannels = (int) channelBuffer.getShort(0);
		
	}
	
	protected void setSampleRate() throws IOException{
		
		byte[] rawRateBuffer = new byte[4];
		
		mFileData.read(rawRateBuffer, 0, 4);
		ByteBuffer rateBuffer = ByteBuffer.wrap(rawRateBuffer);
		rateBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		mSampleRate = rateBuffer.getInt();
		
	}
	
	protected void setDataStart() throws IOException {
		
		int subChunkTitle;
		boolean dataNotFound = true;
		mFileData.skipBytes(8);
		
		
		
		while(dataNotFound){
			
			subChunkTitle = mFileData.readInt();
			
			if (subChunkTitle == DATA){
				
				mDataStartIndex += 4;					//Takes into account reading the 4 byte header
				dataNotFound = false;
				
			}
			else {
				
				mDataStartIndex += 4;					//Takes into account reading the 4 byte header
				int subChunkSize;
				
				byte[] rawSubChunkSize = new byte[4];
				
				mFileData.read(rawSubChunkSize, 0, 4);
				ByteBuffer subChunkBuffer = ByteBuffer.wrap(rawSubChunkSize);
				subChunkBuffer.order(ByteOrder.LITTLE_ENDIAN);
				
				subChunkSize = subChunkBuffer.getInt();
				mDataStartIndex += 4;					//Takes into account reading the 4 byte sub chunk size
				
				mFileData.skipBytes(subChunkSize);
				mDataStartIndex += subChunkSize;		//Takes into account the skipped bytes
				
			}
			
		}	//End of while loop
		
	}	//End of setDataStart method
	
	public void prepare() throws IOException {
		 
		mFileData = null;
		
		InputStream rawInput = mContext.getResources().openRawResource(mRawId);
		mFileData = new DataInputStream(new BufferedInputStream(rawInput));
		
		mFileData.skipBytes(mDataStartIndex);
		
		currentIndex = mDataStartIndex;
		mIsStopped = false;
		
	}
	
	public byte readNext() {
		
		if (mIsStopped == true) {
			return 0;
		}
		
		else if (mIsLooping && currentIndex == mChunkSize - 8) {
			try {
				prepare();
			} catch (IOException e) {
				e.printStackTrace();
				return 0;
			}
		}
		
		else if (currentIndex == mChunkSize - 8) {
			stop();
			return 0;
			
		}
		
		try {
			
			byte nextByte = mFileData.readByte();
			currentIndex += 4;
			
			if (mChannels > 1) {
				
				long dataBytes = (long) nextByte;
				
				for (int i = 1; i < mChannels; i++){
					dataBytes += (long) mFileData.readByte();
					currentIndex += 4;
				}
				nextByte =(byte) (dataBytes/mChannels);
			}
			
			return nextByte;
				
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		
	}//End of readNext method
	
	public void stop() {
		mIsStopped = true;
	}
	
	public boolean isStopped() {
		return mIsStopped;
	}
	
	public int getSampleRate() {
		return mSampleRate;
	}
	
	public int rawId() {
		return mRawId;
	}
	
}	//End of MediaReader class
