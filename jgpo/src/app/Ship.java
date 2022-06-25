package app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.io.Serializable;

public class Ship extends AbstractDrawable implements Serializable {
	private static final long serialVersionUID = 2350374769919795873L;

	public class Position implements Serializable {
		private static final long serialVersionUID = 4562201584935370379L;
		double x, y;
		
		public Position() {}
		
		public Position(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
	
	public class Velocity implements Serializable {
		private static final long serialVersionUID = 7030438553376230551L;
		double dx, dy;
		
		public Velocity() {}
		
		public Velocity(double dx, double dy) {
			this.dx = dx;
			this.dy = dy;
		}
	}
	
	public class Bullet implements Serializable {
		private static final long serialVersionUID = -1024579853080748949L;
		public boolean active;
		public Position bulletPosition;
		public Velocity bulletVelocity;
		
		public Bullet() {}
		
		public Bullet(boolean active, Position bulletPosition, Velocity bulletVelocity) {
			this.active = active;
			this.bulletPosition = new Position(bulletPosition.x, bulletPosition.y);
			this.bulletVelocity = new Velocity(bulletVelocity.dx, bulletVelocity.dy);
		}
	}
	
	public String connectState;
	public Position shipPosition;
	public Velocity shipVelocity;
	public Bullet bullets[];
	public int health;
	public int heading;
	public double radius;
	public int cooldown;
	public int score;
	
	public Ship() {
		this(null, null);
	}
	
	public Ship(Shape shape) {
		this(shape, Color.BLACK);
	}
	
	public Ship(Shape shape, Color color) {
		super(shape, color);
		connectState = "default text";
	}
	
	@Override
	public void draw(Graphics2D g2d) {
		g2d.setColor(color);
		
		drawConnectState(g2d);		

		drawShip(g2d);
		drawBullets(g2d);
	}

	private void drawConnectState(Graphics2D g2d) {
		int connectStatePositionX = (int)shipPosition.x - Constants.SHIP_RADIUS - 10;
		int connectStatePositionY = (int)shipPosition.y + Constants.SHIP_RADIUS + 5;
		g2d.drawString(connectState, connectStatePositionX, connectStatePositionY);
	}

	private void drawShip(Graphics2D g2d) {
		Polygon shipShape = new Polygon(	
			new int[] {	Constants.SHIP_RADIUS, -Constants.SHIP_RADIUS, 
				(Constants.SHIP_TUCK - Constants.SHIP_RADIUS), 
				-Constants.SHIP_RADIUS, Constants.SHIP_RADIUS },
			
			new int[] { 0, Constants.SHIP_WIDTH, 0, -Constants.SHIP_WIDTH, 0 }, 5);
		
		GeneralPath path = new GeneralPath();
		path.moveTo(shipShape.xpoints[0], shipShape.ypoints[0]);
		
		updateShipShapePosition(shipShape, path);
		
		g2d.setColor(color);
		g2d.draw(shipShape);
	}

	private void updateShipShapePosition(Polygon shipShape, GeneralPath path) {
		for (int i = 0; i < shipShape.npoints; i++) {
			double cost, sint, theta;
			int newx, newy;
			
			theta = (double)heading * Constants.PI / 180;
			cost = Math.cos(theta);
			sint = Math.sin(theta);
			newx = (int)(shipShape.xpoints[i] * cost - shipShape.ypoints[i] * sint);
			newy = (int)(shipShape.xpoints[i] * sint + shipShape.ypoints[i] * cost);

			int new_pos_x = (int)(newx + shipPosition.x);
			int new_pos_y = (int)(newy + shipPosition.y);
			
			shipShape.xpoints[i] = new_pos_x;
			shipShape.ypoints[i] = new_pos_y;

			path.lineTo(shipShape.xpoints[i], shipShape.ypoints[i]);
        }
	}
	
	private void drawBullets(Graphics2D g2d) {
		g2d.setColor(new Color(64, 0, 128));
		for(int i = 0; i < Constants.MAX_BULLETS; i++) {
			if(bullets[i].active) {
				g2d.drawRect((int)bullets[i].bulletPosition.x, 
					(int)bullets[i].bulletPosition.y, 2, 2);
			}
		}
	}

	public void InitializeShip(Ship ship) {
		this.color = ship.color;
		this.connectState = ship.connectState;
		this.cooldown = ship.cooldown;
		this.heading = ship.heading;
		this.health = ship.health;
		this.radius = ship.radius;
		this.shape = ship.getShape();
		this.shipPosition = new Position(ship.shipPosition.x, ship.shipPosition.y);
		this.shipVelocity = new Velocity(ship.shipVelocity.dx, ship.shipVelocity.dy);
		this.bullets = new Bullet[ship.bullets.length];
		for(int i = 0; i < this.bullets.length; i++) {
			Bullet currentBullet = ship.bullets[i];
			this.bullets[i] = new Bullet(currentBullet.active, 
				currentBullet.bulletPosition, currentBullet.bulletVelocity);
		}
	}
}
