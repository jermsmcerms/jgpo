package lib.utils;

import javax.swing.JFrame;

import api.JgpoNet.JGPOPlayerHandle;
import lib.backend.JGPOSession;

public class PerformanceMonitor {
	JFrame frame;
	
	public PerformanceMonitor() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setTitle("Performance Monitor");
		frame.setSize(400, 400);
		frame.setLocationRelativeTo(null);        
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(false);
	}
	
	public void toggleView() {
		if(!frame.isVisible()) {
			frame.setVisible(true);
		} else {
			frame.setVisible(false);
		}
	}

	public void update(JGPOSession session, JGPOPlayerHandle[] handles) {
		
	}
}
