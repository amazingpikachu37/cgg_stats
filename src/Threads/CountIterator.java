package Threads;

import java.util.Iterator;

public abstract class CountIterator implements Iterator<CountValue> {
    public abstract Statable getThread();
    public Statable getDefaultThread() {
        return getThread();
    }
    public abstract CountIterator init();
}
