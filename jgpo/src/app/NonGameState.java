package app;

import api.JgpoNet.JGPOPlayerHandle;
import api.JgpoNet.JGPOPlayerType;

public class NonGameState {
	public enum PlayerConnectState {
		Connecting, Synchronizing, Running, Disconnected, Disconnecting
	}
	
	public static class PlayerConnectionInfo {
		public JGPOPlayerType type;
		public PlayerConnectState state;
		public JGPOPlayerHandle handle;
		public int connect_progress;
		public int disconnect_timeout;
		public int disconnect_start;
	}
	
	public static class ChecksumInfo {
		public int frame_number;
		public int checksum;
	}
	
	public JGPOPlayerHandle local_player_handle;
	public PlayerConnectionInfo[] players;
	public ChecksumInfo now;
	public ChecksumInfo periodic;
	
	private int num_players;
	
	public NonGameState(int num_players) {
		this.num_players = num_players;
		local_player_handle = new JGPOPlayerHandle();
		players = new PlayerConnectionInfo[num_players];
		for(int i = 0; i < players.length; i++) {
			players[i] = new PlayerConnectionInfo();
			players[i].state = PlayerConnectState.Connecting;
		}
		now = new ChecksumInfo();
		periodic = new ChecksumInfo();
	}

	public void setConnectState(JGPOPlayerHandle handle, PlayerConnectState state) {
		for(int i = 0; i < num_players; i++) {
			if(players[i].handle.playerHandle == handle.playerHandle) {
				players[i].connect_progress = 0;
				players[i].state = state;
				break;
			}
		}
	}
	public void setDisconnectTimeout() {}
	public void setConnectState(PlayerConnectState state) {}
	public void updateConnectProgress(int progress) {}

	public int getNumPlayers() {
		return num_players;
	}
}
