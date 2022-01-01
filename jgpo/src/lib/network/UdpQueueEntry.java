package lib.network;

import java.net.SocketAddress;

public class UdpQueueEntry {
	public long queueTime;
	public SocketAddress destination;
	public UdpMessage message;
	
	public UdpQueueEntry(long queuetime, SocketAddress destination, 
		UdpMessage message) {
		this.queueTime = queuetime;
		this.destination = destination;
		this.message = message;
	}
}
