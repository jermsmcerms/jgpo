package app;

import java.awt.EventQueue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import api.JgpoNet.JGPOErrorCodes;
import api.JgpoNet.JGPOPlayerHandle;
import api.JgpoNet.JGPOPlayerType;
import api.apievents.JGPOEvent;
import api.Player;
import api.SessionCallbacks;
import lib.SavedFrame;
import lib.SavedState;
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
	private VectorWarSessionCallbacks sessionCallbacks;
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
			// Should be some kind of error code...
			if(	nonGameState.localPlayerHandle.playerHandle != -1) {
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
						int disconnectFlags = (int)data.getData()[1];

						advanceFrame((int[])data.getData()[2], disconnectFlags);
					}
				}
			} else {
				System.out.println("invalid player handle...");
				System.exit(-1);
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

	public void advanceFrame(int[] inputs, int disconnectFlags) {
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
	
	private class VectorWarSessionCallbacks implements SessionCallbacks {
		private JGPOPlayerHandle currentPlayerHandle;
		private NonGameState.PlayerConnectState currentState;
		private int connectionProgress;
		private long nowInMilliSeconds;
		private long disconnectTimeout;
		
		public VectorWarSessionCallbacks() {
			currentPlayerHandle = new JGPOPlayerHandle();
			currentPlayerHandle.playerHandle = -1;
			currentState = NonGameState.PlayerConnectState.Connecting;
		}
		
		public JGPOPlayerHandle getCurrentPlayerHandle() {
			return currentPlayerHandle;
		}
		
		public NonGameState.PlayerConnectState getCurrentState() {
			return currentState;
		}
		
		public long getNowInMilliSeconds() {
			return nowInMilliSeconds;
		}
		
		public long getDisconnectTimeout() {
			return disconnectTimeout;
		}
		
		@Override
		public boolean beginGame(String name) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean saveGameState(SavedState savedState, int frameCount) {
			savedState.frames[savedState.head].frame = frameCount;
			savedState.frames[savedState.head].data = new byte[0];
			
			try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
	            ObjectOutputStream out;
	            out = new ObjectOutputStream(bos);
	            out.writeObject(VectorWar.this.gameState);
	            out.flush();
	            savedState.frames[savedState.head].data = bos.toByteArray();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

			savedState.head = (savedState.head + 1) % savedState.frames.length;
			return true;
		}
		
		@Override
		public boolean loadGameState(SavedFrame loadFrame) {
			ByteArrayInputStream in = new ByteArrayInputStream(loadFrame.data);
			
	        try {
	            ObjectInputStream is = new ObjectInputStream(in);
	            GameState loadedState = (GameState)is.readObject();
	            
	            VectorWar.this.gameState = new GameState(loadedState.getShips(), loadedState.frameNumber);
	        } catch (IOException | ClassNotFoundException e) {
	            e.printStackTrace();
	        }
			return true;
		}
		
		@Override
		public boolean logGameState() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean advanceFrame(int flags) {
			GeneralDataPackage data = api.jgpoSynchronizeInputs();
			int disconnectFlags = (int)data.getData()[1];
			VectorWar.this.advanceFrame((int[])data.getData()[2], disconnectFlags);
			return true;
		}
		
		@Override
		public boolean onEvent(JGPOEvent event) {
			event.processEvent(nonGameState);
			return false;
		}
	}
}

