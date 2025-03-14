package Threads;

import java.util.List;

public class Evens extends Statable {
    public Evens() {
        super("evens", List.of("evens"), "America/Montreal");
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
        return new String[]{"https://counting.gg/thread/evens"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(50, 500, 0);
    }
}
