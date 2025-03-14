package Threads;

import java.util.List;

public class NoRepeatingDigitsDouble extends DoubleCountingThread {
    public NoRepeatingDigitsDouble() {
        super("no_repeating_digits_double", List.of("no_repeating_digits_double"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(841, 0);
    }

    public String getCount(int num) {
        if(num <= 0 || num > 8877690) throw new IllegalArgumentException();
        num--;
        char[] str = new char[]{'1','2','3','4','5','6','7','8','9'};
        int permutations = this.factorial(str.length);
        int i;
        for(i=0;i<=str.length;i++) {
            int size_i_permutations = str.length*permutations/this.factorial(str.length-i);
            if(num >= size_i_permutations) {
                num-=size_i_permutations;
            } else break;
        }
        return findNthPermutationNoLeadingZero(str, i+1, num);
    }
    private int factorial(int n) {
        int result = 1;
        while(n>0) {
            result *= n;
            n--;
        }
        return result;
    }
    private String findNthPermutationNoLeadingZero(char[] set, int k, int n) {
        n *= this.factorial(set.length-k+1);
        int factorial = this.factorial(set.length);
        int index = n/factorial;
        String newPrefix = String.valueOf(set[index]);
        String chars = String.valueOf(set);
        chars = "0"+chars.substring(0,index)+chars.substring(index+1);
        return findNthPermutation(chars.toCharArray(), newPrefix, k-1, n%factorial);
    }
    private String findNthPermutation(char[] set, String prefix, int k, int n) {
        if(k==0) {
            return prefix;
        }
        int factorial = this.factorial(set.length-1);
        int index = n/factorial;
        String newPrefix = prefix+set[index];
        String chars = String.valueOf(set);
        chars = chars.substring(0,index)+chars.substring(index+1);
        return findNthPermutation(chars.toCharArray(), newPrefix, k-1, n%factorial);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/841+1)*841)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/no_repeating_digits_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(29, 841, 0);
    }
}
