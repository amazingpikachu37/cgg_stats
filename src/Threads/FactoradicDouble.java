package Threads;

import java.util.List;

public class FactoradicDouble extends DoubleCountingThread {
    public FactoradicDouble() {
        super("factoradic_double", List.of("factoradic_double"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(720, 0);
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
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/720+1)*720)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/factoradic_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(120, 720, 0);
    }
}
