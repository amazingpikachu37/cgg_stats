package Threads;

import java.util.ArrayList;
import java.util.List;

public class NoCounting extends Statable {
    public NoCounting() {
        super("no_counting", List.of("no_counting"), "America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return new ArrayList<>();
    }

    public String getCount(int num) {
        throw new UnsupportedOperationException();
    }
    public String getNextGet() {
        return "There are no gets";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/no_counting"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        throw new UnsupportedOperationException();
    }
}
