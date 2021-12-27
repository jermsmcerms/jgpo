package lib.network;

import java.net.SocketAddress;

public class UdpQueueEntry {
	public long queueTime;
	public SocketAddress destination;
	public UdpMsg message;
	
	public UdpQueueEntry(long queuetime, SocketAddress destination, 
		UdpMsg message) {
		this.queueTime = queuetime;
		this.destination = destination;
		this.message = message;
	}
}
