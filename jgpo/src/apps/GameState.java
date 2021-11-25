package apps;

import java.awt.Rectangle;

public class GameState {
	public static class GameStateConstants {
		public static final double PI = 3.1415926;
		public static final double SHIP_RADIUS = 15;
		public static final double SHIP_TUCK = 3;
		public static final double SHIP_WIDTH = 8;
	}
	
	public static class Position {
		public double x, y;
	}
	
	public static class Ship {
		public Position position;
		public int health;
		public int heading;	
	}
	
	private Rectangle bounds;
	private Ship[] ships;
	
	public GameState(int num_ships) {
		ships = new Ship[num_ships];
		
		int width, height, radius;
		
		bounds = new Rectangle(50, 50, 515, 330);
		// left - right
		width = (bounds.width + bounds.x) - bounds.x;
		// top - bottom
		height = (bounds.height + bounds.y) - bounds.y; 
		radius = height / 4;
		
		for(int i = 0; i < ships.length; i++) {
			int heading = i * 360 / ships.length;
			double cost, sint, theta;
			
			theta = (double)heading * GameStateConstants.PI / 180;
			cost = Math.cos(theta);
			sint = Math.sin(theta);
			
			ships[i] = new Ship();
			ships[i].position = new Position();
			ships[i].position.x = (width / 2) + radius * cost;
			ships[i].position.y = (height /  2) + radius * sint;
			ships[i].heading = (heading + 180) % 360;
		}
	}
	
	public Rectangle getBounds() {
		return bounds;
	}
	
	public Ship[] getShips() {
		return ships;
	}

	public void update() {
	}
}
