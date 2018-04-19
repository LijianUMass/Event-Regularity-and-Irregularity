import java.util.ArrayList;
import java.util.List;
import java.util.BitSet;

public class Task implements Runnable {
	int step;
	Sequence si;
	List<Message> msgs;
	List<Message> mNext;
	EventHierarchy eh;
	ArrayList<Sequence> ss;
	public Task(int step, Sequence si, List<Message> msgs, List<Message> mNext, EventHierarchy eh,
			ArrayList<Sequence> ss) {
		this.step = step;
		this.si = si;
		this.msgs = msgs;
		this.mNext = mNext;
		this.eh = eh;
		this.ss = ss;
	}
	public void run() {
		if (step == 0) {
			ArrayList<Event> events = si.events;
			for (int i = 0; i < events.size()-1; i++) {
				Event from = events.get(i);
				Event to = events.get(i+1);
				if (from.letter != to.letter) {
				//	synchronized (msgs) {
						msgs.add(new Message(si.id, from, to, i, i+1));
				//	}
				}
			}
		}//if step == 0
		else if (step == 1) {
			for (Message msg : msgs) {
				List<Event> froms = eh.ancestors(msg.from);
				List<Event> tos = eh.ancestors(msg.to);
				if (froms == null || tos == null) continue;
				for (Event from : froms) {
					for (Event to : tos) {
						EventPair ep = new EventPair(from, to);
						if (eh.getStat) {
							if (eh.useBBS) {
							//	synchronized (eh.bbs) {
									eh.numPairs++;
									if (eh.edges.contains(ep)) eh.numFreq++;
							//	}//sync
							}
							else /*synchronized (eh.epCnts)*/ {
								eh.numPairs++;
								if (eh.edges.contains(ep)) eh.numFreq++;
								else {
									BitSet bs = eh.epCnts.get(ep);
									if (bs != null) {
										int freq = bs.cardinality();
										if (! bs.get(msg.sid)) freq += 1;
										if (freq > eh.tau) eh.numFreq++;
									}
								}//else
							}//else synchronized
						}//if eh.getStat
						if (eh.edges.contains(ep)) continue;
						if (eh.newFreqCnt(ep, msg) > eh.tau) {
							if (eh.getStat && eh.useBBS) eh.numFreq++;
							if (eh.reachable.contains(ep)) continue;
							eh.edges.add(ep);
							List<Event> events = eh.allEvents();
							for (Event x : events) {
								if (x.level < from.level) continue;
								for (Event y : events) {
									if (y.level < to.level) continue;
									if (eh.reachable.contains(new EventPair(x, from)) &&
											eh.reachable.contains(new EventPair(to, y))) {
										eh.reachable.add(new EventPair(x, y));
									}//if reachable.contains
								}//for y
							}//for x
						}//if eh.newFreqCnt
					}//for to
				}//for from
				//Propagate message to next neighbor
				si = ss.get(msg.sid);
				if (msg.tid < si.events.size()-1) {
					mNext.add(new Message(msg.sid, msg.from, si.events.get(msg.tid+1), msg.fid,
							msg.tid+1));
				}
			}//for msg
		}//else if step == 1
		else if (step == 2) {
			for (Message msg : msgs) {
				List<Event> froms = eh.ancestors(msg.from);
				List<Event> tos = eh.ancestors(msg.to);
				if (froms == null || tos == null) continue;
				for (Event from : froms) {
					for (Event to : tos) {
						EventPair ep = new EventPair(from, to);
						if (eh.edges.contains(ep)) {
							double w = ep.weight();
							if (w > si.pre[msg.tid]) si.pre[msg.tid] = w;
							if (w > si.post[msg.fid]) si.post[msg.fid] = w;
						}//if eh.edges
					}//for to
				}//for from
				//Propagate message to next neighbor
				if (msg.tid < si.events.size()-1) {
					mNext.add(new Message(msg.sid, msg.from, si.events.get(msg.tid+1), msg.fid,
							msg.tid+1));
				}
			}//for msg
		}//else if step == 2
	}
}
