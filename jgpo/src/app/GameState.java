package app;

import java.awt.Rectangle;

import app.Ship.Bullet;
import app.Ship.Position;

public class GameState {
	public int frame_number;
	private enum Inputs {
		INPUT_THRUST		(1<<0),
		INPUT_BREAK			(1<<1),
		INPUT_ROTATE_LEFT	(1<<2),
		INPUT_ROTATE_RIGHT	(1<<3),
		INPUT_FIRE			(1<<4),
		INPUT_BOMB			(1<<5);
		
		private int input;
		private Inputs(int input) {
			this.input = input;
		}
		public int getInput() {
			return input;
		}
	};
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
			ships[i].bullets = new Bullet[Constants.MAX_BULLETS];
			for(int j = 0; j < ships[i].bullets.length; j++) {
				ships[i].bullets[j] = ships[i].new Bullet();
				ships[i].bullets[j].active = false;
				ships[i].bullets[j].pos = ships[i].new Position();
				ships[i].bullets[j].vel = ships[i].new Velocity();
			}
		}
	}
	
	public Rectangle getBounds() {
		return bounds;
	}

	public Ship[] getShips() {
		return ships;
	}
	
	public void update(int[] inputs, int disconnect_flags) {
		frame_number++;
		for(int i = 0; i < inputs.length; i++) {			
			DataObject data = parseInputs(inputs[i], i);
			moveShip(i, data);
			if(ships[i].cooldown > 0) {
				ships[i].cooldown--;
			}
		}
	}
	
	private DataObject parseInputs(int input, int which) {
		Ship ship = ships[which];
		double thrust = 0, heading = 0;
		boolean fire = false;
		
		if((input & Inputs.INPUT_ROTATE_RIGHT.getInput()) != 0) {
			heading = (ship.heading + Constants.ROTATE_INCREMENT) % 360;
		} else if((input & Inputs.INPUT_ROTATE_LEFT.getInput()) != 0) {
			heading = (ship.heading - Constants.ROTATE_INCREMENT + 360) % 360;
		} else {
			heading = ship.heading;
		}

		if((input & Inputs.INPUT_THRUST.getInput()) != 0) {
			thrust = Constants.SHIP_THRUST;
		} else if((input & Inputs.INPUT_BREAK.getInput()) != 0) {
			thrust = -Constants.SHIP_THRUST;
		} else {
			thrust = 0;
		}
//		
		fire = (input & Inputs.INPUT_FIRE.getInput()) != 0 ? true : false;

		DataObject data = new DataObject();
		data.heading = heading;
		data.thrust = thrust;
		data.fire = fire;
		return data;
	}
	
	private void moveShip(int which, DataObject data) {
		Ship ship = ships[which];
		ship.heading = (int)data.heading;
		
		if(ship.cooldown == 0) {
			if(data.fire) {
				for(int i = 0; i < Constants.MAX_BULLETS; i++) {
					double dx = Math.cos(degToRad(ship.heading));
					double dy = Math.sin(degToRad(ship.heading));
					if(!ship.bullets[i].active) {
						ship.bullets[i].active = true;
						ship.bullets[i].pos.x = ship.pos.x + (ship.radius * dx);
						ship.bullets[i].pos.y = ship.pos.y + (ship.radius * dy);
						ship.bullets[i].vel.dx = ship.vel.dx + (Constants.BULLET_SPEED * dx);
						ship.bullets[i].vel.dy = ship.vel.dy + (Constants.BULLET_SPEED * dy);
						ship.cooldown = Constants.BULLET_COOLDOWN;
					}
				}
			}
		}
		
		if(data.thrust != 0) {
			double dx = data.thrust * Math.cos(degToRad(data.heading));
			double dy = data.thrust * Math.sin(degToRad(data.heading));
			
			
			ship.vel.dx += dx;
			ship.vel.dy += dy;
			double mag = Math.sqrt(
				ship.vel.dx * ship.vel.dx +
				ship.vel.dy * ship.vel.dy);
			if(mag > Constants.SHIP_MAX_THRUST) {
				ship.vel.dx = (ship.vel.dx * Constants.SHIP_MAX_THRUST) / mag;
				ship.vel.dy = (ship.vel.dy * Constants.SHIP_MAX_THRUST) / mag;
			}
		}
		
		ship.pos.x += ship.vel.dx;
		ship.pos.y += ship.vel.dy;
		
		// Handle collisions with wall
		if(	ship.pos.x - ship.radius < bounds.x ||
			ship.pos.x - ship.radius > bounds.x + bounds.width) {
			ship.vel.dx *= -1;
			ship.pos.x += ship.vel.dx * 2;
		}
		
		if(	ship.pos.y - ship.radius < bounds.y ||
			ship.pos.y - ship.radius > bounds.y + bounds.height) {
			ship.vel.dy *= -1;
			ship.pos.y += ship.vel.dy * 2;
		}
		
		for(int i = 0; i < Constants.MAX_BULLETS; i++) {
			Bullet bullet = ship.bullets[i];
			if(bullet.active) {
				bullet.pos.x += bullet.vel.dx;
				bullet.pos.y += bullet.vel.dy;
				if(	bullet.pos.x < bounds.x || 
					bullet.pos.x > bounds.x + bounds.width ||
					bullet.pos.y < bounds.y || 
					bullet.pos.y > bounds.y + bounds.height) {
					bullet.active = false;
				} else {
					for(int j = 0; j < ships.length; j++) {
						Ship other = ships[j];
						if(distance(bullet.pos, other.pos) < other.radius) {
							ship.score++;
							other.health -= Constants.BULLET_DAMAGE;
							bullet.active = false;
						}
					}
				}
			}
		}
	}
	
	private double degToRad(double deg) {
		return Constants.PI * deg / 180;
	}
	
	private double distance(Position lhs, Position rhs) {
		double x = rhs.x - lhs.x;
		double y = rhs.y - lhs.y;
		return Math.sqrt(x*x + y*y);
	}
}

class DataObject {
	public double thrust;
	public double heading;
	boolean fire;
}
