package apps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import apps.GameState.GameStateConstants;

public class Renderer extends JFrame {
	private static final long serialVersionUID = 3778876918694849958L;
	private Window window;
	
	public Renderer(int num_players, Rectangle arena) {
		window = new Window(num_players, arena);
		add(window);
		setResizable(false);
        setTitle("Vector War");
        setSize(640, 480);
        setLocationRelativeTo(null);        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	}
	
	public void update(GameState gs, NonGameState ngs) {
		window.update(gs, ngs);
	}
}

class Window extends JPanel {
	private static final long serialVersionUID = 9128574529773636588L;
	private final int num_players;
	private final Rectangle arena;
	private final Color[] ship_colors = {
		Color.GREEN,
		Color.RED,
		Color.BLUE,
		Color.PINK
	};
	private final int offset = 64;
	private final Point2D.Double[] shape = { 
		new Point2D.Double(GameStateConstants.SHIP_RADIUS, 0),
		new Point2D.Double(-GameStateConstants.SHIP_RADIUS, GameStateConstants.SHIP_WIDTH),
		new Point2D.Double((GameStateConstants.SHIP_TUCK - GameStateConstants.SHIP_RADIUS), 0),
		new Point2D.Double(-GameStateConstants.SHIP_RADIUS, -GameStateConstants.SHIP_WIDTH),
		new Point2D.Double(GameStateConstants.SHIP_RADIUS, 0), 
    };
	
	public Window(int num_players, Rectangle arena) {
		this.num_players = num_players;
		this.arena = arena;
	}
	
	public void update(GameState gs, NonGameState ngs) {
		for(GameState.Ship ship : gs.getShips()) {
			double cost, sint, theta;
			double newX, newY;
		
			theta = (double)ship.heading * GameStateConstants.PI / 180;
			cost = Math.cos(theta);
			sint = Math.sin(theta);
			
			for(int i = 0; i < shape.length; i++) {
				newX = shape[i].x * cost - shape[i].y * sint;
				newY = shape[i].x * sint + shape[i].y * cost;
				shape[i].x = newX + ship.position.x;
				shape[i].y = newY + ship.position.y;
			}
		}
	} 
	
	private void doDrawing(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK);
        g2d.drawRect(arena.x, arena.y, arena.width, arena.height);
        
        for(int i = 0; i < num_players; i++) {
	        g2d.setPaint(ship_colors[i]);
	
	        RenderingHints rh = new RenderingHints(
	                RenderingHints.KEY_ANTIALIASING,
	                RenderingHints.VALUE_ANTIALIAS_ON);
	
	        rh.put(RenderingHints.KEY_RENDERING,
	               RenderingHints.VALUE_RENDER_QUALITY);
	
	        g2d.setRenderingHints(rh);
	
	        g2d.translate(25, 5);
	
	        GeneralPath ship = new GeneralPath();
	
	        ship.moveTo(shape[0].x + offset, shape[0].y + offset);
	
	        for (int k = 1; k < shape.length; k++)
	            ship.lineTo(shape[k].x + offset, shape[k].y + offset);
	
	        ship.closePath();
	        g2d.draw(ship);
        }
        g2d.dispose();
   } 

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }   
}
