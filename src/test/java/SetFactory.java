import java.util.HashMap;

public class SetFactory implements ObjectFactory {
    @Override
    public Object makeObject() {
        HashMap<String, String> set = new HashMap<>((int) (10_000_000 / 0.75));
        for (int i = 0; i < 10_000_000; i++) {
            set.put(BloomFilterTest.randomAlphaNumeric(50), BloomFilterTest.randomAlphaNumeric(50));
        }
        return set;
    }
}
