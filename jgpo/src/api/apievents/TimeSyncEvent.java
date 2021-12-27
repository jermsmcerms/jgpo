package api.apievents;

public class TimeSyncEvent extends JGPOEvent {
	public int framesAhead;
	public TimeSyncEvent(JGPOEventCode code) {
		super(code);
	}

}
