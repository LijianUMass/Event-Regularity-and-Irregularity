
public class EventPair {
	public Event v1;
	public Event v2;
	public EventPair(Event v1, Event v2) {
		this.v1 = v1;
		this.v2 = v2;
	}
	public double weight() {
		double alpha = 0.5; //0.6;
		return Math.pow(alpha, v1.level+v2.level);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((v1 == null) ? 0 : v1.hashCode());
		result = prime * result + ((v2 == null) ? 0 : v2.hashCode());
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
		EventPair other = (EventPair) obj;
		if (v1 == null) {
			if (other.v1 != null)
				return false;
		} else if (!v1.equals(other.v1))
			return false;
		if (v2 == null) {
			if (other.v2 != null)
				return false;
		} else if (!v2.equals(other.v2))
			return false;
		return true;
	}
}
