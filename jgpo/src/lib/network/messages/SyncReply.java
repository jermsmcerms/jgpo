package lib.network.messages;

import java.util.Arrays;

public class SyncReply extends UdpMsgBody {
	public int randomReply;
	
	public SyncReply() {
		sizeInBytes = 4;
		msgType = UdpMsgBody.MsgType.SyncReply;
	}
	
	public SyncReply(byte[] message) {
		sizeInBytes = 4;
		msgType = UdpMsgBody.MsgType.values()[message[8]];
		
		randomReply = ((message[9]  & 0xFF)  << 24) |
		  			  ((message[10] & 0xFF)  << 16) |
		  			  ((message[11] & 0xFF)  << 8 ) |
		  			  ((message[12] & 0xFF) << 0);
	}
	
	@Override
	public byte[] constructMessage() {
		byte[] syncReplyData = new byte[sizeInBytes];
		syncReplyData[0] = (byte)((randomReply >> 24) & 0xff);
		syncReplyData[1] = (byte)((randomReply >> 16) & 0xff);
		syncReplyData[2] = (byte)((randomReply >>  8) & 0xff);
		syncReplyData[3] = (byte)((randomReply >>  0) & 0xff);
		
		return syncReplyData;
	}

}
