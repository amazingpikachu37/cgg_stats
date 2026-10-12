package Threads;

import Stats.CountingThread;
import Stats.ThreadData;
import Stats.Util;

public class Rationals extends CountingThread {
    public Rationals(ThreadData t) {
        super(t);
    }
    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        int t = (int)Math.floor((-1+Math.sqrt(8*num-7))/2);
        int i = num-t*(t+1)/2;
        int j = (t*t+3*t+4)/2-num;
        int count1 = (int)((Util.pow(-1,t)+1)*i-(Util.pow(-1,t)-1)*j)/2;
        int count2 = (int)((Util.pow(-1,t)+1)*j-(Util.pow(-1,t)-1)*i)/2;;
        return count1+"/"+count2;
    }
}
