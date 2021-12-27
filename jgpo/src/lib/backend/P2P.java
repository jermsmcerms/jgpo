package lib.backend;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Arrays;

import api.JgpoNet.JGPOErrorCode;
import api.JgpoNet.JGPONetworkStats;
import api.JgpoNet.JGPOPlayer;
import api.JgpoNet.JGPOPlayerHandle;
import api.JgpoNet.JGPOPlayerType;
import api.JgpoNet.JGPOSessionCallbacks;
import api.apievents.JGPOEvent;
import api.apievents.JGPOEvent.JGPOEventCode;
import api.apievents.ApiSynchronizingEvent;
import api.apievents.ConnectionInterruptedEvent;
import lib.Poll;
import lib.Sync;
import lib.network.Udp;
import lib.network.UdpCallbacks;
import lib.network.UdpMsg;
import lib.network.UdpPeer;
import lib.network.udppeerevents.NetworkInterruptedEvent;
import lib.network.udppeerevents.PeerSynchronizingEvent;
import lib.network.udppeerevents.UdpPeerEvent;
import lib.utils.GeneralDataPackage;
import lib.utils.JGPOEventFactory;

public class P2P extends UdpCallbacks implements JGPOSession {
	public static final int RECOMMENDATION_INTERVAL = 240;
	public static final int DEFAULT_DISCONNECT_TIEMOUT = 5000;
	public static final int DEFAULT_DISCONNECT_NOTIFY_START = 750;
	
	private JGPOSessionCallbacks callbacks;
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
	
	private UdpMsg.ConnectStatus[] localConnectStatus;
	
	public P2P(JGPOSessionCallbacks cb, String game, int localPort, int numPlayers) {
		this.callbacks = cb;
		try {
			udp = new Udp(localPort, this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(udp != null) {
			poll = udp.getPoll();
		}
		
		this.numPlayers = numPlayers;
		endpoints = new UdpPeer[this.numPlayers];
		
		localConnectStatus = new UdpMsg.ConnectStatus[UdpMsg.UDP_MSG_MAX_PLAYERS];
		for(int i = 0; i < localConnectStatus.length; i++) {
			localConnectStatus[i] = new UdpMsg.ConnectStatus(false, -1);
		}
		
		this.callbacks.beginGame(game); // apart of the api but?...
	}
	
	@Override
	public GeneralDataPackage addPlayer(JGPOPlayer player) {
		if(player.type == JGPOPlayerType.JGPO_PLAYERTYPE_SPECTATOR) {
			// TODO: add spectators
		}
		
		int queue = player.playerNum - 1;
		if(player.playerNum < 1 || player.playerNum > numPlayers) {
			return new GeneralDataPackage(JGPOErrorCode.JGPO_PLAYER_OUT_OF_RANGE);
		}
		
		JGPOPlayerHandle handle = queueToPlayerHandle(queue);
		if(player.type == JGPOPlayerType.JGPO_PLAYERTYPE_REMOTE) {
			addRemotePlayer(player.ipAddress, player.port, queue);
		}
		
		return new GeneralDataPackage(handle, JGPOErrorCode.JGPO_OK);
	}

	@Override
	public JGPOErrorCode addLocalInput(JGPOPlayerHandle player, Object values) {
		int queue;
		JGPOErrorCode result;
		if(synchronizing) {
			return JGPOErrorCode.JGPO_NOT_SYNCHRONIZED;
		}
		System.out.println("Send input " + (Arrays.toString((int[])values)));
		return JGPOErrorCode.JGPO_OK;
	}
	
	@Override
	public JGPOErrorCode doPoll(long timeout) {
		poll.pump(0);
		pollUdpProtocolEvents();
		return JGPOErrorCode.JGPO_OK;
	}

	@Override
	public JGPOErrorCode incrementFrame() {
		// TODO Auto-generated method stub
		return JGPOSession.super.incrementFrame();
	}

	@Override
	public JGPOErrorCode disconnectPlayer(JGPOPlayerHandle handle) {
		// TODO Auto-generated method stub
		return JGPOSession.super.disconnectPlayer(handle);
	}

	@Override
	public JGPOErrorCode getNetworkStats(JGPONetworkStats stats,
			JGPOPlayerHandle handle) {
		// TODO Auto-generated method stub
		return JGPOSession.super.getNetworkStats(stats, handle);
	}

	@Override
	public JGPOErrorCode setFrameDelay(JGPOPlayerHandle player, int delay) {
		// TODO Auto-generated method stub
		return JGPOSession.super.setFrameDelay(player, delay);
	}

	@Override
	public JGPOErrorCode setDisconnectTimeout(int timeout) {
		// TODO Auto-generated method stub
		return JGPOSession.super.setDisconnectTimeout(timeout);
	}

	@Override
	public JGPOErrorCode setDisconnectNotifyStart(int timeout) {
		// TODO Auto-generated method stub
		return JGPOSession.super.setDisconnectNotifyStart(timeout);
	}

	@Override
	public JGPOErrorCode syncInput(Object values, int disconnectFlags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onMsg(SocketAddress from, UdpMsg msg) {
		for(int i = 0; i < numPlayers; i++) {
			UdpPeer player = endpoints[i];
			if(player != null && player.handlesMessage(from, msg)) {
				player.onMessage(msg);
				return;
			}
		}
		// TODO: loop through the spectators and to the same thing.
		// Consider creating private function that takes a udp protocol
		// array as an argument.
	}
	
	private JGPOErrorCode playerHandleToQueue(JGPOPlayerHandle player, int queue) {
		return JGPOErrorCode.JGPO_OK;
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
		
		JGPOEvent runningEvent =
			JGPOEventFactory.makeApiEvent(JGPOEventCode.JGPO_RUNNING);
		callbacks.onEvent(runningEvent);
		synchronizing = false;
	}
	
	private int poll2Players(int currentFrame) {
		return 0;
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
	

	private void pollUdpProtocolEvents() {
		UdpPeerEvent event;
		for(int i = 0; i < numPlayers; i++) {
			if(endpoints[i] != null) {
				event = endpoints[i].getEvent();
				while(event != null) {
					onUdpProtocolPeerEvent(event, i);
					event = endpoints[i].getEvent();
				}
			}
		}
		
		// TODO: Do the same for spectators. Extract method for both.
	}
	
	private void onUdpProtocolPeerEvent(UdpPeerEvent event, int queue) {
		onUdpProtocolEvent(event, queueToPlayerHandle(queue));
		// TODO: handle input and disconnect events here UdpEvent event, 
	}

	// TODO: can this be re-factored into some kind of observer
	// pattern? UdpEvent event,
	private void onUdpProtocolEvent(UdpPeerEvent event, JGPOPlayerHandle handle) {
		// Events that get their own event
		// connection interrupted
		// synchronizing
		// timesync *not apart of this event dispatcher*
		switch (event.eventType) {
			case Connected :
				JGPOEvent connectedEvent = 
					JGPOEventFactory.makeApiEvent(JGPOEvent.JGPOEventCode.JGPO_CONNECTED_TO_PEER);
				connectedEvent.playerHandle.playerHandle = handle.playerHandle;
				callbacks.onEvent(connectedEvent);
				break;
			case NetworkResumed :
				JGPOEvent networkResumedEvent =
					JGPOEventFactory.makeApiEvent(JGPOEvent.JGPOEventCode.JGPO_CONNECTION_RESUMED);
				networkResumedEvent.playerHandle.playerHandle = handle.playerHandle;
				callbacks.onEvent(networkResumedEvent);
				break;
			case Disconnected :
				break;
			case Input :
				break;
			case Synchronzied :
				JGPOEvent synchronizedEvent = 
					JGPOEventFactory.makeApiEvent(JGPOEvent.JGPOEventCode.JGPO_SYNCHRONIZED_WITH_PEER);
				synchronizedEvent.playerHandle.playerHandle = handle.playerHandle;
				callbacks.onEvent(synchronizedEvent);
				checkInitialSync();
				break;
			case Unknown :
				break;
			case Synchronizing :
				ApiSynchronizingEvent apiSynchronizingEvent = 
					(ApiSynchronizingEvent) JGPOEventFactory.makeApiEvent(JGPOEvent.JGPOEventCode.JGPO_SYNCHRONIZING_WITH_PEER);
				apiSynchronizingEvent.playerHandle.playerHandle = handle.playerHandle;
				PeerSynchronizingEvent peerSyncEvent = (PeerSynchronizingEvent)event;
				apiSynchronizingEvent.count = peerSyncEvent.count;
				apiSynchronizingEvent.total = peerSyncEvent.total;
				callbacks.onEvent(apiSynchronizingEvent);
				break;
			case NetworkInterrupted :
				ConnectionInterruptedEvent connectionInterruptedEvent = 
					(ConnectionInterruptedEvent) JGPOEventFactory.makeApiEvent(JGPOEvent.JGPOEventCode.JGPO_SYNCHRONIZING_WITH_PEER);
				NetworkInterruptedEvent networkInterrupted = (NetworkInterruptedEvent)event;
				connectionInterruptedEvent.playerHandle.playerHandle = handle.playerHandle;
				connectionInterruptedEvent.disconnectTimeout = networkInterrupted.disconnectTimeout;
				callbacks.onEvent(connectionInterruptedEvent);
				break;
			default :
				break;
		}
	}
	
	private void onUdpProtocolSpectatorEvent(UdpPeerEvent event, int queue) {}
}
