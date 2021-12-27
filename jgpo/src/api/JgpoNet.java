package api;

public class JgpoNet {
	public static final int JGPO_MAX_PLAYERS = 4;
	public static final int JGPO_MAX_PREDICTION_FRAMES = 8;
	public static final int JGPO_MAX_SPECTATORS = 32;
	public static final int JGPO_SPECTATOR_INPUT_INTERVAL = 4;
	
	public enum JGPOPlayerType {
		JGPO_PLAYERTYPE_LOCAL,
		JGPO_PLAYERTYPE_REMOTE,
		JGPO_PLAYERTYPE_SPECTATOR
	}
	
	public enum JGPOErrorCodes {
		JGPO_OK(0),
		JGPO_SUCCESS(0),
		JGPO_GENERAL_FAILURE(-1), 
		JGPO_INVALID_SESSION(1),     
		JGPO_INVALID_PLAYER_HANDLE(2),
		JGPO_PLAYER_OUT_OF_RANGE(3), 
		JGPO_PREDICTION_THRESHOLD(4),
		JGPO_UNSUPPORTED(5),         
		JGPO_NOT_SYNCHRONIZED(6),    
		JGPO_IN_ROLLBACK(7),         
		JGPO_INPUT_DROPPED(8),       
		JGPO_PLAYER_DISCONNECTED(9), 
		JGPO_TOO_MANY_SPECTATORS(10), 
		JGPO_INVALID_REQUEST(11);   
		
		private int code;
		private JGPOErrorCodes(int code) {
			this.code = code;
		}
		public int getCode() {
			return code;
		}
		
		public static boolean operationSucceded(JGPOErrorCodes code) {
			return code == JGPOErrorCodes.JGPO_SUCCESS || code == JGPOErrorCodes.JGPO_OK;
		}
	}
	
	// TODO: re-factor this class. Either move it into it's own file. Or abstract it away somewhere
	// else.
	public static class JGPOPlayerHandle {
		public int playerHandle;
	}
}
