package Threads;

import java.util.List;

public class Test extends Statable {
    public Test() {
        super("test", List.of("test"), "America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(1000, 0);
//        List<Integer> gets = new ArrayList<>();
//        for(int i=0;i<=this.getMaxCountLocation().getChatFile();i++) {
//            List<CountValue> nums = this.getFlattenedCountsNumByFile(i, 0, this.getLast_number());
//            for (int j=0;j<nums.size();j++) {
//                try {
//                    if (Integer.parseInt(nums.get(j).getRawCount()) % 1000 == 0) {
//                        gets.add(nums.get(j).getValidCountNumber());
//                        j+=999; //Minimum amount of counts between gets
//                    }
//                } catch (NumberFormatException ignored) {
//                }
//            }
//        }
//        return gets;
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return Integer.toString(num);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1000+1)*1000)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/test"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(100, 1000, 0);
    }
}
