package lib.network.messages;

public abstract class UdpMsgBody {
	public MsgType msgType;
	
	/** 	
	 * sizeInBytes is calculated by the sum of the unique primitive data types, each multiplied by
	 * the number of occurrences of each data type.
	 * Example:
	 * public class ExampleMessageBody extends UdpMsgBody {
	 *     public int item1;
	 *     public int item2;
	 *     public boolean item3;
	 *     
	 *     public ExampleMessageBody() {
	 *         sizeInBytes = 9; <- 8 bytes (2 * Integer.BYTES) + 1 byte (size of boolean)
	 *	   }
	 * }	         
	*/ 
	protected int sizeInBytes;
	
	public enum MsgType {
		Invalid, SyncRequest, SyncReply
	}
	
	public int getSizeInBytes() {
		return sizeInBytes;
	}
	
	public abstract byte[] constructMessage();
}
