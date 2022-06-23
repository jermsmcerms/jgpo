package app;

import api.JgpoNet.JGPOErrorCodes;
import api.NetworkStats;
import api.Player;
import api.JgpoNet.JGPOPlayerHandle;
import api.SessionCallbacks;
import api.JgpoApi;
import lib.backend.P2P;
import lib.backend.SyncTestBackend;
import lib.utils.GeneralDataPackage;

public class VectorWar_API extends JgpoApi {
	public VectorWar_API(SessionCallbacks callbacks, String name, int numPlayers, int localPort) {
		super(new P2P(callbacks, name, numPlayers, localPort));
	}
	
	public VectorWar_API(SessionCallbacks sessionCallbacks, String name,
			int numPlayers) {
		super(new SyncTestBackend(sessionCallbacks, name, numPlayers));
	}

	@Override
	public GeneralDataPackage jgpoAddPlayer(Player player) {
		if(session == null) {
			return new GeneralDataPackage(JGPOErrorCodes.JGPO_INVALID_SESSION);
		}
		return session.addPlayer(player);
	}

	@Override
	public JGPOErrorCodes jgpoSetFrameDelay(JGPOPlayerHandle player, int frame_delay) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JGPOErrorCodes jgpoIdle(long timeout) {
		if(session == null) {
			return JGPOErrorCodes.JGPO_INVALID_SESSION;
		}
		return session.doPoll(timeout);
	}

	// TODO: implement function tomorrow
	@Override
	public JGPOErrorCodes jgpoAddLocalInput(JGPOPlayerHandle player, Object values) {
		if(session == null) {
			return JGPOErrorCodes.JGPO_INVALID_SESSION;
		}
		return session.addLocalInput(player, values);
	}

	@Override
	public GeneralDataPackage jgpoSynchronizeInputs() {
		if(session == null) {
			return new GeneralDataPackage(JGPOErrorCodes.JGPO_INVALID_SESSION);
		}
		return session.syncInput();
	}

	@Override
	public JGPOErrorCodes jgpoDisconnectPlayer(JGPOPlayerHandle player) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JGPOErrorCodes jgpoAdvanceFrame() {
		if(session == null) {
			return JGPOErrorCodes.JGPO_INVALID_SESSION;
		}
		return session.incrementFrame();
	}

	@Override
	public JGPOErrorCodes jgpoSetNetworkStats(JGPOPlayerHandle player, NetworkStats stats) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JGPOErrorCodes jgpoSetDisconnectTimeout(int timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JGPOErrorCodes jgpoSetDisconnectNotifyStart(int timeout) {
		// TODO Auto-generated method stub
		return null;
	}
}
