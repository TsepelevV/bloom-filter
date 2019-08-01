import com.zaxxer.sparsebits.SparseBitSet;
import projector.BloomFilter;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.Set;

public class BloomFilterTest {
    private static final String ALPHA_NUMERIC_STRING_CUT = "ABCDEFGHIJK";
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    static int elements = 20_000_000;
    private static ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    public static void main(String[] args) {
        testIntersection();
//        testMemoryConsumption();
//        testCorrectness();
//        testRetrieveSpeed();
    }

    public static void testIntersection() {
        BloomFilter bloomFilter1 = new BloomFilter(elements, 0.01);
        BloomFilter bloomFilter2 = new BloomFilter(elements, 0.01);
//        HashSet<String> set1 = new HashSet<>((int) (elements / 0.75));
//        HashSet<String> set2 = new HashSet<>((int) (elements / 0.75));
        for (int i = 0; i < elements; i++) {
            String s = randomAlphaNumeric(10);
            bloomFilter1.add(s);
            bloomFilter1.add(s);
//            set1.add(s);
//            set2.add(s);
        }
        long start = bean.getCurrentThreadCpuTime();
        bloomFilter1.intersect(bloomFilter2);
//        set1.equals(set2);
        long end = bean.getCurrentThreadCpuTime();
        long time = end - start;
        System.out.println(time);
    }

    public static void testMemoryConsumption() {
//        BitsetMemoryFactory bitsetMemoryFactory = new BitsetMemoryFactory();
        SetFactory setFactory = new SetFactory();
        MemoryTestBench m = new MemoryTestBench();
//        m.showMemoryUsage(bitsetMemoryFactory);
        m.showMemoryUsage(setFactory);
    }


    public static void testCorrectness() {
        BloomFilter bloomFilter = new BloomFilter(elements, 0.01);

        System.out.println("Testing correctness.\n" +
                "Creating a Set and filling it together with our filter...");
        bloomFilter.clear();
        Set<String> inside = new HashSet<>((int) (elements / 0.75));
        while (inside.size() < elements) {
            String v = randomAlphaNumeric(5);
            inside.add(v);
            bloomFilter.add(v);
            assert bloomFilter.contains(v) : "There should be no false negative";
        }

        // testing
        int found = 0, total = 0;
        double rate = 0;
        while (total < elements) {
            String v = randomAlphaNumeric(5);
            if (inside.contains(v)) continue;
            total++;
            found += bloomFilter.contains(v) ? 1 : 0;

            rate = (float) found / total;
            if (total % 1000 == 0 || total == elements) {
                System.out.format(
                        "\rElements incorrectly found to be inside: %8d/%-8d (%3.2f%%)",
                        found, total, 100 * rate
                );
            }
        }
        System.out.println("\n");
        double ln2 = Math.log(2);
        double expectedRate = Math.exp(-ln2 * ln2 * bloomFilter.getBucketSize() / elements);
        assert rate <= expectedRate * 1.10 : "error rate p = e^(-ln2^2*m/n)";
    }

    public static void testRetrieveSpeed() {
        System.out.println("Testing retrieval speed...");
        Set<String> inside = new HashSet<>((int) (elements / 0.75));
        BloomFilter bloomFilter = new BloomFilter(elements, 0.01);
        for (int i = 0; i < elements; i++) bloomFilter.add(randomAlphaNumeric(20));
        long start = bean.getCurrentThreadCpuTime();
        for (int i = 0; i < elements; i++) bloomFilter.contains(randomAlphaNumeric(20));
        long end = bean.getCurrentThreadCpuTime();
        long time = end - start;

        System.out.format(
                "Queried %d elements in %d ns.\n" +
                        "Query speed: %g elements/second\n\n",
                elements,
                time,
                elements / (time * 1e-9)
        );
    }

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING_CUT.length());
            builder.append(ALPHA_NUMERIC_STRING_CUT.charAt(character));
        }
        return builder.toString();
    }
}
