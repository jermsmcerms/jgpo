package app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

public class Ship extends AbstractDrawable {
	public class Position {
		double x, y;
	}
	
	public class Velocity {
		double dx, dy;
	}
	
	public class Bullet {
		public boolean active;
		public Position pos;
		public Velocity vel;
	}
	
	public String connectState;
	public Position pos;
	public Velocity vel;
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
		
		g2d.drawString(connectState, (int)pos.x - Constants.SHIP_RADIUS - 10, (int)pos.y + Constants.SHIP_RADIUS + 5);
		Polygon ship_shape = new Polygon(	
			new int[] {	Constants.SHIP_RADIUS, -Constants.SHIP_RADIUS, 
						(Constants.SHIP_TUCK - Constants.SHIP_RADIUS), 
						-Constants.SHIP_RADIUS, Constants.SHIP_RADIUS },
			
			new int[] { 0, Constants.SHIP_WIDTH, 
						0, -Constants.SHIP_WIDTH, 0 }, 5);
		
		GeneralPath path = new GeneralPath();
		path.moveTo(ship_shape.xpoints[0], ship_shape.ypoints[0]);

		for (int i = 0; i < ship_shape.npoints; i++) {
			double cost, sint, theta;
			int newx, newy;
			
			theta = (double)heading * Constants.PI / 180;
			cost = Math.cos(theta);
			sint = Math.sin(theta);
			newx = (int)(ship_shape.xpoints[i] * cost - ship_shape.ypoints[i] * sint);
			newy = (int)(ship_shape.xpoints[i] * sint + ship_shape.ypoints[i] * cost);

			int new_pos_x = (int)(newx + pos.x);
			int new_pos_y = (int)(newy + pos.y);
			
			ship_shape.xpoints[i] = new_pos_x;
			ship_shape.ypoints[i] = new_pos_y;

			path.lineTo(ship_shape.xpoints[i], ship_shape.ypoints[i]);
        }
		
		g2d.setColor(color);
		g2d.draw(ship_shape);
		
		g2d.setColor(new Color(64, 0, 128));
		for(int i = 0; i < Constants.MAX_BULLETS; i++) {
			if(bullets[i].active) {
				g2d.drawRect((int)bullets[i].pos.x, (int)bullets[i].pos.y, 2, 2);
			}
		}
	}
}
