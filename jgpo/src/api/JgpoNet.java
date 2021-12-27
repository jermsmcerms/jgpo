package api;

import api.apievents.JGPOEvent;
import lib.backend.JGPOSession;
import lib.utils.GeneralDataPackage;

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
	
	public enum JGPOErrorCode {
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
		private JGPOErrorCode(int code) {
			this.code = code;
		}
		public int getCode() {
			return code;
		}
	}
	
	public static class JGPOPlayerHandle {
		public int playerHandle;
	}
	
	public static class JGPOPlayer {
		public JGPOPlayerType type;
		public int playerNum;
		public String ipAddress;
		public int port;
	}
	
	public static class JGPONetworkStats {
		public class Network {
			public int sendQueueLength;
			public int recvQueueLength;
			public int ping;
			public int  kbpsSent;
		}
		
		public class Timesync {
			public int localFramesBehind;
			public int remoteFramesBehind;
		}
	}
	
	public interface JGPOSessionCallbacks {
		public boolean beginGame(String name);
		public boolean saveGameState();
		public boolean loadGameState();
		public boolean logGameState();
		// TODO: may need a free buffer function
		public boolean advanceFrame(int flags);
		public boolean onEvent(JGPOEvent event);		
	}
	
	// TODO: consider changing to abstract class to avoid having so many functions have a session
	// argument.
	public interface JGPO_API {
		public GeneralDataPackage jgpo_start_session(JGPOSessionCallbacks cb, final String game, int num_players, int local_port);
		public GeneralDataPackage jgpo_add_player(JGPOSession session, JGPOPlayer player);
		public JGPOErrorCode jgpo_start_synctest(JGPOSessionCallbacks cb, String game, int num_players, int frames);
		// TODO: write spectator functions
		// TODO: may need close session function
		public JGPOErrorCode jgpo_set_frame_delay(JGPOPlayerHandle player, int frame_delay);
		public JGPOErrorCode jgpo_idle(JGPOSession session, long timeout);
		public JGPOErrorCode jgpo_add_local_input(JGPOSession session, JGPOPlayerHandle player, Object values);
		public GeneralDataPackage jgpo_synchronize_input(JGPOSession session);
		public JGPOErrorCode jgpo_disconnect_player(JGPOPlayerHandle player);
		public JGPOErrorCode jgpo_advance_frame(JGPOSession session);
		public JGPOErrorCode jgpo_get_network_stats(JGPOPlayerHandle player, JGPONetworkStats stats);
		public JGPOErrorCode jgpo_set_disconnect_timeout(JGPOSession session, int timeout);
		public JGPOErrorCode jgpo_set_disconnect_notify_start(JGPOSession session, int timeout);
		// TODO: may need log functions
	}
}
