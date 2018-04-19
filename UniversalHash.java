import java.util.Random;

public class UniversalHash {
	/**
     * Prime smaller than Integer.MAX_VALUE. (Normally must be chosen so that is greater than largest table size.)
     */
    private static final int p = 1999999973;
    private long[] a;
    private long[] b;

    public UniversalHash(int k) {
    	a = new long[k];
    	b = new long[k];
    	Random rand = new Random();
    	for (int i = 0; i < k; i++) {
            while ((a[i] = rand.nextInt()) == 0)
                continue;
            b[i] = rand.nextInt();
        }
    }
    public void multihash(Object o, int[] output) {
    	int h = o.hashCode();
        for (int i = 0; i < output.length; i++) {
        	output[i] = (int)((a[i] * h + b[i])) % p;
        	if (output[i] < 0) output[i] += p;
        }//for i
    }
}
