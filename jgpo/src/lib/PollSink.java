package lib;

public interface PollSink {
	default boolean onHandlePoll(Object object) { return true; }
    default boolean onMsgPoll(Object object) { return true; }
    default boolean onPeriodicPoll(Object object, int index) { return true; }
    default boolean onLoopPoll(Object object) { return true; }
}
