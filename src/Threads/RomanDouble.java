package Threads;

import java.util.List;

public class RomanDouble extends DoubleCountingThread {
    public RomanDouble() {
        super("roman_double", List.of("roman_double"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(1000, 0);
    }

    public String getCount(int num) {
        if(num <= 0 || num >= 4000000) throw new IllegalArgumentException();
        StringBuilder count = new StringBuilder();
        int[] values = {1000000,900000,500000,400000,100000,90000,50000,40000,10000,9000,5000,4000,1000,900,500,400,100,90,50,40,10,9,5,4,1};
        String[] romanLetters = {"M̅","C̅M̅","D̅","C̅D̅","C̅","X̅C̅","L̅","X̅L̅","X̅","MX̅","V̅","MV̅","M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"};
        for(int i=0;i<values.length;i++) {
            while (num >= values[i]) {
                num-=values[i];
                count.append(romanLetters[i]);
            }
        }
        return count.toString();
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1000+1)*1000)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/roman_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(100, 1000, 0);
    }
}
