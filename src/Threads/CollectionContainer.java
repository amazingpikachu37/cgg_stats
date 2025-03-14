package Threads;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CollectionContainer<K,V> {
    void add(K key, V value);
    V getOrDefault(K key, V defaultValue);
    V get(K key);
    boolean containsKey(K key);
    List<V> toList();
    boolean isList();
    Set<K> keySet();
    void addAll(Collection<? extends V> c);
}
