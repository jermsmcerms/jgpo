package app;

import java.awt.Rectangle;

import app.Ship.Bullet;
import app.Ship.Position;
import app.VectorWar.VectorWarInputs;

public class GameState {
	public int frameNumber;
	
	private Rectangle arenaBoundary;
	private Ship[] ships;
	
	public GameState(int numShips) {
		// TODO: change this to values that are proportional and relative to the size of the frame.
		arenaBoundary = new Rectangle(50, 50, 515, 330);
		ships = new Ship[numShips];
		
		initializeShips(numShips);
	}
	
	public Rectangle getBounds() {
		return arenaBoundary;
	}

	public Ship[] getShips() {
		return ships;
	}
	
	public void update(int[] inputs, int disconnectFlags) {
		frameNumber++;
		for(int i = 0; i < inputs.length; i++) {			
			ShipActionData ShipActionData = parseInputs(inputs[i], i);
			moveShip(i, ShipActionData);
			if(ships[i].cooldown > 0) {
				ships[i].cooldown--;
			}
		}
	}
	
	private void initializeShips(int numShips) {
		for(int i = 0; i < ships.length; i++) {
			ships[i] = new Ship();
			ships[i].shipPosition = ships[i].new Position();
			ships[i].shipVelocity = ships[i].new Velocity();
			ships[i].health = Constants.STARTING_HEALTH;
			
			int heading = i * 360 / numShips;
			double cost, sint, theta;
			
			theta = (double)heading * Constants.PI / 180;
			cost = Math.cos(theta);
			sint = Math.sin(theta);
			
			int radius = arenaBoundary.height / 4;
			ships[i].shipPosition.x = arenaBoundary.x + arenaBoundary.width / 2 + radius * cost;
			ships[i].shipPosition.y = arenaBoundary.y + arenaBoundary.height / 2 + radius * sint;
			ships[i].heading = (heading + 180) % 360;
			ships[i].radius = Constants.SHIP_RADIUS;
			ships[i].bullets = new Bullet[Constants.MAX_BULLETS];
			initializeBullets(i);
		}
	}

	private void initializeBullets(int shipIndex) {
		for(int j = 0; j < ships[shipIndex].bullets.length; j++) {
			ships[shipIndex].bullets[j] = ships[shipIndex].new Bullet();
			ships[shipIndex].bullets[j].active = false;
			ships[shipIndex].bullets[j].bulletPosition = ships[shipIndex].new Position();
			ships[shipIndex].bullets[j].bulletVelocity = ships[shipIndex].new Velocity();
		}
	}

	private ShipActionData parseInputs(int input, int which) {
		Ship ship = ships[which];
		double thrust = 0, heading = 0;
		boolean fire = false;
		
		heading = updateHeading(input, ship);
		thrust = updateThrust(input);
		fire = (input & VectorWarInputs.FIRE.getInput()) != 0 ? true : false;

		return new ShipActionData(heading, thrust, fire);
	}

	private double updateThrust(int input) {
		double thrust;
		if((input & VectorWarInputs.THRUST.getInput()) != 0) {
			thrust = Constants.SHIP_THRUST;
		} else if((input & VectorWarInputs.BREAK.getInput()) != 0) {
			thrust = -Constants.SHIP_THRUST;
		} else {
			thrust = 0;
		}
		return thrust;
	}

	private double updateHeading(int input, Ship ship) {
		double heading;
		if((input & VectorWarInputs.ROTATE_RIGHT.getInput()) != 0) {
			heading = (ship.heading + Constants.ROTATE_INCREMENT) % 360;
		} else if((input & VectorWarInputs.ROTATE_LEFT.getInput()) != 0) {
			heading = (ship.heading - Constants.ROTATE_INCREMENT + 360) % 360;
		} else {
			heading = ship.heading;
		}
		return heading;
	}
	
	private void moveShip(int whichShip, ShipActionData data) {
		Ship ship = ships[whichShip];
		ship.heading = (int)data.heading;
		
		if(ship.cooldown == 0 && data.canFire) {
			fireBullet(ship);
		}
		
		if(data.thrust != 0) {
			updateVeclocity(data, ship);
		}

		ship.shipPosition.x += ship.shipVelocity.dx;
		ship.shipPosition.y += ship.shipVelocity.dy;
		
		keepShipInBounds(ship);
		
		handleBulletCollisions(ship);
	}

	private void handleBulletCollisions(Ship ship) {
		for(int i = 0; i < Constants.MAX_BULLETS; i++) {
			Bullet bullet = ship.bullets[i];
			if(bullet.active) {
				bullet.bulletPosition.x += bullet.bulletVelocity.dx;
				bullet.bulletPosition.y += bullet.bulletVelocity.dy;
				if(	bullet.bulletPosition.x < arenaBoundary.x || 
					bullet.bulletPosition.x > arenaBoundary.x + arenaBoundary.width ||
					bullet.bulletPosition.y < arenaBoundary.y || 
					bullet.bulletPosition.y > arenaBoundary.y + arenaBoundary.height) {
					bullet.active = false;
				} else {
					for(int j = 0; j < ships.length; j++) {
						Ship other = ships[j];
						if(distance(bullet.bulletPosition, other.shipPosition) < other.radius) {
							ship.score++;
							other.health -= Constants.BULLET_DAMAGE;
							bullet.active = false;
						}
					}
				}
			}
		}
	}

	private void keepShipInBounds(Ship ship) {
		if(	ship.shipPosition.x - ship.radius < arenaBoundary.x ||
			ship.shipPosition.x - ship.radius > arenaBoundary.x + arenaBoundary.width) {
			ship.shipVelocity.dx *= -1;
			ship.shipPosition.x += ship.shipVelocity.dx * 2;
		}
		
		if(	ship.shipPosition.y - ship.radius < arenaBoundary.y ||
			ship.shipPosition.y - ship.radius > arenaBoundary.y + arenaBoundary.height) {
			ship.shipVelocity.dy *= -1;
			ship.shipPosition.y += ship.shipVelocity.dy * 2;
		}
	}

	private void updateVeclocity(ShipActionData data, Ship ship) {
		double dx = data.thrust * Math.cos(degToRad(data.heading));
		double dy = data.thrust * Math.sin(degToRad(data.heading));
		
		
		ship.shipVelocity.dx += dx;
		ship.shipVelocity.dy += dy;
		double mag = Math.sqrt(
			ship.shipVelocity.dx * ship.shipVelocity.dx +
			ship.shipVelocity.dy * ship.shipVelocity.dy);
		if(mag > Constants.SHIP_MAX_THRUST) {
			ship.shipVelocity.dx = (ship.shipVelocity.dx * Constants.SHIP_MAX_THRUST) / mag;
			ship.shipVelocity.dy = (ship.shipVelocity.dy * Constants.SHIP_MAX_THRUST) / mag;
		}
	}

	private void fireBullet(Ship ship) {
		for(int i = 0; i < Constants.MAX_BULLETS; i++) {
			double dx = Math.cos(degToRad(ship.heading));
			double dy = Math.sin(degToRad(ship.heading));
			updateActiveBullets(ship, i, dx, dy);
		}
	}

	private void updateActiveBullets(Ship ship, int whcihBullet, double dx, double dy) {
		if(!ship.bullets[whcihBullet].active) {
			ship.bullets[whcihBullet].active = true;
			ship.bullets[whcihBullet].bulletPosition.x = ship.shipPosition.x + (ship.radius * dx);
			ship.bullets[whcihBullet].bulletPosition.y = ship.shipPosition.y + (ship.radius * dy);
			ship.bullets[whcihBullet].bulletVelocity.dx = ship.shipVelocity.dx + (Constants.BULLET_SPEED * dx);
			ship.bullets[whcihBullet].bulletVelocity.dy = ship.shipVelocity.dy + (Constants.BULLET_SPEED * dy);
			ship.cooldown = Constants.BULLET_COOLDOWN;
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
