package Threads;

import java.util.List;

public class RussianRoulette extends Statable {
    public RussianRoulette() {
        super("russian_roulette", List.of("russian_roulette"), "America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(1000,0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return Integer.toString(num);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1000+1)*1000)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/russian_roulette"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(100, 1000, 0);
    }
}
