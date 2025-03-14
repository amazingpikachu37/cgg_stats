package Threads;

import java.util.List;

public class Septenary extends Statable {
    public Septenary() {
        super("septenary", List.of("septenary"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(1029, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return Integer.toString(num, 7);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1029+1)*1029)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/septenary"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(49, 1029, 0);
    }
}
