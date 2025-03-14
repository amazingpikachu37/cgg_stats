package Threads;

import java.util.*;

public class ListContainer<K,T> implements CollectionContainer<K,T> {
    private final List<T> list;
    protected ListContainer() {
        this.list = new ArrayList<>();
    }
    protected ListContainer(List<T> list) {
        this.list = list;
    }
    @Override
    public void add(K key, T value) {
        list.add(value);
    }

    @Override
    public T getOrDefault(K key, T defaultValue) {
        return defaultValue;
    }

    @Override
    public T get(K key) {
        return null;
    }

    @Override
    public List<T> toList() {
        return list;
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public Set<K> keySet() {
        return new HashSet<>();
    }

    @Override
    public void addAll(Collection<? extends T> c) {
        list.addAll(c);
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }
}
