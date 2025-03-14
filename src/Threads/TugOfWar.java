package Threads;

import java.util.List;

public class TugOfWar extends Statable {
    public TugOfWar() {
        super("tug_of_war", List.of("tug_of_war"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(1000, 0);
    }

    public String getCount(int num) {
        throw new UnsupportedOperationException();
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1000+1)*1000)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/tug_of_war"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(100, 1000, 0);
    }
}
