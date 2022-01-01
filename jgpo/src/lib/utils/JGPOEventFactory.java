package lib.utils;

import api.apievents.JGPOEvent.JGPOEventCode;
import api.apievents.*;
import api.JgpoNet.JGPOPlayerHandle;
import lib.network.udppeerevents.NetworkInterruptedEvent;
import lib.network.udppeerevents.PeerSynchronizingEvent;
import lib.network.udppeerevents.UdpPeerEvent;

public class JGPOEventFactory {
	public static JGPOEvent makeApiEvent(UdpPeerEvent udpPeerEvent, JGPOPlayerHandle handle) {
		switch(udpPeerEvent.eventType) {
			case Connected :
				return new JGPOEvent(JGPOEventCode.JGPO_CONNECTED_TO_PEER, handle.playerHandle);
			case Synchronzied :
				return new JGPOEvent(JGPOEventCode.JGPO_SYNCHRONIZED_WITH_PEER, handle.playerHandle);
			case NetworkInterrupted :
				NetworkInterruptedEvent networkInterrupted = (NetworkInterruptedEvent)udpPeerEvent;
				return new ConnectionInterruptedEvent(JGPOEventCode.JGPO_CONNECTION_INTERRUPTED, networkInterrupted.disconnectTimeout, handle.playerHandle);
			case Synchronizing :
				PeerSynchronizingEvent peerSyncEvent = (PeerSynchronizingEvent)udpPeerEvent;
				return new ApiSynchronizingEvent(JGPOEventCode.JGPO_SYNCHRONIZING_WITH_PEER, peerSyncEvent.count, peerSyncEvent.total, handle.playerHandle);				
			case NetworkResumed :
			case Unknown :
			default :
				return null;
		}
	}
}
	
//	case Connected :
//		JGPOEvent connectedEvent = 
//			JGPOEventFactory.makeApiEvent(JGPOEvent.JGPOEventCode.JGPO_CONNECTED_TO_PEER);
//		connectedEvent.playerHandle.playerHandle = handle.playerHandle;
//		callbacks.onEvent(connectedEvent);
//		return null;
//	case NetworkResumed :
//		JGPOEvent networkResumedEvent =
//			JGPOEventFactory.makeApiEvent(JGPOEvent.JGPOEventCode.JGPO_CONNECTION_RESUMED);
//		networkResumedEvent.playerHandle.playerHandle = handle.playerHandle;
//		callbacks.onEvent(networkResumedEvent);
//		return null;
//	case Disconnected :
//		return null;
//	case Input :
//		return null;
//	case Synchronzied :
//		JGPOEvent synchronizedEvent = 
//			JGPOEventFactory.makeApiEvent(JGPOEvent.JGPOEventCode.JGPO_SYNCHRONIZED_WITH_PEER);
//		synchronizedEvent.playerHandle.playerHandle = handle.playerHandle;
//		callbacks.onEvent(synchronizedEvent);
//		checkInitialSync();
//		return null;
//	case Unknown :
//		return null;
//	case Synchronizing :
//		ApiSynchronizingEvent apiSynchronizingEvent = 
//			(ApiSynchronizingEvent) JGPOEventFactory.makeApiEvent(JGPOEvent.JGPOEventCode.JGPO_SYNCHRONIZING_WITH_PEER);
//		apiSynchronizingEvent.playerHandle.playerHandle = handle.playerHandle;
//		PeerSynchronizingEvent peerSyncEvent = (PeerSynchronizingEvent)event;
//		apiSynchronizingEvent.count = peerSyncEvent.count;
//		apiSynchronizingEvent.total = peerSyncEvent.total;
//		callbacks.onEvent(apiSynchronizingEvent);
//		return null;
//	case NetworkInterrupted :
//		ConnectionInterruptedEvent connectionInterruptedEvent = 
//			(ConnectionInterruptedEvent) JGPOEventFactory.makeApiEvent(JGPOEvent.JGPOEventCode.JGPO_SYNCHRONIZING_WITH_PEER);
//		NetworkInterruptedEvent networkInterrupted = (NetworkInterruptedEvent)event;
//		connectionInterruptedEvent.playerHandle.playerHandle = handle.playerHandle;
//		connectionInterruptedEvent.disconnectTimeout = networkInterrupted.disconnectTimeout;
//		callbacks.onEvent(connectionInterruptedEvent);
//		return null;
//	default :
//		return null;
//}
