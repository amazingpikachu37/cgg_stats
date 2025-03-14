package Threads;

import java.util.List;

public class Base64Double extends DoubleCountingThread {
    public Base64Double() {
        super("base_64_double", List.of("base_64_double"), "America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(1024,0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        StringBuilder count = new StringBuilder();
        int exp = 64;
        while(num > 0) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
            count.insert(0, chars.charAt(num%exp));
            num /= exp;
        }
        return count.toString();
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1024+1)*1024)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/base_64_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(64, 1024, 0);
    }
}
