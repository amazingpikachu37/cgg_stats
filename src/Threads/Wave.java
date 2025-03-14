package Threads;

import java.util.List;

public class Wave extends Statable {
    public Wave() {
        super("wave", List.of("wave"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(1000, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        int iter = (int)Math.floor((3+Math.sqrt(8*num))/2-1);
        int goal;
        if(iter%2==0) {
            goal = -1*iter/2;
        } else {
            goal = (iter-1)/2;
        }
        int count = num - iter*(iter-1)/2;
        if(goal <= 0) {
            count = (-1*goal+1) - count;
        } else {
            count = (-1*goal-1) + count;
        }
        return count+" ("+goal+")";
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1000+1)*1000)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/wave"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(100, 1000, 0);
    }
}
