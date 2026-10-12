package Threads;

import Stats.CountingThread;
import Stats.ThreadData;

public class BaseN extends CountingThread {
    private final int base;
    private final int offset;
    private final int increment;
    private final String base_string;
    private final boolean contains_zero;
    public BaseN(ThreadData data, int n, int offset, int increment, String base_string, boolean contains_zero) {
        super(data);
        this.base = n;
        this.offset = offset;
        this.increment = increment;
        this.base_string = base_string;
        this.contains_zero = contains_zero;
    }
    public String getCount(int num) {
        num*=increment;
        num+=offset;
        if(num==0) return contains_zero ? base_string.substring(0,1) : "";
        boolean negative = false;
        if(num<0) {
            negative = true;
            num*=-1;
        }
        int zero_offset = contains_zero ? 0 : 1;
        StringBuilder count = new StringBuilder();
        while(num > 0) {
            int val = (num-zero_offset)%base;
            count.insert(0, base_string.charAt(val));
            num -= val;
            num /= base;
        }
        if(negative) count.insert(0, "-");
        return count.toString();
    }
    public int parse(String raw_count) {
        int current_num = 0;
        int exp = 1;
        int zero_offset = contains_zero ? 0 : 1;
        for(int i=raw_count.length()-1;i>=0;i--) {
            int val = base_string.indexOf(raw_count.charAt(i));
            if(val==-1) return -1;
            current_num+=(val+zero_offset)*exp;
            exp*=base;
        }
        return (current_num-offset)/increment;
    }
}
