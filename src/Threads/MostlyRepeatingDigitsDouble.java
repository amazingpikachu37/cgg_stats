package Threads;

import java.util.List;

public class MostlyRepeatingDigitsDouble extends Statable {
    public MostlyRepeatingDigitsDouble() {
        super("mostly_repeating_digits_double", List.of("mostly_repeating_digits_double"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(500, 0);
    }

    public String getCount(int num) {
        //Not a possible condition with integers, but included in case this ever gets changed to be with a long or BigInt
        //if(num > 10_000_000_000) return num-8877690
        num--;
        StringBuilder count = new StringBuilder();
        int counter;
        for(counter=0;counter<=9;counter++) {
            int size_i_permutations = (int)(pow(10,counter+1)-pow(10,counter)-(9*nPr(9, counter)));
            //System.out.println(num+" "+size_i_permutations);
            if(num >= size_i_permutations) {
                num-=size_i_permutations;
            } else break;
        }
        int[] freq = new int[10];
        end:
        for(int i=0;i<counter+1;i++) {
            if(i==counter) {
                for(int j=0;j<10;j++) {
                    if(freq[j]!=0) {
                        if(num==0) {
                            count.append(j);
                            break end;
                        }
                        num--;
                    }
                }
            }
            int step_a;
            int step_b = (int)pow(10,counter-i);
            int digit = 0;
            if(i==0) {
                step_a = (int)(step_b-nPr(9,counter));
                digit=1;
            }
            else step_a = (int)(step_b-nPr(9-i,counter-i));
            // System.out.println(counter+" "+num+" "+step_a+" "+step_b+" "+i+" "+count);
            while(num>=step_a) {
                if(freq[digit] == 1) {
                    if(num >= step_b) {
                        num-=step_b;
                        digit++;
                    } else {
                        count.append(digit);
                        while(count.length()<counter+1-String.valueOf(num).length()) count.append(0);
                        count.append(num);
                        break end;
                    }
                } else {
                    num-=step_a;
                    digit++;
                }
            }
            count.append(digit);
            freq[digit]++;
            if(freq[digit]>1) {
                while(count.length()<counter+1-String.valueOf(num).length()) count.append(0);
                count.append(num);
                break end;
            }
        }

        return count.toString();
    }
    protected static long nPr(int n, int r) {
        long total=1;
        r=n-r;
        while(n>r) {
            total*=n;
            n--;
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
    private int factorial(int n) {
        int result = 1;
        while(n>0) {
            result *= n;
            n--;
        }
        return result;
    }

    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/500+1)*500)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/mostly_repeating_digits_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(50, 500, 0);
    }
}
