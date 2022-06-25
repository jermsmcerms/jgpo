package lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import api.SessionCallbacks;
import api.JgpoNet.JGPOErrorCodes;
import lib.network.UdpMessage;
import lib.utils.GeneralDataPackage;

public class Sync {
	public static final int MAX_PREDICTION_FRAMES = 8;
	// Configuration
	private SessionCallbacks sessionCallbacks;
	private int numPlayers;
	
	private boolean isRollingBack;
	private int lastConfirmedFrame;
	private int frameCount;
	
	private List<InputQueue> inputQueues;
	private RingBuffer<SyncEvent> syncEventQueue;
	
	private UdpMessage.ConnectStatus[] localConnectStatus;
	
	private SavedState savedState;
	
	public Sync(SessionCallbacks sessionCallbacks, int numPlayers) {
		this.sessionCallbacks = sessionCallbacks;
		this.numPlayers = numPlayers;
		
		localConnectStatus = 
			new UdpMessage.ConnectStatus[this.numPlayers];
		for(int i = 0; i < localConnectStatus.length; i++) {
			localConnectStatus[i] = new UdpMessage.ConnectStatus(false, -1);
		}
		
		frameCount = 0;
		lastConfirmedFrame = -1;
		isRollingBack = false;
		
		inputQueues = new ArrayList<InputQueue>(this.numPlayers);
        for(int i = 0; i < this.numPlayers; i++) {
        	inputQueues.add(new InputQueue());
        }
        
        savedState = new SavedState();
	}
	
	public boolean isInRollbackMode() {
		return isRollingBack;
	}

	public boolean addLocalInput(int queuePosition, GameInput input) {
		int framesBehind= frameCount - lastConfirmedFrame;
		if( frameCount>= MAX_PREDICTION_FRAMES &&
			framesBehind >= MAX_PREDICTION_FRAMES) {
			System.out.println("rejecting input from emulator: reached prediction barrier");
			return false;
        }

	    if(frameCount == 0) {
	        incrementFrame();
	    }
	
	    input.setFrame(frameCount);
	    inputQueues.get(queuePosition).addInput(new GameInput(input.getInput(), input.getFrame()));
	    return true;
	}

	public void incrementFrame() {
		saveCurrentFrame();
		frameCount++;
	}

	public int getFrameCount() {
		return frameCount;
	}

	public void saveCurrentFrame() {
		sessionCallbacks.saveGameState(savedState, frameCount);
	}
	
	public GeneralDataPackage synchronizeInputs() {
		int disconnectFlags = 0;
		int[] inputs = new int[numPlayers];
		for(int i = 0; i < numPlayers; i++) {
			GameInput input;
			if(localConnectStatus[i].disconnected && frameCount > localConnectStatus[i].lastFrame) {
				disconnectFlags |= (1 << i);
			} else {
				input = inputQueues.get(i).getInput(frameCount);
				inputs[i] = input.getInput();
			}
		}
		
		return new GeneralDataPackage(JGPOErrorCodes.JGPO_OK, disconnectFlags, inputs);
	}

	public void checkSimulation(long timeout) {
		int seekTo = checkSimulationConsitency(); 
		if(seekTo >= 0) {
			adjustSimulation(seekTo);
		}
	}

	private void adjustSimulation(int seekTo) {
		int count = frameCount - seekTo;
		isRollingBack = true;
		loadFrame(seekTo);
		resetPrediction();
		
		for(int i = 0; i < count; i++) {
			sessionCallbacks.advanceFrame(0);
		}
		isRollingBack = false;
	}

	private void loadFrame(int frame) {
		if(frameCount == frame) {
			System.out.println("skipping nop");
			return;
		}
		
		savedState.head = findSavedFrameIndex(frame);
		sessionCallbacks.loadGameState(savedState.frames[savedState.head]);
		frameCount = savedState.frames[savedState.head].frame;
		savedState.head = (savedState.head + 1) % savedState.frames.length;
	}

	private int findSavedFrameIndex(int frame) {
		int i, count = savedState.frames.length;
		   for (i = 0; i < count; i++) {
		      if (savedState.frames[i].frame == frame) {
		         break;
		      }
		   }
		   
		   if (i == count) {
		      System.out.println("error index cannot equal array length");
		      System.out.println("frame: " + frame);
		      for(i = 0; i < savedState.frames.length; i++) {
		    	  System.out.println("frame at " + i + " i'th: " + savedState.frames[i].frame);
		      }
		      System.exit(-1);
		   }
		   return i;
	}

	private void resetPrediction() {
		for(int i =  0; i < numPlayers; i++) {
			inputQueues.get(i).resetPrediction(frameCount);
		}
	}

	private int checkSimulationConsitency() {
		int firstIncorrect = GameInput.NULL_FRAME;
		for(int i = 0; i < numPlayers; i++) {
			int incorrect = inputQueues.get(i).getFirstIncorrectFrame();
			if(incorrect != GameInput.NULL_FRAME && firstIncorrect == GameInput.NULL_FRAME ||
				incorrect < firstIncorrect) {
				firstIncorrect = incorrect;
			}
		}
		
		if(firstIncorrect == GameInput.NULL_FRAME) {
			return -1;
		}
		return firstIncorrect;
	}

	public void setLastConfirmedFrame(int frame) {
		lastConfirmedFrame = frame;
        if(lastConfirmedFrame > 0) {
            for(int i = 0; i < inputQueues.size(); i++) {
            	inputQueues.get(i).discardConfirmedFrames(frame - 1);
            }
        }	
	}

	public void addRemoteInput(int queue, GameInput input) {
		inputQueues.get(queue).addInput(input);
	}
}
