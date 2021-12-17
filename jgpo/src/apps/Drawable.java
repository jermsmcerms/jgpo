package apps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

public interface Drawable {
	public Shape getShape();
	public void setShape(Shape shape);
	public Color getColor();
	public void setColor(Color color);
	public void draw(Graphics2D g2d);
}
