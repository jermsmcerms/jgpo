package api;

import api.apievents.JGPOEvent;
import lib.SavedState;

public interface SessionCallbacks {
	public boolean beginGame(String name);
	public boolean saveGameState(SavedState savedState, int frameCount);
	public boolean loadGameState();
	public boolean logGameState();
	// TODO: may need a free buffer function
	public boolean advanceFrame(int flags);
	public boolean onEvent(JGPOEvent event);
}
