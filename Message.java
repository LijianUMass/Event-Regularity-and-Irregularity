
public class Message {
	public int sid; //from 0
	public Event from;
	public int fid;
	public Event to;
	public int tid;
	public Message(int sid, Event from, Event to, int fid, int tid) {
		this.sid = sid;
		this.from = from;
		this.to = to;
		this.fid = fid;
		this.tid = tid;
	}
}
