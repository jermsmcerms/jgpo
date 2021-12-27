package api.apievents;

import api.JgpoNet.JGPOPlayerHandle;

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
	
	public JGPOEvent(JGPOEventCode code) {
		this.code = code;
		this.playerHandle = new JGPOPlayerHandle();
	}
}