package Threads;

import java.util.ArrayList;
import java.util.List;

public class NoMistakesDouble extends NoMistakesThread {
    public NoMistakesDouble() {
        super("no_mistakes_double", List.of("no_mistakes_double"), "America/Montreal");
    }
    @Override
    public int doubleCountInterval() {
        return 1;
    }

    @Override
    public List<Integer> getGetValues() {
        List<Integer> gets = new ArrayList<>();
        for(int i=0;i<=this.getMaxCountLocation().getChatFile();i++) {
            List<CountValue> nums = this.getFlattenedCountsNumByFile(i, 0, this.getLast_number());
            for (int j=0;j<nums.size();j++) {
                try {
                    if (Integer.parseInt(nums.get(j).getRawCount()) % 1000 == 0) {
                        gets.add(nums.get(j).getValidCountNumber());
                        j+=999; //Minimum amount of counts between gets
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return gets;
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return Integer.toString(num);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1000+1)*1000)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/no_mistakes_double"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        throw new UnsupportedOperationException();
    }
}
