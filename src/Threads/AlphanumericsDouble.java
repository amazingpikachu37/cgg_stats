package Threads;

import java.util.List;

public class AlphanumericsDouble extends DoubleCountingThread {
    public AlphanumericsDouble() {
        super("alphanumerics_double", List.of("alphanumerics_double"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(1296, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return Integer.toString(num, 36);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1296+1)*1296)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/alphanumerics_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(36, 1296, 0);
    }
}
