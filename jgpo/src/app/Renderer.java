package app;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class Renderer extends JComponent {
	private List<Drawable> drawables;
	private String status;
	
	public Renderer() {
		drawables = new ArrayList<>();
	}
	
	public Drawable getDrawableAt(int index) {
		return drawables.get(index);
	}
	
	public void add(Drawable drawable) {
		drawables.add(drawable);
		repaint();
	}
	
	public void remove(Drawable drawable) {
		drawables.remove(drawable);
		repaint();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g.create();
		RenderingHints rh = new RenderingHints(
    		RenderingHints.KEY_ANTIALIASING,
    		RenderingHints.VALUE_ANTIALIAS_ON);
        
        rh.put(RenderingHints.KEY_RENDERING,
    		RenderingHints.VALUE_RENDER_QUALITY);
        
        g2d.setRenderingHints(rh);
        
		for(Drawable d : drawables) {
			d.draw(g2d);
		}
		
		g2d.setColor(new Color(0,102,0));
		int status_x = drawables.get(0).getShape().getBounds().width / 2 - 5;
		int status_y = drawables.get(0).getShape().getBounds().height + 45;
		g2d.drawString(status, status_x, status_y);
		g2d.dispose();
	}

	public void setStatus(String status) {
		this.status = status;		
	}
}
