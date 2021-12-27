package app;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

public abstract class AbstractDrawable implements Drawable {
	protected Shape shape;
	protected Color color;
	
	public AbstractDrawable(Shape shape, Color color) {
		setShape(shape);
		setColor(color);
	}
	
	public Shape getShape() {
		return shape;
	}
	
	public void setShape(Shape shape) {
		this.shape = shape;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public abstract void draw(Graphics2D g2d);
}
