package api.apievents;

import api.JgpoNet.JGPOPlayerHandle;
import app.NonGameState;
import app.NonGameState.PlayerConnectState;

public class JGPOEvent {
	public enum JGPOEventCode {          
		JGPO_CONNECTED_TO_PEER      	(1000),
		JGPO_SYNCHRONIZING_WITH_PEER	(1001),
	    JGPO_SYNCHRONIZED_WITH_PEER 	(1002),
	    JGPO_RUNNING                	(1003),
	    JGPO_DISCONNECTED_FROM_PEER 	(1004),
	    JGPO_TIMESYNC               	(1005),
	    JGPO_CONNECTION_INTERRUPTED 	(1006),
	    JGPO_CONNECTION_RESUMED     	(1007);
		
	 	private int code;
		private JGPOEventCode(int code) {
			this.code = code;
		}
		
		public int getCode() {
			return code;
		}
	}

	public JGPOEventCode code;
	public JGPOPlayerHandle playerHandle;
		
	public JGPOEvent(JGPOEventCode code, int playerHandle) {
		this.code = code;
		this.playerHandle = new JGPOPlayerHandle();
		this.playerHandle.playerHandle = playerHandle;
	}

	public void processEvent(NonGameState nonGameState) {
		// If these events  ever get more complex they can be abstracted away
		// into their own class. Since they only handle which player needs
		// to be updated, this should be okay for now.
		NonGameState.PlayerConnectState currentState = null;
		if(code == JGPOEventCode.JGPO_CONNECTED_TO_PEER) {
			currentState = PlayerConnectState.Connecting;
			nonGameState.setConnectState(playerHandle, currentState);
		} else if(code == JGPOEventCode.JGPO_RUNNING ||
				code == JGPOEventCode.JGPO_CONNECTION_RESUMED) {
			currentState = PlayerConnectState.Running;
			nonGameState.setConnectState(currentState);
		} else if(code == JGPOEventCode.JGPO_SYNCHRONIZED_WITH_PEER) {
			nonGameState.updateConnectProgress(playerHandle, 100);
		}
	}
}