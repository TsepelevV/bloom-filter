import projector.BloomFilter;

public class BitsetMemoryFactory implements ObjectFactory{
    @Override
    public Object makeObject() {
        BloomFilter bloomFilter = new BloomFilter(5_000_000, 0.01);
        for (int i = 0; i < 5_000_000; i++) {
            bloomFilter.add(BloomFilterTest.randomAlphaNumeric(50));
        }
        return bloomFilter;
    }
}
