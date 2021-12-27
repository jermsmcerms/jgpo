package app;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

import api.JgpoNet;
import api.JgpoNet.JGPOPlayer;

public class Main {
	private static final int MAX_PLAYERS = 4;
	private static VectorWar vectorWar;

    public static void main(String[] args) {
    	int offset = 0;
    	if(args.length < 4) {
    		syntax();
    	}
    	
    	try {
    		int local_port = Integer.parseInt(args[offset++]);
    		int num_players = Integer.parseInt(args[offset++]);
    		
    		if(num_players < 1 || num_players > MAX_PLAYERS) {
    			syntax();
    		}
    		
    		if("spectate".equals(args[offset])) {
    			System.out.println("create spectator");
    		} else {		
	    		JGPOPlayer players[] = new JGPOPlayer[JgpoNet.JGPO_MAX_SPECTATORS + 
	    		                                      JgpoNet.JGPO_MAX_PLAYERS];
	    		
	    		for(int i = 0; i < num_players; i++) {
	    			players[i] = new JGPOPlayer();
	    			players[i].playerNum = i + 1;
	    			String playerTypeArgument = args[offset++];
	    			if("local".equals(playerTypeArgument)) {
	    				players[i].type = JgpoNet.JGPOPlayerType.JGPO_PLAYERTYPE_LOCAL;
	    				continue;
	    			}
	    			
	    			
	    			players[i].type = JgpoNet.JGPOPlayerType.JGPO_PLAYERTYPE_REMOTE;
	    			List<String> splitByColon = Stream.of(playerTypeArgument)
	    		            .map(w -> w.split(":")).flatMap(Arrays::stream)
	    		            .collect(Collectors.toList());
	    			if(splitByColon.size() != 2) {
	    				System.out.println(splitByColon);
	    				syntax();
	    			}
	    			players[i].ipAddress = splitByColon.get(0);
	    			players[i].port = Integer.parseInt(splitByColon.get(1));
	    		}
	    		
	    		// TODO: get the spectator stuff here
	    		vectorWar = new VectorWar(num_players, local_port, players, 0);
    		}
    		
    		runMainLoop();
    		// End of game
    	} catch (IllegalArgumentException e) {
    		syntax();
    	}
    	
    }
    
    private static void runMainLoop() {
    	long now = System.currentTimeMillis();
		long next = now;
		
		while(true) {
			now = System.currentTimeMillis();
			vectorWar.idle(Math.max(0, next - now - 1));
			if(now >= next) {
				vectorWar.runFrame();
				next = now + (1000 / 60);
			}
		}		
	}
    
	private static void syntax() {
    	String syntaxMsg = "arguments format must follow: <num players> <local port> ('local' | <remote ip>:<remote port>)";
        JOptionPane.showMessageDialog(null, syntaxMsg, "InfoBox: Vector War", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }
}
