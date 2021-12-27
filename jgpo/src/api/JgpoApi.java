package api;

import api.JgpoNet.JGPOErrorCodes;
import api.JgpoNet.JGPOPlayerHandle;
import lib.backend.JGPOSession;
import lib.utils.GeneralDataPackage;

public abstract class JgpoApi {
	protected JGPOSession session;
	public JgpoApi(JGPOSession session) {
		this.setSession(session);
	}
	
	public abstract GeneralDataPackage jgpoAddPlayer(Player player);
	// TODO: write spectator functions
	// TODO: may need close session function
	public abstract JGPOErrorCodes jgpoSetFrameDelay(JGPOPlayerHandle player, int frame_delay);
	public abstract JGPOErrorCodes jgpoIdle(long timeout);
	public abstract JGPOErrorCodes jgpoAddLocalInput(JGPOPlayerHandle player, Object values);
	public abstract GeneralDataPackage jgpoSynchronizeInputs();
	public abstract JGPOErrorCodes jgpoDisconnectPlayer(JGPOPlayerHandle player);
	public abstract JGPOErrorCodes jgpoAdvanceFrame();
	public abstract JGPOErrorCodes jgpoSetNetworkStats(JGPOPlayerHandle player, NetworkStats stats);
	public abstract JGPOErrorCodes jgpoSetDisconnectTimeout(int timeout);
	public abstract JGPOErrorCodes jgpoSetDisconnectNotifyStart(int timeout);
	// TODO: may need log functions

	public JGPOSession getSession() {
		return session;
	}

	public void setSession(JGPOSession session) {
		this.session = session;
	}
}
