package Threads;

import java.util.List;

public class _2048 extends Statable {
    public _2048() {
        super("2048", List.of("2048"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(3844, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        StringBuilder count = new StringBuilder();
        int mask = 0b1 << 31;
        for(int i=0;i<32;i++) {
            if((mask&num)!=0)
                count.append(mask<<1).append(" ");
            mask>>>=1;
        }
        return count.toString().trim();
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1024+1)*1024)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/2048"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(64, 1024, 0);
    }
}
