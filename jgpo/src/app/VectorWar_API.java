package app;

import api.JgpoNet.JGPOErrorCode;
import api.JgpoNet.JGPONetworkStats;
import api.JgpoNet.JGPOPlayer;
import api.JgpoNet.JGPOPlayerHandle;
import api.JgpoNet.JGPOSessionCallbacks;
import api.JgpoNet.JGPO_API;
import lib.backend.JGPOSession;
import lib.backend.P2P;
import lib.utils.GeneralDataPackage;

public class VectorWar_API implements JGPO_API {
	@Override
	public GeneralDataPackage jgpo_start_session(JGPOSessionCallbacks cb, String game, int num_players, int local_port) {
		return new GeneralDataPackage(
			new P2P(cb, game, local_port, num_players),
			JGPOErrorCode.JGPO_OK
		);
	}
	
	@Override
	public GeneralDataPackage jgpo_add_player(JGPOSession session, JGPOPlayer player) {
		if(session == null) {
			return new GeneralDataPackage(JGPOErrorCode.JGPO_INVALID_SESSION);
		}
		return session.addPlayer(player);
	}

	@Override
	public JGPOErrorCode jgpo_start_synctest(JGPOSessionCallbacks cb, String game, int num_players, int frames) {
		return null;
	}

	@Override
	public JGPOErrorCode jgpo_set_frame_delay(JGPOPlayerHandle player, int frame_delay) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JGPOErrorCode jgpo_idle(JGPOSession session, long timeout) {
		if(session == null) {
			return JGPOErrorCode.JGPO_INVALID_SESSION;
		}
		return session.doPoll(timeout);
	}

	@Override
	public JGPOErrorCode jgpo_add_local_input(JGPOSession session, JGPOPlayerHandle player, Object values) {
		if(session == null) {
			return JGPOErrorCode.JGPO_INVALID_SESSION;
		}
		return JGPOErrorCode.JGPO_OK;
	}

	@Override
	public GeneralDataPackage jgpo_synchronize_input(JGPOSession session) {
		// TODO Auto-generated method stub
		return new GeneralDataPackage(JGPOErrorCode.JGPO_OK);
	}

	@Override
	public JGPOErrorCode jgpo_disconnect_player(JGPOPlayerHandle player) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JGPOErrorCode jgpo_advance_frame(JGPOSession session) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JGPOErrorCode jgpo_get_network_stats(JGPOPlayerHandle player, JGPONetworkStats stats) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JGPOErrorCode jgpo_set_disconnect_timeout(JGPOSession session, int timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JGPOErrorCode jgpo_set_disconnect_notify_start(JGPOSession session,int timeout) {
		// TODO Auto-generated method stub
		return null;
	}
}
