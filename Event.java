import java.util.ArrayList;

public class Event {
//	public long ts;  TimedEvent has ts.
	public int letter;
	public int level; //0 is leaf
	public boolean isRoot;
	public Event parent;
	public ArrayList<Event> children;
	
	public Event(int letter, int level, boolean isRoot) {
		this.letter = letter;
		this.level = level;
		this.isRoot = isRoot;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + letter;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Event other = (Event) obj;
		if (letter != other.letter)
			return false;
		return true;
	}
}
