package Threads;

import java.util.*;

public class RepeatingDigits extends Statable {
    private final static List<List<Long>> repDigitCache = new ArrayList<>();

    public RepeatingDigits() {
        super("repeating_digits", List.of("repeating_digits"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(500, 0);
    }

    protected static long factorial(long n) {
        long result = 1;
        while(n>0) {
            result *= n;
            n--;
        }
        return result;
    }

    public String getCount(int num) {
        long repLenK;
        StringBuilder count = new StringBuilder();
        int counter = 2;
        while((repLenK=totalRepDigitsLenNBaseK(counter,10,false)) < num) {
            num-=(int)repLenK;
            counter++;
        }
        long step = repLenK/9;
        int digit = (int)((num-1)/step+1);
        num-=(int)((digit-1)*step);
        count.append(digit);
        for(int i=1;i<counter;i++) {
            digit=0;
            Map<int[], Long> patternCache = new HashMap<>();
            for(int z=0;z<10;z++) {
                int[] pattern = generatePattern(count.toString()+z);
                pattern[0] = counter - i - 1;
                boolean cached_step = false;
                for(int[] old_pattern: patternCache.keySet()) {
                    if(Arrays.equals(pattern, old_pattern)) {
                        step = patternCache.get(old_pattern);
                        cached_step = true;
                        break;
                    }
                }
                if(!cached_step) step = getStepSize(pattern);
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

    protected static long getStepSize(int[] pattern) {
        long step = 0;
        int free_digits = pattern[0];
        for (int j = 0; j <= pattern[2]; j++) {
            long substep = 0;
            for (int k = 0; k <= pattern[1]; k++) {
                substep += nPr(free_digits-j, k) * nCr(pattern[1], k)
                        * totalRepDigitsLenNBaseKIncludingLDigits(free_digits-j-k, 10-j-k,pattern[1]-k);
            }
            step += nPr(free_digits, j) * nCr(pattern[2], j) * substep;
        }
        return step;
    }

    protected static int[] generatePattern(String str) {
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

    protected static long totalRepDigitsLenNBaseK(int n, int k, boolean include_zero) {
        if(n<0) return 0;
        if(n==0) return 1;
        if(n==1) return 0;
        if(k<=0) return 0;
        if(include_zero && repDigitCache.size() > (n-2) && repDigitCache.get(n-2).size() > k
                && repDigitCache.get(n-2).get(k) >= 0) {
            return repDigitCache.get(n-2).get(k);
        }
        long total=0;
        for (int i = 0; i <= k; i++) {
            total += nPr(k-1,i) * stirling2Order2(n, i + 1);
        }
        while(repDigitCache.size() <= (n-2)) repDigitCache.add(new ArrayList<>());
        List<Long> current = repDigitCache.get(n-2);
        while(current.size() <= k) current.add(-1L);
        current.set(k,total*k);
        return total*(include_zero ? k : (k-1));
    }
    protected static long totalRepDigitsLenNBaseKIncludingLDigits(int n, int k, int l) {
        if(2*l>n) return 0;
        long total = 0;
        for(int i=0;i<=l;i++) {
            total+=pow(-1,i)*totalRepDigitsLenNBaseK(n, k-i, true)*nCr(l,i);
        }
        return total;
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
    protected static long nPr(int n, int r) {
        if(r>n || n<0 || r<0) return 0;
        long total=1;
        r=n-r;
        while(n>r) {
            total*=n;
            n--;
        }
        return total;
    }
    private static long nCr(int n, int r) {
        if(r>n || n<0 || r<0) return 0;
        long total=1;
        for(int i=r+1;i<=n;i++) {
            total*=i;
        }
        return total/factorial(n-r);
    }
    private static long stirling2(int n, int k) {
        long total=0;
        for(int i=0;i<=k;i++) {
            total+=pow(-1,k-i)*pow(i,n)*nCr(k,i);
        }
        return total/factorial(k);
    }

    private static long stirling2Order2(int n, int k) {
        if(k>n/2) return 0;
        long total=0;
        for(int i=0;i<=k;i++) {
            total+=nCr(n,k-i)*stirling2(n-k+i, i)*pow(-1,i);
        }
        return Math.abs(total);
    }

    protected static String getNthNextRepDigitAfterK(String k, int n) {
        int[] frequency = new int[10];
        char[] repDigit = k.toCharArray();
        for(char c: repDigit) {
            frequency[c-'0']++;
        }
        while(n>0) {
            n--;
            int free_chars = 0;
            end:
            for (int i = repDigit.length - 1; i >= 0; i--) {
                if (repDigit[i] == '9') {
                    frequency[9]--;
                    free_chars++;
                    continue;
                } else {
                    frequency[repDigit[i] - '0']--;
                    repDigit[i]++;
                    frequency[repDigit[i] - '0']++;
                }
                if (checkComplete(repDigit, frequency, free_chars, i)) break;

                free_chars++;
                frequency[repDigit[i] - '0']--;
                int num_ones = 0;
                for (int j = 0; j < 10; j++) {
                    if (frequency[j] == 1) {
                        num_ones++;
                    }
                }
                if (frequency[repDigit[i] - '0'] != 1 && num_ones - free_chars <= 0) {
                    for (int j = repDigit[i] - '0'; j < 10; j++) {
                        if (frequency[j] == 1 || (num_ones - free_chars < 0 && frequency[j] > 1)) {
                            free_chars--;
                            repDigit[i] = (char) ('0' + j);
                            frequency[j]++;
                            checkComplete(repDigit, frequency, free_chars, i); //Guaranteed to succeed
                            break end;
                        }
                    }
                }
            }
        }
        return String.copyValueOf(repDigit);
    }
    private static boolean checkComplete(char[] repDigit, int[] frequency, int free_chars, int i) {
        int num_ones = 0;
        for(int j=0;j<10;j++) {
            if(frequency[j]==1) {
                num_ones++;
            }
        }
        if(free_chars < num_ones) return false;
        if(free_chars-num_ones>=2) {
            if(frequency[0]==1) num_ones--;
            while(free_chars>num_ones) {
                i++;
                repDigit[i]='0';
                free_chars--;
                frequency[0]++;
            }
        } else if(free_chars-num_ones==1) {
            i++;
            free_chars--;
            for(int j=0;j<10;j++) {
                if(frequency[j]==1) {
                    repDigit[i]=(char)('0'+j);
                    i++;
                    repDigit[i]=(char)('0'+j);
                    free_chars--;
                    frequency[j]+=2;
                    break;
                } else if(frequency[j]>1) {
                    repDigit[i]=(char)('0'+j);
                    frequency[j]++;
                    break;
                }
            }
        }
        if(free_chars==0) return true;
        for(int j=0;j<10;j++) {
            if(frequency[j]==1) {
                i++;
                repDigit[i] = (char)('0'+j);
                frequency[j]++;
            }
        }
        return true;
    }

    protected static List<List<Integer>> integerPartitionNo1(int n) {
        List<List<Integer>> partitions = new ArrayList<>();
        List<Integer> current_partition = new ArrayList<>();
        current_partition.add(n);
        partitions.add(new ArrayList<>(current_partition));
        while(current_partition.size() < n) {
            int previous_num = 0;
            for(int i=current_partition.size()-1;i>=0;i--) {
                int current_num = current_partition.get(i);
                if(current_num>1) {
                    current_partition.set(i,current_num-1);
                    if(previous_num==0 || current_num==2) {
                        current_partition.add(1);
                    } else {
                        current_partition.set(i+1, previous_num+current_partition.size()-i-1);
                        current_partition = current_partition.subList(0,i+2);
                        int ultimate = current_partition.get(current_partition.size()-1);
                        int penultimate = current_partition.get(current_partition.size()-2);
                        while(ultimate > penultimate) {
                            current_partition.add(ultimate-penultimate);
                            current_partition.set(current_partition.size()-2, penultimate);

                            ultimate = current_partition.get(current_partition.size()-1);
                            penultimate = current_partition.get(current_partition.size()-2);
                        }
                    }
                    if(current_partition.get(current_partition.size()-1) != 1)
                        partitions.add(new ArrayList<>(current_partition));
                    break;
                }
                previous_num = current_num;
            }
        }
        return partitions;
    }

    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/500+1)*500)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/repeating_digits"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(50, 500, 0);
    }
}
