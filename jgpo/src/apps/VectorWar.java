package apps;

import java.awt.EventQueue;

public class VectorWar {
	private GameState gs;
	private Renderer renderer;
	
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
	
	public VectorWar(int num_players) {
		gs = new GameState(num_players);
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				renderer = new Renderer(num_players, gs.getBounds());
				renderer.setVisible(true);
			}
		});
	}

	public void runFrame() {
		advanceFrame(new int[] {4,1}, 0);
		drawCurrentFrame();
	}

	private void drawCurrentFrame() {
		
	}

	private void advanceFrame(int inputs[], int disconnect_flags) {
		
	}
}
