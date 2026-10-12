package Stats;

import java.util.Iterator;
import java.util.List;

public interface CountingIterator extends Iterator<UpdateOrCountValue> {
    CountingIterator init();
    void changeStartUUID(UUID start);
    void changeStartUpdate(int start_update);
    List<CountingThread> getIteratedThreads();
    void setCounterAllowList(List<Integer> allow_list);
    void setThreadAllowList(List<Integer> allow_list);
}
