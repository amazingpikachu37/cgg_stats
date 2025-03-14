package Threads;

import java.util.List;

public abstract class DoubleCountingThread extends Statable {
    public DoubleCountingThread(String name, List<String> live_threads) {
        super(name, live_threads);
    }
    public DoubleCountingThread(String name, List<String> live_threads, String timezone) {
        super(name, live_threads, timezone);
    }
    @Override
    public int doubleCountInterval() {
        return 1;
    }
}
