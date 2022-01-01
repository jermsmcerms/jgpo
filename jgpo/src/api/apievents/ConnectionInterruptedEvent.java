package api.apievents;

public class ConnectionInterruptedEvent extends JGPOEvent {
	private int disconnectTimeout;
	
	public ConnectionInterruptedEvent(JGPOEventCode code, int disconnectTimeout, int playerHandle) {
		super(code, playerHandle);
		this.disconnectTimeout = disconnectTimeout;
	}
	
	public int getDisconnectTimeou() {
		return disconnectTimeout;
	}
}
