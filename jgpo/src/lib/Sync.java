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
	        saveCurrentFrame();
	    }
	
	    inputQueues.get(queuePosition).addInput(new GameInput(input.getFrame(), input.getInput()));
	    return true;
	}

	private void saveCurrentFrame() {
		// TODO Auto-generated method stub
		
	}

	public int getFrameCount() {
		return frameCount;
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
}
