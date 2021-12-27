package api.apievents;

public class ApiSynchronizingEvent extends JGPOEvent {
	public int count;
	public int total;
	public ApiSynchronizingEvent(JGPOEventCode code) {
		super(code);
	}

}
