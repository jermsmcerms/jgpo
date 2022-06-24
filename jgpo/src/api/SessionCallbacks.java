package api;

import api.apievents.JGPOEvent;
import lib.SavedFrame;
import lib.SavedState;

public interface SessionCallbacks {
	public boolean beginGame(String name);
	public boolean saveGameState(SavedState savedState, int frameCount);
	public boolean loadGameState(SavedFrame loadFrame);
	public boolean logGameState();
	// TODO: may need a free buffer function
	public boolean advanceFrame(int flags);
	public boolean onEvent(JGPOEvent event);
}
