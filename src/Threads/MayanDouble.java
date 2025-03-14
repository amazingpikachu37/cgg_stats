package Threads;

import java.util.List;

public class MayanDouble extends DoubleCountingThread {
    public MayanDouble() {
        super("mayan_double", List.of("mayan_double"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(400, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        StringBuilder count = new StringBuilder();
        int exp = 20;
        while(num > 0) {
            StringBuilder digit = new StringBuilder().append(';');
            int val = num%20;
            if(val != 0) {
                while (val > 0) {
                    if (val >= 5) {
                        digit.append('-');
                        val-=5;
                    } else {
                        digit.append('.');
                        val--;
                    }
                }
            }
            count.insert(0,digit);
            num/=exp;
        }
        return count.substring(1);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/400+1)*400)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/mayan_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(20, 400, 0);
    }
}
