package Threads;

import Stats.CountingThread;
import Stats.ThreadData;
import Stats.Util;

public class MostlyRepeatingDigits extends CountingThread {
    public MostlyRepeatingDigits(ThreadData t) {
        super(t);
    }
    public String getCount(int num) {
        //Not a possible condition with integers, but included in case this ever gets changed to be with a long or BigInt
        //if(num >= 10_000_000_000) return num-8877690
        num--;
        StringBuilder count = new StringBuilder();
        int counter;
        for(counter=0;counter<=9;counter++) {
            int size_i_permutations = (int)(Util.pow(10,counter+1)-Util.pow(10,counter)-(9*Util.nPr(9, counter)));
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
            int step_b = (int)Util.pow(10,counter-i);
            int digit = 0;
            if(i==0) {
                step_a = (int)(step_b-Util.nPr(9,counter));
                digit=1;
            }
            else step_a = (int)(step_b-Util.nPr(9-i,counter-i));
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
                break;
            }
        }

        return count.toString();
    }
}
