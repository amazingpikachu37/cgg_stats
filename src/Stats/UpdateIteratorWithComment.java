package Stats;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class UpdateIteratorWithComment implements CountingIterator {
    private final boolean[] thread_allow_list = new boolean[AllThreads.all_threads_with_combined.size()];
    private final boolean[] counter_allow_list = new boolean[Counters.counters.size()];
    private final CountingThread iterated_thread;
    private UUID uuid1;
    private final UUID uuid2;
    private Integer start_update;
    private final Integer end_update;
    private final boolean use_counts;
    private AllUpdateTypeIterator<UpdateOrCountValue> count_iterator;
    private AllUpdateTypeIterator<UpdateBody> comment_iterator;
    private UpdateOrCountValueWithComment next_count = null;
    private boolean override = false;
    public UpdateIteratorWithComment(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean use_counts) {
        Arrays.fill(counter_allow_list, true);
        for(CountingThread thread:threads) {
            thread_allow_list[thread.thread.id] = true;
            if(thread.thread.id==0) Arrays.fill(thread_allow_list, true);
        }
        if(threads.size() > 1) {
            this.iterated_thread = AllThreads.combined_thread;
        } else {
            this.iterated_thread = threads.get(0);
        }
        this.uuid1 = uuid1;
        this.uuid2 = uuid2;
        this.start_update = null;
        this.end_update = null;
        this.use_counts = use_counts;
    }
    public UpdateIteratorWithComment(List<CountingThread> threads, int start_update, int end_update, boolean use_counts) {
        Arrays.fill(counter_allow_list, true);
        for(CountingThread thread:threads) {
            thread_allow_list[thread.thread.id] = true;
            if(thread.thread.id==0) Arrays.fill(thread_allow_list, true);
        }
        if(threads.size() > 1) {
            this.iterated_thread = AllThreads.combined_thread;
        } else {
            this.iterated_thread = threads.get(0);
        }
        this.uuid1 = null;
        this.uuid2 = null;
        this.start_update = start_update;
        this.end_update = end_update;
        this.use_counts = use_counts;
    }
    public UpdateIteratorWithComment init() {
        if(uuid1 != null) {
            count_iterator = new AllUpdateTypeIterator<>(iterated_thread, uuid1, uuid2, use_counts ? CountDataType.COUNT : CountDataType.UPDATE);
            comment_iterator = new AllUpdateTypeIterator<>(iterated_thread, uuid1, uuid2, use_counts ? CountDataType.COUNT_BODY : CountDataType.UPDATE_BODY);
        } else {
            assert start_update != null && end_update != null;
            count_iterator = new AllUpdateTypeIterator<>(iterated_thread, start_update, end_update, use_counts ? CountDataType.COUNT : CountDataType.UPDATE);
            comment_iterator = new AllUpdateTypeIterator<>(iterated_thread, start_update, end_update, use_counts ? CountDataType.COUNT_BODY : CountDataType.UPDATE_BODY);
        }
        count_iterator.init();
        comment_iterator.init();
        this.override = true;
        try {
            next();
        } catch (NoSuchElementException ignored) {}
        this.override = false;
        return this;
    }
    public void changeStartUUID(UUID start) {
        uuid1 = start;
    }
    public void changeStartUpdate(int start_update) {
        this.start_update = start_update;
    }
    public List<CountingThread> getIteratedThreads() {
        return List.of(iterated_thread);
    }
    public void setCounterAllowList(List<Integer> counter_ids) {
        Arrays.fill(counter_allow_list, false);
        for(int id: counter_ids) {
            counter_allow_list[id] = true;
        }
    }

    public void setThreadAllowList(List<Integer> counter_ids) {
        Arrays.fill(thread_allow_list, false);
        for(int id: counter_ids) {
            thread_allow_list[id] = true;
        }
    }
    @Override
    public boolean hasNext() {
        return next_count!=null;
    }
    @Override
    public UpdateOrCountValueWithComment next() {
        if(this.hasNext() || override) {
            UpdateOrCountValueWithComment output = next_count;
            do {
                if(!count_iterator.hasNext()) this.next_count = null;
                else {
                    UpdateOrCountValue next = count_iterator.next();
                    UpdateBody comment = comment_iterator.next();
                    next_count = new UpdateOrCountValueWithComment(next, comment);
                }
            } while(next_count != null && (!thread_allow_list[next_count.getThreadId()] || !counter_allow_list[next_count.getAuthorId()]));
            return output;
        } else {
            throw new NoSuchElementException();
        }
    }

}
