import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.*;
import java.io.BufferedReader;
import java.io.FileReader;

public class Main {
	public static ArrayList<Sequence> ss;
	public static EventHierarchy eh;
	public static HashMap<DataWin, Integer> wincnt;
	public static boolean doWin = true;
	public static int kwin = 6;
	
	public static void getEHandSS() {
		//scan file to get event tree
	//	testEHSS();
	//	syntheticEHSS();
		manyEHSS(4); //0:aruba, 1:NASA, 2:smart, 3: GPS, 4: hybrid
	}
	//0:aruba, 1:NASA, 2:smart, 3: GPS, 4: hybrid
	public static void manyEHSS(int type) {
		String treefile = null, datafile = null;
		doWin = doWin && (type == 2 || type == 4);
		if (doWin) {
			wincnt = new HashMap<DataWin, Integer>();
		}
		int nseq = 0;
		if (type == 0) { //aruba
			treefile = "data//aruba//LeaftoRootPath.txt";
			datafile = "data//aruba//57DaysSequence.txt";
		//	nseq = 57;
		}
		else if (type == 1) { //NASA
			treefile = "data//nasa//LeaftoRootPath.txt";
			datafile = "data//nasa//27DaysSequence.txt";
		//	nseq = 27;
		}
		else if (type == 2) { //smart house
			treefile = "data//smarthouse//LeaftoRootPath.txt";
			datafile = "data//smarthouse//16DaysSequence.txt";
		//	nseq = 16;
		}
		else if (type == 3) { //GPS
			treefile = "data//GPS2//LeaftoRootPath.txt";
			datafile = "data//GPS2//30DaysSequence.txt";
		}
		else { //type == 4, hybrid
			treefile = "data//hybrid//Leaf_to_Root_Path.txt";
			datafile = "data//hybrid//AnyDays.txt";
		}
		try {
			ss = new ArrayList<Sequence>(nseq);
			BufferedReader br = new BufferedReader(new FileReader(datafile));
			String line;
			int day = 0, sid = 0, p_letter = 0;
			ArrayList<Event> events = new ArrayList<Event>();
			ArrayList<DataWin> dw = new ArrayList<DataWin>();
			int[] oneWin = new int[kwin]; int wsize = 0;
			while ((line = br.readLine()) != null) {
				String[] fields = line.split(",");
				int letter = Integer.parseInt(fields[0]);
				String dayid = null;
				if (type == 0 || type == 3) dayid = fields[2]+fields[3];
				else if (type == 1 || type == 4) dayid = fields[1];
				else dayid = fields[2]; //type == 2
				int dayi = Integer.parseInt(dayid);
				if (dayi != day) { //a new day
					nseq++;
					if (day != 0) { //wrap up previous day
						Sequence si = new Sequence(sid, events, dw);
						sid++;
						ss.add(si);
					}//if day
					events = new ArrayList<Event>();
					if (doWin) {
						dw = new ArrayList<DataWin>();
						wsize = 0;
					}
					day = dayi;
				}//if dayi
				else if (letter == p_letter) continue; //skip identical events
				if (doWin) {
					if (wsize == kwin) {
						for (int i = 0; i < wsize-1; i++) oneWin[i] = oneWin[i+1];
						wsize--;
					}
					oneWin[wsize++] = letter;
					if (wsize == kwin) {
						DataWin newWin = new DataWin(oneWin);
						dw.add(newWin);
						Integer cnt = wincnt.get(newWin);
						if (cnt == null) {
							wincnt.put(newWin, 1);
						}
						else {
							wincnt.put(newWin, cnt.intValue() + 1);
						}
					}
				}
				events.add(new Event(letter, 0, false));
				p_letter = letter;
			}//while
			Sequence si = new Sequence(sid, events, dw); //wrap up last day
			sid++;
			ss.add(si);
			br.close();
			System.out.println("Number of sequences is: "+nseq);
			parseL2R(treefile, nseq);
		} catch (Exception e) {e.printStackTrace();}
	}
	public static void printAnomalyScore(int sid, int letter) {
		double es = 0, seqs = 0, alls = 0, seqflat = 0;
		int num = 0;
		HashMap<Integer, Double> seqScores = new HashMap<Integer, Double>();
		for (Sequence si : ss) {
			num += si.wins.size();
			for (DataWin dw : si.wins) {
				int wcnt = wincnt.get(dw);
				double wscore = 1.0 / (double)(wcnt);
				alls += wscore;
				if (sid == si.id) {
					seqflat += wscore;
					for (int i = 0; i < kwin; i++) {
						if (letter == dw.letters[i] && wscore > es) es = wscore;
						Double score = seqScores.get(dw.letters[i]);
						if (score == null || score.doubleValue() < wscore) {
							seqScores.put(dw.letters[i], wscore);
						}
					}//for i
				}//if sid match
			}//for dw
			if (sid == si.id) {
				seqflat /= si.wins.size();
				for (double ones : seqScores.values()) {
					seqs += ones;
				}
				seqs /= seqScores.size();
			}
		}//for si
		alls /= num;
		System.out.println("Event anomaly score: "+es+" Seq score: "+seqs+" Overall score: "+alls);
		System.out.println("Seq flat average: "+seqflat);
	}
	public static void parseL2R(String treefile, int nseq) throws Exception {
		HashSet<Event> nodes = new HashSet<Event>();
		HashMap<Event, List<Event>> ancTab = new HashMap<Event, List<Event>>();
		ArrayList<Event> ancestors;
		BufferedReader br = new BufferedReader(new FileReader(treefile));
		String line;
		while ((line = br.readLine()) != null) {
			String[] fields = line.split(": ");
			Event leaf = new Event(Integer.parseInt(fields[0]), 0, false);
			nodes.add(leaf);
			String[] anc = fields[1].split(",");
			ancestors = new ArrayList<Event>(anc.length);
			ancestors.add(leaf);
			for (int i = 1; i < anc.length; i++) {
				Event node = new Event(Integer.parseInt(anc[i]), i, 
						(i==anc.length-1) ? true : false);
				ancestors.add(node);
				nodes.add(node);
			}//for i
			ancTab.put(leaf, ancestors);
		}//while line
		br.close();
		ArrayList<Event> allNodes = new ArrayList<Event>(nodes.size());
		for (Event e : nodes) allNodes.add(e);
		eh = new EventHierarchy(nseq, allNodes, ancTab);
	}
	public static void syntheticEHSS() {
		String treefile = "data//generated//Leaf_to_Root_Path.txt";
		String dayfile = "data//generated//Day";
		int nseq = 14;
		try {
			parseL2R(treefile, nseq);
			ss = new ArrayList<Sequence>(nseq);
			for (int i = 1; i <= nseq; i++) {
				String dayi = dayfile + i + ".txt";
				BufferedReader br = new BufferedReader(new FileReader(dayi));
				ArrayList<Event> events = new ArrayList<Event>();
				String line;
				while ((line = br.readLine()) != null) {
					String[] fields = line.split(",");
					events.add(new Event(Integer.parseInt(fields[0]), 0, false));
				}//while
				br.close();
				Sequence si = new Sequence(i-1, events, null);
				ss.add(si);
			}//for i
		} catch (Exception e) {e.printStackTrace();}
	}
	public static void testEHSS() {
		ArrayList<Event> allNodes = new ArrayList<Event>(11);
		Event[] leaf = new Event[7];
		for (int i = 1; i < 7; i++) {
			leaf[i] = new Event(i, 0, false);
			allNodes.add(leaf[i]);
		}//for i
		Event e12 = new Event(12, 1, false);
		allNodes.add(e12);
		Event e34 = new Event(34, 1, false);
		allNodes.add(e34);
		Event e56 = new Event(56, 1, false);
		allNodes.add(e56);
		Event e14 = new Event(14, 2, false);
		allNodes.add(e14);
		Event e16 = new Event(16, 3, true);
		allNodes.add(e16);
		HashMap<Event, List<Event>> ancTab = new HashMap<Event, List<Event>>(6);
		ArrayList<Event> anc = new ArrayList<Event>(4);
		anc.add(leaf[1]); anc.add(e12); anc.add(e14); anc.add(e16);
		ancTab.put(leaf[1], anc);
		anc = new ArrayList<Event>(4);
		anc.add(leaf[2]); anc.add(e12); anc.add(e14); anc.add(e16);
		ancTab.put(leaf[2], anc);
		anc = new ArrayList<Event>(4);
		anc.add(leaf[3]); anc.add(e34); anc.add(e14); anc.add(e16);
		ancTab.put(leaf[3], anc);
		anc = new ArrayList<Event>(4);
		anc.add(leaf[4]); anc.add(e34); anc.add(e14); anc.add(e16);
		ancTab.put(leaf[4], anc);
		anc = new ArrayList<Event>(3);
		anc.add(leaf[5]); anc.add(e56); anc.add(e16);
		ancTab.put(leaf[5], anc);
		anc = new ArrayList<Event>(3);
		anc.add(leaf[6]); anc.add(e56); anc.add(e16);
		ancTab.put(leaf[6], anc);
		int rep = 5, rep2 = 1;//5;
		ss = new ArrayList<Sequence>(4*rep);
		eh = new EventHierarchy(4*rep, allNodes, ancTab);
		ArrayList<Event> events;
		for (int i = 0; i < rep; i++) {
			events = new ArrayList<Event>(4*rep2);
			for (int j = 0; j < rep2; j++) {
				events.add(leaf[1]); events.add(leaf[2]); events.add(leaf[4]); events.add(leaf[6]);
			}
			ss.add(new Sequence(i*4, events, null));
			events = new ArrayList<Event>(5*rep2);
			for (int j = 0; j < rep2; j++) {
				events.add(leaf[6]); events.add(leaf[1]); events.add(leaf[5]); events.add(leaf[6]); events.add(leaf[5]);
			}
			ss.add(new Sequence(i*4+1, events, null));
			events = new ArrayList<Event>(5*rep2);
			for (int j = 0; j < rep2; j++) {
				events.add(leaf[3]); events.add(leaf[1]); events.add(leaf[4]); events.add(leaf[6]); events.add(leaf[3]);
			}
			ss.add(new Sequence(i*4+2, events, null));
			events = new ArrayList<Event>(5*rep2);
			for (int j = 0; j < rep2; j++) {
				events.add(leaf[2]); events.add(leaf[1]); events.add(leaf[1]); events.add(leaf[5]); events.add(leaf[6]);
			}
			ss.add(new Sequence(i*4+3, events, null));
		}
	}
	public static void buildModel(boolean earlyStop) throws Exception {
	//	int poolSize = 1; //change this for 1 thread
		int tasklen = 200;  //task granularity 200
		ExecutorService tpool = Executors.newFixedThreadPool(1);
	//	ExecutorService tpool = Executors.newCachedThreadPool();
		int sslen = 0;
		for (Sequence si : ss) {
			sslen += si.events.size();
		}
		ArrayList<Message> msgs = new ArrayList<Message>(sslen);
		ArrayList<Future<?>> futures = new ArrayList<Future<?>>(sslen/tasklen + 1);
		int nsteps = 0;
		for (Sequence si : ss) {
			if (si.events.size()-1 > nsteps) nsteps = si.events.size()-1;
			Future<?> future = tpool.submit(new Task(0, si, msgs, null, null, null));
			futures.add(future);
		}//for si
		for (Future<?> future : futures) {
			future.get(); //wait to complete
		}
		futures.clear();
		List<Event> events = eh.allEvents();
		for (Event e : events) {
			EventPair ep = new EventPair(e, e);
			eh.reachable.add(ep);
		}
		ArrayList<Message> msgs2 = new ArrayList<Message>(sslen);
		ArrayList<Message> mNow = null, mNext = null;
		double[] f = new double[nsteps+1], p = new double[nsteps+1], q = new double[nsteps+1];
		q[0] = 1;
		int radius = nsteps; //Math.min(nsteps, 100); //
		for (int t = 0; t < radius; t++) {
			if (msgs.size() == 0) {
				if (msgs2.size() == 0) break;
				mNow = msgs2;
				mNext = msgs;
			}
			else {
				mNow = msgs;
				mNext = msgs2;
			}
			if (eh.getStat) {
				eh.numPairs = 0;
				eh.numFreq = 0;
			}//if eh.getStat
			int ptr = 0;
			while (ptr < mNow.size()) {
				int ptr2 = ptr + tasklen;
				if (ptr2 > mNow.size()) ptr2 = mNow.size();
				List<Message> mtask = mNow.subList(ptr, ptr2);
				Task task = new Task(1, null, mtask, mNext, eh, ss);
				Future<?> future = tpool.submit(task);
				futures.add(future);
				ptr = ptr2;
			}//while ptr
			for (Future<?> future : futures) {
				future.get(); //wait to complete
			}
			futures.clear();
			mNow.clear();
			if (eh.getStat && t < 30) {
				int n = t+1;
				f[n] = (double)eh.numFreq / (double)eh.numPairs;
				double tmp = 0;
				for (int i = 1; i < n; i++) tmp += p[i]*q[n-i];
				p[n] = f[n] * (1 - tmp);
				q[n] = tmp + p[n];
				System.out.println("p["+n+"] = "+p[n]+" f["+n+"] = "+f[n]+" q["+n+"] = "+q[n]);
				if (earlyStop && p[n] <= 0.01) break;
			}
		}//for t
		tpool.shutdownNow();
	}
	public static double computeRegularity(Sequence si) throws Exception {
		int tasklen = 20;  //task granularity
		int poolSize = 1; //8; //change this for 1 thread
		ExecutorService tpool = Executors.newFixedThreadPool(poolSize);
		ArrayList<Message> msgs = new ArrayList<Message>(si.events.size());
		ArrayList<Future<?>> futures = new ArrayList<Future<?>>(si.events.size()/tasklen + 1);
		si.pre = new double[si.events.size()];
		si.post = new double[si.events.size()];
		si.reg = new double[si.events.size()];
		Future<?> future = tpool.submit(new Task(0, si, msgs, null, null, null));
		future.get();
		ArrayList<Message> msgs2 = new ArrayList<Message>(si.events.size());
		ArrayList<Message> mNow = null, mNext = null;
		int nsteps = si.events.size()-1;
		int radius = Math.min(nsteps, 100);
		for (int t = 0; t < radius; t++) {
			if (msgs.size() == 0) {
				if (msgs2.size() == 0) break;
				mNow = msgs2;
				mNext = msgs;
			}
			else {
				mNow = msgs;
				mNext = msgs2;
			}
			int ptr = 0;
			while (ptr < mNow.size()) {
				int ptr2 = ptr + tasklen;
				if (ptr2 > mNow.size()) ptr2 = mNow.size();
				List<Message> mtask = mNow.subList(ptr, ptr2);
				Task task = new Task(2, si, mtask, mNext, eh, null);
				future = tpool.submit(task);
				futures.add(future);
				ptr = ptr2;
			}//while ptr
			for (Future<?> fut : futures) {
				fut.get(); //wait to complete
			}
			futures.clear();
			mNow.clear();
		}//for t
		tpool.shutdownNow();
		int n = si.pre.length;
		double reg = 0;
		for (int i = 0; i < n; i++) {
			si.reg[i] = si.pre[i] * si.post[i]; //(si.pre[i] + si.post[i])/2; //
			reg += si.reg[i];
		}
		return reg/n;
	}
	public static void printModel() {
		int nleaf = 0;
		int nnode = 0;
		double cll = 0, cln = 0, cnn = 0;
		int leaflevel = 0; //2; //
		for (Event e : eh.allNodes) {
			if (e.level == leaflevel) nleaf++;
			if (e.level >= leaflevel) nnode++;
		}
		System.out.println("Learned model is:");
		for (EventPair ep : eh.edges) {
			String le = (ep.v1.level <= leaflevel && ep.v2.level <= leaflevel) ? ": Leaf to leaf!" : "";
			System.out.println(ep.v1.letter + " --> " + ep.v2.letter + le);
			if (ep.v1.level <= leaflevel || ep.v2.level <= leaflevel) {
				if (ep.v1.level <= leaflevel && ep.v2.level <= leaflevel) {
					cll += 2;
					cln += 2;
					cnn += 2;
				}//if &&
				else { //one is leaf
					cln += 1;
					cnn += 2;
				}//else
			}//if ||
			else { //neither leaf
				cnn += 2;
			}//else
		}//for ep
		System.out.println("Average leaf-leaf degree is: "+(cll/nleaf)+
				" Average leaf-all degree is: "+(cln/nleaf)+
				" Average all-all degree is: "+(cnn/nnode));
	}
	public static void printReg(Sequence si) throws Exception {
		double reg = computeRegularity(si);
	//	if (reg < 0.001) return;  //FOR NOW
		System.out.print(reg + " | ");
		int len = Math.min(20, si.reg.length);
		for (int i = 0; i < len; i++) System.out.print(si.reg[i]+" ");
		System.out.println();
	}
	public static void reportMemory() {
		Runtime rt = Runtime.getRuntime();
		rt.gc(); rt.gc();
		long memUsed = rt.totalMemory() - rt.freeMemory();
		System.out.println("$$$ Used memory "+(memUsed/1000000.0)+" MB.");
	}
	public static void getPrecisionRecall(boolean testEarlyStop) {
		try {
			if (! testEarlyStop) EventHierarchy.useBBS = false;
			getEHandSS();
			buildModel(testEarlyStop ? false : true);
			HashSet<EventPair> edges1 = eh.edges;
			if (! testEarlyStop) EventHierarchy.useBBS = true;
			getEHandSS();
			buildModel(true);
			double count = 0;
			for (EventPair ep : eh.edges) {
				if (edges1.contains(ep)) count += 1;
			}
			System.out.println("Precision is: "+(count/eh.edges.size()));
			count = 0;
			for (EventPair ep : edges1) {
				if (eh.edges.contains(ep)) count += 1;
			}
			System.out.println("Recall is: "+(count/edges1.size()));
		} catch (Exception e) {e.printStackTrace();}		
	}
	public static void main(String[] args) {
		EventHierarchy.useBBS = false;
		getEHandSS();
		try {
			long startTime = System.nanoTime();
			buildModel(true);
			reportMemory();
			long estimatedTime = System.nanoTime() - startTime;
		    double time = estimatedTime/1000000.0;
		    System.out.println("Used "+time+" ms.");
			printModel();
			for (Sequence si : ss) {
				printReg(si);
			}
		//	printAnomalyScore(7, 19); //smart e1
		//	printAnomalyScore(0, 34); //smart e2; add line "34,3,27,2003,6,55,8,468" as line 3
		//	printAnomalyScore(14, 30); //smart; add line "30,4,10,2003,21,1,1,300" as line 206
		//	printAnomalyScore(1, 10); //hybrid e1
		//	printAnomalyScore(5, 12); //hybrid e2
			printAnomalyScore(0, 10); //hybrid make tea (original), common
		//	printAnomalyScore(0, 9); //hybrid change data 10->9, make coffee, rare
		} catch (Exception e) {e.printStackTrace();}
	//	getPrecisionRecall(false);
	}
}
