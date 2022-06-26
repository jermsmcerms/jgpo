package lib.network;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import api.NetworkStats;
import api.apievents.JGPOEvent;
import lib.GameInput;
import lib.PollSink;
import lib.TimeSync;
import lib.network.UdpMessage.ConnectStatus;
import lib.network.messages.*;
import lib.network.udppeerevents.InputEvent;
import lib.network.udppeerevents.PeerSynchronizingEvent;
import lib.network.udppeerevents.UdpPeerEvent;
import lib.utils.UdpPeerEventFactory;

public class UdpPeer implements PollSink {	
	private static final int NUM_SYNC_PACKETS = 5;
	private static final int MAX_SEQUENCE_DISTANCE = 32768;
	private static final int SYNC_FIRST_INTERVAL = 500;
	private static final int SYNC_RETRY_INTERVAL = 2000;
	private static final int QUALITY_REPORT_INTERVAL = 1000;
	private static final long RUNNING_RETRY_INTERVAL = 200;
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
	private long roundTripTime;
	private int packetsSent;
	private int bytesSent;
	private int kbpsSent;
	private int statsStartTime;
	
	// Network state machine
	private UdpMessage.ConnectStatus[] localConnectStatus;
	private UdpMessage.ConnectStatus[] peerConnectStatus;
	private State currentState;
	private ConnectionState connectionState;
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
		public UdpMessage message;
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
		
		this.localConnectStatus = new UdpMessage.ConnectStatus[localConnectStatus.length];
		System.arraycopy(localConnectStatus, 0, 
			this.localConnectStatus, 0, localConnectStatus.length);
		peerConnectStatus = new UdpMessage.ConnectStatus[UdpMessage.UDP_MSG_MAX_PLAYERS];
		for(int i = 0; i < peerConnectStatus.length; i++) {
			peerConnectStatus[i] = new UdpMessage.ConnectStatus(false, -1);
		}
		
		ooPacket = new OoPacket();
		// TODO: send latency and oopPercent are set by some environment variable.
		// I still don't know how this works and running ggpo always gives them
		// a zero value...
		
		do {
			magicNumber = ThreadLocalRandom.current().nextInt();
		} while (magicNumber < 1);
		
		this.udp.getPoll().registerLoop(this);
		
		timeSync = new TimeSync();
		
		sendQueue = new ArrayList<>(64);
		eventQueue = new ArrayList<>(64);
		pendingOutput = new LinkedList<GameInput>();
		connectionState = new ConnectionState();
		
		lastAckedInput = new GameInput(-1, -1);
		lastReceivedInput = new GameInput(-1, -1);
		lastSentInput = new GameInput(-1, -1);
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

	public void onMessage(UdpMessage message) {
		boolean messageHandled = false;
		
		// TODO : Re-factor so I don't have instantiate this every time I need to process a message?
		MessageDispatcher[] messageDispatchTable = new MessageDispatcher[] {
			new MessageDispatcher() { public boolean dispatch() { return onInvalid(message); }},
			new MessageDispatcher() { public boolean dispatch() { return onSyncRequest(message); }},
			new MessageDispatcher() { public boolean dispatch() { return onSyncReply(message); }},
			new MessageDispatcher() { public boolean dispatch() { return onInput(message); }},
			new MessageDispatcher() { public boolean dispatch() { return onQualityReport(message); }},
			new MessageDispatcher() { public boolean dispatch() { return onQualityReply(message); }}
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
	
	public int recommendFrameDelay() { return timeSync.recommendFrameWaitDuration(false); }
	
	public void setDisconnectTimeout(int disconnectTimeout) {
		// TODO Auto-generated method stub
		
	}

	public void setDisconnectNotifyStart(int disconnectNotifyStart) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean handlesMessage(SocketAddress from, UdpMessage msg) {
		if(udp == null) {
			return false;
		}
		
		return peerAddress.equals(from);
	}

	public void sendInput(GameInput gameInput) {
		if(udp != null) {
			if(currentState == State.Running) {
				timeSync.advanceFrame(gameInput, localFrameAdvantage, remoteFrameAdvantage);
				pendingOutput.add(gameInput);
			}
			sendPendingOutput();
		}
	}

	private boolean getPeerConnectStatus(int id, int frame) { return true; }
		
	private void disconnect() {}
	
	private void getNetworkStats(NetworkStats stats) {}
	
	private void updateNetworkStats() {}
	
	private void queueEvent(UdpPeerEvent udpPeerEvent) {
		eventQueue.add(udpPeerEvent);
	}
	
	// TODO: add logging functions
	
	private void sendSyncRequest() {
		random = ThreadLocalRandom.current().nextInt() & 0xFFFF;
		UdpMessage syncRequest = new UdpMessage(UdpMessageBody.MessageType.SyncRequest);
		SyncRequest message = (SyncRequest)syncRequest.messageBody;
		message.randomRequest = random;
		syncRequest.messageBody = message;
		sendMessage(syncRequest);
	}
	
	private void sendMessage(UdpMessage message) {
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
	
	private void sendPendingOutput() {
		UdpMessage msg = new UdpMessage(UdpMessageBody.MessageType.Input);
		Input inputMsg = new Input((byte)UdpMessage.UDP_MSG_MAX_PLAYERS);
        // TODO: something for inputs?

        if(pendingOutput.size() > 0) {
            inputMsg.startFrame = pendingOutput.get(0).getFrame();
            for(int i = 0; i < pendingOutput.size(); i++) {
                GameInput current = pendingOutput.get(i);
                inputMsg.AddInput(current);
                lastSentInput = current;
            }
        } else {
            inputMsg.startFrame = 0;
        }

        inputMsg.acknowlegedFrame = lastReceivedInput.getFrame();
        inputMsg.numBits = pendingOutput.size();
        inputMsg.disconnectRequested = currentState == State.Disconnected ? (byte)1 : 0;
        // Copy local connect status into msg.payload.input.connect_status
        if(localConnectStatus != null) {
            // copy this local connect status into the peer connect status for
        	// the input message
        	for(int i = 0; i < localConnectStatus.length; i++) {
        		inputMsg.peerConnectStatus[i] = 
    				new UdpMessage.ConnectStatus(localConnectStatus[i].disconnected,
						localConnectStatus[i].lastFrame);
        	}
        } else {
        	localConnectStatus = new UdpMessage.ConnectStatus[UdpMessage.UDP_MSG_MAX_PLAYERS];
        	for(int i = 0; i < localConnectStatus.length; i++) {
        		localConnectStatus[i] = new UdpMessage.ConnectStatus(false, 0);
        	}
        }
        
        msg.messageBody = inputMsg;
        sendMessage(msg);
	}
	
	private boolean onInvalid(UdpMessage message) { return false; }
	
	private boolean onSyncRequest(UdpMessage message) { 
		if(remoteMagicNumber != 0	&& message.header.magicNumber != remoteMagicNumber) {
			return false;
		}
		
		UdpMessage syncReply = new UdpMessage(UdpMessageBody.MessageType.SyncReply);
		SyncReply syncReplyBody = (SyncReply) syncReply.messageBody; 
		SyncRequest syncRequest = (SyncRequest) message.messageBody;
		
		syncReplyBody.randomReply = syncRequest.randomRequest;
		
		syncReply.messageBody = syncReplyBody;
		sendMessage(syncReply);
		return true; 
	}
	
	private boolean onSyncReply(UdpMessage message) {
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
	
	private boolean onInput(UdpMessage message) { 
 		Input input = (Input)message.messageBody;
		boolean disconnect_requested = input.disconnectRequested == (byte) 1;
        if(disconnect_requested) {
            if(currentState != State.Disconnected && !disconnectEventSent) {
                System.out.println("Disconnecting endpoint on remote request");
                eventQueue.add(new UdpPeerEvent(UdpPeerEvent.EventType.Disconnected));
                disconnectEventSent = true;
            }
        } else {
        	UdpMessage.ConnectStatus[] remote_status = input.peerConnectStatus;
            for (int i = 0; i < peerConnectStatus.length; i++) {
               peerConnectStatus[i].disconnected = peerConnectStatus[i].disconnected || remote_status[i].disconnected;
               peerConnectStatus[i].lastFrame = Math.max(peerConnectStatus[i].lastFrame, remote_status[i].lastFrame);
            }
        }

        if(input.numBits > 0) {
            int current_frame = input.startFrame;
            int[] inputs = new int[input.numBits];
            
            // This will have to change if we start storing single input's into multiple
            // bytes.
            for(int i = 0; i < inputs.length; i++) {
            	inputs[i] = (int)input.inputs[i];
            }

            if(lastReceivedInput.getFrame() < 0) {
            	lastReceivedInput.setFrame(input.startFrame - 1);
            }
            
            for(int inputValue : inputs) {
                boolean useInputs = current_frame == lastReceivedInput.getFrame() + 1;
                lastReceivedInput.setInput(inputValue);

                if(useInputs) {
                    lastReceivedInput.setFrame(current_frame);
                    InputEvent event = new InputEvent(UdpPeerEvent.EventType.Input);
                    event.input = new GameInput(lastReceivedInput.getInput(), lastReceivedInput.getFrame());
                    eventQueue.add(event);
                    //System.out.println("adding input: " + event.input.getInput() + " to frame: " + event.input.getFrame());
                    connectionState.running.last_input_packet_recv_time = System.currentTimeMillis();
                }

                current_frame++;
            }
            
            while(pendingOutput.size() > 0) {
                lastAckedInput = new GameInput(pendingOutput.get(0).getFrame(), pendingOutput.get(0).getInput());
                pendingOutput.remove(0);
            }
        }
		return true;
	}
	
	private boolean onInputAck(UdpMessage message) { return true; }
	
	private boolean onQualityReport(UdpMessage message) { 
		QualityReport qualityReport = (QualityReport)message.messageBody;
		QualityReply qualityReply = new QualityReply();
		qualityReply.pong = qualityReport.ping;
		UdpMessage qualityReplyMessage = new UdpMessage(UdpMessageBody.MessageType.QualityReply);
		qualityReplyMessage.messageBody = qualityReply;
		sendMessage(qualityReplyMessage);
		remoteFrameAdvantage = qualityReport.frameAdvantage;
		return true; 
	}
	
	private boolean onQualityReply(UdpMessage message) { 
		QualityReply qualityReply = (QualityReply)message.messageBody;
		roundTripTime = qualityReply.pong;
		return true; 
	}
	
	private boolean onKeepAlive(UdpMessage message) { return true; }
	
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
		
		if(currentState == State.Running) {
			if(connectionState.running.last_input_packet_recv_time == 0 ||
				connectionState.running.last_input_packet_recv_time + RUNNING_RETRY_INTERVAL < now) {
				System.out.println("haven't exchanged packets in a while. Resending. " + 
				"(last received: " + lastReceivedInput.getFrame() + 
				", last sent: " + lastSentInput.getFrame() + ")");
				sendPendingOutput();
				connectionState.running.last_input_packet_recv_time = now;
			}
			
			if(connectionState.running.last_quality_report_time == 0 ||
				connectionState.running.last_quality_report_time + QUALITY_REPORT_INTERVAL < now) {
				QualityReport qualityReport = new QualityReport();
				qualityReport.ping = System.currentTimeMillis();
				qualityReport.frameAdvantage = (byte)localFrameAdvantage;
				UdpMessage qualityReportMessage = new UdpMessage(UdpMessageBody.MessageType.QualityReport);
				qualityReportMessage.messageBody = qualityReport;
				sendMessage(qualityReportMessage);
				connectionState.running.last_quality_report_time = now;
			}
			
			// TODO: update network stats for logging and handle disconnects
		}
		
		return true;
	}
}
	
class ConnectionState {
    public Sync sync;
    public Running running;
    
    public ConnectionState() {
        sync = new Sync();
        running = new Running();
    }

    public class Sync {
        public int round_trips_remaining;
        public int random;
    }
    
    public class Running {
    	public long last_quality_report_time;
    	public long last_network_stats_interval;
    	public long last_input_packet_recv_time;
    }
}
