package Threads;

import java.util.List;

public class Hexadecimal extends Statable {
    public Hexadecimal() {
        super("hexadecimal", List.of("hexadecimal"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(1024, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return Integer.toString(num, 16);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1024+1)*1024)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/hexadecimal"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(64, 1024, 0);
    }
}
