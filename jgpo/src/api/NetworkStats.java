package api;

public class NetworkStats {
	public class Network {
		public int sendQueueLength;
		public int recvQueueLength;
		public int ping;
		public int  kbpsSent;
	}
	
	public class Timesync {
		public int localFramesBehind;
		public int remoteFramesBehind;
	}
}
