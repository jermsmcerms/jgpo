package lib.network.messages;

public class SyncRequest extends UdpMessageBody {
	public int remoteMagic;
	public int randomRequest;
	public byte remoteEndpoint;
	
	public SyncRequest() {
		sizeInBytes = 9;
		messageType = UdpMessageBody.MessageType.SyncRequest;
	}

	public SyncRequest(byte[] message) {
		sizeInBytes = 9;
		
		messageType = UdpMessageBody.MessageType.values()[message[8]];
		
		remoteMagic = ((message[9]  & 0xFF) << 24) |
        			  ((message[10] & 0xFF) << 16) |
        			  ((message[11] & 0xFF) <<  8) |
        			  ((message[12] & 0xFF) <<  0);
		
		randomRequest = ((message[13] & 0xFF) << 24) |
		  			    ((message[14] & 0xFF) << 16) |
		  			    ((message[15] & 0xFF) <<  8) |
		  			    ((message[16] & 0xFF) <<  0);
		
		remoteEndpoint = message[17];
	}

	@Override
	public byte[] constructMessageBody() {
		byte[] syncRequestData = new byte[sizeInBytes];
		syncRequestData[0] = (byte)((remoteMagic >> 24) & 0xff);
		syncRequestData[1] = (byte)((remoteMagic >> 16) & 0xff);
		syncRequestData[2] = (byte)((remoteMagic >>  8) & 0xff);
		syncRequestData[3] = (byte)((remoteMagic >>  0) & 0xff);
		
		syncRequestData[4] = (byte)((randomRequest >> 24) & 0xff);
		syncRequestData[5] = (byte)((randomRequest >> 16) & 0xff);
		syncRequestData[6] = (byte)((randomRequest >>  8) & 0xff);
		syncRequestData[7] = (byte)((randomRequest >>  0) & 0xff);
		
		syncRequestData[8] = remoteEndpoint;

		return syncRequestData;
	}
}