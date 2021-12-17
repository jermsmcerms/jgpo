package apps;

import java.awt.Rectangle;

public class GameState {
	private Rectangle bounds;
	private Ship[] ships;
	
	public GameState(int num_ships) {		
		bounds = new Rectangle(50, 50, 515, 330);
		ships = new Ship[num_ships];

		for(int i = 0; i < ships.length; i++) {
			ships[i] = new Ship();
			ships[i].pos = ships[i].new Position();
			ships[i].vel = ships[i].new Velocity();
			ships[i].health = Constants.STARTING_HEALTH;
			
			int heading = i * 360 / num_ships;
			double cost, sint, theta;
			
			theta = (double)heading * Constants.PI / 180;
			cost = Math.cos(theta);
			sint = Math.sin(theta);
			
			int r = bounds.height / 4;
			ships[i].pos.x = bounds.x + bounds.width / 2 + r * cost;
			ships[i].pos.y = bounds.y + bounds.height / 2 + r * sint;
			ships[i].heading = (heading + 180) % 360;
			ships[i].radius = Constants.SHIP_RADIUS;
		}
	}
	
	public Rectangle getBounds() {
		return bounds;
	}

	public Ship[] getShips() {
		return ships;
	}
}
