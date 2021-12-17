package apps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

public class Ship extends AbstractDrawable{
	public class Position {
		double x, y;
	}
	
	public class Velocity {
		double dx, dy;
	}
	
	public Position pos;
	public Velocity vel;
	public int health;
	public int heading;
	public double radius;
	
	public Ship() {
		this(new Polygon(	
			new int[] {	Constants.SHIP_RADIUS, -Constants.SHIP_RADIUS, 
						(Constants.SHIP_TUCK - Constants.SHIP_RADIUS), 
						-Constants.SHIP_RADIUS, Constants.SHIP_RADIUS },
			
			new int[] { 0, Constants.SHIP_WIDTH, 
						0, -Constants.SHIP_WIDTH, 0 }, 5), null);
	}
	
	public Ship(Shape shape) {
		this(shape, Color.BLACK);
	}
	
	public Ship(Shape shape, Color color) {
		super(shape, color);
	}
	
	@Override
	public void draw(Graphics2D g2d) {
		Polygon ship_shape = (Polygon)shape;
		GeneralPath path = new GeneralPath();
		path.moveTo(pos.x, pos.y);
		
		for (int i = 0; i < ship_shape.npoints; i++) {
			double cost, sint, theta;
			int newx, newy;
			
			theta = (double)heading * Constants.PI / 180;
			cost = Math.cos(theta);
			sint = Math.sin(theta);
			newx = (int)(ship_shape.xpoints[i] * cost - ship_shape.ypoints[i] * sint);
			newy = (int)(ship_shape.xpoints[i] * sint + ship_shape.ypoints[i] * cost);
			
			ship_shape.xpoints[i] = (int)(newx + pos.x);
			ship_shape.ypoints[i] = (int)(newy + pos.y);
						
            path.lineTo(ship_shape.xpoints[i], ship_shape.ypoints[i]);
        }   
		
		g2d.setColor(color);
		g2d.draw(ship_shape);
	}
}
