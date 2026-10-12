package Threads;

import Stats.CountingThread;
import Stats.ThreadData;

public class Factoradic extends CountingThread {
    public Factoradic(ThreadData t) {
        super(t);
    }
    public String getCount(int num) {
        if(num <= 0 || num >= 3628800) throw new IllegalArgumentException();
        StringBuilder output = new StringBuilder(10);
        for(int i=2;i<=10;i++) {
            int val = num % i;
            output.insert(0, val);
            num-=val;
            num/=i;
        }
        int nonzero_pointer = 0;
        String output_str = output.toString();
        for(char c:output_str.toCharArray()) {
            if(c=='0') nonzero_pointer++;
            else break;
        }
        return output_str.substring(nonzero_pointer);
    }
}
