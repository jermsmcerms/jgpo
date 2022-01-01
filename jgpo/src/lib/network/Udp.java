package lib.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import lib.Poll;
import lib.PollSink;

public class Udp implements PollSink {
	public static final int MAX_UDP_ENDPOINTS = 16;
	public static final int MAX_UDP_PACKET_SIZE = 4096;
	
	private DatagramChannel datagramChannel;
	private UdpCallbacks callbacks;
	private Poll poll;
	private ByteBuffer receiveBuffer;
	private SocketAddress fromAddress;
	
	public Udp(int localPort, UdpCallbacks callbacks) throws IOException {
		datagramChannel = DatagramChannel.open();
		datagramChannel.configureBlocking(false);
		System.out.println("binding to localport: " + localPort);
		datagramChannel.socket().bind(new InetSocketAddress(localPort));
		this.poll = new Poll();
		poll.registerLoop(this);
		this.callbacks = callbacks;
		receiveBuffer = ByteBuffer.allocate(MAX_UDP_PACKET_SIZE);
	}

	public Poll getPoll() {
		return poll;
	}
	
	public void sendTo(UdpQueueEntry queueEntry) {
		try {
			byte[] data = queueEntry.message.getMessageData();
			ByteBuffer buffer = ByteBuffer.wrap(data);
			SocketAddress destination = queueEntry.destination;
			datagramChannel.send(buffer, destination);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public boolean onLoopPoll(Object cookie) {
		while(true) {
			try {
				receiveBuffer.clear();
				fromAddress = datagramChannel.receive(receiveBuffer);
				if(fromAddress == null) {
					break;
				} else {
					UdpMessage receivedMessage = new UdpMessage(receiveBuffer.array());
					callbacks.onMsg(fromAddress, receivedMessage);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				break;
			}
		}
		return true;
	}
}
