package app;

import java.awt.EventQueue;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import api.JgpoNet;
import api.JgpoNet.JGPOErrorCode;
import api.JgpoNet.JGPOPlayer;
import api.JgpoNet.JGPOPlayerHandle;
import api.JgpoNet.JGPOPlayerType;
import api.JgpoNet.JGPOSessionCallbacks;
import lib.backend.JGPOSession;
import lib.utils.GeneralDataPackage;
import lib.utils.PerformanceMonitor;

public class VectorWar {
	private static final boolean SYNC_TEST = false;
	private static final int MAX_PLAYERS = 64;
	private GameState gs;
	private NonGameState ngs;
	private Frame frame;
	private JGPOSession session;
	private JGPOSessionCallbacks callbacks;
	private VectorWar_API api;
	private PerformanceMonitor perf_mon;
	
	public enum VectorWarInputs {
		INPUT_THRUST		(1<<0),
		INPUT_BREAK			(1<<1),
		INPUT_ROTATE_LEFT	(1<<2),
		INPUT_ROTATE_RIGHT	(1<<3),
		INPUT_FIRE			(1<<4);
		
		private int input;
		private VectorWarInputs(int input) {
			this.input = input;
		}
		public int getInput() {
			return input;
		}
	}
	
	public VectorWar(	int num_players, int local_port, JGPOPlayer players[], 
						int num_spectators) {		
		EventQueue.invokeLater(new Runnable() {
			@Override 
			public void run() {
				try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
				frame = new Frame(gs);
				perf_mon = new PerformanceMonitor();
				frame.setStatusText("Connecting to peers...");
			}
		});
		
		gs = new GameState(num_players);
		ngs = new NonGameState(num_players);
		api = new VectorWar_API();
		callbacks = new VectorWarSessionCallbacks();
		
		if(SYNC_TEST) {
			
		} else {
			GeneralDataPackage data = api.jgpo_start_session(callbacks, "vectorwar", num_players, local_port);
			try {
				session = (JGPOSession)data.getData()[0];
			} catch(ClassCastException ex) {
				
			}
		}
		
		api.jgpo_set_disconnect_timeout(session, 3000);
		api.jgpo_set_disconnect_notify_start(session, 1000);
		
		for(int i = 0; i < num_players + num_spectators; i++) {
			// jgpo add player
			GeneralDataPackage data = api.jgpo_add_player(session, players[i]);
			try {
				JGPOPlayerHandle handle = (JGPOPlayerHandle)data.getData()[0];
				ngs.players[i].handle = handle;
				ngs.players[i].type = players[i].type;
				if(players[i].type == JGPOPlayerType.JGPO_PLAYERTYPE_LOCAL) {
					ngs.players[i].connect_progress = 100;
					ngs.local_player_handle = handle;
					ngs.setConnectState(handle, NonGameState.PlayerConnectState.Connecting);
					api.jgpo_set_frame_delay(handle, Constants.FRAME_DLAY);
				}
			} catch(ClassCastException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void runFrame() {
		if(frame != null) {
			JGPOErrorCode result = JGPOErrorCode.JGPO_OK;
			int[] inputs = new int[Constants.MAX_SHIPS];
			if(	ngs.local_player_handle.playerHandle != 
				JGPOErrorCode.JGPO_INVALID_PLAYER_HANDLE.getCode()) {
				int local_input = 0;
				if(SYNC_TEST) {
					local_input = ThreadLocalRandom.current().nextInt();
				} else {
					local_input = frame.getInput();
				}
				
				result = api.jgpo_add_local_input(session, ngs.local_player_handle, local_input);
				if(result == JGPOErrorCode.JGPO_SUCCESS || result == JGPOErrorCode.JGPO_OK) {
					GeneralDataPackage data = api.jgpo_synchronize_input(session);
					if((JGPOErrorCode)data.getData()[0] == JGPOErrorCode.JGPO_SUCCESS ||
						(JGPOErrorCode)data.getData()[0] == JGPOErrorCode.JGPO_OK) {
						advanceFrame((int[])data.getData()[1], (int)data.getData()[2]);
					}
				}
			}
			frame.update(gs, ngs);
		}
	}

	private void advanceFrame(int[] inputs, int disconnect_flags) {
		System.out.println("advancing frame");
		gs.update(inputs, disconnect_flags);
		
		ngs.now.frame_number = gs.frame_number;
		ngs.now.checksum = 0; // TODO: use checksum function using the game state
		if(gs.frame_number % 90 == 0) {
			ngs.periodic = ngs.now;
		}
		
		api.jgpo_advance_frame(session);
		
		JGPOPlayerHandle[] handles = new JGPOPlayerHandle[MAX_PLAYERS];
		for(int i = 0; i < ngs.getNumPlayers(); i++) {
			if(ngs.players[i].type == JGPOPlayerType.JGPO_PLAYERTYPE_REMOTE) {
				handles[i] = ngs.players[i].handle;
			}
		}
		perf_mon.update(session, handles);
	}

	public void idle(long timeout) {
		api.jgpo_idle(session, timeout);
	}
}
