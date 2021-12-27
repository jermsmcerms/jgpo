package app;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

@SuppressWarnings("serial")
class GameWindow extends JPanel {
	private final Color[] ship_colors = {
		Color.GREEN,
		Color.RED,
		Color.BLUE,
		Color.MAGENTA
	};
							
	private final Renderer renderer;
	private Ship ships[];
	
	public GameWindow(GameState gs) {
		setLayout(new BorderLayout());
		renderer = new Renderer();
		add(renderer);
		
		renderer.add(new DrawableRectangle(gs.getBounds(), Color.BLACK));
		
		ships = gs.getShips();
		for(int i = 0; i < ships.length; i++) {
			ships[i].setColor(ship_colors[i]);
			renderer.add(ships[i]);
		}
	}
	
	public Renderer getRenderer() {
		return renderer;
	}
}