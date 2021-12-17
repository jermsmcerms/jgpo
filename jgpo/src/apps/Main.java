package apps;

import javax.swing.JOptionPane;

public class Main {
	private static final int MAX_PLAYERS = 4;
	
	public Main(int num_players) {
		new VectorWar(num_players);
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
