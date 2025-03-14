package Threads;

import java.util.List;

public class NonaryDouble extends DoubleCountingThread {
    public NonaryDouble() {
        super("nonary_double", List.of("nonary_double"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(729, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return Integer.toString(num, 9);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/729+1)*729)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/nonary_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(81, 729, 0);
    }
}
