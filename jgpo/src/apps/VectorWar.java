package apps;

import java.awt.EventQueue;

public class VectorWar {
	private GameState gs;
	private NonGameState ngs;
	private Renderer renderer;
	
	public VectorWar(int num_players) {
		gs = new GameState(num_players);
		ngs = new NonGameState();
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				renderer = new Renderer(num_players, gs.getBounds());
				renderer.setVisible(true);
			}
		});
	}

	public void runFrame() {
		advanceFrame();
		drawCurrentFrame();
	}

	private void drawCurrentFrame() {
		if(gs != null && ngs != null && renderer != null) {
			renderer.update(gs, ngs);
		}
	}

	private void advanceFrame() {
		if(gs != null) {
			gs.update();
		}
	}
}
