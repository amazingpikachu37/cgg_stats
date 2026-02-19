package Threads;

import java.util.List;

public class RationalsDouble extends Statable {
    public RationalsDouble() {
        super("rationals_double", List.of("rationals_double"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(1000, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        int t = (int)Math.floor((-1+Math.sqrt(8*num-7))/2);
        int i = num-t*(t+1)/2;
        int j = (t*t+3*t+4)/2-num;
        int count1 = (int)((pow(-1,t)+1)*i-(pow(-1,t)-1)*j)/2;
        int count2 = (int)((pow(-1,t)+1)*j-(pow(-1,t)-1)*i)/2;;
        return count1+"/"+count2;
    }
    protected static long pow(long base, int exp) {
        if(base==-1) return (exp%2)==0 ? 1 : -1;
        if(exp==0) return 1;
        if(base==0) return 0;
        if(base==1) return 1;
        long subtotal = 1;
        while(exp>1) {
            if(exp%2==1) {
                subtotal*=base;
                exp--;
            }
            base*=base;
            exp/=2;
        }
        return base*subtotal;
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1000+1)*1000)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/rationals_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(100, 1000, 0);
    }
}
