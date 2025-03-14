package Threads;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator<String> {
    private final Map<String, ? extends Comparable> map;

    public ValueComparator(Map<String, ? extends Comparable> map) {
        this.map = map;
    }

    public int compare(String a, String b) {
        int result = map.get(b).compareTo(map.get(a));
        return result != 0 ? result : a.compareTo(b);
    }
}
