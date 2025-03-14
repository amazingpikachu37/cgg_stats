package Threads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermutationsDouble extends DoubleCountingThread {
    public PermutationsDouble() {
        super("permutations_double", List.of("permutations_double"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(720, 0);
    }

    private int factorial(int n) {
        int result = 1;
        while(n>0) {
            result *= n;
            n--;
        }
        return result;
    }
    public String getCount(int num) {
        if(num <= 0 || num > 409113) throw new IllegalArgumentException();
        num--;
        ArrayList<Character> str = new ArrayList<>(Arrays.asList('1','2','3','4','5','6','7','8','9'));
        int i;
        for(i=0;i<=str.size();i++) {
            int size_i_permutations = this.factorial(i+1);
            if(num >= size_i_permutations) {
                num-=size_i_permutations;
            } else break;
        }
        i++;
        StringBuilder output = new StringBuilder();
        for(int j=0;j<i;j++) {
            int fact = this.factorial(i-j-1);
            output.append(str.get(num / fact));
            str.remove(num/fact);
            num%=fact;
        }
        return output.toString();
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/720+1)*720)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/permutations_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(120, 720, 0);
    }
}
