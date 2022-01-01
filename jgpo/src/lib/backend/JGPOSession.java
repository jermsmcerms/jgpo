package lib.backend;

import api.JgpoNet.JGPOErrorCodes;
import api.NetworkStats;
import api.Player;
import api.JgpoNet.JGPOPlayerHandle;
import lib.utils.GeneralDataPackage;

public interface JGPOSession {
	public GeneralDataPackage addPlayer(Player player);
	public JGPOErrorCodes addLocalInput(JGPOPlayerHandle player, Object values);
	public GeneralDataPackage syncInput();
	
	default JGPOErrorCodes doPoll(long timeout) { return JGPOErrorCodes.JGPO_OK; }
	default JGPOErrorCodes incrementFrame() { return JGPOErrorCodes.JGPO_OK; }
	default JGPOErrorCodes chat(String text) { return JGPOErrorCodes.JGPO_OK; }
	default JGPOErrorCodes disconnectPlayer(JGPOPlayerHandle handle) { return JGPOErrorCodes.JGPO_OK; }
	default JGPOErrorCodes getNetworkStats(NetworkStats stats, JGPOPlayerHandle handle) { return JGPOErrorCodes.JGPO_OK; }
	// TODO: may need some kind of log function...
	default JGPOErrorCodes setFrameDelay(JGPOPlayerHandle player, int delay) { return JGPOErrorCodes.JGPO_UNSUPPORTED; }
	default JGPOErrorCodes setDisconnectTimeout(int timeout) { return JGPOErrorCodes.JGPO_UNSUPPORTED; }
	default JGPOErrorCodes setDisconnectNotifyStart(int timeout) { return JGPOErrorCodes.JGPO_UNSUPPORTED; }
}
