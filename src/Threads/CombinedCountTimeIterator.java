package Threads;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public class CombinedCountTimeIterator extends CountIterator {
    private Statable last_count_thread = null;
    private final List<Statable> threads;
    private final UUID uuid1;
    private final UUID uuid2;
    private final TreeMap<CountValue, CountTimeIterator> next_counts = new TreeMap<>((CountValue k1,CountValue k2) -> Long.signum(k1.getUUID().getTime()-k2.getUUID().getTime()));
    public CombinedCountTimeIterator(List<Statable> threads, UUID uuid1, UUID uuid2) {
        this.threads = threads;
        this.uuid1 = uuid1;
        this.uuid2 = uuid2;
    }

    public CombinedCountTimeIterator init() {
        for(Statable thread:threads) {
            CountTimeIterator thread_counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            if(thread_counts.hasNext()) {
                next_counts.put(thread_counts.next(), thread_counts);
            }
        }
        return this;
    }

    @Override
    public boolean hasNext() {
        return next_counts.size() > 0;
    }
    @Override
    public CountValue next() {
        if(this.hasNext()) {
            Map.Entry<CountValue, CountTimeIterator> next_count = next_counts.pollFirstEntry();
            if (next_count.getValue().hasNext()) {
                next_counts.put(next_count.getValue().next(), next_count.getValue());
            }
            last_count_thread = next_count.getValue().getThread();
            return next_count.getKey();
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public Statable getThread() {
        return last_count_thread;
    }
    @Override
    public Statable getDefaultThread() {
        return CombinedStats.all_threads_map.get("no_counting");
    }
}
