package com.appdelegates.speedofmusic;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.appdelegates.solnetwork.SOLMessage;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;


public class AttractAnimationVUMeter extends AttractAnimation {

	public AttractAnimationVUMeter(HandsetController handsetController) {
		super(handsetController);
	}


	
	@Override
	public void setupAnimationThread() {

		
		
		animationThread = new Thread(new Runnable(){	
			
			
			
			
			
			@Override
			public void run() {
				
				int minBufSize = AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
				AudioRecord recorder = new AudioRecord(AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufSize*10);
				
				double [] sensitivity = new double[mHandsetController.mNumCols];
				sensitivity[0] = 20;
				sensitivity[1] = 10;
				sensitivity[2] = 6;
				
				
				int maxBars = 10;
				int sampleRate = 100;
				double fallTime = 600.0;
				
				recorder.startRecording();
				
				ArrayList<RowCol> handsets = new ArrayList<RowCol>(mHandsetController.handsets.size());
				ArrayList<BiQuadraticFilter> filters = new ArrayList<BiQuadraticFilter>();
				
				
				short[] audioData = new short[4410];
				final double[] runningTotals = new double[mHandsetController.mNumCols];
				final double MAX_READING = 32768;
				
				for (int col=0; col<mHandsetController.mNumCols; col++ ){
					
					for (int row=0; row<mHandsetController.mNumRows; row++ ){
						handsets.add(new RowCol(row, col));
					}
					
				}
				
				double [] decay = new double [mHandsetController.mNumCols];
				
				for (int i = 0; i < mHandsetController.mNumCols; i++) {
					//decay[i] =(mHandsetController.mNumRows * sampleRate)/(fallTime * sensitivity[i]);
					decay[i] =(mHandsetController.mNumRows * sampleRate)/(fallTime);
				}
				
				double[] currentPositions = new double [mHandsetController.mNumCols];
				
				filters.add(new BiQuadraticFilter(BiQuadraticFilter.Type.BANDPASS, 100, 44100.0, 20.0));
				filters.add(new BiQuadraticFilter(BiQuadraticFilter.Type.BANDPASS, 400, 44100.0, 50.0));
				filters.add(new BiQuadraticFilter(BiQuadraticFilter.Type.BANDPASS, 850, 44100.0, 70.0));
				
				while (running){
					
					int validSamples = recorder.read(audioData, 0, 4410);
					
					for(int i = 0; i < validSamples; i++){
						
						for (int fnum = 0; fnum < mHandsetController.mNumCols; fnum++){
							runningTotals[fnum] += Math.abs(filters.get(fnum).filter((double)(audioData[i])));
						}
						
					}
					
					final double [] scaleFactors = new double [mHandsetController.mNumCols];
					for (int i = 0; i < mHandsetController.mNumCols; i++){
						scaleFactors[i] =5/((validSamples*MAX_READING)/sensitivity[i]);
					}
					
					int handsetIndex = 0;
					
					for (int col=0; col < mHandsetController.mNumCols; col++ ){
						
						
						double phonesLit = runningTotals[col]*scaleFactors[col];
						
						if (currentPositions[col] - decay[col] > phonesLit) {
							phonesLit = currentPositions[col] - decay[col];
						}
						
						int wholePhonesLit = (int) Math.floor(phonesLit);
						int barsLit = (int) Math.round((10 * phonesLit) % 10);
						
						for (int row=0; row < mHandsetController.mNumRows; row++ ){
							
							if (row < wholePhonesLit) {
								
								ByteBuffer bb = ByteBuffer.allocateDirect(9);
								bb.putInt(maxBars);
								bb.putInt(maxBars);
								bb.put((byte)0);
								
								mHandsetController.sendMessageDirect(
										handsets.get(handsetIndex).row,
										handsets.get(handsetIndex).col,
										SOLMessage.buildPayload(SOLMessage.SHOW_BARS, bb.array()));
								
								//mHandsetController.setState(handsets.get(handsetIndex).row, handsets.get(handsetIndex).col, HandsetState.ON);
							}
							
							else if (row == wholePhonesLit) {
								
								ByteBuffer bb = ByteBuffer.allocateDirect(9);
								bb.putInt(barsLit);
								bb.putInt(maxBars);
								bb.put((byte)0);
								
								mHandsetController.sendMessageDirect(
										handsets.get(handsetIndex).row,
										handsets.get(handsetIndex).col,
										SOLMessage.buildPayload(SOLMessage.SHOW_BARS, bb.array()));
								
								//mHandsetController.setState(handsets.get(handsetIndex).row, handsets.get(handsetIndex).col, HandsetState.OFF);
							}
							
							else {
								
								ByteBuffer bb = ByteBuffer.allocateDirect(9);
								bb.putInt(0);
								bb.putInt(maxBars);
								bb.put((byte)0);
								
								mHandsetController.sendMessageDirect(
										handsets.get(handsetIndex).row,
										handsets.get(handsetIndex).col,
										SOLMessage.buildPayload(SOLMessage.SHOW_BARS, bb.array()));
							}
							
							handsetIndex++;
						}
						
						currentPositions[col] = phonesLit;
						
					}
					
					for (int i = 0; i < mHandsetController.mNumCols; i++) {
						runningTotals[i] = 0;
					}
					
					
					try {
						Thread.sleep(sampleRate);
					} catch (InterruptedException e) {
						running = false;
						break;
					}
					
					/*
					for (int i=0; i < handsets.size(); i++){
						mHandsetController.setState(handsets.get(i).row, handsets.get(i).col, HandsetState.OFF);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							running = false;
							break;
						}
					}
					*/
					
					sendLoopDone();
					
				}
				
			
			}
			
		});
		
	}

}
