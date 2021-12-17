package apps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class DrawableRectangle extends AbstractDrawable {
	public DrawableRectangle(Rectangle bounds, Color color) {
		super(bounds, color);
	}

	@Override
	public void draw(Graphics2D g2d) {
		g2d.setColor(color);
		g2d.draw(shape);		
	}
}
