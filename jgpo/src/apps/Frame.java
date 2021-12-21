package apps;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Frame extends JFrame {
	private GameWindow gameWindow;
	private KeyboardInput keyboardInput;

	public Frame(GameState gs) {
		gameWindow = new GameWindow(gs);
		keyboardInput = new KeyboardInput();
		add(gameWindow);
		addKeyListener(keyboardInput);
		pack();
		setResizable(false);
        setTitle("Vector War");
        setSize(640, 480);
        setLocationRelativeTo(null);        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
	}
	
	public void update(GameState gs) {
		gameWindow.getRenderer().repaint();	
	}
	
	public int getInput() {
		int input = 0;
		for(int i = 0; i < keyboardInput.getMultiDown().length; i++) {
			input += keyboardInput.getMultiDown()[i] ? (1 << i) : 0;
		}
		return input;
	}
}

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
