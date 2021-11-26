package apps;

import java.awt.Rectangle;

public class GameState {
	public static class GameStateConstants {
		public static final double PI = 3.1415926;
		public static final double SHIP_THRUST = 0.06;
		public static final double MAX_SHIP_THRUST = 4.0;
		
		public static final int SHIP_RADIUS = 15;
		public static final int SHIP_TUCK = 3;
		public static final int SHIP_WIDTH = 8;
		public static final int STARTING_HEALTH = 100;
		public static final int MAX_BULLETS = 30;
		public static final int BULLET_COOLDOWN = 8;
		public static final int ROTATE_INCREMENT = 3;
		public static final int BULLET_SPEED = 5;
		public static final int BULLET_DAMAGE = 10;
	}
	
	public static class Position {
		public double x, y;
	}
	
	public static class Velocity {
		public double dx, dy;
	}
	
	public static class Bullet {
		public boolean active;
		public Position position;
		public Velocity velocity;
	}
	
	public static class Ship {
		public Position position;
		public Velocity velocity;
		public Bullet[] bullets;
		public int health;
		public int heading;	
		public int radius;
		public int cooldown;
		public int score;
	}
	
	private Rectangle bounds;
	private Ship[] ships;
	
	public GameState(int num_ships) {
		ships = new Ship[num_ships];
		
		int radius;
		
		bounds = new Rectangle(50, 50, 515, 330);
		radius = bounds.height / 4;
		
		for(int i = 0; i < ships.length; i++) {
			int heading = i * 360 / ships.length;
			double cost, sint, theta;
			
			theta = (double)heading * GameStateConstants.PI / 180;
			cost = Math.cos(theta);
			sint = Math.sin(theta);
			
			ships[i] = new Ship();
			ships[i].position = new Position();
			ships[i].position.x = (bounds.width  / 2) + radius * cost;
			ships[i].position.y = (bounds.height / 2) + radius * sint;
			
			ships[i].velocity = new Velocity();
			
			ships[i].bullets = new Bullet[GameStateConstants.MAX_BULLETS];
			for(int j = 0; j < ships[i].bullets.length; j++) {
				ships[i].bullets[j] = new Bullet();
				ships[i].bullets[j].active = false;
				ships[i].bullets[j].position = new Position();
				ships[i].bullets[j].velocity = new Velocity();
			}
			ships[i].heading = (heading + 180) % 360;
			ships[i].health = GameStateConstants.STARTING_HEALTH;
			ships[i].radius = GameStateConstants.SHIP_RADIUS;
		}
	}
	
	public Rectangle getBounds() {
		return bounds;
	}
	
	public Ship[] getShips() {
		return ships;
	}

	public void update(int[] inputs, int disconnect_flags) {
		for(int i = 0; i < ships.length; i++) {
			DataObject object = null;
			if((disconnect_flags & (1 << i)) != 0) {
				getShipAi(i);
			} else {
				object = parseShipInputs(inputs[i], i);
			}
			moveShip(i, object);
			
			if(ships[i].cooldown > 0) {
				ships[i].cooldown--;
			}
		}
	}
	
	private void getShipAi(int i) {
		
	}
	
	private DataObject parseShipInputs(int input, int i) {
		Ship ship = ships[i];
		double thrust = 0.0;
		double heading = 0.0;
		boolean fire = false;
				
		if((input & VectorWar.VectorWarInputs.INPUT_ROTATE_RIGHT.ordinal()) != 0) {
			heading = ship.heading % 360;
		} else if((input & VectorWar.VectorWarInputs.INPUT_ROTATE_LEFT.ordinal()) != 0) {
			heading = (ship.heading - GameStateConstants.ROTATE_INCREMENT + 360) % 360;
		} else {
			heading = ship.heading;
		}
		
		if((input & VectorWar.VectorWarInputs.INPUT_THRUST.ordinal()) != 0) {
			thrust = GameStateConstants.SHIP_THRUST;
		} else if((input & VectorWar.VectorWarInputs.INPUT_BREAK.ordinal()) != 0) {
			thrust = -GameStateConstants.SHIP_THRUST;
		} else {
			thrust = 0;
		}
		
		fire = (input & VectorWar.VectorWarInputs.INPUT_FIRE.ordinal()) != 0;
		return new DataObject(heading, thrust, fire);
	}
	
	private void moveShip(int which, DataObject object) {
		Ship ship = ships[which];
		ship.heading = (int)object.heading;
		
		if(ship.cooldown == 0) {
			if(object.fire) {
				for(int i = 0; i < ship.bullets.length; i++) {
					double dx = Math.cos(degToRad(ship.heading));
					double dy = Math.sin(degToRad(ship.heading));
					
					if(!ship.bullets[i].active) {
						ship.bullets[i].active = true;
						if(ship.bullets[i].position != null) {
							ship.bullets[i].position.x = ship.position.x + ship.radius * dx;
							ship.bullets[i].position.x = ship.position.y + ship.radius * dy;
						}
						
						if(ship.bullets[i].velocity != null) {
							ship.bullets[i].velocity.dx = ship.velocity.dx + (GameStateConstants.BULLET_SPEED * dx);
							ship.bullets[i].velocity.dy = ship.velocity.dy + (GameStateConstants.BULLET_SPEED * dy);
							ship.cooldown = GameStateConstants.BULLET_COOLDOWN;
						}
					}
				}
			}
		}
		
		if(object.thrust > 0) {
			double dx = object.thrust * Math.cos(degToRad(object.heading));
			double dy = object.thrust * Math.sin(degToRad(object.heading));
	
			ship.velocity.dx += dx;
			ship.velocity.dy += dy;
	      
			double mag = Math.sqrt(ship.velocity.dx * ship.velocity.dx + 
								   ship.velocity.dy  * ship.velocity.dy);
	      
			if (mag > GameStateConstants.MAX_SHIP_THRUST) {
				ship.velocity.dx = (ship.velocity.dx * GameStateConstants.MAX_SHIP_THRUST) / mag;
				ship.velocity.dy = (ship.velocity.dy * GameStateConstants.MAX_SHIP_THRUST) / mag;
			}
		}
	
		ship.position.x += ship.velocity.dx;
		ship.position.y += ship.velocity.dy;
	
		if (ship.position.x - ship.radius < bounds.x || 
			ship.position.x + ship.radius > bounds.width) {
			ship.velocity.dx *= -1;
			ship.position.x += (ship.velocity.dx * 2);
		}
	
		if (ship.position.y - ship.radius < bounds.y || 
			ship.position.y + ship.radius > bounds.height) {
			ship.velocity.dy *= -1;
			ship.position.y += (ship.velocity.dy * 2);
		}
	
		for (int i = 0; i < GameStateConstants.MAX_BULLETS; i++) {
			Bullet bullet = ship.bullets[i];
			if (bullet.active) {
				bullet.position.x += bullet.velocity.dx;
				bullet.position.y += bullet.velocity.dy;
				if (bullet.position.x < bounds.x ||
					bullet.position.y < bounds.y ||
					bullet.position.x > bounds.x + bounds.width ||
					bullet.position.y > bounds.y + bounds.height) {
					bullet.active = false;
				} else {
					for (int j = 0; j < ships.length; j++) {
						Ship other = ships[j];
						if (distance(bullet.position, other.position) < other.radius) {
							ship.score++;
							other.health -= GameStateConstants.BULLET_DAMAGE;
							bullet.active = false;
							break;
						}
					}
				}
			}
		}
	}
	
	private double degToRad(double deg) {
		return GameStateConstants.PI * deg / 180;
	}
	
	private double distance(Position lhs, Position rhs) {
		double x = rhs.x - lhs.x;
		double y = rhs.y - lhs.y;
		return Math.sqrt(x*x + y*y);
	}
}

class DataObject {
	public double heading;
	public double thrust;
	public boolean fire;
	
	public DataObject(double heading, double thrust, boolean fire) {
		this.heading = heading;
		this.thrust = thrust;
		this.fire = fire;
	}
}
