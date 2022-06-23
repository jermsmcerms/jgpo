package lib;

public class SavedState {
	private static final int MAX_PREDICTION_FRAMES = 8;
	public SavedFrame[] frames;
	public int head;
	
	public SavedState() {
		frames = new SavedFrame[MAX_PREDICTION_FRAMES + 2];
		for(int i = 0; i < frames.length; i++) {
			frames[i] = new SavedFrame();
		}
	}
}
