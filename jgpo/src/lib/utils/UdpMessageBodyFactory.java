package lib.utils;

import lib.network.UdpMessage;
import lib.network.messages.*;

public class UdpMessageBodyFactory {
	public static UdpMessageBody makeUdpMessageBody(UdpMessageBody.MessageType messageType) {
		switch (messageType) {
			case SyncRequest :
				return new SyncRequest();
			case SyncReply :
				return new SyncReply();
			case Input :
				return new Input((byte)UdpMessage.UDP_MSG_MAX_PLAYERS);
			case QualityReport :
				return new QualityReport();
			case QualityReply :
				return new QualityReply();
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
			case 3 :
				return new Input(message);
			case 4 :
				return new QualityReport(message);
			case 5 : 
				return new QualityReply(message);
			default :
				return null;
		}
	}
}
