package Threads;

import Stats.CountingThread;
import Stats.ThreadData;

public class Wave extends CountingThread {
    public Wave(ThreadData t) {
        super(t);
    }
    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        int iter = (int)Math.floor((3+Math.sqrt(8*num))/2-1);
        int goal;
        if(iter%2==0) goal = -1*iter/2;
        else goal = (iter-1)/2;
        int count = num - iter*(iter-1)/2;
        if(goal <= 0) count = (-1*goal+1) - count;
        else count = (-1*goal-1) + count;
        return count+" ("+goal+")";
    }
}
