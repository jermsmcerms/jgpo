package app;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class Renderer extends JComponent {
	private CopyOnWriteArrayList<Drawable> drawables;
	private String status;
	private RenderingHints rh;
	
	public Renderer() {
		drawables = new CopyOnWriteArrayList<>();
		rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, 
			RenderingHints.VALUE_ANTIALIAS_ON);
	        
        rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
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
        g2d.setRenderingHints(rh);
        
		for(Drawable d : drawables) {
			d.draw(g2d);
		}
		
		drawStatusText(g2d);
		g2d.dispose();
	}

	private void drawStatusText(Graphics2D g2d) {
		g2d.setColor(new Color(0,102,0));
		int status_x = drawables.get(0).getShape().getBounds().width / 2 - 5;
		int status_y = drawables.get(0).getShape().getBounds().height + 45;
		g2d.drawString(status, status_x, status_y);
	}

	public void setStatus(String status) {
		this.status = status;		
	}

	public List<Drawable> getDrawables() {
		return drawables;
	}
}
