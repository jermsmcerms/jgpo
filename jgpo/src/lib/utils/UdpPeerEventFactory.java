package lib.utils;

import lib.network.udppeerevents.*;

public class UdpPeerEventFactory {
	public static UdpPeerEvent makeUdpPeerEvent(UdpPeerEvent.EventType eventType) {
		switch (eventType) {
			case Input :
				return new InputEvent(eventType);
			case NetworkResumed :
				return new NetworkInterruptedEvent(eventType);
			case Synchronizing :
				return new PeerSynchronizingEvent(eventType);
			case Connected :
			case Disconnected :
			case NetworkInterrupted :
			case Synchronzied :
			case Unknown :
				return new UdpPeerEvent(eventType);
			default :
				return null;
						
		}
	}
}
