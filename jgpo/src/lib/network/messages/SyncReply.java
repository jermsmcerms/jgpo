package lib.network.messages;

public class SyncReply extends UdpMessageBody {
	public int randomReply;
	
	public SyncReply() {
		sizeInBytes = 4;
		messageType = UdpMessageBody.MessageType.SyncReply;
	}
	
	public SyncReply(byte[] message) {
		sizeInBytes = 4;
		messageType = UdpMessageBody.MessageType.values()[message[8]];
		
		randomReply = ((message[9]  & 0xFF)  << 24) |
		  			  ((message[10] & 0xFF)  << 16) |
		  			  ((message[11] & 0xFF)  <<  8) |
		  			  ((message[12] & 0xFF)  <<  0);
	}
	
	@Override
	public byte[] constructMessageBody() {
		byte[] syncReplyData = new byte[sizeInBytes];
		syncReplyData[0] = (byte)((randomReply >> 24) & 0xff);
		syncReplyData[1] = (byte)((randomReply >> 16) & 0xff);
		syncReplyData[2] = (byte)((randomReply >>  8) & 0xff);
		syncReplyData[3] = (byte)((randomReply >>  0) & 0xff);
		
		return syncReplyData;
	}

}
