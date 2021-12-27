package api.apievents;

public class ConnectionInterruptedEvent extends JGPOEvent {
	public int disconnectTimeout;
	public ConnectionInterruptedEvent(JGPOEventCode code) {
		super(code);
	}

}
