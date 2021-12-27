package lib;

public interface PollSink {
	default boolean onHandlePoll(Object o) { return true; }
    default boolean onMsgPoll(Object o) { return true; }
    default boolean onPeriodicPoll(Object o, int i) { return true; }
    default boolean onLoopPoll(Object o) { return true; }
}
