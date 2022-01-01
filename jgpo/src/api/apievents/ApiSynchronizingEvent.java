package api.apievents;

import app.NonGameState;

public class ApiSynchronizingEvent extends JGPOEvent {
	private int count;
	private int total;
	
	public ApiSynchronizingEvent(JGPOEventCode code, int count, int total, int playerHandle) {
		super(code, playerHandle);
		this.count = count;
		this.total = total;
	}
	
	public int getCount() {
		return count;
	}
	
	public int getTotal() {
		return total;
	}
	
	@Override
	public void processEvent(NonGameState nonGameState) {
		int progress = count / total;
		nonGameState.updateConnectProgress(playerHandle, progress);
	}
}
