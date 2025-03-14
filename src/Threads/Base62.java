package Threads;

import java.util.List;

public class Base62 extends Statable {
    public Base62() {
        super("base_62", List.of("base_62"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(3844, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        StringBuilder count = new StringBuilder();
        int exp = 62;
        while(num > 0) {
            String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
            count.insert(0, chars.charAt(num%exp));
            num /= exp;
        }
        return count.toString();
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/3844+1)*3844)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/base_62"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(62, 3844, 0);
    }
}
