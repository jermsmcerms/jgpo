package lib.network;

import java.net.SocketAddress;

public abstract class UdpCallbacks {
	// TODO: double check socket address argument is correct...
	public abstract void onMsg(SocketAddress from, UdpMsg msg);
}
