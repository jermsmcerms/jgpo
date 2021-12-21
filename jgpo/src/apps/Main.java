package apps;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

import api.JgpoNet;
import api.JgpoNet.JGPOPlayer;

public class Main {
	private static final int MAX_PLAYERS = 4;
	private final VectorWar vectorWar;
	
	public Main(int num_players, int local_port, JGPOPlayer players[]) {
		vectorWar = new VectorWar(num_players, local_port, players);
		
		long now = System.currentTimeMillis();
		long next = now;
		
		while(true) {
			now = System.currentTimeMillis();
			if(now >= next) {
				vectorWar.runFrame();
				next = now + (1000 / 60);
			}
		}
    }

    public static void main(String[] args) {
    	if(args.length < 4) {
    		syntax();
    	}
    	
    	try {
    		int local_port = Integer.parseInt(args[0]);
    		int num_players = Integer.parseInt(args[1]);
    		
    		if(num_players < 1 || num_players > MAX_PLAYERS) {
    			syntax();
    		}
    		    		
    		JGPOPlayer players[] = new JGPOPlayer[JgpoNet.JGPO_MAX_SPECTATORS + 
    		                                      JgpoNet.JGPO_MAX_PLAYERS];
    		
    		for(int i = 0; i < num_players; i++) {
    			players[i] = new JGPOPlayer();
    			players[i].player_num = i + 1;
    			if(args[2] == "local") {
    				players[i].type = JgpoNet.JGPOPlayerType.JGPO_PLAYERTYPE_LOCAL;
    				continue;
    			}
    			
    			players[i].type = JgpoNet.JGPOPlayerType.JGPO_PLAYERTYPE_REMOTE;
    			List<String> splitByColon = Stream.of(args[3])
    		            .map(w -> w.split(":")).flatMap(Arrays::stream)
    		            .collect(Collectors.toList());
    			if(splitByColon.size() != 2) {
    				syntax();
    			}
    			players[i].ip_address = splitByColon.get(0);
    			players[i].port = Integer.parseInt(splitByColon.get(1));
    		}
    		
    		// TODO: get the spectator stuff here
    		
    		new Main(num_players, local_port, players);
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
