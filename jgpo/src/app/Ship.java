package app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.io.Serializable;

public class Ship extends AbstractDrawable implements Serializable {
	public class Position implements Serializable {
		double x, y;
	}
	
	public class Velocity implements Serializable {
		double dx, dy;
	}
	
	public class Bullet implements Serializable {
		public boolean active;
		public Position bulletPosition;
		public Velocity bulletVelocity;
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
				g2d.drawRect((int)bullets[i].bulletPosition.x, (int)bullets[i].bulletPosition.y, 2, 2);
			}
		}
	}
}
