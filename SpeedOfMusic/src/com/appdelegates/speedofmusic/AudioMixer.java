package com.appdelegates.speedofmusic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;

public class AudioMixer {
	
	private Context mContext;
	
	//Event lists
	private ArrayList<MusicEvent> mixQueue;
	private ArrayList<MusicEvent> creationFailed;
	
	//Readers being read for mixing
	private ArrayList<MediaReader> activeReaders;
	
	//Time and sample rate values
	private static final double MILLIS_PER_SEC = 1000.0;
	private int mSecsLong;
	private long mMillisLong;
	private int mSampleRateHz;
	private int mByteRate;
	private int mSamplesLong;
	private double mSamplesPerMilli;
	
	//WAV constants and other info for mixing
	private static final int RIFF = 1380533830;
	private int mChunkSizeFileInt;
	private static final int WAVE = 1463899717;
	private static final int FMT = 1718449184;
	private static final int SUBCHUNK1_SIZE = 268435456;
	private static final int FORMAT_AND_CHANNELS = 16777472;
	private int mSampleRateFileInt;
	private int mByteRateFileInt;
	private static final short BLOCK_ALIGN = 512;
	private static final short BITS_PER_SAMPLE = 4096; 
	private static final int DATA = 1684108385;
	private int mTotalUniqueTracks;
	private static final int INFO_SUBCHUNK_SIZE = 32;
	private static final int BYTES_PER_SAMPLE = 2;
	
	private File sdCard;
	private File finalMixDown;
	
	
	public AudioMixer (Context context, int lengthInSecs, int sampleRate, ArrayList<MusicEvent> eventList) {
		
		mContext = context;
		
		mixQueue = new ArrayList<MusicEvent>();
		mixQueue.addAll(eventList);
		creationFailed = new ArrayList<MusicEvent>();
		
		activeReaders = new ArrayList<MediaReader>();
		
		mSecsLong = lengthInSecs;
		mMillisLong = mSecsLong * 1000;
		
		mSampleRateHz = sampleRate;
		
		mSamplesLong = mSecsLong * mSampleRateHz;
		mSamplesPerMilli = mSampleRateHz/MILLIS_PER_SEC;
		
		mTotalUniqueTracks = getTotalUniqueTracks();
		
		sdCard = Environment.getExternalStorageDirectory();
		
		setupHeaderData();
		
	}
	
	private void setupHeaderData() {
		
		ByteBuffer chunkSize = ByteBuffer.allocate(4);
		chunkSize.putInt(mSamplesLong + INFO_SUBCHUNK_SIZE);
		chunkSize.order(ByteOrder.LITTLE_ENDIAN);
		chunkSize.flip();
		mChunkSizeFileInt = chunkSize.getInt();
		
		ByteBuffer sampleRate = ByteBuffer.allocate(4);
		sampleRate.putInt(mSampleRateHz);
		sampleRate.order(ByteOrder.LITTLE_ENDIAN);
		sampleRate.flip();
		mSampleRateFileInt = sampleRate.getInt();
		
		mByteRate = BYTES_PER_SAMPLE * mSampleRateHz;
		
		ByteBuffer byteRate = ByteBuffer.allocate(4);
		byteRate.putInt(mByteRate);
		byteRate.order(ByteOrder.LITTLE_ENDIAN);
		byteRate.flip();
		mByteRateFileInt = byteRate.getInt();
		
	}
	
	public void mix() throws IOException {
		
		//finalMixDown = new File(sdCard, "DaMix");
		
		//FileOutputStream fileData = new FileOutputStream(finalMixDown);
		
		//writeWAVHead(fileData);
		byte[] header = writeWAVHeadTest();
		
		for (int sampleNum = 0; sampleNum < mSamplesLong; sampleNum++) {
			
			byte sample = 0;
			
			updateReaders(sampleNum);
			
			for (MediaReader mr : activeReaders) {
				
				sample += mr.readNext()/mTotalUniqueTracks;
				
			}
			
			//fileData.write(sample);
		}
		
	}//End of mix() method

	private void writeWAVHead(FileOutputStream fileData) throws IOException {
		
		ByteBuffer headerBB = ByteBuffer.allocate(40);
		headerBB.putInt(RIFF);
		headerBB.putInt(mChunkSizeFileInt);
		headerBB.putInt(WAVE);
		headerBB.putInt(FMT);
		headerBB.putInt(SUBCHUNK1_SIZE);
		headerBB.putInt(FORMAT_AND_CHANNELS);
		headerBB.putInt(mSampleRateFileInt);
		headerBB.putInt(mByteRateFileInt);
		headerBB.putShort(BLOCK_ALIGN);
		headerBB.putShort(BITS_PER_SAMPLE); 
		headerBB.putInt(DATA);
		
		byte[] header = headerBB.array();
		
		fileData.write(header, 0, header.length);
		
	}
	
	private byte[] writeWAVHeadTest() throws IOException {
		
		ByteBuffer headerBB = ByteBuffer.allocate(40);
		headerBB.putInt(RIFF);
		headerBB.putInt(mChunkSizeFileInt);
		headerBB.putInt(WAVE);
		headerBB.putInt(FMT);
		headerBB.putInt(SUBCHUNK1_SIZE);
		headerBB.putInt(FORMAT_AND_CHANNELS);
		headerBB.putInt(mSampleRateFileInt);
		headerBB.putInt(mByteRateFileInt);
		headerBB.putShort(BLOCK_ALIGN);
		headerBB.putShort(BITS_PER_SAMPLE); 
		headerBB.putInt(DATA);
		
		byte[] header = headerBB.array();
		
		return header;
		
	}

	/*
	private ArrayList<MediaReader> removeFinishedReaders(ArrayList<MediaReader> currentMRs) {
		
		ArrayList<MediaReader> updatedMRs = new ArrayList<MediaReader>();
		
		for (MediaReader mr: currentMRs) {
			
			if (! mr.isStopped()) {
				updatedMRs.add(mr);
			}
			
		}
		return updatedMRs;
		
	}
	*/

	private void updateReaders(int sampleNumber) {
		
		for (int i = 0; i < mixQueue.size(); i++) {
			
			MusicEvent event = mixQueue.get(i);
			
			if (event.time() * mSamplesPerMilli == sampleNumber) {
				
				if (event.type() == MusicEvent.EventType.STOP_LOOPING) {
					
					killLoopingReader(event.rawId());
					
				}
				
				else {
					
					try {
						MediaReader mr = new MediaReader(mContext, event.rawId(), event.type() == MusicEvent.EventType.START_LOOPING);
						mr.prepare();
						activeReaders.add(mr);
						
					} catch (IOException e) {
						creationFailed.add(event);
						e.printStackTrace();
					}
					
				}
				
				mixQueue.remove(event);
				i--;
				
			}//End of time-check if-branch
			
		}//End of new event iterator
		
		if (creationFailed.size() > 0) {
			
			//Copy ArrayList used to avoid concurrent modification exception in iterator.
			ArrayList<MusicEvent> creationFailedCopy = new ArrayList<MusicEvent>();
			creationFailedCopy.addAll(creationFailed);
			
			for (MusicEvent event: creationFailedCopy) {
						
				try {
					MediaReader mr = new MediaReader(mContext, event.rawId(), event.type() == MusicEvent.EventType.START_LOOPING);
					mr.prepare();
					activeReaders.add(mr);
					creationFailed.remove(event);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}//End of failed event iterator
			
		}
		
	}//End of updateReaders() method

	private void killLoopingReader(int rawId) {
		
		for (int i = 0; i < activeReaders.size(); i++) {
			
			MediaReader mr = activeReaders.get(i);
			
			if (mr.rawId() == rawId || mr.isStopped()) {
				mr.stop();
				activeReaders.remove(i);
				i--;
			}
		}
		
	}

	/*
	private void stopReader(int rawId) {
		
		for (MediaReader mr : activeReaders) {
			if (mr.rawId() == rawId) {
				mr.stop();
			}
		}
		
	}
	*/
	
	private int getTotalUniqueTracks() {
		
		ArrayList<Integer> trackIds = new ArrayList<Integer>();
		
		for (MusicEvent event : mixQueue) {
			
			boolean unique = true;
			
			for (Integer id : trackIds) {
				
				if (id == event.rawId()) {
					unique = false;
				}
				
			}
			
			if (unique == true) {
				trackIds.add(event.rawId());
			}
			
		}
		
		return trackIds.size();
		
	}
	
}
