package Threads;

import Stats.CountingThread;
import Stats.ThreadData;

public class Test extends CountingThread {
    public Test(ThreadData t) {
        super(t);
    }
    public String getCount(int num) {
        if(num<=641) return String.valueOf(num);
        return String.valueOf(num-1);
    }
}
