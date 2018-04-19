import java.util.BitSet;

public class BBS {
	public int k, w, n, b, bsize; //n is number of groups, b is number of perm blocks
	public UniversalHash h;
	public BitSet[][] bss;
	public int sum0, c0, delta = 5;
	public double pc; //probability of collision
	
	public BBS(int k, int w, int n, int bsize) { //bsize must be multiples of 64 or 8
		this.k = k;
		this.w = w;
		this.n = n;
		this.bsize = bsize;
		b = (n+bsize-1)/bsize;
		h = new UniversalHash(k);
		bss = new BitSet[k][w];
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < w; j++) {
				bss[i][j] = new BitSet(b*bsize);
			}//for j
		}//for i
	}//constructor
	public void setElementGroup(Object e, int gid) {
		int[] ha = new int[k];
		h.multihash(e, ha);
		boolean newElem = false;
		for (int i = 0; i < k; i++) {
			int col = ha[i] % w;
			if (col < 0) col += w;
			int bid = gid / bsize; //block id
			int boff = gid % bsize;
			if (boff < 0) boff += bsize; //block offset
			int hv = ha[i];
			for (int j = b-1; j > 0; j--) { //random permutation
				int bid2 = hv % (j+1);
				if (bid2 < 0) bid2 += j+1; //block bid2 is to swap with block j
				if (bid == j) bid = bid2;
				else if (bid == bid2) bid = j;
				hv /= (j+1);
			}
			int gid2 = bid * bsize + boff;
			if (! bss[i][col].get(gid2)) { //not previously set
				newElem = true;
				bss[i][col].set(gid2);
			}//if ! bss
		}//for i
		if (newElem) sum0++;
	}
	public BitSet permReverse(BitSet bm, int hv) {
		int[] perm = new int[b];
		for (int i = 0; i < b; i++) perm[i] = i;
		for (int j = b-1; j > 0; j--) { //random permutation
			int bid2 = hv % (j+1);
			if (bid2 < 0) bid2 += j+1; //block bid2 is to swap with block j
			int tmp = perm[j];
			perm[j] = perm[bid2];
			perm[bid2] = tmp;
			hv /= (j+1);
		}//for j
		if (bsize % 64 == 0) {
			int blen = bsize/64; //block length in #long's
			int nlongs = blen * b; //number of bytes
			long[] longs = new long[nlongs];
			long[] tmp = bm.toLongArray();
			for (int i = 0; i < tmp.length; i++) longs[i] = tmp[i];
			long[] longs0 = new long[longs.length]; //original bitmap
			for (int i = 0; i < b; i++) {
				//block i of longs goes to block perm[i] of longs0
				int to = perm[i]*blen;
				int from = i*blen;
				for (int j = 0; j < blen; j++) longs0[to+j] = longs[from+j];
			}//for i
			return BitSet.valueOf(longs0);
		}//if bsize % 64
		if (bsize % 8 == 0) {
			int blen = bsize/8; //block length in #byte's
			int nbytes = blen * b; //number of bytes
			byte[] bytes = new byte[nbytes];
			byte[] tmp = bm.toByteArray();
			for (int i = 0; i < tmp.length; i++) bytes[i] = tmp[i];
			byte[] bytes0 = new byte[bytes.length]; //original bitmap
			for (int i = 0; i < b; i++) {
				//block i of bytes goes to block perm[i] of bytes0
				int to = perm[i]*blen;
				int from = i*blen;
				for (int j = 0; j < blen; j++) bytes0[to+j] = bytes[from+j];
			}//for i
			return BitSet.valueOf(bytes0);
		}//if bsize % 8
		return null;
	}
	public double lookup(Object e, BitSet[] result) { //return estimated group count
		BitSet gm = null;
		int[] ha = new int[k];
		h.multihash(e, ha);
		for (int i = 0; i < k; i++) {
			int col = ha[i] % w;
			if (col < 0) col += w;
			BitSet bm = bss[i][col];
			BitSet bm0 = permReverse(bm, ha[i]);
			if (gm == null) {
				gm = (BitSet) bm0.clone();
			}
			else gm.and(bm0);
		}//for i
		int c1 = sum0 / n;
		if (c1 > c0 + delta) {
			c0 = c1;
			double p0 = 0;
			while (true) {
				pc = Math.pow((1 - Math.pow((1 - 1.0 / w), (c0 / (1 - p0)))), k);
				if (pc - p0 < 0.001) break;
				p0 = pc;
			}//while true
		}//if c1
		if (result != null) result[0] = gm;
		return (gm.cardinality() - n*pc) / (1 - pc);
	//	return gm.cardinality();
	}
}
