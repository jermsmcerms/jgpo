package lib.utils;

import lib.network.messages.*;

public class UdpMessageBodyFactory {
	public static UdpMessageBody makeUdpMessageBody(UdpMessageBody.MessageType messageType) {
		switch (messageType) {
			case SyncRequest :
				return new SyncRequest();
			case SyncReply :
				return new SyncReply();
			default :
				return null;
		}
	}
	
	public static UdpMessageBody makeUdpMessageBody(byte[] message) {
		// TODO: is there a better way to get this value?...
		byte messageType = message[8];
		switch (messageType) {
			case 1 :
				return new SyncRequest(message);
			case 2 :
				return new SyncReply(message);
			default :
				return null;
		}
	}
}
