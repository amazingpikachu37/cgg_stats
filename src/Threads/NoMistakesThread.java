package Threads;

import java.util.ArrayList;
import java.util.List;

public abstract class NoMistakesThread extends Statable {
    private int lastValidCountNumber = this.getTotal_counts();

    public NoMistakesThread(String name, List<String> live_threads) {
        super(name, live_threads);
    }

    public NoMistakesThread(String name, List<String> live_threads, String timezone) {
        super(name, live_threads, timezone);
    }
    @Override
    protected void modifyValidCountNumber(UpdateValue num) {
        num.setValidCountNumber(++this.lastValidCountNumber);
    }
    public record IntIntPair(int value1, int value2) implements NonUnique {
        @Override
        public int compareTo(NonUnique o) {
            if(!(o instanceof IntIntPair p)) throw new IllegalArgumentException();
            int compare = p.value1-this.value1;
            if(compare != 0) return compare;
            return this.value2-p.value2;
        }
        public String toString() {
            return value1+"|"+value2;
        }

        @Override
        public String user() {
            return String.valueOf(value1);
        }
    }
    public List<IntIntPair> peak_no_mistakes_counts(UUID uuid1, UUID uuid2) {
        UpdateIterator updates = new UpdateIterator(this, uuid1, uuid2).init();
        List<IntIntPair> peaks = new ArrayList<>();
        int last_count = 0;
        int current_peak = 0;
        while(updates.hasNext()) {
            UpdateValue update = updates.next();
            if(update.isIsCount()) {
                if(update.isStricken() && last_count!=0) {
                    peaks.add(new IntIntPair(last_count-1, current_peak));
                    last_count = 0;
                } else {
                    current_peak = update.getValidCountNumber();
                    last_count++;
                }
            }

        }
        return peaks;
    }
}
