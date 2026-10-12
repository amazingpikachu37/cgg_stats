package Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class StatGenerator {
    private StatContainer final_stat;
    private StatContainer temp_stat;
    private final List<CountingIterator> iterators = new ArrayList<>();
    private final List<CountingThread> threads;
    private final String stat_name;
    private final String[] headers;
    private final StatDataType[] stat_types;
    private final Object[] default_object; //Contains default values for temp data
    private final int[] sort_order; //Negative element in sort_order implies reversed sort. This is 1-indexed
    private final boolean unique; //true for unique, false for non unique
    private final boolean use_counter; //true for counter, false for thread
    private final boolean use_count; //true for count, false for update
    private final Function<Object[], Object[]> finalize;
    private final boolean thread_counts_combined; //true if all counts are treated as from the same thread, false otherwise
    private final List<Integer> focus_loc = new ArrayList<>();
    private final int thread_insert_location;
    private final UUID start_uuid;
    private final UUID end_uuid;
    private UpdateOrCountValue last_count = null;
    private boolean switched = false;
    private final boolean add_thread_stats;
    private final StatContainer.StatSaveMode save_mode;

    public StatGenerator(List<CountingThread> threads, UUID uuid1, UUID uuid2, Function<Object[], Object[]> finalize, Object[] default_object, String stat_name, String[] headers, StatDataType[] stat_types, int[] sort_order, boolean unique, boolean use_counter, boolean use_count, boolean thread_counts_combined, boolean include_comments, boolean add_thread_stats, StatContainer.StatSaveMode save_mode) {
        this.threads = threads;
        this.start_uuid = uuid1;
        this.end_uuid = uuid2;
        int thread_insert_index = -1;
        for(int i=0;i<stat_types.length;i++) {
            if(stat_types[i]==StatDataType.FOCUS) {
                focus_loc.add(i);
            } else if(stat_types[i]==StatDataType.THREAD_INSERT) {
                thread_insert_index=i;
            }
        }
        thread_insert_location = thread_insert_index;
        this.finalize = setFinalize(finalize);
        this.default_object = default_object;
        this.stat_name = stat_name;
        this.headers = headers;
        this.stat_types = stat_types;
        this.sort_order = sort_order;
        this.unique = unique;
        this.use_counter = use_counter;
        this.use_count = use_count;
        this.thread_counts_combined = thread_counts_combined;
        this.add_thread_stats = add_thread_stats;
        this.save_mode = save_mode;
        final_stat = new StatContainer(this.finalize.apply(default_object), stat_name, headers, stat_types, sort_order, unique, use_counter, use_count, thread_counts_combined, add_thread_stats, save_mode);
        if(!thread_counts_combined) {
            for(CountingThread thread:threads) {
                if(include_comments) {
                    iterators.add(new UpdateIteratorWithComment(List.of(thread), uuid1, uuid2, use_count));
                } else {
                    iterators.add(new UpdateIterator(List.of(thread), uuid1, uuid2, use_count));
                }
            }
            temp_stat = StatContainer.load(threads.get(0), uuid1, uuid2, iterators.get(0), this.finalize.apply(default_object), stat_name, headers, stat_types, sort_order, unique, use_counter, use_count, thread_counts_combined, save_mode);
        } else {
            if(include_comments) {
                iterators.add(new UpdateIteratorWithComment(threads, uuid1, uuid2, use_count));
            } else {
                iterators.add(new UpdateIterator(threads, uuid1, uuid2, use_count));
            }
            temp_stat = StatContainer.load(threads, uuid1, uuid2, iterators.get(0), this.finalize.apply(default_object), stat_name, headers, stat_types, sort_order, unique, use_counter, use_count, thread_counts_combined, save_mode);
        }
        iterators.get(0).init();
    }

    public void setAllowedCounters(List<Integer> counters) {
        if(use_counter) {
            for (CountingIterator iterator : iterators) {
                iterator.setCounterAllowList(counters);
            }
        } else {
            for (CountingIterator iterator : iterators) {
                iterator.setThreadAllowList(counters);
            }
        }
    }

    public Function<Object[], Object[]> setFinalize(Function<Object[], Object[]> f) {
        return k -> {
            Object[] o = f.apply(k);
            Object[] p = new Object[o.length + focus_loc.size() + ((thread_insert_location>=0) ? 1 : 0)];
            int skip = 0;
            int focus_index = 1;
            for(int i=0;i<p.length;i++) {
                if(stat_types[i] == StatDataType.FOCUS) {
                    p[i] = k[k.length-focus_index] == null ? -1 : use_counter ? ((UpdateOrCountValue) k[k.length-focus_index]).getAuthorId() : ((UpdateOrCountValue) k[k.length-focus_index]).getThreadId();
                    focus_index++;
                    skip++;
                } else if(stat_types[i] == StatDataType.THREAD_INSERT) {
                    //Should only be null when finalizing default object
                    p[i] = k[k.length-1] == null ? -1 : ((UpdateOrCountValue) k[k.length-1]).getThreadId();
                    skip++;
                } else p[i] = o[i-skip];
            }
            return p;
        };
    }

    public boolean hasNext() {
        while(!iterators.isEmpty()) {
            if(iterators.get(0).hasNext()) return true;
            if(!thread_counts_combined) temp_stat.save(iterators.get(0).getIteratedThreads().get(0), last_count, start_uuid, end_uuid, finalize);
            else temp_stat.save(threads, last_count, start_uuid, end_uuid, finalize);
            if(add_thread_stats) {
                temp_stat.setAddThreadStats(true);
                final_stat = StatContainer.add(List.of(final_stat, temp_stat));
            }
            else final_stat.addAll(temp_stat);
            iterators.remove(0);
            if(iterators.isEmpty()) break;
            if(!thread_counts_combined) temp_stat = StatContainer.load(iterators.get(0).getIteratedThreads().get(0), start_uuid, end_uuid, iterators.get(0), this.finalize.apply(default_object), stat_name, headers, stat_types, sort_order, unique, use_counter, use_count, thread_counts_combined, save_mode);
            else temp_stat = StatContainer.load(threads, start_uuid, end_uuid, iterators.get(0), this.finalize.apply(default_object), stat_name, headers, stat_types, sort_order, unique, use_counter, use_count, thread_counts_combined, save_mode);
            iterators.get(0).init();
            switched = true;
        }
        return false;
    }
    public UpdateOrCountValue next() {
        if(hasNext()) {
            last_count = iterators.get(0).next();
            return last_count;
        } else {
            throw new NoSuchElementException();
        }
    }
    public void addTempObject(UpdateOrCountValue u, Object... o) {
        temp_stat.addTempObject(u, o);
    }
    public void addTempObject(UpdateOrCountValue u1, UpdateOrCountValue u2, Object... o) {
        temp_stat.addTempObject(u1, u2, o);
    }

    //This always ensures when a temp update is created, the update is at the end of the temp update.
    public Object[] getTempObject(UpdateOrCountValue u) {
        Object[] output = temp_stat.getTempObject(u);
        if(output==null) {
            output = default_object.clone();
            output[output.length-1] = u;
            addTempObject(u, output);
        }
        return output;
    }
    public Object[] getTempObject(UpdateOrCountValue u1, UpdateOrCountValue u2) {
        Object[] output = temp_stat.getTempObject(u1, u2);
        if(output==null) {
            output = default_object.clone();
            output[output.length-1] = u1;
            output[output.length-2] = u2;
            addTempObject(u1, u2, output);
        }
        return output;
    }
    public void finalizeObject(UpdateOrCountValue u) {
        temp_stat.finalizeObject(u, finalize);
    }
    public void finalizeObject(UpdateOrCountValue u1, UpdateOrCountValue u2) {
        temp_stat.finalizeObject(u1, u2, finalize);
    }
    public StatContainer finalizeStat() {
        return final_stat;
    }
    public boolean switched() {
        boolean output = switched;
        switched = false;
        return output;
    }
}
