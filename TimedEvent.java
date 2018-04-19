
public class TimedEvent extends Event {
	public long ts;
	public TimedEvent(int letter, int level, boolean isRoot) {
		super(letter, level, isRoot);
		ts = 0;
	}
}
