package apps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import javax.swing.JFrame;
import javax.swing.JPanel;

import apps.GameState.Ship;

public class Renderer extends JFrame {
	private static final long serialVersionUID = 3778876918694849958L;
	private Window window;
	
	public Renderer(int num_players, GameState gs) {
		window = new Window(num_players, gs);
		add(window);
		setResizable(false);
        setTitle("Vector War");
        setSize(640, 480);
        setLocationRelativeTo(null);        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	}
	
	public void update(GameState gs) {
		window.update(gs);
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
		Color.MAGENTA
	};
	
	private final Point[][] shapes;
	
	public Window(int num_players, GameState gs) {
		this.num_players = num_players;
		this.arena = gs.getBounds();
		this.shapes = new Point[num_players][];

		Ship ships[] = gs.getShips();
		for(int i = 0; i < ships.length; i++) {
			this.shapes[i] = new Point[] {
					new Point(Constants.SHIP_RADIUS, 0),
					new Point(-Constants.SHIP_RADIUS, Constants.SHIP_WIDTH),
					new Point((Constants.SHIP_TUCK - Constants.SHIP_RADIUS), 0),
					new Point(-Constants.SHIP_RADIUS, -Constants.SHIP_WIDTH),
					new Point(Constants.SHIP_RADIUS, 0)	
			};
			Ship ship = ships[i];
			if(ship != null) {
				double cost, sint, theta;
				int newx, newy;
				
				theta = (double)ship.heading * Constants.PI / 180;
				cost = Math.cos(theta);
				sint = Math.sin(theta);
				
				for(int j = 0; j < shapes[i].length; j++) {
					newx = (int)(shapes[i][j].x * cost - shapes[i][j].y * sint);
					newy = (int)(shapes[i][j].x * sint + shapes[i][j].y * cost);
					shapes[i][j].x = (int)(newx + ship.pos.x);
					shapes[i][j].y = (int)(newy + ship.pos.y);
				}
			}
		}	
	}
	
	public void update(GameState gs) {
		Ship ships[] = gs.getShips();
		for(int i = 0; i < ships.length; i++) {
			Ship ship = ships[i];
			if(ship != null) {
				double cost, sint, theta;
				int newx, newy;
				
				theta = (double)ship.heading * Constants.PI / 180;
				cost = Math.cos(theta);
				sint = Math.sin(theta);
				
				for(int j = 0; j < shapes[i].length; j++) {
					newx = (int)(shapes[i][j].x * cost - shapes[i][j].y * sint);
					newy = (int)(shapes[i][j].x * sint + shapes[i][j].y * cost);
					shapes[i][j].x = (int)(newx + ship.pos.x);
					shapes[i][j].y = (int)(newy + ship.pos.y);
				}
			}
		}
	}
	
	private void doDrawing(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        RenderingHints rh = new RenderingHints(
        		RenderingHints.KEY_ANTIALIASING,
        		RenderingHints.VALUE_ANTIALIAS_ON);
        
        rh.put(RenderingHints.KEY_RENDERING,
        		RenderingHints.VALUE_RENDER_QUALITY);
        
        g2d.setRenderingHints(rh);

        g2d.setColor(Color.BLACK);
        g2d.drawRect(arena.x, arena.y, arena.width, arena.height);
        
        for(int i = 0; i < num_players; i++) {
	        GeneralPath ship = new GeneralPath();
	    	
	        g2d.setColor(ship_colors[i]);
	        ship.moveTo(shapes[i][0].x, shapes[i][0].y);
	        for (int k = 0; k < shapes[i].length; k++) {
	            ship.lineTo(shapes[i][k].x, shapes[i][k].y);
	        }
		        
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

//package apps;
//
//import java.awt.Color;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Rectangle;
//import java.awt.RenderingHints;
//import java.awt.geom.GeneralPath;
//import java.awt.geom.Point2D;
//
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//
//import apps.GameState.Ship;
//
//public class Renderer extends JFrame {
//	private static final long serialVersionUID = 3778876918694849958L;
//	private Window window;
//	
//	public Renderer(int num_players, Rectangle arena) {
//		window = new Window(num_players, arena);
//		add(window);
//		setResizable(false);
//        setTitle("Vector War");
//        setSize(640, 480);
//        setLocationRelativeTo(null);        
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
//	}
//	
//	public void update(GameState gs) {
//		window.update(gs);
//	}
//}

//class Window extends JPanel {
//	private static final long serialVersionUID = 9128574529773636588L;
//	private final int num_players;
//	private final Rectangle arena;
//	private final Color[] ship_colors = {
//		Color.GREEN,
//		Color.RED,
//		Color.BLUE,
//		Color.PINK
//	};
//	
//	private final Point2D.Double[][] shapes;
//		
//	public Window(int num_players, Rectangle arena) {
//		this.num_players = num_players;
//		this.arena = arena;
//		shapes = new Point2D.Double[num_players][];
//		for(int i = 0; i < shapes.length; i++) {
//			shapes[i] = new Point2D.Double[] {
//				new Point2D.Double(Constants.SHIP_RADIUS, 0),
//				new Point2D.Double(-Constants.SHIP_RADIUS, Constants.SHIP_WIDTH),
//				new Point2D.Double((Constants.SHIP_TUCK - Constants.SHIP_RADIUS), 0),
//				new Point2D.Double(-Constants.SHIP_RADIUS, -Constants.SHIP_WIDTH),
//				new Point2D.Double(Constants.SHIP_RADIUS, 0)
//			};
//		}
//	}
//	
//	public void update(GameState gs) {
//		Ship[] ships = gs.getShips();
//		for(int i = 0; i < ships.length; i++) {
//			double cost, sint, theta;
//			double newX, newY;
//			
//			theta = (double)ships[i].heading * Constants.PI / 180;
//			cost = Math.cos(theta);
//			sint = Math.sin(theta);
//			
//			for(int j = 0; j < shapes.length; j++) {
//				newX = shapes[i][j].x * cost - shapes[i][j].y * sint;
//		        newY = shapes[i][j].x * sint + shapes[i][j].y * cost;
//				shapes[i][j].x = newX + ships[i].pos.x;
//				shapes[i][j].y = newY + ships[i].pos.y;
//			}
//		}	
//	}
//	
//	private void doDrawing(Graphics g) {
//        Graphics2D g2d = (Graphics2D) g;
//        
//        RenderingHints rh = new RenderingHints(
//        		RenderingHints.KEY_ANTIALIASING,
//        		RenderingHints.VALUE_ANTIALIAS_ON);
//        
//        rh.put(RenderingHints.KEY_RENDERING,
//        		RenderingHints.VALUE_RENDER_QUALITY);
//        
//        g2d.setRenderingHints(rh);
//
//        g2d.setColor(Color.BLACK);
//        g2d.drawRect(arena.x, arena.y, arena.width, arena.height);
//        
//        for(int i = 0; i < num_players; i++) {
//	        GeneralPath ship = new GeneralPath();
//	    	
//	        g2d.setColor(ship_colors[i]);
//	        ship.moveTo(shapes[0][0].x, shapes[0][0].y);
//	        for (int j = 0; j < shapes[i].length; j++) {
//	            ship.lineTo(shapes[i][j].x, shapes[i][j].y);
//	        }
//	
//	        ship.closePath();	        
//	        g2d.draw(ship);
//        }
//        g2d.dispose();
//   } 
//
//    @Override
//    public void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        doDrawing(g);
//    }   
//}

