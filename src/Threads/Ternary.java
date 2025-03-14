package Threads;

import java.util.List;

public class Ternary extends Statable {
    public Ternary() {
        super("ternary", List.of("ternary"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(729, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return Integer.toString(num, 3);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/729+1)*729)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/ternary"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(81, 729, 0);
    }
}
