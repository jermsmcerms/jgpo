package lib;

public class GameInput {
	public static final int NULL_INPUT = -1;
	public static final int NULL_FRAME = -1;

	private int input;
	private int frame;
	
	public GameInput(int input, int frame) {
		this.setInput(input);
		this.setFrame(frame);
	}

	public int getInput() {
		return input;
	}

	public void setInput(int input) {
		this.input = input;
	}

	public int getFrame() {
		return frame;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	public void incrementFrame() {
		frame++;
	}
}
