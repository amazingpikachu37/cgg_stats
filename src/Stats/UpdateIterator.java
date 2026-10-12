package Stats;

import java.util.*;
import java.util.function.Predicate;

public class UpdateIterator implements CountingIterator {
    private final boolean[] thread_allow_list = new boolean[AllThreads.all_threads_with_combined.size()];
    private final boolean[] counter_allow_list = new boolean[Counters.counters.size()];
    private Predicate<UpdateOrCountValue> custom_filter = k->true;
    private final List<CountingThread> iterated_threads;
    private UUID uuid1;
    private final UUID uuid2;
    private Integer start_update;
    private final Integer end_update;
    private final boolean use_counts;
    private boolean override = false;
    private UpdateOrCountValue next_count = null;
    private final TreeMap<UpdateOrCountValue, AllUpdateTypeIterator<UpdateOrCountValue>> next_counts = new TreeMap<>((UpdateOrCountValue k1, UpdateOrCountValue k2) -> Long.signum(k1.getTimestamp()-k2.getTimestamp()));



    public UpdateIterator(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean use_counts) {
        Arrays.fill(counter_allow_list, true);
        for(CountingThread thread:threads) {
            thread_allow_list[thread.thread.id] = true;
            if(thread.thread.id==0) Arrays.fill(thread_allow_list, true);
        }
        if(threads.size() > 10) {
            this.iterated_threads = List.of(AllThreads.combined_thread);
        } else {
            this.iterated_threads = threads;
        }
        this.uuid1 = uuid1;
        this.uuid2 = uuid2;
        this.start_update = null;
        this.end_update = null;
        this.use_counts = use_counts;
    }
    public UpdateIterator(List<CountingThread> threads, TimeUnit start_date, TimeUnit end_date, ParticipationDuration mode, boolean use_counts) {
        Arrays.fill(counter_allow_list, true);
        for(CountingThread thread:threads) {
            thread_allow_list[thread.thread.id] = true;
            if(thread.thread.id==0) Arrays.fill(thread_allow_list, true);
        }
        if(threads.size() > 10) {
            this.iterated_threads = List.of(AllThreads.combined_thread);
        } else {
            this.iterated_threads = threads;
        }
        this.uuid1 = Util.getUUIDFromDate(start_date, CountingThread.timezone);
        this.uuid2 = Util.getUUIDFromDate(end_date.addTime(1), CountingThread.timezone);
        this.start_update = null;
        this.end_update = null;
        this.use_counts = use_counts;
    }

    public UpdateIterator(List<CountingThread> threads, int start_update, int end_update, boolean use_counts) {
        Arrays.fill(counter_allow_list, true);
        for(CountingThread thread:threads) {
            thread_allow_list[thread.thread.id] = true;
            if(thread.thread.id==0) Arrays.fill(thread_allow_list, true);
        }
        if(threads.size() > 10) {
            this.iterated_threads = List.of(AllThreads.combined_thread);
        } else {
            this.iterated_threads = threads;
        }
        this.uuid1 = null;
        this.uuid2 = null;
        this.start_update = start_update;
        this.end_update = end_update;
        this.use_counts = use_counts;
    }
    public void changeStartUUID(UUID start) {
        this.uuid1 = start;
    }
    public void changeStartUpdate(int start_update) {
        this.start_update = start_update;
    }
    public List<CountingThread> getIteratedThreads() {
        return iterated_threads;
    }

    public void setCounterAllowList(List<Integer> counter_ids) {
        Arrays.fill(counter_allow_list, false);
        for(int id: counter_ids) {
            counter_allow_list[id] = true;
        }
        if(next_count!=null && !counter_allow_list[next_count.getAuthorId()]) next();
    }

    public void setThreadAllowList(List<Integer> counter_ids) {
        Arrays.fill(thread_allow_list, false);
        for(int id: counter_ids) {
            thread_allow_list[id] = true;
        }
        if(next_count!=null && !thread_allow_list[next_count.getThreadId()]) next();
    }
    public void setFilter(Predicate<UpdateOrCountValue> filter) {
        this.custom_filter = filter;
        if(next_count!=null && !custom_filter.test(next_count)) next();
    }

    public UpdateIterator init() {
        for(CountingThread thread:iterated_threads) {
            AllUpdateTypeIterator<UpdateOrCountValue> thread_counts;
            if(uuid1 != null) {
                if(uuid1.getTime()>=uuid2.getTime()) return this;
                thread_counts = new AllUpdateTypeIterator<>(thread, uuid1, uuid2, use_counts ? CountDataType.COUNT : CountDataType.UPDATE);
            } else {
                if(start_update>end_update) return this;
                thread_counts = new AllUpdateTypeIterator<>(thread, start_update, end_update, use_counts ? CountDataType.COUNT : CountDataType.UPDATE);
            }
            thread_counts.init();
            if(thread_counts.hasNext()) {
                next_counts.put(thread_counts.next(), thread_counts);
            }
        }
        if(uuid1 != null) System.out.println("Init with "+uuid1.getTime()+" "+uuid2.getTime());
        else System.out.println("Init with "+start_update+" "+end_update);
        this.override = true;
        try {
           next();
        } catch (NoSuchElementException ignored) {}
        this.override = false;
        return this;
    }

    @Override
    public boolean hasNext() {
        return next_count!=null;
    }
    @Override
    public UpdateOrCountValue next() {
        if(this.hasNext() || override) {
            UpdateOrCountValue output = next_count;
            do {
                Map.Entry<UpdateOrCountValue, AllUpdateTypeIterator<UpdateOrCountValue>> next = next_counts.pollFirstEntry();
                if(next==null) next_count=null;
                else if (next.getValue().hasNext()) {
                    next_counts.put(next.getValue().next(), next.getValue());
                    next_count = next.getKey();
                }  else next_count = next.getKey();
            } while(next_count != null && (!custom_filter.test(next_count) || !thread_allow_list[next_count.getThreadId()] || !counter_allow_list[next_count.getAuthorId()]));
            return output;
        } else {
            throw new NoSuchElementException();
        }
    }
}
