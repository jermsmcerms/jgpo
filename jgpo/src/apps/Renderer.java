package apps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Renderer extends JFrame {
	private Window window;
	
	public Renderer(GameState gs) {
		window = new Window(gs);
		add(window);
		pack();
		setResizable(false);
        setTitle("Vector War");
        setSize(640, 480);
        setLocationRelativeTo(null);        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
	}
}

@SuppressWarnings("serial")
class Window extends JPanel {
	private final Color[] ship_colors = {
		Color.GREEN,
		Color.RED,
		Color.BLUE,
		Color.MAGENTA
	};
	
	private final Shape ship_shape;
						
	private final Canvas canvas;
	private Ship ships[];
	
	public Window(GameState gs) {
		setLayout(new BorderLayout());
		canvas = new Canvas();
		add(canvas);
		
		canvas.add(new DrawableRectangle(gs.getBounds(), Color.BLACK));
		
		ship_shape = new Polygon(
			new int[] {	Constants.SHIP_RADIUS, -Constants.SHIP_RADIUS, 
						(Constants.SHIP_TUCK - Constants.SHIP_RADIUS), 
						-Constants.SHIP_RADIUS, Constants.SHIP_RADIUS },
			// y-points
			new int[] { 0, Constants.SHIP_WIDTH, 
						0, -Constants.SHIP_WIDTH, 0 }, 5);
		
		ships = gs.getShips();
		for(int i = 0; i < ships.length; i++) {
			ships[i].setShape(ship_shape);
			ships[i].setColor(ship_colors[i]);
			canvas.add(ships[i]);
		}
	}
}
