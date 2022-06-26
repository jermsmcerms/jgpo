package lib.network.messages;

public class QualityReply extends UdpMessageBody {
	public long pong;
	
	public QualityReply() {
		sizeInBytes = 9;
		messageType = UdpMessageBody.MessageType.QualityReport;
	}
	
	public QualityReply(byte[] message) {
		sizeInBytes = 9;
		
		messageType = UdpMessageBody.MessageType.values()[message[8]];
		
		pong = ((message[9]  & 0xFF) << 56) |
		  ((message[10] & 0xFF) << 48) |
		  ((message[11] & 0xFF) << 40) |
		  ((message[12] & 0xFF) << 32) |
		  ((message[13] & 0xFF) << 24) |
		  ((message[14] & 0xFF) << 16) |
		  ((message[15] & 0xFF) <<  8) |
		  ((message[16] & 0xFF) <<  0);	
	}
	
	@Override
	public byte[] constructMessageBody() {
		byte[] qualityReplyData = new byte[sizeInBytes];
		
		qualityReplyData[0] = (byte)((pong >> 56) & 0xff);
		qualityReplyData[1] = (byte)((pong >> 48) & 0xff);
		qualityReplyData[2] = (byte)((pong >> 40) & 0xff);
		qualityReplyData[3] = (byte)((pong >> 32) & 0xff);
		qualityReplyData[4] = (byte)((pong >> 24) & 0xff);
		qualityReplyData[5] = (byte)((pong >> 16) & 0xff);
		qualityReplyData[6] = (byte)((pong >>  8) & 0xff);
		qualityReplyData[7] = (byte)((pong >>  0) & 0xff);

		return qualityReplyData;
	}
}
