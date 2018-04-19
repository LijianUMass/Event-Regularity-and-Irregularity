import java.util.ArrayList;

public class Sequence {
	public int id; //starting from 0
	public ArrayList<Event> events;
	public ArrayList<DataWin> wins;
	public double[] pre, post, reg;
	
	public Sequence(int id, ArrayList<Event> events, ArrayList<DataWin> wins) {
		this.id = id;
		this.events = events;
		this.wins = wins;
	}
}
