package lib.network;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import api.NetworkStats;
import api.apievents.JGPOEvent;
import lib.GameInput;
import lib.PollSink;
import lib.TimeSync;
import lib.network.UdpMsg.ConnectStatus;
import lib.network.messages.*;
import lib.network.udppeerevents.PeerSynchronizingEvent;
import lib.network.udppeerevents.UdpPeerEvent;
import lib.utils.UdpPeerEventFactory;

public class UdpPeer implements PollSink {	
	private static final int NUM_SYNC_PACKETS = 5;
	private static final int MAX_SEQUENCE_DISTANCE = 32768;
	private static final int SYNC_FIRST_INTERVAL = 500;
	private static final int SYNC_RETRY_INTERVAL = 2000;
	// Network transmission information
	private Udp udp;
	private SocketAddress peerAddress;
	private int magicNumber;
	private int queueId;
	private int remoteMagicNumber;
	private boolean connected;
	private int sendLatency;
	private OoPacket ooPacket;
	private int oopPercent;
	private List<UdpQueueEntry> sendQueue;
	
	// Network stats
	private int roundTripTime;
	private int packetsSent;
	private int bytesSent;
	private int kbpsSent;
	private int statsStartTime;
	
	// Network state machine
	private UdpMsg.ConnectStatus[] localConnectStatus;
	private UdpMsg.ConnectStatus[] peerConnectStatus;
	private State currentState;
	private JGPOEvent event;
	private int roundTripsRemaining;
	private int random;
	private int lastQualityReportTime;
	private int lastNetworkStatsInterval;
	private int lastInputPacketReceivedTime;
	
	// Fairness
	private int localFrameAdvantage;
	private int remoteFrameAdvantage;
	
	// Packet loss
	private List<GameInput> pendingOutput;
	private GameInput lastReceivedInput;
	private GameInput lastSentInput;
	private GameInput lastAckedInput;
	private long lastSendTime;
	private long lastReceivedTime;
	private long shutdownTimeout;
	private boolean disconnectEventSent;
	private long disconnectTimeout;
	private long disconnectNotifyStart;
	private boolean disconnectNotifySent;
	private int nextSendSequence;
	private short nextReceiveSequence;
	
	// Rift synchronization
	TimeSync timeSync;
	
	// Event queue
	List<UdpPeerEvent> eventQueue;
	
	private interface MessageDispatcher {
		public boolean dispatch();
	}
	
	private class OoPacket {
		public int sendTime;
		public SocketAddress destination;
		public UdpMsg message;
	}

	private enum State {
		Syncing, Synchronized, Running, Disconnected
	}
	
	// TODO: combine ipAddress and port arguments into a SocketAddress object.
	public UdpPeer(Udp udp, int queue, String ipAddress, int port, 
		ConnectStatus[] localConnectStatus) {
		this.udp = udp;
		this.queueId = queue;
		// TODO: Change parameter list to use InetSocketAddress
		this.peerAddress = new InetSocketAddress(ipAddress, port);
		
		this.localConnectStatus = new UdpMsg.ConnectStatus[localConnectStatus.length];
		System.arraycopy(localConnectStatus, 0, 
			this.localConnectStatus, 0, localConnectStatus.length);
		peerConnectStatus = new UdpMsg.ConnectStatus[UdpMsg.UDP_MSG_MAX_PLAYERS];
		for(int i = 0; i < peerConnectStatus.length; i++) {
			peerConnectStatus[i] = new UdpMsg.ConnectStatus(false, -1);
		}
		
		ooPacket = new OoPacket();
		// TODO: send latency and oopPercent are set by some environment variable.
		// I still don't know how this works and running ggpo always gives them
		// a zero value...
		
		do {
			magicNumber = ThreadLocalRandom.current().nextInt();
		} while (magicNumber < 1);
		
		this.udp.getPoll().registerLoop(this);
		
		sendQueue = new ArrayList<>(64);
		eventQueue = new ArrayList<>(64);
	}

	public void synchronize() {
		if(udp != null) {
			currentState = State.Syncing;
			roundTripsRemaining = NUM_SYNC_PACKETS;
			sendSyncRequest();
		}
	}
	
	public boolean isRunning() { return currentState == State.Running; }
	
	public boolean isInitialized() { return udp != null; }		

	public void onMessage(UdpMsg message) {
		boolean messageHandled = false;
		
		// TODO : Re-factor so I don't have instantiate this everytime I need to process a message
		MessageDispatcher[] messageDispatchTable = new MessageDispatcher[] {
			new MessageDispatcher() { public boolean dispatch() { return onInvalid(message); }},
			new MessageDispatcher() { public boolean dispatch() { return onSyncRequest(message); }},
			new MessageDispatcher() { public boolean dispatch() { return onSyncReply(message); }}
		};
		
		int sequence = message.header.sequenceNumber;
		int skipped = sequence - nextReceiveSequence;
		// TODO: wrap this if in a couple more checks for message type
		// and magic number validity
		if(skipped > MAX_SEQUENCE_DISTANCE) {
			return;
		}
		
		nextSendSequence = sequence;
		byte messageType = message.header.messageType;
		if(messageType >= messageDispatchTable.length) {
			onInvalid(message);
		} else {
			messageHandled = messageDispatchTable[messageType].dispatch();
		}
		
		if(messageHandled) {
			lastReceivedTime = System.currentTimeMillis();
			if(disconnectNotifySent && currentState == State.Running) {
//				queueEvent(JGPOEventFactory.makeUdpEvent(JGPOEvent.JGPOEventCode.NetworkResumed));
				disconnectNotifySent = false;
			}
		}
	};
	
	public UdpPeerEvent getEvent() { 
		if(eventQueue.isEmpty()) {
			return null;
		}
		
		return eventQueue.remove(0); 
	}
	
	public void setLocalFrameNumber(int frameNumber) {}
	
	public int recommendFrameDelay() { return 0; }
	
	public void setDisconnectTimeout(int disconnectTimeout) {
		// TODO Auto-generated method stub
		
	}

	public void setDisconnectNotifyStart(int disconnectNotifyStart) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean handlesMessage(SocketAddress from, UdpMsg msg) {
		if(udp == null) {
			return false;
		}
		
		return peerAddress.equals(from);
	}

	private boolean getPeerConnectStatus(int id, int frame) { return true; }
	
	private void sendInput(GameInput gameInput) {}
	
	private void disconnect() {}
	
	private void getNetworkStats(NetworkStats stats) {}
	
	private void updateNetworkStats() {}
	
	private void queueEvent(UdpPeerEvent udpPeerEvent) {
		eventQueue.add(udpPeerEvent);
	}
	
	// TODO: add logging functions
	
	private void sendSyncRequest() {
		random = ThreadLocalRandom.current().nextInt() & 0xFFFF;
		UdpMsg syncRequest = new UdpMsg(UdpMessageBody.MessageType.SyncRequest);
		SyncRequest message = (SyncRequest)syncRequest.messageBody;
		message.randomRequest = random;
		syncRequest.messageBody = message;
		sendMessage(syncRequest);
	}
	
	private void sendMessage(UdpMsg message) {
		packetsSent++;
		lastSendTime = System.currentTimeMillis();
		message.header.magicNumber = magicNumber;
		message.header.sequenceNumber = nextSendSequence++;
		sendQueue.add(new UdpQueueEntry(System.currentTimeMillis(), peerAddress, message));
		pumpSendQueue();
	}
	
	private void pumpSendQueue() {
		while (sendQueue.iterator().hasNext()) {
			UdpQueueEntry queueEntry = sendQueue.iterator().next();
			udp.sendTo(queueEntry);
			sendQueue.remove(queueEntry);
		}
	}
	
	private void sendPendingOutput(UdpMsg message) {}
	
	private boolean onInvalid(UdpMsg message) { return false; }
	
	private boolean onSyncRequest(UdpMsg message) { 
		if(remoteMagicNumber != 0	&& message.header.magicNumber != remoteMagicNumber) {
			return false;
		}
		
		UdpMsg syncReply = new UdpMsg(UdpMessageBody.MessageType.SyncReply);
		SyncReply syncReplyBody = (SyncReply) syncReply.messageBody; 
		SyncRequest syncRequest = (SyncRequest) message.messageBody;
		
		syncReplyBody.randomReply = syncRequest.randomRequest;
		
		syncReply.messageBody = syncReplyBody;
		sendMessage(syncReply);
		return true; 
	}
	
	private boolean onSyncReply(UdpMsg message) {
		if(currentState != State.Syncing) {
			return message.header.magicNumber == remoteMagicNumber;
		}
		
		SyncReply syncReply = (SyncReply) message.messageBody;
		if(syncReply.randomReply != random) {
			return false;
		}
		
		if(!connected) {
			UdpPeerEvent connectedEvent = 
				(UdpPeerEvent) UdpPeerEventFactory.makeUdpPeerEvent(UdpPeerEvent.EventType.Connected);
			queueEvent(connectedEvent);
			connected = true;
		}
		
		if(--roundTripsRemaining == 0) {
			System.out.println("Synchronized!");
				UdpPeerEvent synchronizedEvent = 
					UdpPeerEventFactory.makeUdpPeerEvent(UdpPeerEvent.EventType.Synchronzied);
			queueEvent(synchronizedEvent);
			currentState = State.Running;
			lastReceivedInput = new GameInput(GameInput.NULL_INPUT, GameInput.NULL_FRAME);
			remoteMagicNumber = message.header.magicNumber;
		} else {
			// This is a sync event. GGPO doesnt really do any thing with theses...
			PeerSynchronizingEvent peerSynchronizingEvent = 
				(PeerSynchronizingEvent) UdpPeerEventFactory.makeUdpPeerEvent(UdpPeerEvent.EventType.Synchronizing);
			peerSynchronizingEvent.total = NUM_SYNC_PACKETS;
			peerSynchronizingEvent.count = NUM_SYNC_PACKETS - roundTripsRemaining;
			queueEvent(peerSynchronizingEvent);
			sendSyncRequest();
		}
		
		return true; 
	}
	
	private boolean onInput(UdpMsg message) { return true; }
	
	private boolean onInputAck(UdpMsg message) { return true; }
	
	private boolean onQualityReport(UdpMsg message) { return true; }
	
	private boolean onReply(UdpMsg message) { return true; }
	
	private boolean onKeepAlive(UdpMsg message) { return true; }
	
	@Override
	public boolean onLoopPoll(Object o) {
		long now = System.currentTimeMillis();
		long nextInterval;
		
		if(udp == null) {
			return true;
		}
		
		pumpSendQueue();
		
		// TODO: There will be more states to manage. Try and find a way to do it without a switch
		// statement...
		if(currentState == State.Syncing) {
			nextInterval = roundTripsRemaining == NUM_SYNC_PACKETS ? 
				SYNC_FIRST_INTERVAL : SYNC_RETRY_INTERVAL;
			if(lastSendTime > 0 && lastSendTime + nextInterval < now) {
				sendSyncRequest();
			}
		}
		return true;
	}
}
