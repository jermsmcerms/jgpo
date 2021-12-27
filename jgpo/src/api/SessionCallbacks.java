package api;

import api.apievents.JGPOEvent;

public interface SessionCallbacks {
	public boolean beginGame(String name);
	public boolean saveGameState();
	public boolean loadGameState();
	public boolean logGameState();
	// TODO: may need a free buffer function
	public boolean advanceFrame(int flags);
	public boolean onEvent(JGPOEvent event);
}
