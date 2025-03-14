package Threads;

import java.util.List;

public class Letters extends Statable {
    public Letters() {
        super("letters", List.of("letters"), "America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(676, -27);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        StringBuilder count = new StringBuilder();
        int exp = 26;
        while(num > 0) {
            int val = (num-1)%26;
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            count.insert(0, chars.charAt(val));
            num-=val;
            num/=exp;
        }
        return count.toString();
    }
    public String getNextGet() {
        return "The next get is `"+ getCount(((this.getLast_number()-27)/676+1)*676+27)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/letters"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(26, 676, -27);
    }
}
