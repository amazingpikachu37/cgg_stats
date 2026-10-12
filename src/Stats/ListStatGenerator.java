package Stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ListStatGenerator<K> {
    private final StatContainer final_stat;
    private final StatContainer[] temp_stat = new StatContainer[AllThreads.all_threads_with_combined.size()];
    private final List<K> data;
    private int index;
    private final Object[] default_object; //Contains default values for temp data
    private final StatDataType[] stat_types;
    private final Function<Object[], Object[]> finalize;
    private final int thread_insert_location;
    private final boolean use_counter;
    private final boolean use_counts;
    private final boolean add_thread_stats;
    private final List<Integer> focus_loc = new ArrayList<>();

    public ListStatGenerator(List<K> data, Function<Object[], Object[]> finalize, Object[] default_object, String stat_name, String[] headers, StatDataType[] stat_types, int[] sort_order, boolean unique, boolean use_counter, boolean use_count, boolean thread_counts_combined, boolean add_thread_stats) {
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
        this.data = data;
        index = 0;
        this.default_object = default_object;
        this.stat_types = stat_types;
        this.use_counter = use_counter;
        this.use_counts = use_count;
        final_stat = new StatContainer(this.finalize.apply(default_object), stat_name, headers, stat_types, sort_order, unique, use_counter, use_count, thread_counts_combined, add_thread_stats, StatContainer.StatSaveMode.NO_SAVE);
        for(int i=0;i<AllThreads.all_threads_with_combined.size();i++) {
            temp_stat[i] = new StatContainer(this.finalize.apply(default_object), stat_name, headers, stat_types, sort_order, unique, use_counter, use_count, thread_counts_combined, add_thread_stats, StatContainer.StatSaveMode.NO_SAVE);
        }
        this.add_thread_stats = add_thread_stats;
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
        return data.size() > index;
    }
    public K next() {
        if(hasNext()) {
            return data.get(index++);
        } else {
            throw new NoSuchElementException();
        }
    }

    public void addTempObject(int focus_index, int thread_index, Object... o) {
        UpdateOrCountValue u = UpdateOrCountValue.getDummyObject(use_counts, focus_index, thread_index);
        temp_stat[final_stat.useThreadInsert() ? thread_index : 0].addTempObject(u, o);
    }
    public void addTempObject(int focus_index_1, int focus_index_2, int thread_index, Object... o) {
        UpdateOrCountValue u1 = UpdateOrCountValue.getDummyObject(use_counts, focus_index_1, thread_index);
        UpdateOrCountValue u2 = UpdateOrCountValue.getDummyObject(use_counts, focus_index_2, thread_index);
        temp_stat[final_stat.useThreadInsert() ? thread_index : 0].addTempObject(u1, u2, o);
    }

    //This always ensures when a temp update is created, the update is at the end of the temp update.
    public Object[] getTempObject(int focus_index, int thread_index) {
        UpdateOrCountValue u = UpdateOrCountValue.getDummyObject(use_counts, focus_index, thread_index);
        Object[] output = temp_stat[final_stat.useThreadInsert() ? thread_index : 0].getTempObject(u);
        if(output==null) {
            output = default_object.clone();
            output[output.length-1] = u;
            addTempObject(focus_index, thread_index, output);
        }
        return output;
    }
    public Object[] getTempObject(int focus_index_1, int focus_index_2, int thread_index) {
        UpdateOrCountValue u1 = UpdateOrCountValue.getDummyObject(use_counts, focus_index_1, thread_index);
        UpdateOrCountValue u2 = UpdateOrCountValue.getDummyObject(use_counts, focus_index_2, thread_index);
        Object[] output = temp_stat[final_stat.useThreadInsert() ? thread_index : 0].getTempObject(u1, u2);
        if(output==null) {
            output = default_object.clone();
            output[output.length-1] = u1;
            output[output.length-2] = u2;
            addTempObject(focus_index_1, focus_index_2, thread_index, output);
        }
        return output;
    }
    public void finalizeObject(int focus_index, int thread_index) {
        UpdateOrCountValue u = UpdateOrCountValue.getDummyObject(use_counts, focus_index, thread_index);
        temp_stat[final_stat.useThreadInsert() ? thread_index : 0].finalizeObject(u, finalize);
    }
    public void finalizeObject(int focus_index_1, int focus_index_2, int thread_index) {
        UpdateOrCountValue u1 = UpdateOrCountValue.getDummyObject(use_counts, focus_index_1, thread_index);
        UpdateOrCountValue u2 = UpdateOrCountValue.getDummyObject(use_counts, focus_index_2, thread_index);
        temp_stat[final_stat.useThreadInsert() ? thread_index : 0].finalizeObject(u1, u2, finalize);
    }
    public StatContainer finalizeStat() {
        if(add_thread_stats) {
            for(StatContainer temp: temp_stat) temp.finalizeAll(finalize);
            return StatContainer.add(Arrays.asList(temp_stat));
        }
        for (StatContainer temp : temp_stat) {
            temp.finalizeAll(finalize);
            for(Object[] user: temp.getStat()) {
                if(focus_loc.size()==1) {
                    UpdateOrCountValue u = UpdateOrCountValue.getDummyObject(use_counts, (int) user[focus_loc.get(0)], (int) user[thread_insert_location]);
                    final_stat.addTempObject(u, user);
                    final_stat.finalizeObject(u, k -> k);
                } else if(focus_loc.size()==2) {
                    UpdateOrCountValue u1 = UpdateOrCountValue.getDummyObject(use_counts, (int) user[focus_loc.get(0)], (int) user[thread_insert_location]);
                    UpdateOrCountValue u2 = UpdateOrCountValue.getDummyObject(use_counts, (int) user[focus_loc.get(1)], (int) user[thread_insert_location]);
                    final_stat.addTempObject(u1, u2, user);
                    final_stat.finalizeObject(u1, u2, k -> k);
                }
            }
            temp.clear();
        }
        return final_stat;
    }
}
