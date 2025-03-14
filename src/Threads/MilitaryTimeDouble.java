package Threads;

import java.util.List;

public class MilitaryTimeDouble extends DoubleCountingThread {
    public MilitaryTimeDouble() {
        super("military_time_double", List.of("military_time_double"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(900, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        int hours = num%86400/3600;
        int minutes = num%3600/60;
        int seconds = num%60;
        return String.format("%02d:%02d:%02d",hours,minutes,seconds);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/900+1)*900)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/military_time_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(60, 900, 0);
    }
}
