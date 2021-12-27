package app;

import java.awt.EventQueue;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import api.JgpoNet.JGPOErrorCodes;
import api.JgpoNet.JGPOPlayerHandle;
import api.JgpoNet.JGPOPlayerType;
import api.Player;
import api.SessionCallbacks;
import lib.backend.JGPOSession;
import lib.utils.GeneralDataPackage;
import lib.utils.PerformanceMonitor;

public class VectorWar {
	private static final boolean SYNC_TEST = false;
	private static final int MAX_PLAYERS = 64;
	private GameState gameState;
	private NonGameState nonGameState;
	private Frame applicationFrame;
	private JGPOSession session;
	private SessionCallbacks sessionCallbacks;
	private VectorWar_API api;
	private PerformanceMonitor performanceMonitor;
	private int numPlayers;
	private int numSpectators;
	
	public enum VectorWarInputs {
		THRUST			(1<<0),
		BREAK			(1<<1),
		ROTATE_LEFT		(1<<2),
		ROTATE_RIGHT	(1<<3),
		FIRE			(1<<4);
		
		private int input;
		private VectorWarInputs(int input) {
			this.input = input;
		}
		public int getInput() {
			return input;
		}
	}
	
	public VectorWar(int numPlayers, int localPort, Player players[], int numSpectators) {
		this.numPlayers = numPlayers;
		this.numSpectators = numSpectators;
		createFrame();
		
		gameState = new GameState(numPlayers);
		nonGameState = new NonGameState(numPlayers);
		sessionCallbacks = new VectorWarSessionCallbacks();
		
		// if running a sync test, create a new sync test back end. Otherwise create a P2P back end.
		if(SYNC_TEST) {
			api = new VectorWar_API(sessionCallbacks, "vectorwar sync test", numPlayers);
		} else {
			api = new VectorWar_API(sessionCallbacks, "vectorwar", numPlayers, localPort);
		}
		
		api.jgpoSetDisconnectTimeout(3000);
		api.jgpoSetDisconnectNotifyStart(1000);
		
		initializePlayers(players);
	}

	public void idle(long timeout) {
		api.jgpoIdle(timeout);
	}
	
	public void executeSingleFrame() {
		if(applicationFrame != null) {
			JGPOErrorCodes result = JGPOErrorCodes.JGPO_OK;
			if(	nonGameState.localPlayerHandle.playerHandle != 
				JGPOErrorCodes.JGPO_INVALID_PLAYER_HANDLE.getCode()) {
				int local_input = 0;
				
				if(SYNC_TEST) {
					local_input = ThreadLocalRandom.current().nextInt();
				} else {
					local_input = applicationFrame.getInput();
				}
				
				result = api.jgpoAddLocalInput(nonGameState.localPlayerHandle, local_input);
				if(JGPOErrorCodes.operationSucceded(result)) {
					GeneralDataPackage data = api.jgpoSynchronizeInputs();
					
					result = (JGPOErrorCodes)data.getData()[0];
					if(JGPOErrorCodes.operationSucceded(result)) {
						int[] inputs = new int[Constants.MAX_SHIPS];
						System.arraycopy((int[])data.getData()[1], 0, inputs, 0, inputs.length);

						int disconnectFlags = (int)data.getData()[2];

						advanceFrame(inputs, disconnectFlags);
					}
				}
			}
			
			applicationFrame.update(gameState, nonGameState);
		}
	}
	
	private void initializePlayers(Player[] players) {
		for(int i = 0; i < numPlayers + numSpectators; i++) {
			GeneralDataPackage data = api.jgpoAddPlayer(players[i]);
			
			try {
				JGPOPlayerHandle handle = (JGPOPlayerHandle)data.getData()[0];
				nonGameState.players[i].handle = handle;
				nonGameState.players[i].type = players[i].type;
				
				if(players[i].type == JGPOPlayerType.JGPO_PLAYERTYPE_LOCAL) {
					nonGameState.players[i].connectProgress = 100;
					nonGameState.localPlayerHandle = handle;
					nonGameState.setConnectState(handle, NonGameState.PlayerConnectState.Connecting);
					api.jgpoSetFrameDelay(handle, Constants.FRAME_DLAY);
				}
			} catch(ClassCastException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void createFrame() {
		EventQueue.invokeLater(new Runnable() {
			@Override 
			public void run() {
				try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
				applicationFrame = new Frame(gameState);
				performanceMonitor = new PerformanceMonitor();
				applicationFrame.setStatusText("Connecting to peers...");
			}
		});
	}

	private void advanceFrame(int[] inputs, int disconnectFlags) {
		gameState.update(inputs, disconnectFlags);
		
		nonGameState.now.frameNumber = gameState.frameNumber;
		nonGameState.now.checksum = 0; // TODO: use checksum function using the game state
		if(gameState.frameNumber % 90 == 0) {
			nonGameState.periodic = nonGameState.now;
		}
		
		api.jgpoAdvanceFrame();
		
		JGPOPlayerHandle[] handles = new JGPOPlayerHandle[MAX_PLAYERS];
		for(int i = 0; i < nonGameState.getNumPlayers(); i++) {
			if(nonGameState.players[i].type == JGPOPlayerType.JGPO_PLAYERTYPE_REMOTE) {
				handles[i] = nonGameState.players[i].handle;
			}
		}
		performanceMonitor.update(session, handles);
	}
}
