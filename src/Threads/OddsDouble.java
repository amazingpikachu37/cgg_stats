package Threads;

import java.util.List;

public class OddsDouble extends DoubleCountingThread {
    public OddsDouble() {
        super("odds_double", List.of("odds_double"), "America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(500,-1);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return Integer.toString(num*2-1);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount(((this.getLast_number()-1)/500+1)*500+1)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/odds_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(50, 500, -1);
    }
}
