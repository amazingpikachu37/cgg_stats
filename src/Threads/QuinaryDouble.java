package Threads;

import java.util.List;

public class QuinaryDouble extends DoubleCountingThread {
    public QuinaryDouble() {
        super("quinary_double", List.of("quinary_double"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(625, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return Integer.toString(num, 5);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/625+1)*625)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/quinary_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(125, 625, 0);
    }
}
