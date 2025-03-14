package Threads;

import java.util.List;

public class SenaryDouble extends DoubleCountingThread {
    public SenaryDouble() {
        super("senary_double", List.of("senary_double"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(1296, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return Integer.toString(num, 6);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1296+1)*1296)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/senary_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(108, 1296, 0);
    }
}
