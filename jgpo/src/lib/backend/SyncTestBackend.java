package lib.backend;

import api.JgpoNet.JGPOErrorCodes;
import api.JgpoNet.JGPOPlayerHandle;
import api.Player;
import api.SessionCallbacks;
import lib.utils.GeneralDataPackage;

public class SyncTestBackend implements JGPOSession {

	public SyncTestBackend(SessionCallbacks sessionCallbacks, String name,
			int numPlayers) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public GeneralDataPackage addPlayer(Player player) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JGPOErrorCodes addLocalInput(JGPOPlayerHandle player,
			Object values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GeneralDataPackage syncInput() {
		// TODO Auto-generated method stub
		return null;
	}

}
