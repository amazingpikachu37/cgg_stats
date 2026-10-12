package Threads;

import Stats.ThreadData;

public class Wait2 extends BaseN {
    public Wait2(ThreadData data, int n, int offset, int increment, String base_string, boolean contains_zero) {
        super(data, n, offset, increment, base_string, contains_zero);
    }
    @Override
    public int doubleCountInterval() {
        return 3;
    }
}
