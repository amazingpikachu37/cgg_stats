package Threads;

import java.util.List;
import java.util.Map;

public class CollectionFactory {
    public static <K,V> CollectionContainer<K, V> createCollection(boolean useMap) {
        if (useMap) {
            return new MapContainer<K,V>();
        } else {
            return new ListContainer<K,V>();
        }
    }
    public static <K,V> CollectionContainer<K, V> of(List<V> list) {
        return new ListContainer<K,V>(list);
    }
    public static <K,V> CollectionContainer<K, V> of(Map<K, V> map) {
        return new MapContainer<K,V>(map);
    }
}
