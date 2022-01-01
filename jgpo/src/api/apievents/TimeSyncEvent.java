package api.apievents;

public class TimeSyncEvent extends JGPOEvent {
	public int framesAhead;
	public TimeSyncEvent(JGPOEventCode code, int playerHandle) {
		super(code, playerHandle);
	}

}
