package lib.backend;

import api.JgpoNet.JGPOErrorCode;
import api.JgpoNet.JGPONetworkStats;
import api.JgpoNet.JGPOPlayer;
import api.JgpoNet.JGPOPlayerHandle;
import lib.utils.GeneralDataPackage;

public interface JGPOSession {
	public GeneralDataPackage addPlayer(JGPOPlayer player);
	public JGPOErrorCode addLocalInput(JGPOPlayerHandle player, Object values);
	public JGPOErrorCode syncInput(Object values, int disconnect_flags);
	
	default JGPOErrorCode doPoll(long timeout) { return JGPOErrorCode.JGPO_OK; }
	default JGPOErrorCode incrementFrame() { return JGPOErrorCode.JGPO_OK; }
	default JGPOErrorCode chat(String text) { return JGPOErrorCode.JGPO_OK; }
	default JGPOErrorCode disconnectPlayer(JGPOPlayerHandle handle) { return JGPOErrorCode.JGPO_OK; }
	default JGPOErrorCode getNetworkStats(JGPONetworkStats stats, JGPOPlayerHandle handle) { return JGPOErrorCode.JGPO_OK; }
	// TODO: need som kind of log function...
	default JGPOErrorCode setFrameDelay(JGPOPlayerHandle player, int delay) { return JGPOErrorCode.JGPO_UNSUPPORTED; }
	default JGPOErrorCode setDisconnectTimeout(int timeout) { return JGPOErrorCode.JGPO_UNSUPPORTED; }
	default JGPOErrorCode setDisconnectNotifyStart(int timeout) { return JGPOErrorCode.JGPO_UNSUPPORTED; }
}
