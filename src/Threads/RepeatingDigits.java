package Threads;

import Stats.CountingThread;
import Stats.ThreadData;
import Stats.Util;

import java.util.ArrayList;
import java.util.List;

public class RepeatingDigits extends CountingThread {
    private final static List<List<Long>> repDigitCache = new ArrayList<>();
    private final static List<List<List<Long>>> stepCache = new ArrayList<>();
    private final static List<Long> repDigitLengthCache = new ArrayList<>();
    public RepeatingDigits(ThreadData t) {
        super(t);
    }
    static {
        generateRepeatingDigitsCache(12);
    }

    public static void generateRepeatingDigitsCache(int max_length) {
        //Outer loop: free characters
        //Middle loop: single characters
        //Inner loop: doubled characters
        for(int i=0;i<=max_length;i++) {
            repDigitLengthCache.add(totalRepDigitsLenNBaseK(i,10,false));
            List<List<Long>> current = new ArrayList<>();
            for(int j=0;j<=max_length-i && j<=i;j++) {
                List<Long> current_2 = new ArrayList<>();
                for(int k=0;k<=(max_length-i-j)/2;k++) {
                    current_2.add(getStepSize(new int[]{i,j,k}));
                }
                current.add(current_2);
            }
            stepCache.add(current);
        }
    }
    public String getCount(int num) {
        long repLenK;
        StringBuilder count = new StringBuilder();
        int counter = 2;
        while((repLenK=repDigitLengthCache.get(counter)) < num) {
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
                int[] pattern = generatePattern(count.toString()+z);
                pattern[0] = counter - i - 1;
                if(pattern[1]>pattern[0]) step = 0;
                else step = stepCache.get(pattern[0]).get(pattern[1]).get(pattern[2]);
                if(num>step) {
                    num-=(int)step;
                    digit++;
                } else break;
            }
            count.append(digit);
        }
        return count.toString();
    }
    private static int[] generatePattern(String str) {
        int[] pattern = new int[3];
        int[] freq_table = new int[10];
        char[] chars = str.toCharArray();
        for(char c: chars) {
            freq_table[c-'0']++;
        }
        for(int i=0;i<10;i++) {
            int freq = freq_table[i];
            if(freq>1) {
                pattern[2]++;
            } else if(freq==1) {
                pattern[1]++;
            }
        }
        return pattern;
    }

    private static long totalRepDigitsLenNBaseK(int n, int k, boolean include_zero) {
        if(n==0) return 1;
        if(n<0 || n==1 || k<=0) return 0;
        if(include_zero && repDigitCache.size() > (n-2) && repDigitCache.get(n-2).size() > k
                && repDigitCache.get(n-2).get(k) >= 0) {
            return repDigitCache.get(n-2).get(k);
        }
        long total=0;
        for (int i = 0; i <= k; i++) {
            total += Util.nPr(k-1,i) * stirling2Order2(n, i + 1);
        }
        while(repDigitCache.size() <= (n-2)) repDigitCache.add(new ArrayList<>());
        List<Long> current = repDigitCache.get(n-2);
        while(current.size() <= k) current.add(-1L);
        current.set(k,total*k);
        return total*(include_zero ? k : (k-1));
    }
    private static long totalRepDigitsLenNBaseKIncludingLDigits(int n, int k, int l) {
        if(2*l>n) return 0;
        long total = 0;
        for(int i=0;i<=l;i++) {
            total+=Util.pow(-1,i)*totalRepDigitsLenNBaseK(n, k-i, true)*Util.nCr(l,i);
        }
        return total;
    }

    private static long stirling2(int n, int k) {
        long total=0;
        for(int i=0;i<=k;i++) {
            total+=Util.pow(-1,k-i)*Util.pow(i,n)*Util.nCr(k,i);
        }
        return total/Util.factorial(k);
    }

    private static long stirling2Order2(int n, int k) {
        if(k>n/2) return 0;
        long total=0;
        for(int i=0;i<=k;i++) {
            total+=Util.nCr(n,k-i)*stirling2(n-k+i, i)*Util.pow(-1,i);
        }
        return Math.abs(total);
    }
    private static long getStepSize(int[] pattern) {
        long step = 0;
        int free_digits = pattern[0];
        for (int j = 0; j <= pattern[2]; j++) {
            long substep = 0;
            for (int k = 0; k <= pattern[1]; k++) {
                substep += Util.nPr(free_digits-j, k) * Util.nCr(pattern[1], k)
                        * totalRepDigitsLenNBaseKIncludingLDigits(free_digits-j-k, 10-j-k,pattern[1]-k);
            }
            step += Util.nPr(free_digits, j) * Util.nCr(pattern[2], j) * substep;
        }
        return step;
    }
}
