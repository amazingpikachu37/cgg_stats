package Threads;

import Stats.CountingThread;
import Stats.ThreadData;

public class Time extends CountingThread {
    public Time(ThreadData t) {
        super(t);
    }
    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        String TOD = "AM";
        int hours = num%86400/3600;
        if(hours >= 12) {
            TOD = "PM";
            hours -= 12;
        }
        if(hours==0) hours=12;
        int minutes = num%3600/60;
        int seconds = num%60;
        return String.format("%01d:%02d:%02d %s",hours,minutes,seconds,TOD);
    }
}
