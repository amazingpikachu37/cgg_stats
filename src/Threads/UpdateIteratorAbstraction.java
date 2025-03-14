package Threads;

import java.util.Iterator;

public abstract class UpdateIteratorAbstraction implements Iterator<UpdateValue> {
    public abstract Statable getThread();
    public Statable getDefaultThread() {
        return getThread();
    }
    public abstract UpdateIteratorAbstraction init();
}
