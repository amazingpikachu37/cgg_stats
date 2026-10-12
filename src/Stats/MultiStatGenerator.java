package Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class MultiStatGenerator {
    private final List<StatContainer> final_stat = new ArrayList<>();
    private List<StatContainer> temp_stat = new ArrayList<>();
    private final List<UpdateIterator> iterators = new ArrayList<>();
    private final List<CountingThread> threads;
    private final String stat_name;
    private final String[] headers;
    private final StatDataType[] stat_types;
    private final Object[] default_object; //Contains default values for non-focus elements
    private final int[] sort_order; //Negative element in sort_order implies reversed sort. This is 1-indexed
    private final boolean unique; //true for unique, false for non unique
    private final boolean use_counter; //true for counter, false for thread
    private final boolean use_count; //true for count, false for update
    private final Function<Object[], Object[]> finalize;
    private final boolean thread_counts_combined; //true if all counts are treated as from the same thread, false otherwise
    private final int thread_insert_location; //
    private final int focus_loc;
    private int index;
    private boolean switched = false;
    private UpdateOrCountValue last_count = null;
    private final StatLoader.SaveType save_type;
    private final StatContainer.StatSaveMode save_mode;

    public MultiStatGenerator(List<CountingThread> threads, TimeUnit date1, TimeUnit date2, ParticipationDuration mode, Function<Object[], Object[]> finalize, Object[] default_object, String stat_name, String[] headers, StatDataType[] stat_types, int[] sort_order, boolean unique, boolean use_counter, boolean use_count, boolean thread_counts_combined, StatContainer.StatSaveMode save_mode) {
        this.save_type = StatLoader.SaveType.UUID;
        this.threads = threads;
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
        this.save_mode = save_mode;

        if(!thread_counts_combined) {
            for(CountingThread thread:threads) {
                iterators.add(new UpdateIterator(List.of(thread), date1, date2, mode, use_count));
            }
            temp_stat = StatContainer.loadBatch(iterators.get(0).getIteratedThreads().get(0), iterators.get(0), stat_name, unique, use_counter, use_count);
        } else {
            iterators.add(new UpdateIterator(threads, date1, date2, mode, use_count));
            temp_stat = StatContainer.loadBatch(threads, iterators.get(0), stat_name, unique, use_counter, use_count);
        }
        iterators.get(0).init();
        int focus_index = -1;
        int thread_insert_index = -1;
        for(int i=0;i<stat_types.length;i++) {
            if(stat_types[i]==StatDataType.FOCUS && focus_index==-1) {
                focus_index=i;
            } else if(stat_types[i]==StatDataType.THREAD_INSERT) {
                thread_insert_index=i;
            }
        }
        focus_loc = focus_index;
        thread_insert_location = thread_insert_index;
    }

    public MultiStatGenerator(List<CountingThread> threads, int start_k, int end_k, int k_size, Function<Object[], Object[]> finalize, Object[] default_object, String stat_name, String[] headers, StatDataType[] stat_types, int[] sort_order, boolean unique, boolean use_counter, boolean use_count, boolean thread_counts_combined) {
        this.save_type = StatLoader.SaveType.COUNT;
        this.threads = threads;
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
        this.save_mode = StatContainer.StatSaveMode.CONTINUE;
        for(CountingThread thread:threads) {
            int counts_per_k = thread.thread.countsPerSplit*thread.thread.splitsPerGet*k_size;
            int offset = thread.thread.splitOffset;
            if(!thread_counts_combined) {
                if (thread.thread.resetOnMistakes) {
                    iterators.add(new UpdateIterator(List.of(thread), 0, thread.getTotalCounts(), use_count));
                    iterators.get(iterators.size() - 1).setFilter(count -> {
                        int k = ((CountValue) count).getK(k_size);
                        return k >= start_k && k < end_k;
                    });
                } else {
                    int start = start_k == 0 ? 0 : (int)Math.min(Integer.MAX_VALUE-1, (long)start_k * counts_per_k + offset);
                    int end = (int)Math.min(Integer.MAX_VALUE-1, (long)end_k * counts_per_k + offset);
                    iterators.add(new UpdateIterator(List.of(thread), start, end, use_count));
                }
            }
        }
        if(thread_counts_combined) {
            //Could reduce counts searched by creating a new iterator type that "stacks" iterators of each thread on top of each other, but that's a lot of work for a stat type that will rarely get used and still isn't too slow
            iterators.add(new UpdateIterator(threads, 0, AllThreads.combined_thread.getTotalCounts(), use_count));
            iterators.get(0).setFilter(count -> {
                int k = ((CountValue) count).getK(k_size);
                return k >= start_k && k < end_k;
            });
        }
        if(!thread_counts_combined) {
            temp_stat = StatContainer.loadBatch(iterators.get(0).getIteratedThreads().get(0), iterators.get(0), stat_name, unique, use_counter, use_count);
        } else {
            temp_stat = StatContainer.loadBatch(threads, iterators.get(0), stat_name, unique, use_counter, use_count);
        }
        iterators.get(0).init();
        int focus_index = -1;
        int thread_insert_index = -1;
        for(int i=0;i<stat_types.length;i++) {
            if(stat_types[i]==StatDataType.FOCUS && focus_index==-1) {
                focus_index=i;
            } else if(stat_types[i]==StatDataType.THREAD_INSERT) {
                thread_insert_index=i;
            }
        }
        focus_loc = focus_index;
        thread_insert_location = thread_insert_index;
    }


    public Function<Object[], Object[]> setFinalize(Function<Object[], Object[]> f) {
        return k -> {
            Object[] o = f.apply(k);
            Object[] p = new Object[o.length + ((focus_loc>=0) ? 1 : 0) + ((thread_insert_location>=0) ? 1 : 0)];
            int skip = 0;
            for(int i=0;i<p.length;i++) {
                if(stat_types[i] == StatDataType.FOCUS) {
                    p[i] = k[k.length-1] == null ? -1 : use_counter ? ((UpdateOrCountValue) k[k.length-1]).getAuthorId() : ((UpdateOrCountValue) k[k.length-1]).getThreadId();
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
            if(!thread_counts_combined) {
                StatContainer.saveBatch(temp_stat, iterators.get(0).getIteratedThreads().get(0), last_count, finalize, save_type, save_mode);
            } else {
                StatContainer.saveBatch(temp_stat, threads, last_count, finalize, save_type, save_mode);
            }
            for(int i=0;i<temp_stat.size();i++) {
                if(final_stat.size()<=i) final_stat.add(new StatContainer(this.finalize.apply(default_object), stat_name, headers, stat_types, sort_order, unique, use_counter, use_count, thread_counts_combined, false, StatContainer.StatSaveMode.CONTINUE));
                final_stat.get(i).addAll(temp_stat.get(i));
            }
            iterators.remove(0);
            if(!iterators.isEmpty()) {
                if(!thread_counts_combined) {
                    temp_stat = StatContainer.loadBatch(iterators.get(0).getIteratedThreads().get(0), iterators.get(0), stat_name, unique, use_counter, use_count);
                } else {
                    temp_stat = StatContainer.loadBatch(threads, iterators.get(0), stat_name, unique, use_counter, use_count);
                }
            }
            if(!iterators.isEmpty()) iterators.get(0).init();
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
        temp_stat.get(index).addTempObject(u, o);
    }

    public Object[] getTempObject(UpdateOrCountValue u) {
        Object[] output = temp_stat.get(index).getTempObject(u);
        if(output==null) {
            output = default_object.clone();
            output[output.length-1] = u;
            addTempObject(u, output);
        }
        return output;
    }
    public List<StatContainer> finalizeStat() {
        return final_stat;
    }
    public void setIndex(int index) {
        while(temp_stat.size()<=index) {
            temp_stat.add(new StatContainer(finalize.apply(default_object), stat_name, headers, stat_types, sort_order, unique, use_counter, use_count, thread_counts_combined, false, StatContainer.StatSaveMode.CONTINUE));
        }
        while(final_stat.size()<=index) {
            final_stat.add(new StatContainer(this.finalize.apply(default_object), stat_name, headers, stat_types, sort_order, unique, use_counter, use_count, thread_counts_combined, false, StatContainer.StatSaveMode.CONTINUE));
        }
        this.index = index;
    }
    public boolean switched() {
        boolean output = switched;
        switched = false;
        return output;
    }
}
