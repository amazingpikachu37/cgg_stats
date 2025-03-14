package Threads;

import java.util.*;

public class MapContainer<K,V> implements CollectionContainer<K,V> {
    private final Map<K, V> map;
    protected MapContainer() {
        this.map = new HashMap<>();
    }
    protected MapContainer(Map<K,V> map) {
        this.map = map;
    }
    @Override
    public void add(K key, V value) {
        map.put(key, value);
    }
    @Override
    public V getOrDefault(K key, V defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    @Override
    public List<V> toList() {
        return new ArrayList<>(map.values());
    }

    @Override
    public boolean isList() {
        return false;
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }
    @Override
    public void addAll(Collection<? extends V> c) {}
}
