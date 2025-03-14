package Threads;

import java.util.List;

public class Countdown extends Statable {
    public Countdown() {
        super("countdown", List.of("countdown"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(1000, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return String.valueOf(1000001-num);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount(((this.getLast_number()-1)/1000+1)*1000+1)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/countdown"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(100, 1000, 0);
    }
}
