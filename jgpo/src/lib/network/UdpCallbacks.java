package lib.network;

import java.net.SocketAddress;

public abstract class UdpCallbacks {
	public abstract void onMsg(SocketAddress from, UdpMessage msg);
}
