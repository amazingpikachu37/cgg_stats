package Threads;

import java.util.List;

public class SeptenaryDouble extends DoubleCountingThread {
    public SeptenaryDouble() {
        super("septenary_double", List.of("septenary_double"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(1029, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return Integer.toString(num, 7);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1029+1)*1029)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/septenary_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(49, 1029, 0);
    }
}
