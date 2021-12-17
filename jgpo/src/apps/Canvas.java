package apps;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public class Canvas extends JComponent {
	private List<Drawable> drawables;
	
	public Canvas() {
		drawables = new ArrayList<>();
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
		
		g2d.dispose();
	}
}
