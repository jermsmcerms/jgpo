package lib;

public class GameInput {
	public static final int NULL_INPUT = -1;
	public static final int NULL_FRAME = -1;

	private int input;
	private int frame;
	
	public GameInput(int input, int frame) {
		this.input = input;
		this.frame = frame;
	}
}
