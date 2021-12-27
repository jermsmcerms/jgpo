package lib.utils;

import api.apievents.ApiSynchronizingEvent;
import api.apievents.*;

public class JGPOEventFactory {
	public static JGPOEvent makeApiEvent(JGPOEvent.JGPOEventCode code) {
		switch(code) {
			case JGPO_CONNECTED_TO_PEER :
				return new JGPOEvent(code);
			case JGPO_CONNECTION_RESUMED :
				return new JGPOEvent(code);
			case JGPO_DISCONNECTED_FROM_PEER :
				return new JGPOEvent(code);
			case JGPO_RUNNING :
				return new JGPOEvent(code);
			case JGPO_SYNCHRONIZED_WITH_PEER :
				return new JGPOEvent(code);
			case JGPO_CONNECTION_INTERRUPTED :
				return  new ConnectionInterruptedEvent(code);
			case JGPO_SYNCHRONIZING_WITH_PEER :
				return new ApiSynchronizingEvent(code);
			case JGPO_TIMESYNC :
				return new TimeSyncEvent(code);
			default :
				return null;
			
		}
	}
}
