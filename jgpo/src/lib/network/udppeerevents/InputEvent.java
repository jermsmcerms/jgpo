package lib.network.udppeerevents;

import lib.GameInput;

public class InputEvent extends UdpPeerEvent {
	public GameInput input;
	public InputEvent(EventType eventType) {
		super(eventType);
	}
}
