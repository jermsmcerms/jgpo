package app;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardInput implements KeyListener {
	private static final int NUM_INPUTS = 6;
	private boolean pressed = false;
	private boolean[] multiDown;
	
	public KeyboardInput() {
		multiDown = new boolean[NUM_INPUTS];
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.VK_UP:
			multiDown[0] = true;
			break;
		case KeyEvent.VK_DOWN:
			multiDown[1] = true;
			break;
		case KeyEvent.VK_LEFT:
			multiDown[2] = true;
			break;
		case KeyEvent.VK_RIGHT:
			multiDown[3] = true;
			break;
		case KeyEvent.VK_A:
			multiDown[4] = true;
			break;
		case KeyEvent.VK_D:
			multiDown[5] = true;
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.VK_UP:
			multiDown[0] = false;
			break;
		case KeyEvent.VK_DOWN:
			multiDown[1] = false;
			break;
		case KeyEvent.VK_LEFT:
			multiDown[2] = false;
			break;
		case KeyEvent.VK_RIGHT:
			multiDown[3] = false;
			break;
		case KeyEvent.VK_A:
			multiDown[4] = false;
			break;
		case KeyEvent.VK_D:
			multiDown[5] = false ;
			break;
		}
	}

	public boolean getPressed() {
		return pressed;
	}
	
	public boolean[] getMultiDown() {
		return multiDown;
	}

}
