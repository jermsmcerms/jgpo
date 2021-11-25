package apps;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main extends JFrame {
	private static final long serialVersionUID = -1183229478321125258L;
	private static final int MAX_PLAYERS = 4;
	private final VectorWar vector_war;
	
	public Main(int num_players) {
		vector_war = new VectorWar(num_players);
		long now = System.currentTimeMillis();
		long next = now;
		
		while(true) {
			now = System.currentTimeMillis();
			if(now >= next) {
				vector_war.runFrame();
				next = now + (1000 / 60);
			}
		}
    }

    public static void main(String[] args) {
    	if(args.length < 1) {
    		syntax();
    	}
    	
    	try {
    		int num_players = Integer.parseInt(args[0]);
    		if(num_players < 1 || num_players > MAX_PLAYERS) {
    			syntax();
    		}
    		
    		new Main(num_players);
    	} catch (IllegalArgumentException e) {
    		syntax();
    	}
    	
    }
    
    private static void syntax() {
    	String syntaxMsg = "arguments format must follow: <num players> <local port> ('local' | <remote ip>:<remote port>)";
        JOptionPane.showMessageDialog(null, syntaxMsg, "InfoBox: Vector War", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }
}
