package lib.utils;

import lib.network.messages.*;

public class UdpMessageBodyFactory {
	public static UdpMsgBody makeUdpMessageBody(UdpMsgBody.MsgType messageType) {
		switch (messageType) {
			case SyncRequest :
				return new SyncRequest();
			case SyncReply :
				return new SyncReply();
			default :
				return null;
		}
	}
	
	public static UdpMsgBody makeUdpMessageBody(byte[] message) {
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
