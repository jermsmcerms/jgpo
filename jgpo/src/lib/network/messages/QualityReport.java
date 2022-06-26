package lib.network.messages;

public class QualityReport extends UdpMessageBody {
	public byte frameAdvantage;
	public long ping;
	
	public QualityReport() {
		sizeInBytes = 9;
		messageType = UdpMessageBody.MessageType.QualityReport;
	}
	
	public QualityReport(byte[] message) {
		sizeInBytes = 9;
		
		messageType = UdpMessageBody.MessageType.values()[message[8]];
		
		frameAdvantage = message[9];

		ping = ((message[10]  & 0xFF) << 56) |
		  ((message[11] & 0xFF) << 48) |
		  ((message[12] & 0xFF) << 40) |
		  ((message[13] & 0xFF) << 32) |
		  ((message[14] & 0xFF) << 24) |
		  ((message[15] & 0xFF) << 16) |
		  ((message[16] & 0xFF) <<  8) |
		  ((message[17] & 0xFF) <<  0);	
	}
	
	@Override
	public byte[] constructMessageBody() {
		byte[] qualityReportData = new byte[sizeInBytes];
		qualityReportData[0] = frameAdvantage;
		
		qualityReportData[1] = (byte)((ping >> 56) & 0xff);
		qualityReportData[2] = (byte)((ping >> 48) & 0xff);
		qualityReportData[3] = (byte)((ping >> 40) & 0xff);
		qualityReportData[4] = (byte)((ping >> 32) & 0xff);
		qualityReportData[5] = (byte)((ping >> 24) & 0xff);
		qualityReportData[6] = (byte)((ping >> 16) & 0xff);
		qualityReportData[7] = (byte)((ping >>  8) & 0xff);
		qualityReportData[8] = (byte)((ping >>  0) & 0xff);

		return qualityReportData;
	}

}
