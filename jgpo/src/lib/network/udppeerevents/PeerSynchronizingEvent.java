package lib.network.udppeerevents;

public class PeerSynchronizingEvent extends UdpPeerEvent {
	public int total;
	public int count;
	public PeerSynchronizingEvent(EventType eventType) {
		super(eventType);
	}

}
