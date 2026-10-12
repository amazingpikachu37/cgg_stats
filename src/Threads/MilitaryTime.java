package Threads;

import Stats.CountingThread;
import Stats.ThreadData;

public class MilitaryTime extends CountingThread {
    public MilitaryTime(ThreadData t) {
        super(t);
    }
    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        int hours = num%86400/3600;
        int minutes = num%3600/60;
        int seconds = num%60;
        return String.format("%02d:%02d:%02d",hours,minutes,seconds);
    }
}
