package apps;

import java.awt.Rectangle;

public class GameState {	
	private Rectangle bounds;
	
	public GameState(int num_ships) {		
		bounds = new Rectangle(50, 50, 515, 330);
	}
	
	public Rectangle getBounds() {
		return bounds;
	}
}
