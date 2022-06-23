package lib.network.messages;

import java.util.LinkedList;
import java.util.List;

import lib.GameInput;
import lib.network.UdpMessage;
import lib.network.UdpMessage.ConnectStatus;

public class Input extends UdpMessageBody {
	public int startFrame;
	public byte disconnectRequested;
	public int acknowlegedFrame;
	public byte numPeers;
	public UdpMessage.ConnectStatus[] peerConnectStatus;
	public int numBits;
	public byte[] inputs;
	public List<Byte> inputList = new LinkedList<Byte>();
	
	public Input(byte[] message) {
		messageType = UdpMessageBody.MessageType.values()[message[8]];
		
		startFrame = ((message[9]  & 0xFF) << 24) |
		  ((message[10] & 0xFF) << 16) |
		  ((message[11] & 0xFF) <<  8) |
		  ((message[12] & 0xFF) <<  0);
		
		disconnectRequested = message[13];
		
		acknowlegedFrame = ((message[14]  & 0xFF) << 24) |
		  ((message[15] & 0xFF) << 16) |
		  ((message[16] & 0xFF) <<  8) |
		  ((message[17] & 0xFF) <<  0);
		
		numPeers = message[18];
		
		peerConnectStatus = new UdpMessage.ConnectStatus[numPeers];
		int offset = 19;
		for(int i = 0; i < peerConnectStatus.length; i++) {
			boolean disconnectRequested = message[offset++] == (byte) 1;
			int lastFrame = ((message[offset++]  & 0xFF) << 24) |
					  ((message[offset++] & 0xFF) << 16) |
					  ((message[offset++] & 0xFF) <<  8) |
					  ((message[offset++] & 0xFF) <<  0);
			peerConnectStatus[i] = 
					new UdpMessage.ConnectStatus(disconnectRequested, lastFrame);
		}

		
		numBits = ((message[offset++]  & 0xFF) << 24) |
				  ((message[offset++] & 0xFF) << 16) |
				  ((message[offset++] & 0xFF) <<  8) |
				  ((message[offset++] & 0xFF) <<  0);
		
		inputs = new byte[numBits];
		for(int i = 0; i < inputs.length; i++) {
			inputs[i] = message[offset++];
		}
		
		// size calculation: 14 = size of static data, 5 = size of one peer
		sizeInBytes = 14 + (5 * numPeers) + inputs.length;
	}

	public Input(byte numPeers) {
		peerConnectStatus = new UdpMessage.ConnectStatus[numPeers];
		this.numPeers = numPeers;
		sizeInBytes = 14 + (5 * this.numPeers); // inputs will be added later.
	}
	
	@Override
	public byte[] constructMessageBody() {		
		byte[] inputData = new byte[getSizeInBytes()];
		inputData[0] = (byte)((startFrame >> 24) & 0xff);
		inputData[1] = (byte)((startFrame >> 16) & 0xff);
		inputData[2] = (byte)((startFrame >>  8) & 0xff);
		inputData[3] = (byte)((startFrame >>  0) & 0xff);
		
		inputData[4] = disconnectRequested;
		
		inputData[5] = (byte)((acknowlegedFrame >> 24) & 0xff);
		inputData[6] = (byte)((acknowlegedFrame >> 16) & 0xff);
		inputData[7] = (byte)((acknowlegedFrame >>  8) & 0xff);
		inputData[8] = (byte)((acknowlegedFrame >>  0) & 0xff);
		
		inputData[9] = numPeers;
		
		int offset = 10;
		for(int i = 0; i < peerConnectStatus.length; i++) {
			inputData[offset++] = peerConnectStatus[i].disconnected ? (byte) 1 : 0;
			inputData[offset++] = (byte)((peerConnectStatus[i].lastFrame >> 24) & 0xff);
			inputData[offset++] = (byte)((peerConnectStatus[i].lastFrame >> 16) & 0xff);
			inputData[offset++] = (byte)((peerConnectStatus[i].lastFrame >>  8) & 0xff);
			inputData[offset++] = (byte)((peerConnectStatus[i].lastFrame >>  0) & 0xff);
		}
		
		inputData[offset++] = (byte)((numBits >> 24) & 0xff);
		inputData[offset++] = (byte)((numBits >> 16) & 0xff);
		inputData[offset++] = (byte)((numBits >>  8) & 0xff);
		inputData[offset++] = (byte)((numBits >>  0) & 0xff);
		
		if(inputData.length - offset < inputList.size()) {
			System.out.println("not enough space for inputs " + (inputData.length - offset) + " " + inputList.size());
			return null;
		} else {
			int counter = offset;
			for(byte input : inputList) {
				inputData[counter] = input;
				counter++;
			}
		}
		
		inputList.clear();
		return inputData;
	}

	public void AddInput(GameInput input) {
		// Note: doing it this way limits input to 127
		// (Only 7 unique inputs if inputs are a power of 2)
		if(input.getInput() > 127) {
			System.out.println("Warning, input is too large for a single byte");
		}
		
		inputList.add((byte)input.getInput());		
	}

	@Override
	public int getSizeInBytes() {
		return sizeInBytes + inputList.size();
	}
}
