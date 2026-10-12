package Threads;

import Stats.CountingThread;
import Stats.ThreadData;

public class Mayan extends CountingThread {
    public Mayan(ThreadData t) {
        super(t);
    }
    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        StringBuilder count = new StringBuilder();
        int exp = 20;
        while(num > 0) {
            StringBuilder digit = new StringBuilder().append(';');
            int val = num%20;
            if(val != 0) {
                while (val > 0) {
                    if (val >= 5) {
                        digit.append('-');
                        val-=5;
                    } else {
                        digit.append('.');
                        val--;
                    }
                }
            }
            count.insert(0,digit);
            num/=exp;
        }
        return count.substring(1);
    }
}
