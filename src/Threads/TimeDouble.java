package Threads;

import java.util.List;

public class TimeDouble extends DoubleCountingThread {
    public TimeDouble() {
        super("time_double", List.of("time_double"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(900, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        String TOD = "AM";
        int hours = num%86400/3600;
        if(hours >= 12) {
            TOD = "PM";
            hours -= 12;
        }
        if(hours==0) hours=12;
        int minutes = num%3600/60;
        int seconds = num%60;
        return String.format("%01d:%02d:%02d %s",hours,minutes,seconds,TOD);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/900+1)*900)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/time_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(60, 900, 0);
    }
}
