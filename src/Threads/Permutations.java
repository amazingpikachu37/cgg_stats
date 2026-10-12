package Threads;

import Stats.CountingThread;
import Stats.ThreadData;
import Stats.Util;

import java.util.ArrayList;
import java.util.Arrays;

public class Permutations extends CountingThread {
    public Permutations(ThreadData t) {
        super(t);
    }

    @Override
    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        num--;
        ArrayList<Character> str = new ArrayList<>(Arrays.asList('1','2','3','4','5','6','7','8','9','A','B','C'));
        int i;
        for(i=0;i<=str.size();i++) {
            int size_i_permutations = (int) Util.factorial(i+1);
            if(num >= size_i_permutations) {
                num-=size_i_permutations;
            } else break;
        }
        i++;
        StringBuilder output = new StringBuilder();
        for(int j=0;j<i;j++) {
            int fact = (int)Util.factorial(i-j-1);
            output.append(str.get(num / fact));
            str.remove(num/fact);
            num%=fact;
        }
        return output.toString();
    }
}
