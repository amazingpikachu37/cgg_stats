package Threads;
import java.util.List;
public class TrifectaIterator {
    private final CountTimeIterator[] iterators;
    private long current_bar;
    private final CountValue[] counts;
    public TrifectaIterator(List<Statable> threads, UUID uuid1, UUID uuid2) {
        iterators = new CountTimeIterator[threads.size()];
        counts = new CountValue[threads.size()];
        uuid1 = Util.roundUUIDToLowerHour(uuid1);
        for(int i=0;i<threads.size();i++) {
            iterators[i] = new CountTimeIterator(threads.get(i), uuid1, uuid2);
        }
        current_bar = uuid1.getTime()-36000000000L; //Include the current hour trifectas
    }
    public TrifectaIterator init() {
        for(CountTimeIterator iterator: iterators) {
            iterator.init();
        }
        return this;
    }
    public long getCurrentBar() {
        return this.current_bar;
    }
    public CountValue[] next() {
        current_bar += 36000000000L;
        int found_next = 0;
        CountValue[] ret_counts = new CountValue[3];
        for(int i=0;i<iterators.length;i++) {
            if (counts[i] == null || counts[i].getUUID().getTime() < current_bar) {
                while (iterators[i].hasNext()) {
                    counts[i] = iterators[i].next();
                    if (counts[i].getUUID().getTime() >= current_bar) {
                        found_next++;
                        break;
                    }
                }
            } else {
                found_next++;
            }
            if(counts[i]!=null) {
                if (counts[i].getUUID().getTime() >= current_bar + 36000000000L) {
                    ret_counts[i] = null;
                } else {
                    ret_counts[i] = counts[i];
                }
            }
        }
        if(found_next==0) {
            return null;
        }
        return ret_counts;
    }
}
