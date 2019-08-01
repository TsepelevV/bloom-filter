package projector;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.zaxxer.sparsebits.SparseBitSet;


public class BloomFilter {
    private static final double LN2 = 0.6931471805599453; // ln(2)
    private int k; // Number of hash functions
    private int m;
    private int n;
    private int elementsAddedCounter = 0;
    private SparseBitSet filter;
    private HashFunction[] hashFunctionArr;
    public SparseBitSet getBitset(){return filter;}
    public BloomFilter(int n, double p) {
        k = getOptimalNumberOfHashFunctions(n, m);
        if (k <= 0) {
            k = 1;
        }
        this.m = calculateAcceptableBucketSize(p, n);
        this.n = n;
        this.filter = new SparseBitSet(m);
        hashFunctionArr = new HashFunction[k];
        populateHashFunctionsArray(k);
    }

    public BloomFilter(int n, int m) {
        k = getOptimalNumberOfHashFunctions(n, m);
        if (k <= 0) {
            k = 1;
        }
        this.m = m;
        this.n = n;
        this.filter = new SparseBitSet(m);
        hashFunctionArr = new HashFunction[k];
        populateHashFunctionsArray(k);
    }

    public double getFalsePositiveProbability() {
        return Math.pow((1 - Math.exp(-this.k * (double) this.n / (double) this.m)), this.k);
    }

    private void populateHashFunctionsArray(int k) {
        for (int i = 0; i < k; i++) {
            this.hashFunctionArr[i] = Hashing.murmur3_32(i);
        }
    }

    public void add(String value) {
        this.elementsAddedCounter++;
        for (int i = 0; i < k; i++) {
            filter.set((hashFunctionArr[i].hashString(value).asInt() & 0x7fffffff) % Integer.MAX_VALUE);
        }
    }

    public int calculateAcceptableBucketSize(double p) {
        return Math.toIntExact(Math.round(-this.n * Math.log(p) / Math.pow(LN2, 2)));
    }


    public int getNumberOfElemetsAdded() {
        return this.elementsAddedCounter;
    }

    public int calculateAcceptableBucketSize(double p, int n) {
        return Math.toIntExact(Math.round(-n * Math.log(p) / Math.pow(LN2, 2)));
    }

    public int getBucketSize(){return this.m;}

    public boolean contains(String value) {
        for (int i = 0; i < k; i++) {
            if (!filter.get((hashFunctionArr[i].hashString(value).asInt() & 0x7fffffff) % Integer.MAX_VALUE)) {
                return false;
            }
        }
        return true;
    }

    public int getOptimalNumberOfHashFunctions(int n, int m) {
        return (int) Math.round(LN2 * m / n);
    }

    public void clear() {
        this.filter.clear();
    }

    public SparseBitSet intersect(BloomFilter bloomFilterB) {
        SparseBitSet cutBitset = new SparseBitSet();
        SparseBitSet bitSetA = this.filter;
        SparseBitSet bitSetB = bloomFilterB.getBitset();
        for (int i = bitSetA.nextSetBit(0); i >= 0; i = bitSetA.nextSetBit(i + 1)) {
            if (bitSetB.get(i)) {
                cutBitset.set(i);
            }
        }
        return cutBitset;
    }

}
