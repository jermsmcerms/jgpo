/**
 * Original creators license :
 * -----------------------------------------------------------------------
 * GGPO.net (http://ggpo.net)  -  Copyright 2009 GroundStorm Studios, LLC.
 *
 * Use of this software is governed by the MIT license that can be found
 * in the LICENSE file.
 * -----------------------------------------------------------------------
 * JGPO - Copyright 2021 JB Room Studios (SP, Unregistered)
 * 
 * Use of this software is governed by the MIT license that can be found
 * in the LICENSE file.
 */
package lib.network;

import lib.network.messages.UdpMessageBody;
import lib.utils.UdpMessageBodyFactory;

// TODO: This may be too complicated...
/**
 * The UdpMsg class constructs and process messages that can be read by a UDP network.
 * The format for every message is as follows:
 * Header:
 * 	- remote magic number: a randomly generated number created every time a new udp instance is
 * 						   is created. It is the device id and used to validate that the end user
 * 						   only handles messages from remote connections the expect.
 * 
 * 	- sequence number:     A number that increments every time a udp packet is sent.
 * 					       Used to keep packets in order as they may not all arrive at the same time.
 * 
 * 	- message type: 	   A flag that tells the client what kind of message it needs to process.
 * 
 * Message Body: The body of the message. Contains the data needed to run the application.
 * 
 * The entire packet is built using a byte array. Each piece of data is split into a corresponding
 * index in the array. The header contains a constant amount of space, while the body portion may
 * vary in length.
 * 
 * The first n indices are reserved for the header. The history of this reserved space is as follows:
 * ***NOTE***
 * If the header is changed in any way, make sure to change anywhere specific header values are 
 * accessed!
 * **********
 * Date: December 25, 2021
 * messageData[0] - messageData[3] : magic number
 * messageData[4] - messageData[7] : sequence number
 * messageData[8] : message type
 * 
 * @author jerms_mcerms
 */
public class UdpMsg {
	public static final int UDP_MSG_MAX_PLAYERS = 4;
	public Header header;
	public UdpMessageBody messageBody;
	public byte[] messageData;
	
	public static class ConnectStatus {
		public int lastFrame;
		public boolean disconnected;
		
		public ConnectStatus(boolean disconnected, int lastFrame) {
			this.lastFrame = lastFrame;
			this.disconnected = disconnected;
		}
	}

	public class Header {
		public int magicNumber;
		public int sequenceNumber;
		public byte messageType;
		public int headerSizeInBytes = 9; // 4 bytes * 2 (two ints) + 1 (one byte)
	}
	
	public UdpMsg(byte[] message) {
		header = new Header();
		header.magicNumber = ((message[0] & 0xFF) << 24) |
				  			 ((message[1] & 0xFF) << 16) |
				  			 ((message[2] & 0xFF) << 8 ) |
				  			 ((message[3] & 0xFF));
		
		header.sequenceNumber = ((message[4] & 0xFF) << 24) |
  			  					((message[5] & 0xFF) << 16) |
		  						((message[6] & 0xFF) << 8 ) |
	  							((message[7] & 0xFF));
		
		header.messageType = message[8];
		
		messageBody = UdpMessageBodyFactory.makeUdpMessageBody(message);
	}

	public UdpMsg(UdpMessageBody.MessageType messageType) {
		header = new Header();
		header.messageType = (byte) messageType.ordinal();
		messageBody = UdpMessageBodyFactory.makeUdpMessageBody(messageType);
	}

	public byte[] getMessageData() {
		if(messageData == null) {
			int messageBodySize = messageBody.getSizeInBytes();
			int headerSize = header.headerSizeInBytes;
			int packetSize = headerSize + messageBodySize;
			messageData = new byte[packetSize];
			byte[] messageBodyData = messageBody.constructMessageBody();
			
			// Build the header
			messageData[0] = (byte)((header.magicNumber >> 24) & 0xff);
			messageData[1] = (byte)((header.magicNumber >> 16) & 0xff);
			messageData[2] = (byte)((header.magicNumber >>  8) & 0xff);
			messageData[3] = (byte)((header.magicNumber >>  0) & 0xff);
			messageData[4] = (byte)((header.sequenceNumber >> 24) & 0xff);
			messageData[5] = (byte)((header.sequenceNumber >> 16) & 0xff);
			messageData[6] = (byte)((header.sequenceNumber >>  8) & 0xff);
			messageData[7] = (byte)((header.sequenceNumber >>  0) & 0xff);
			messageData[8] = header.messageType;
			
			// Build the body
			for(int i = headerSize; i < packetSize; i++) {
				messageData[i] = messageBodyData[(i - headerSize)]; 
			}
			
			return messageData;
		}
		return new byte[] { -1 };
	}
}
