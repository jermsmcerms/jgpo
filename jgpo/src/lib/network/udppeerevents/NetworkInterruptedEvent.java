package lib.network.udppeerevents;

public class NetworkInterruptedEvent extends UdpPeerEvent {
	public int disconnectTimeout;
	public NetworkInterruptedEvent(EventType eventType) {
		super(eventType);
	}
}
