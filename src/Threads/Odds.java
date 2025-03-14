package Threads;

import java.util.List;

public class Odds extends Statable {
    public Odds() {
        super("odds", List.of("odds"), "America/Montreal");
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
        return new String[]{"https://counting.gg/thread/odds"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(50, 500, -1);
    }
}
