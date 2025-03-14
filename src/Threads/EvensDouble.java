package Threads;

import java.util.List;

public class EvensDouble extends DoubleCountingThread {
    public EvensDouble() {
        super("evens_double", List.of("evens_double"), "America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(500,0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return Integer.toString(num*2);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount(((this.getLast_number())/500+1)*500)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/evens_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(50, 500, 0);
    }
}
