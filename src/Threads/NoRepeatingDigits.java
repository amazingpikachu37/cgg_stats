package Threads;

import Stats.CountingThread;
import Stats.ThreadData;
import Stats.Util;

public class NoRepeatingDigits extends CountingThread {
    public NoRepeatingDigits(ThreadData t) {
        super(t);
    }
    public String getCount(int num) {
        if(num <= 0 || num > 8877690) throw new IllegalArgumentException();
        num--;
        char[] str = new char[]{'1','2','3','4','5','6','7','8','9'};
        int permutations = (int) Util.factorial(str.length);
        int i;
        for(i=0;i<=str.length;i++) {
            int size_i_permutations = str.length*permutations/(int)Util.factorial(str.length-i);
            if(num >= size_i_permutations) {
                num-=size_i_permutations;
            } else break;
        }
        return Util.findNthPermutationNoLeadingZero(str, i+1, num);
    }
}
