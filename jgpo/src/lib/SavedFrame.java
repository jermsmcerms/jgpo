package lib;

public class SavedFrame {
	public byte[] data;
	public int cbuf;
	public int frame;
	public long checksum;
	
	public SavedFrame() {
		frame = -1;
	}
}
