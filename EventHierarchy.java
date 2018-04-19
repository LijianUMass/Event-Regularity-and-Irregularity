import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.BitSet;

public class EventHierarchy {
	public int nseq;
	public int tau;
	public HashSet<EventPair> edges;
	public HashSet<EventPair> reachable;
	public HashMap<EventPair, BitSet> epCnts;
	public boolean getStat = true;
	public int numPairs;
	public int numFreq;
	public ArrayList<Event> allNodes;
	public HashMap<Event, List<Event>> ancTab;
	public static boolean useBBS; //= true; // 
	public int bsize = 8; //64; //
	public BBS bbs;
	
	public EventHierarchy(int nseq, ArrayList<Event> allNodes, HashMap<Event, List<Event>> ancTab) {
		this.nseq = nseq;
		this.allNodes = allNodes;
		this.ancTab = ancTab;
		tau = (int) (nseq * 0.5);  //0.15
		edges = new HashSet<EventPair>();
		reachable = new HashSet<EventPair>();
		if (useBBS) bbs = new BBS(3, 800, nseq, bsize);
		else epCnts = new HashMap<EventPair, BitSet>();
	}
	public List<Event> ancestors(Event e) { //return ancestors, including itself
		return ancTab.get(e);
	}
	public List<Event> allEvents() {
		return allNodes;
	}
	public double newFreqCnt(EventPair ep, Message msg) { //set the bit, return new cnt if it is
		if (useBBS) {
		//	synchronized (bbs) {
				bbs.setElementGroup(ep, msg.sid);
				return bbs.lookup(ep, null);
		//	}//synchronized
		}
	//	synchronized (epCnts) {
			BitSet bs = epCnts.get(ep);
			if (bs == null) {
				bs = new BitSet(nseq);
				bs.set(msg.sid);
				epCnts.put(ep, bs);
				return 1;
			}
			if (bs.get(msg.sid)) return 0;
			bs.set(msg.sid);
			epCnts.put(ep, bs);
			return bs.cardinality();
	//	}
	}
}
