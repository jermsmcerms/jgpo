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
		public int connectProgress;
		public int disconnectTimeout;
		public int disconnectStart;
	}
	
	public static class ChecksumInfo {
		public int frameNumber;
		public int checksum;
	}
	
	public JGPOPlayerHandle localPlayerHandle;
	public PlayerConnectionInfo[] players;
	public ChecksumInfo now;
	public ChecksumInfo periodic;
	
	private int numPlayers;
	
	public NonGameState(int num_players) {
		this.numPlayers = num_players;
		localPlayerHandle = new JGPOPlayerHandle();
		players = new PlayerConnectionInfo[num_players];
		for(int i = 0; i < players.length; i++) {
			players[i] = new PlayerConnectionInfo();
			players[i].state = PlayerConnectState.Connecting;
		}
		now = new ChecksumInfo();
		periodic = new ChecksumInfo();
	}

	public void setConnectState(JGPOPlayerHandle handle, PlayerConnectState state) {
		for(int i = 0; i < numPlayers; i++) {
			if(players[i].handle.playerHandle == handle.playerHandle) {
				players[i].connectProgress = 0;
				players[i].state = state;
				System.out.println(players[i].state);
				break;
			}
		}
	}
	
	public void setDisconnectTimeout() {}
	public void setConnectState(PlayerConnectState state) {
		for(int i = 0; i < numPlayers; i++) {
			players[i].state = state;
		}
	}
	
	public void updateConnectProgress(JGPOPlayerHandle playerHandle, int progress) {
		for(int i = 0; i < numPlayers; i++) {
			if(players[i].handle.playerHandle == playerHandle.playerHandle) {
				players[i].connectProgress = progress;
			}
		}
	}

	public int getNumPlayers() {
		return numPlayers;
	}
}
