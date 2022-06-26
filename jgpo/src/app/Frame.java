package app;

import javax.swing.JFrame;

import api.JgpoNet.JGPOPlayerType;
import app.NonGameState.PlayerConnectionInfo;

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
	
	public void update(GameState gs, NonGameState ngs) {
		Ship[] ships = gs.getShips();
		
		PlayerConnectionInfo[] players = ngs.players;
		
		for(int i = 0; i < ships.length; i++) {
			switch(players[i].state) {
				case Connecting :
					ships[i].connectState = (players[i].type == JGPOPlayerType.JGPO_PLAYERTYPE_LOCAL) ? "Local player: " : "Connecting...";
					break;
				case Synchronizing :
					ships[i].connectState = "Synchronizing";
					break;
				case Disconnecting :
					ships[i].connectState = "Waiting for player...";
				case Disconnected :
					ships[i].connectState = "Disconnected";
					break;
				case Running:
					ships[i].connectState = "";
					gameWindow.getRenderer().setStatus("");
					break;
			}
		}
		
		gameWindow.getRenderer().repaint();	
	}
	
	public int getInput() {
		int input = 0;
		for(int i = 0; i < keyboardInput.getMultiDown().length; i++) {
			input += keyboardInput.getMultiDown()[i] ? (1 << i) : 0;
		}
		return input;
	}

	public void setStatusText(String status) {
		gameWindow.getRenderer().setStatus(status);
	}
	
	public void updateShips(Ship[] ships) {
		gameWindow.updateShips(ships);
	}
}
