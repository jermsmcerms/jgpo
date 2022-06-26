package lib.backend;

import java.io.IOException;
import java.net.SocketAddress;

import api.JgpoNet.JGPOErrorCodes;
import api.NetworkStats;
import api.Player;
import api.JgpoNet.JGPOPlayerHandle;
import api.JgpoNet.JGPOPlayerType;
import api.SessionCallbacks;
import api.apievents.JGPOEvent;
import api.apievents.JGPOEvent.JGPOEventCode;
import api.apievents.ApiSynchronizingEvent;
import api.apievents.ConnectionInterruptedEvent;
import lib.GameInput;
import lib.Poll;
import lib.Sync;
import lib.network.Udp;
import lib.network.UdpCallbacks;
import lib.network.UdpMessage;
import lib.network.UdpPeer;
import lib.network.udppeerevents.InputEvent;
import lib.network.udppeerevents.NetworkInterruptedEvent;
import lib.network.udppeerevents.PeerSynchronizingEvent;
import lib.network.udppeerevents.UdpPeerEvent;
import lib.utils.GeneralDataPackage;
import lib.utils.JGPOEventFactory;

public class P2P extends UdpCallbacks implements JGPOSession {
	public static final int RECOMMENDATION_INTERVAL = 240;
	public static final int DEFAULT_DISCONNECT_TIEMOUT = 5000;
	public static final int DEFAULT_DISCONNECT_NOTIFY_START = 750;
	
	private SessionCallbacks apiCallbacks;
	private Poll poll;
	private Sync sync;
	private Udp udp;
	private UdpPeer[] endpoints;
	private UdpPeer[] spectators;
	private int numSpectators;
	
	private boolean synchronizing;
	private int numPlayers;
	private int nextRecommendedSleep;
	
	private int nextSpectatorFrame;
	private int disconnectTimeout;
	private int disconnectNotifyStart;
	
	private UdpMessage.ConnectStatus[] localConnectStatus;
	
	public P2P(SessionCallbacks cb, String game, int numPlayers, int localPort) {
		this.apiCallbacks = cb;
		this.numPlayers = numPlayers;
		try {
			udp = new Udp(localPort, this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(udp != null) {
			poll = udp.getPoll();
		}
		
		endpoints = new UdpPeer[this.numPlayers];
		
		localConnectStatus = new UdpMessage.ConnectStatus[UdpMessage.UDP_MSG_MAX_PLAYERS];
		for(int i = 0; i < localConnectStatus.length; i++) {
			localConnectStatus[i] = new UdpMessage.ConnectStatus(false, -1);
		}

		sync = new Sync(this.apiCallbacks, this.numPlayers);
		
		this.apiCallbacks.beginGame(game); // apart of the api but?...
	}
	
	@Override
	public GeneralDataPackage addPlayer(Player player) {
		if(player.type == JGPOPlayerType.JGPO_PLAYERTYPE_SPECTATOR) {
			// TODO: add spectators
		}
		
		int queue = player.playerNum - 1;
		if(player.playerNum < 1 || player.playerNum > numPlayers) {
			return new GeneralDataPackage(JGPOErrorCodes.JGPO_PLAYER_OUT_OF_RANGE);
		}
		
		JGPOPlayerHandle handle = queueToPlayerHandle(queue);
		if(player.type == JGPOPlayerType.JGPO_PLAYERTYPE_REMOTE) {
			addRemotePlayer(player.ipAddress, player.port, queue);
		}
		
		return new GeneralDataPackage(handle, JGPOErrorCodes.JGPO_OK);
	}

	@Override
	public JGPOErrorCodes addLocalInput(JGPOPlayerHandle player, Object values) {
		int queue;
		JGPOErrorCodes result;
		
		if(sync.isInRollbackMode()) {
			return JGPOErrorCodes.JGPO_IN_ROLLBACK;
		}
		
		if(synchronizing) {
			return JGPOErrorCodes.JGPO_NOT_SYNCHRONIZED;
		}
		
		queue = playerHandleToQueue(player);
		if(queue < 0 || queue >= numPlayers) {
			return JGPOErrorCodes.JGPO_INVALID_PLAYER_HANDLE;
		}
		
		GameInput input = new GameInput((int)values, GameInput.NULL_FRAME);
		
		if(!sync.addLocalInput(queue, input)) {
			return JGPOErrorCodes.JGPO_PREDICTION_THRESHOLD;
		}
		
		input.setFrame(sync.getFrameCount());
		
		if(input.getFrame() != GameInput.NULL_FRAME) {
			localConnectStatus[queue].lastFrame = input.getFrame();
			for(int i = 0; i < numPlayers; i++) {
				UdpPeer peer = endpoints[i];
				if(peer != null) {
					if(peer.isInitialized()) {
						peer.sendInput(input);
					}
				}
			}
		}
		return JGPOErrorCodes.JGPO_OK;
	}
	
	@Override
	public JGPOErrorCodes doPoll(long timeout) {
		if(!sync.isInRollbackMode()) {
			poll.pump(0);
			processEvents(endpoints);
//			Uncomment when spectators are ready
//			processEvents(spectators);
			if(!synchronizing) {
				sync.checkSimulation(timeout);
				int current_frame = sync.getFrameCount();
				for(UdpPeer peer : endpoints) {
					if(peer != null) {
						peer.setLocalFrameNumber(current_frame);
					}
				}
				
				int total_min_confirmed = poll2Players(current_frame);
                if (total_min_confirmed >= 0) {
                    assert (total_min_confirmed != Integer.MAX_VALUE);
                    sync.setLastConfirmedFrame(total_min_confirmed);
                }
				
				if(current_frame > nextRecommendedSleep) {
					int interval = 0;
					for(UdpPeer peer : endpoints) {
						// need to add quality reports so we can determine how long we
						// need to sleep.
						if(peer != null) {
							int recommendFrameDelay = peer.recommendFrameDelay();
							interval = Math.max(0, recommendFrameDelay);
						}
					}
	                
	                if(interval > 0) {
	                	try {
	                		//System.out.println("sleep for " + interval + " frames.");
	                        Thread.sleep(1000L * interval / 60);
	                    } catch (InterruptedException e) {
	                        e.printStackTrace();
	                    }
	                	nextRecommendedSleep = current_frame + RECOMMENDATION_INTERVAL;
	                }
				}
			}
			
		}
		return JGPOErrorCodes.JGPO_OK;
	}

	@Override
	public JGPOErrorCodes incrementFrame() {
		sync.incrementFrame();
		doPoll(0);
		// poll sync events
		return JGPOErrorCodes.JGPO_OK;
	}

	@Override
	public JGPOErrorCodes disconnectPlayer(JGPOPlayerHandle handle) {
		// TODO Auto-generated method stub
		return JGPOSession.super.disconnectPlayer(handle);
	}

	@Override
	public JGPOErrorCodes getNetworkStats(NetworkStats stats,
			JGPOPlayerHandle handle) {
		// TODO Auto-generated method stub
		return JGPOSession.super.getNetworkStats(stats, handle);
	}

	@Override
	public JGPOErrorCodes setFrameDelay(JGPOPlayerHandle player, int delay) {
		// TODO Auto-generated method stub
		return JGPOSession.super.setFrameDelay(player, delay);
	}

	@Override
	public JGPOErrorCodes setDisconnectTimeout(int timeout) {
		// TODO Auto-generated method stub
		return JGPOSession.super.setDisconnectTimeout(timeout);
	}

	@Override
	public JGPOErrorCodes setDisconnectNotifyStart(int timeout) {
		// TODO Auto-generated method stub
		return JGPOSession.super.setDisconnectNotifyStart(timeout);
	}

	@Override
	public GeneralDataPackage syncInput() {
		if(synchronizing) {
			return new GeneralDataPackage(JGPOErrorCodes.JGPO_NOT_SYNCHRONIZED);
		}
		
		return sync.synchronizeInputs();
	}

	@Override
	public void onMsg(SocketAddress from, UdpMessage msg) {
		processPeerMessage(endpoints, from, msg);
//		processPeerMessage(spectators, from, msg);
	}
	
	private void processPeerMessage(UdpPeer[] peers, SocketAddress from, UdpMessage msg) {
		for(int i = 0; i < numPlayers; i++) {
			UdpPeer player = peers[i];
			if(player != null && player.handlesMessage(from, msg)) {
				player.onMessage(msg);
				return;
			}
		}
	}

	private int playerHandleToQueue(JGPOPlayerHandle player) {
		int offset = player.playerHandle - 1;
		if(offset < 0 || offset >= numPlayers) {
			return -1;
		}
		return offset;
	}
	
	private JGPOPlayerHandle queueToPlayerHandle(int queue) {
		JGPOPlayerHandle handle = new JGPOPlayerHandle();
		handle.playerHandle = queue + 1;
		return handle;
	}

	private JGPOPlayerHandle queueToSpectatorHandle(int queue) {
		JGPOPlayerHandle handle = new JGPOPlayerHandle();
		handle.playerHandle = queue + 1000;
		return handle;
	}
	
	private void disconnectPlayerQueue(int queue, int syncTo) {}
	
	private void pollSyncEvents() {}
	
	private void checkInitialSync() {
		UdpPeer peer;
		if(synchronizing) {
			for(int i = 0; i < numPlayers; i++) {
				if(endpoints[i] != null) {
					peer = endpoints[i];
					if(peer.isInitialized() && 
						!peer.isRunning() && !localConnectStatus[i].disconnected) {
						return;
					}
				}	
			}
		}
		synchronizing = false;
	}
	
	private int poll2Players(int currentFrame) {
		int total_min_confirmed = Integer.MAX_VALUE;
        for(int i = 0; i < numPlayers; i++) {
            // TODO: Get peer connect status and disconnect if needed.
            if(!localConnectStatus[i].disconnected) {
                total_min_confirmed =
                    Math.min(localConnectStatus[i].lastFrame, total_min_confirmed);
            }
        }
        return total_min_confirmed;
	}
	
	private int pollNPlayers(int currentFrame) {
		return 0;
	}
	
	private void addRemotePlayer(String ipAddress, int port, int queue) {
		synchronizing = true;
		endpoints[queue] = new UdpPeer(udp, queue, ipAddress, port, localConnectStatus);
		endpoints[queue].setDisconnectTimeout(disconnectTimeout);
		endpoints[queue].setDisconnectNotifyStart(disconnectNotifyStart);
		endpoints[queue].synchronize();
	}
	
	private void onSyncEvent(/*TODO: add sync event argument*/) {}

	private void processEvents(UdpPeer[] peers) {
		UdpPeerEvent event;
		for(int i = 0; i < numPlayers; i++) {
			if(peers[i] != null) {
				event = peers[i].getEvent();
				while(event != null) {
					if(event.eventType == UdpPeerEvent.EventType.Synchronzied) {
						System.out.println("sync'd...");
						checkInitialSync();
						apiCallbacks.onEvent(new JGPOEvent(JGPOEvent.JGPOEventCode.JGPO_RUNNING, 
							queueToPlayerHandle(i).playerHandle));
					} else if(event.eventType == UdpPeerEvent.EventType.Input) {
						try {
							addRemoteInput(event, i);
						} catch (Exception e) {
							System.out.println(e.getMessage());
						}
					} else if (event.eventType == UdpPeerEvent.EventType.Disconnected) {
						disconnectPlayer(queueToPlayerHandle(i));
					} else {
						sendEventToApi(event, i);						
					}
					event = peers[i].getEvent();
				}
			}
		}
	}

	private void addRemoteInput(UdpPeerEvent event, int queue) throws Exception {
		InputEvent inputEvent = (InputEvent) event;
		if(!localConnectStatus[queue].disconnected) {
			int currentRemoteFrame = localConnectStatus[queue].lastFrame;
			int newRemoteFrame = inputEvent.input.getFrame();
			assert(currentRemoteFrame == GameInput.NULL_FRAME || 
				newRemoteFrame == currentRemoteFrame + 1);
			sync.addRemoteInput(queue, inputEvent.input);
			localConnectStatus[queue].lastFrame = inputEvent.input.getFrame();
		}
	}

	private void sendEventToApi(UdpPeerEvent event, int queue) {
		JGPOPlayerHandle handle = queueToPlayerHandle(queue);
		JGPOEvent jgpoEvent = JGPOEventFactory.makeApiEvent(event, handle);
		apiCallbacks.onEvent(jgpoEvent);		
	}

	private void onUdpProtocolSpectatorEvent(UdpPeerEvent event, int queue) {}
}
