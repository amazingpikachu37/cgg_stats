package Threads;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepeatingDigitsDouble extends Statable {
    public RepeatingDigitsDouble() {
        super("repeating_digits_double", List.of("repeating_digits_double"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(500, 0);
    }

    public String getCount(int num) {
        long repLenK;
        Map<int[], Long> patternCache = new HashMap<>();
        StringBuilder count = new StringBuilder();
        int counter = 2;
        while((repLenK=RepeatingDigits.totalRepDigitsLenNBaseK(counter,10,false)) < num) {
            num-=(int)repLenK;
            counter++;
        }
        long step = repLenK/9;
        int digit = (int)((num-1)/step+1);
        num-=(int)((digit-1)*step);
        count.append(digit);
        for(int i=1;i<counter;i++) {
            digit=0;
            for(int z=0;z<10;z++) {
                int[] pattern = RepeatingDigits.generatePattern(count.toString()+z);
                pattern[0] = counter - i - 1;
                boolean cached_step = false;
                for(int[] old_pattern: patternCache.keySet()) {
                    if(Arrays.equals(pattern, old_pattern)) {
                        step = patternCache.get(old_pattern);
                        cached_step = true;
                        break;
                    }
                }
                if(!cached_step) step = RepeatingDigits.getStepSize(pattern);
                if(num>=step) {
                    if(num==step) break;
                    num-=(int)step;
                    digit++;
                    patternCache.put(pattern, step);
                } else break;
            }
            count.append(digit);
        }
        return count.toString();
    }

    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/500+1)*500)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/repeating_digits_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(50, 500, 0);
    }
}
