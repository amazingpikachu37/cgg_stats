package Threads;

import Stats.CountingThread;
import Stats.ThreadData;

public class _2048 extends CountingThread {
    public _2048(ThreadData t) {
        super(t);
    }

    @Override
    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        StringBuilder count = new StringBuilder();
        int mask = 0b1 << 31;
        for(int i=0;i<32;i++) {
            if((mask&num)!=0)
                count.append(mask<<1).append(" ");
            mask>>>=1;
        }
        return count.toString().trim();
    }
}
