package Stats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

public class StatContainer {
    private BalancedBinarySearchTree<Object[]> stat = new BalancedBinarySearchTree<>((a, b)->compare(b,a));
    private Map<Long, Object[]> temp_values = new HashMap<>(); //Supports 1d and 2d lookup (longs are twice as long as ints)
    private final Object[] default_object; //Default **FINAL** object, as opposed to temp object for stat generators. All entries in the stat must be at least as good as this.
    private Map<Long, Object[]> temp_final = new HashMap<>();
    public final String stat_name;
    private final String[] headers;
    private final StatDataType[] stat_types;
    private final int[] sort_order; //Negative element in sort_order implies reversed sort. This is 1-indexed
    private final boolean unique; //true for unique, false for non unique
    private final boolean use_counter; //true for counter, false for thread
    private final boolean use_count; //true for count, false for update
    private boolean add_thread_stats;
    private final List<Integer> focus_loc = new ArrayList<>(); //Guaranteed size > 0 for "unique" stats.
    private final boolean thread_counts_combined;
    private final int thread_insert_loc;
    private final StatSaveMode save_mode;

    public enum StatSaveMode {
        /**
         * Save the entire stat container before finalization, so it can be continued when more counts are downloaded.
         */
        CONTINUE,
        /**
         * Save the entire stat after finalization. The stat will be recomputed from scratch when more counts are downloaded.
         */
        NO_CONTINUE,
        /**
         * The stat does not get saved.
         */
        NO_SAVE
    }

    public StatContainer(Object[] default_object, String stat_name, String[] headers, StatDataType[] stat_types, int[] sort_order, boolean unique, boolean use_counter, boolean use_count, boolean thread_counts_combined, boolean add_thread_stats, StatSaveMode save_mode) {
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
        int thread_insert_index = -1;
        for(int i=0;i<stat_types.length;i++) {
            if(stat_types[i]==StatDataType.FOCUS) {
                focus_loc.add(i);
            } else if(stat_types[i]==StatDataType.THREAD_INSERT) {
                thread_insert_index=i;
            }
        }
        thread_insert_loc = thread_insert_index;
        this.save_mode = save_mode;
    }

    public List<Object[]> getStat() {
        return stat.toList();
    }
    public void setAddThreadStats(boolean add_thread_stats) {
        this.add_thread_stats = add_thread_stats;
    }

    public void addAll(StatContainer to_add) {
        List<Object[]> objects = to_add.getStat();
        if(!unique) {
            stat.addAll(objects);
        } else {
            for(Object[] object: objects) {
                if(focus_loc.size()==1) {
                    finalize(object, (int)object[focus_loc.get(0)]);
                } else if(focus_loc.size()==2) {
                    finalize(object, ((long) object[focus_loc.get(0)] << 32) + (long) object[focus_loc.get(1)]);
                }
            }
        }
    }
    private void finalize(Object[] final_object, long index) {
        if(compare(final_object, default_object)<=0 && sort_order.length>0) return;
        if(!unique) {
            stat.addNode(final_object);
        } else if(!temp_final.containsKey(index)) {
            stat.addNode(final_object);
            temp_final.put(index, final_object);
        } else if(compare(final_object, temp_final.get(index)) > 0) {
            stat.deleteNode(temp_final.get(index));
            stat.addNode(final_object);
            temp_final.put(index, final_object);
        }
    }
    public void finalizeObjectDirect(long index, Function<Object[], Object[]> f) {
        Object[] modified = f.apply(temp_values.get(index));
        finalize(modified, index);
    }
    public void finalizeObject(UpdateOrCountValue u, Function<Object[], Object[]> f) {
        long index = getIndex(u);
        Object[] modified = f.apply(temp_values.get(index));
        finalize(modified, index);
    }
    public void finalizeObject(UpdateOrCountValue u1, UpdateOrCountValue u2, Function<Object[], Object[]> f) {
        long index = getIndex(u1, u2);
        Object[] modified = f.apply(temp_values.get(index));
        finalize(modified, index);
    }
    public void finalizeAll(Function<Object[], Object[]> f) {
        for(long key: temp_values.keySet()) {
            Object[] modified = f.apply(temp_values.get(key));
            finalize(modified, key);
        }
        temp_values = new HashMap<>();
    }
    public void addTempObjectDirect(long index, Object... o) {
        temp_values.put(index, o);
    }
    public void addTempObject(UpdateOrCountValue u, Object... o) {
        temp_values.put(getIndex(u), o);
    }
    public void addTempObject(UpdateOrCountValue u1, UpdateOrCountValue u2, Object... o) {
        temp_values.put(getIndex(u1, u2), o);
    }
    public Object[] getTempObject(UpdateOrCountValue u) {
        return temp_values.getOrDefault(getIndex(u), null);
    }

    public Object[] getTempObject(UpdateOrCountValue u1, UpdateOrCountValue u2) {
        return temp_values.getOrDefault(getIndex(u1, u2), null);
    }
    private long getIndex(UpdateOrCountValue u) {
        if(use_counter) return u.getAuthorId();
        else if(thread_counts_combined) return 0L;
        else return u.getThreadId();
    }
    private long getIndex(UpdateOrCountValue u1, UpdateOrCountValue u2) {
        if(use_counter) return (((long)u1.getAuthorId())<<32)+(long)u2.getAuthorId();
        else if(thread_counts_combined) return 0L;
        else return (((long)u1.getThreadId())<<32)+(long)u2.getThreadId();
    }
    public void save(CountingThread thread, UpdateOrCountValue last_count, UUID start_uuid, UUID end_uuid, Function<Object[], Object[]> finalize) {
        if(save_mode == StatSaveMode.NO_SAVE) {
            this.finalizeAll(finalize);
            return;
        }
        String filename;
        if (save_mode == StatSaveMode.CONTINUE && end_uuid.getTime() >= CountingThread.getLastUpdateTimestamp()) {
            filename = stat_name + " " + start_uuid.getTime() + " x " + (unique ? 0 : 1) + (use_counter ? 0 : 1) + (use_count ? 0 : 1);
            StatLoader.saveStatToBytes(thread, this, filename, last_count, true, StatLoader.SaveType.UUID);
            this.finalizeAll(finalize);
        } else {
            filename = stat_name + " " + start_uuid.getTime() + " " + end_uuid.getTime() + " " + (unique ? 0 : 1) + (use_counter ? 0 : 1) + (use_count ? 0 : 1);
            this.finalizeAll(finalize);
            temp_values = new HashMap<>();
            StatLoader.saveStatToBytes(thread, this, filename, last_count, false, StatLoader.SaveType.UUID);
        }
    }
    public static StatContainer load(CountingThread thread, UUID start_uuid, UUID end_uuid, CountingIterator updates, Object[] default_object, String stat_name, String[] headers, StatDataType[] stat_types, int[] sort_order, boolean unique, boolean use_counter, boolean use_count, boolean thread_counts_combined, StatSaveMode save_mode) {
        String filename;
        boolean complete = false;
        if (save_mode == StatSaveMode.CONTINUE && end_uuid.getTime() > CountingThread.getLastUpdateTimestamp()) {
            filename = stat_name + " " + start_uuid.getTime() + " x " + (unique ? 0 : 1) + (use_counter ? 0 : 1) + (use_count ? 0 : 1);
        } else {
            filename = stat_name + " " + start_uuid.getTime() + " " + end_uuid.getTime() + " " + (unique ? 0 : 1) + (use_counter ? 0 : 1) + (use_count ? 0 : 1);
            complete = true;
        }
        StatContainer loaded = StatLoader.loadStatFromBytes(thread, filename, updates, use_count?CountDataType.COUNT:CountDataType.UPDATE, complete);
        if(loaded!=null) {
            loaded.stat.setComparator((a, b)->loaded.compare(b,a));
            return loaded;
        }
        return new StatContainer(default_object, stat_name, headers, stat_types, sort_order, unique, use_counter, use_count, thread_counts_combined, false, save_mode);
    }
    public static void saveBatch(List<StatContainer> stats, CountingThread thread, UpdateOrCountValue last_count, Function<Object[], Object[]> finalize, StatLoader.SaveType save_type, StatSaveMode save_mode) {
        if(stats.isEmpty()) return;
        String stat_name = stats.get(0).stat_name;
        boolean unique = stats.get(0).unique;
        boolean use_counter = stats.get(0).use_counter;
        boolean use_count = stats.get(0).use_count;
        String filename = stat_name + " " + (unique ? 0 : 1) + (use_counter ? 0 : 1) + (use_count ? 0 : 1);
        for(int i=0;i<stats.size()-1;i++) {
            stats.get(i).finalizeAll(finalize);
            stats.get(i).temp_values = new HashMap<>();
        }
        if(save_mode!=StatSaveMode.NO_SAVE) StatLoader.saveStatToBytes(thread, stats, filename, last_count, true, save_type);
        stats.get(stats.size()-1).finalizeAll(finalize);
    }
    public static List<StatContainer> loadBatch(CountingThread thread, CountingIterator updates, String stat_name, boolean unique, boolean use_counter, boolean use_count) {
        String filename = stat_name + " " + (unique ? 0 : 1) + (use_counter ? 0 : 1) + (use_count ? 0 : 1);
        List<StatContainer> loaded = StatLoader.loadStatFromBytes(thread, filename, updates, use_count?CountDataType.COUNT:CountDataType.UPDATE, false);
        if(loaded!=null) {
            for(StatContainer stat:loaded)
                stat.stat.setComparator((a, b)->stat.compare(b,a));
            return loaded;
        }
        return new ArrayList<>();
    }

    public static String generateThreadDirectory(List<CountingThread> threads) {
        StringBuilder thread_string = new StringBuilder();
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_";
        byte[] byte_array = new byte[(AllThreads.all_threads.size()-1)/6+1];
        Arrays.fill(byte_array, (byte) 0b00111111);
        for(CountingThread thread:threads) {
            int id = thread.thread.id-1;
            int index = byte_array.length-1-id/6;
            int position = id%6;
            byte_array[index] = (byte) (byte_array[index] ^ (0b1 << position));
        }
        for(int i=AllThreads.all_threads.size()%6;i<6;i++) {
            if(i==0) break;
            byte_array[0] = (byte) (byte_array[0] ^ (0b1 << i));
        }
        for(byte b:byte_array) {
            if(!thread_string.isEmpty() || b != 0) thread_string.append(chars.charAt(b));
        }
        if(thread_string.isEmpty()) thread_string.append("0");
        return thread_string.toString();
    }

    public void save(List<CountingThread> threads, UpdateOrCountValue last_count, UUID start_uuid, UUID end_uuid, Function<Object[], Object[]> finalize) {
        if(save_mode == StatSaveMode.NO_SAVE) {
            this.finalizeAll(finalize);
            return;
        }
        String thread_string = generateThreadDirectory(threads);
        if(new File("SidethreadData/all_threads/stats_data/"+thread_string).mkdir()) {
            try (PrintWriter p = new PrintWriter("SidethreadData/all_threads/stats_data/" + thread_string + "/Thread List.txt")) {
                for (CountingThread thread : threads) {
                    p.println(thread.thread.name);
                }
                p.println("And all threads with thread id > " + AllThreads.all_threads.size());
            } catch (FileNotFoundException ignored) {
            }
        }
        String filename;
        if (save_mode == StatSaveMode.CONTINUE && end_uuid.getTime() > CountingThread.getLastUpdateTimestamp()) {
            filename = stat_name + " " + start_uuid.getTime() + " x " + (unique ? 0 : 1) + (use_counter ? 0 : 1) + (use_count ? 0 : 1);
            StatLoader.saveStatToBytes(AllThreads.combined_thread, this, thread_string+"/"+filename, last_count, true, StatLoader.SaveType.UUID);
            this.finalizeAll(finalize);
        } else {
            filename = stat_name + " " + start_uuid.getTime() + " " + end_uuid.getTime() + " " + (unique ? 0 : 1) + (use_counter ? 0 : 1) + (use_count ? 0 : 1);
            this.finalizeAll(finalize);
            temp_values = new HashMap<>();
            StatLoader.saveStatToBytes(AllThreads.combined_thread, this, thread_string+"/"+filename, last_count, false, StatLoader.SaveType.UUID);
        }
    }
    public static StatContainer load(List<CountingThread> threads, UUID start_uuid, UUID end_uuid, CountingIterator updates, Object[] default_object, String stat_name, String[] headers, StatDataType[] stat_types, int[] sort_order, boolean unique, boolean use_counter, boolean use_count, boolean thread_counts_combined, StatSaveMode save_mode) {
        String thread_string = generateThreadDirectory(threads);
        String filename;
        boolean complete = false;
        if (save_mode == StatSaveMode.CONTINUE && end_uuid.getTime() > CountingThread.getLastUpdateTimestamp()) {
            filename = stat_name + " " + start_uuid.getTime() + " x " + (unique ? 0 : 1) + (use_counter ? 0 : 1) + (use_count ? 0 : 1);
        } else {
            filename = stat_name + " " + start_uuid.getTime() + " " + end_uuid.getTime() + " " + (unique ? 0 : 1) + (use_counter ? 0 : 1) + (use_count ? 0 : 1);
            complete = true;
        }
        StatContainer loaded = StatLoader.loadStatFromBytes(AllThreads.combined_thread, thread_string+"/"+filename, updates, use_count?CountDataType.COUNT:CountDataType.UPDATE, complete);
        if(loaded!=null) {
            loaded.stat.setComparator((a, b)->loaded.compare(b,a));
            return loaded;
        }
        return new StatContainer(default_object, stat_name, headers, stat_types, sort_order, unique, use_counter, use_count, thread_counts_combined, false, save_mode);
    }
    public static void saveBatch(List<StatContainer> stats, List<CountingThread> threads, UpdateOrCountValue last_count, Function<Object[], Object[]> finalize, StatLoader.SaveType save_type, StatSaveMode save_mode) {
        String thread_string = generateThreadDirectory(threads);
        if(new File("SidethreadData/all_threads/stats_data/"+thread_string).mkdir()) {
            try (PrintWriter p = new PrintWriter("SidethreadData/all_threads/stats_data/" + thread_string + "/Thread List.txt")) {
                for (CountingThread thread : threads) {
                    p.println(thread.thread.name);
                }
                p.println("And all threads with thread id > " + AllThreads.all_threads.size());
            } catch (FileNotFoundException ignored) {
            }
        }
        if(stats.isEmpty()) return;
        String stat_name = stats.get(0).stat_name;
        boolean unique = stats.get(0).unique;
        boolean use_counter = stats.get(0).use_counter;
        boolean use_count = stats.get(0).use_count;
        String filename = stat_name + " " + (unique ? 0 : 1) + (use_counter ? 0 : 1) + (use_count ? 0 : 1);
        for(int i=0;i<stats.size()-1;i++) {
            stats.get(i).finalizeAll(finalize);
            stats.get(i).temp_values = new HashMap<>();
        }
        if(save_mode!=StatSaveMode.NO_SAVE) StatLoader.saveStatToBytes(AllThreads.combined_thread, stats, thread_string+"/"+filename, last_count, true, save_type);
        stats.get(stats.size()-1).finalizeAll(finalize);
    }
    public static List<StatContainer> loadBatch(List<CountingThread> threads, CountingIterator updates, String stat_name, boolean unique, boolean use_counter, boolean use_count) {
        String thread_string = generateThreadDirectory(threads);
        String filename = stat_name + " " + (unique ? 0 : 1) + (use_counter ? 0 : 1) + (use_count ? 0 : 1);
        List<StatContainer> loaded = StatLoader.loadStatFromBytes(AllThreads.combined_thread, thread_string+"/"+filename, updates, use_count?CountDataType.COUNT:CountDataType.UPDATE, false);
        if(loaded!=null) {
            for(StatContainer stat:loaded)
                stat.stat.setComparator((a, b)->stat.compare(b,a));
            return loaded;
        }
        return new ArrayList<>();
    }


    //Negative element in sort_order implies reversed order
    private int compare(Object[] a, Object[] b) {
        for(int i:sort_order) {
            int sign = Integer.signum(i);
            int index = Math.abs(i)-1;
            int compare = sign * switch(stat_types[index]) {
                case NUMBER, PERCENT, TIME, TIME_AT_POSITION, HOUR, COUNTER, THREAD, THREAD_INSERT, FOCUS -> compare((Number)a[index], (Number)b[index]);
                case TIMEUNIT -> ((TimeUnit)a[index]).compareTo((TimeUnit)b[index]);
                case STRING -> ((String)a[index]).compareTo((String)b[index]);
                case COUNT, COUNT_OR_UPDATE -> {
                    if(a[index]==null) yield b[index]==null ? 0 : 1;
                    else if(b[index]==null) yield -1;
                    else yield ((UpdateOrCountValue)a[index]).compareTo((UpdateOrCountValue)b[index]);
                }
                case LIST_COUNTER -> throw new RuntimeException("Lists aren't comparable");
            };
            if(compare!=0) return compare;
        }
        return 0;
    }
    private String toString(Object a, StatDataType type) {
        return switch(type) {
            case NUMBER, STRING, TIMEUNIT -> a.toString();
            case PERCENT -> 100*(float)a+"%";
            case TIME, TIME_AT_POSITION -> {
                if(a==null) yield "";
                yield new TimeOutput((long)a).toString();
            }
            case HOUR -> {
                LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochSecond((long)a/1000000000L), ZoneId.of(CountingThread.timezone));
                yield time.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
            }
            case COUNTER -> {
                CounterData counter = Counters.counters.get((int)a);
                yield "["+counter.name()+"](https://counting.gg/counter/"+counter.username()+")";
            }
            case THREAD, THREAD_INSERT -> {
                if(a==null) yield "";
                ThreadData thread = AllThreads.all_threads_with_combined.get((int)a).thread;
                yield "["+thread.title+"](https://counting.gg/thread/"+thread.name+")";
            }
            case FOCUS -> {
                if(a==null) yield "";
                if(use_counter) {
                    CounterData counter = Counters.counters.get((int)a);
                    yield "["+counter.name()+"](https://counting.gg/counter/"+counter.username()+")";
                }
                else if(thread_counts_combined) yield "[cgg](https://counting.gg/threads)";
                else {
                    ThreadData thread = AllThreads.all_threads_with_combined.get((int)a).thread;
                    yield "["+thread.title+"](https://counting.gg/thread/"+thread.name+")";
                }
            }
            case COUNT -> {
                if(a==null) yield "";
                UpdateOrCountValue count = (UpdateOrCountValue)a;
                String link = "(https://counting.gg/thread/"+count.getThreadName()+"/"+count.getUUID()+")";
                yield "["+count.validCountNumber()+"]"+link;
            }
            case COUNT_OR_UPDATE -> {
                if(a==null) yield "";
                UpdateOrCountValue count = (UpdateOrCountValue)a;
                String link = "(https://counting.gg/thread/"+count.getThreadName()+"/"+count.getUUID()+")";
                if(use_count) yield "["+count.validCountNumber()+"]"+link;
                else yield "["+(count.getUpdateId()+1)+"]"+link;
            }
            case LIST_COUNTER -> {
                List<?> b = (List<?>)a;
                if(b.isEmpty()) yield "";
                CounterData counter = Counters.counters.get((int)b.get(0));
                StringBuilder sb = new StringBuilder("["+counter.name()+"](https://counting.gg/counter/"+counter.username()+")");
                for(int i=1;i<b.size();i++) {
                    counter = Counters.counters.get((int)b.get(i));
                    sb.append(", ").append("[").append(counter.name()).append("](https://counting.gg/counter/").append(counter.username()).append(")");
                }
                yield sb.toString();
            }
        };
    }
    private String toString(Object[] a) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<a.length;i++) {
            if(!useThreadInsert() && i==thread_insert_loc) continue;
            sb.append(toString(a[i], stat_types[i])).append("|");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }


    private int compare(Number a, Number b) {
        if (a instanceof Long) {
            return Long.compare(a.longValue(), b.longValue());
        } else if (a instanceof Integer) {
            return Integer.compare(a.intValue(), b.intValue());
        } else if (a instanceof Double) {
            return Double.compare(a.doubleValue(), b.doubleValue());
        } else if (a instanceof Float) {
            return Float.compare(a.floatValue(), b.floatValue());
        } else if (a instanceof Short) {
            return Short.compare(a.shortValue(), b.shortValue());
        } else if (a instanceof Byte) {
            return Byte.compare(a.byteValue(), b.byteValue());
        }
        throw new IllegalArgumentException("Unsupported Number type");
    }
    private static Number add(Number a, Number b) {
        if (a instanceof Long) {
            return a.longValue()+b.longValue();
        } else if (a instanceof Integer) {
            return a.intValue()+b.intValue();
        } else if (a instanceof Double) {
            return a.doubleValue()+b.doubleValue();
        } else if (a instanceof Float) {
            return a.floatValue()+b.floatValue();
        } else if (a instanceof Short) {
            return a.shortValue()+b.shortValue();
        } else if (a instanceof Byte) {
            return a.byteValue()+b.byteValue();
        }
        throw new IllegalArgumentException("Unsupported Number type");
    }
    public int size() {
        return stat.size();
    }

    public String toFullTable() {
        List<Object[]> sorted_stat = stat.toList();
        StringBuilder base = new StringBuilder("\\#");
        StringBuilder row2 = new StringBuilder();
        for (int i=0;i<headers.length;i++) {
            if(!useThreadInsert() && i==thread_insert_loc) continue;
            base.append("|").append(headers[i]);
            row2.append("|:--");
        }
        base.append("\n--:").append(row2).append("\n");
        int counter = 0;
        for(Object[] key:sorted_stat) {
            if(counter>1000) break;
            counter++;
            base.append(String.format("%s|%s\n", counter, toString(key)));
        }
        return base.toString();
    }

    public String toCompressedTable(int range, String focus) {
        if(!use_counter && thread_counts_combined) return toCompressedTable(range, 1);
        List<Object[]> sorted_stat = stat.toList();
        if(focus_loc.isEmpty()) return toCompressedTable(range, 0);
        for(int i=0;i<sorted_stat.size();i++) {
            int focus_id = (int)sorted_stat.get(i)[focus_loc.get(0)];
            String compare = use_counter ? Counters.counters.get(focus_id).username() : AllThreads.all_threads_with_combined.get(focus_id).thread.name;
            if(focus.equals(compare)) return toCompressedTable(range, i+1);
        }
        return toCompressedTable(range, 0);
    }

    public String toCompressedTable(int range, int position) {
        List<Object[]> sorted_stat = stat.toList();
        if(range < 1) range = 1;
        else if(range > 10) range = 10;
        if(position < 0 || position > sorted_stat.size()) position = 0;
        position--;
        StringBuilder base = new StringBuilder("\\#");
        StringBuilder row2 = new StringBuilder();
        for (int i=0;i<headers.length; i++) {
            if(!useThreadInsert() && i==thread_insert_loc) continue;
            base.append("|").append(headers[i]);
            row2.append("|:--");
        }
        base.append("\n--:").append(row2).append("\n");
        if(sorted_stat.isEmpty()) return "No Entries Found For the Given Stat";
        for(int i=0;i<Math.min(Math.max(position+range+1,3),sorted_stat.size());i++) {
            if (i == 3 && position - range > 3) {
                i = position - range;
                base.append("...|".repeat(headers.length + 1));
                base.append("...\n");
            }
            if (i == position) {
                String temp = String.format("***%s|%s***\n", i + 1, toString(sorted_stat.get(i)));
                base.append(temp.replace("|", "***|***"));
            } else {
                base.append(String.format("%s|%s\n", i + 1, toString(sorted_stat.get(i))));
            }
        }
        return base.toString();
    }
    public void clear() {
        stat = new BalancedBinarySearchTree<>((a, b)->compare(b,a));
        temp_values = new HashMap<>();
        temp_final = new HashMap<>();
    }
    public static StatContainer add(List<StatContainer> stats) {
        Map<List<Object>, List<Object>> map = new HashMap<>(); //Key is list of non-numbers. Values are lists of numbers.
        for(StatContainer stat:stats) {
            if(stat==null) continue;
            for (Object[] o : stat.getStat()) {
                List<Object> keys = new ArrayList<>();
                List<Object> values = new ArrayList<>();
                for (int i = 0; i < o.length; i++) {
                    if (stat.stat_types[i] == StatDataType.NUMBER || stat.stat_types[i] == StatDataType.TIME) {
                        values.add(o[i]);
                    } else if (stat.stat_types[i] == StatDataType.THREAD_INSERT && !stat.useThreadInsert()) {
                        keys.add(-1); //the thread is unimportant if THREAD_INSERT is ignored
                    } else {
                        keys.add(o[i]);
                    }
                }
                if (map.containsKey(keys)) {
                    List<Object> add_values = map.get(keys);
                    for (int i = 0; i < values.size(); i++) {
                        values.set(i, StatContainer.add((Number) values.get(i), (Number) add_values.get(i)));
                    }
                }
                map.put(keys, values);
            }
        }
        List<Object[]> added_values = new ArrayList<>();
        StatContainer initial_stat = stats.get(0);
        for(Map.Entry<List<Object>, List<Object>> pairs: map.entrySet()) {
            Object[] out = new Object[initial_stat.stat_types.length];
            int key_index = 0;
            int value_index = 0;
            for(int i=0;i<initial_stat.stat_types.length;i++) {
                if(initial_stat.stat_types[i]==StatDataType.NUMBER || initial_stat.stat_types[i]==StatDataType.TIME) {
                    out[i] = pairs.getValue().get(value_index);
                    value_index++;
                } else {
                    out[i] = pairs.getKey().get(key_index);
                    key_index++;
                }
            }
            added_values.add(out);
        }
        StatContainer output = new StatContainer(initial_stat.default_object, initial_stat.stat_name, initial_stat.headers, initial_stat.stat_types, initial_stat.sort_order, initial_stat.unique, initial_stat.use_counter, initial_stat.use_count, initial_stat.thread_counts_combined, true, initial_stat.save_mode);
        output.stat.addAll(added_values);
        return output;
    }
    public boolean useThreadInsert() {
        return !add_thread_stats && !thread_counts_combined && use_counter;
    }
}
