package Threads;

import java.util.ArrayList;
import java.util.List;

public class NoMistakesOrBan extends NoMistakesThread {
    public NoMistakesOrBan() {
        super("no_mistakes_or_ban", List.of("no_mistakes_or_ban"), "America/Montreal");
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
        return new String[]{"https://counting.gg/thread/no_mistakes_or_ban"};
    }

    public List<String> getBanList() {
        List<String> ban_list = new ArrayList<>();
        UpdateIterator updates = new UpdateIterator(this,new UUID("00000000-0000-4000-a000-000000000000"),new UUID("99999999-9999-4999-a999-999999999999")).init();
        while(updates.hasNext()) {
            UpdateValue update = updates.next();
            if(update.isStricken()) {
                ban_list.add(update.getAuthor());
            }
        }
        return ban_list;
    }
    @Override
    public List<List<Integer>> getSplitValues() {
        throw new UnsupportedOperationException();
    }
}
