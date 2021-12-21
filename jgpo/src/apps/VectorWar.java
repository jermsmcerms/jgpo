package apps;

import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import api.JgpoNet.JGPOPlayer;

public class VectorWar {
	private GameState gs;
	private Frame frame;
	
	public enum VectorWarInputs {
		INPUT_THRUST		(1<<0),
		INPUT_BREAK			(1<<1),
		INPUT_ROTATE_LEFT	(1<<2),
		INPUT_ROTATE_RIGHT	(1<<3),
		INPUT_FIRE			(1<<4);
		
		private int input;
		private VectorWarInputs(int input) {
			this.input = input;
		}
		public int getInput() {
			return input;
		}
	}
	
	public VectorWar(int num_players, int local_port, JGPOPlayer players[]) {
		gs = new GameState(num_players);
		
		EventQueue.invokeLater(new Runnable() {
			@Override 
			public void run() {
				try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
				frame = new Frame(gs);
			}
		});
	}

	public void runFrame() {
		if(frame != null) {
			int local_input = frame.getInput();
			gs.update(new int[] {local_input, 0});
			frame.update(gs);
		}
	}
}
