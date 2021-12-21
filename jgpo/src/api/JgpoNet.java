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
	
	public static class JGPOPlayer {
		public JGPOPlayerType type;
		public int player_num;
		public String ip_address;
		public int port;
	}
}
