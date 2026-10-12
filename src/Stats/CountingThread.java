package Stats;

import Stats.FileParser.FileData;
import Stats.StatLoader.Split;
import Stats.StatLoader.SplitHOC;
import Stats.StatLoader.SplitWithHOC;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;


public class CountingThread {
    public static final int FULL_DB_MSGS_PER_FILE = 100000;
    public static final int MSGS_PER_FILE = 500000;
    public static final String timezone = "America/Montreal";
//    public static final String timezone = "UTC";
    public final int CORES = 6;
    protected static final ObjectMapper mapper = new ObjectMapper();
    private static final String full_db_filepath = "SidethreadData/all_threads/full_db/chat_";
    private final String update_filepath;
    private final String update_body_filepath;
    private final String count_filepath;
    private final String count_body_filepath;
    public final ThreadData thread;
    protected static int full_db_total_updates;
    protected static int full_db_last_update_synced;
    private int total_updates;
    private int total_counts;
    protected static final List<Integer> full_db_updates_per_file;
    protected final List<Integer> updates_per_file;
    protected final List<Integer> update_body_per_file;
    protected final List<Integer> counts_per_file;
    protected final List<Integer> count_body_per_file;
    protected static final List<Long[]> first_and_last_full_db_updates;
    protected final List<Long[]> first_and_last_updates;
    private final List<Long[]> first_and_last_counts;
    static {
        createFileStructure(new String[]{"updates", "update_body", "counts", "count_body", "stats_data", "full_db"}, "all_threads");
        FileData data = FileParser.getFileData(-1);
        if (data == null) {
            data = new FileData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(Arrays.asList(0, 0)), new ArrayList<>(), new ArrayList<>());
            FileParser.updateFileData(-1, data);
        }
        full_db_total_updates = data.count_values().get(0);
        full_db_last_update_synced = data.count_values().get(1);
        first_and_last_full_db_updates = data.first_and_last_updates();
        full_db_updates_per_file = data.updates_per_file();
    }
    public CountingThread(ThreadData thread) {
        if(thread == null) throw new IllegalArgumentException();
        this.thread = thread;
        String file_dir = "SidethreadData/" + thread.name;
        this.update_filepath = file_dir + "/updates/chat_";
        this.update_body_filepath = file_dir + "/update_body/chat_";
        this.count_filepath = file_dir + "/counts/chat_";
        this.count_body_filepath = file_dir + "/count_body/chat_";
        createFileStructure(new String[]{"updates", "update_body", "counts", "count_body", "stats_data"},thread.name);
        FileData data = FileParser.getFileData(thread.id);
        if (data == null) {
            data = new FileData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(Arrays.asList(0, 0)), new ArrayList<>(), new ArrayList<>());
            FileParser.updateFileData(thread.id, data);
        }
        List<Integer> count_values = data.count_values();
        this.total_updates = count_values.get(0);
        this.total_counts = count_values.get(1);
        this.updates_per_file = data.updates_per_file();
        this.update_body_per_file = data.update_body_per_file();
        this.counts_per_file = data.counts_per_file();
        this.count_body_per_file = data.count_body_per_file();
        this.first_and_last_updates = data.first_and_last_updates();
        this.first_and_last_counts = data.first_and_last_counts();
    }
    private static void createFileStructure(String[] subfiles, String thread_name) {
        String path = "SidethreadData/" + thread_name;
        if(new File(path).mkdir())
            System.out.println(path + " was created");
        for(String directory: subfiles) {
            if(new File(path + "/"+ directory).mkdir()) {
                System.out.println(path + "/"+ directory + " was created");
            }
        }
    }
    private void updateFileData() {
        ArrayList<Integer> a = new ArrayList<>(Arrays.asList(this.total_updates, this.total_counts));
        FileParser.updateFileData(thread.id, new FileData(this.updates_per_file, this.update_body_per_file, this.counts_per_file, this.count_body_per_file, a, this.first_and_last_updates, this.first_and_last_counts));
    }
    protected static void updateFullDBFileData() {
        ArrayList<Integer> a = new ArrayList<>(Arrays.asList(full_db_total_updates, full_db_last_update_synced));
        FileParser.updateFileData(-1, new FileData(full_db_updates_per_file, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), a, first_and_last_full_db_updates, new ArrayList<>()));
    }
    public boolean validateFileDataIntegrity() {
        return this.getTotalCounts()==Util.sumIntList(this.counts_per_file) && this.getTotalUpdates()==Util.sumIntList(this.updates_per_file);
    }
    protected void serializeUpdateOrCountFile(List<? extends Serializable> updates, int file_number, CountDataType mode) {
        String filepath = switch(mode) {
            case COUNT -> this.count_filepath + file_number;
            case COUNT_BODY -> this.count_body_filepath + file_number;
            case UPDATE -> this.update_filepath + file_number;
            case UPDATE_BODY -> this.update_body_filepath + file_number;
            case FULL -> full_db_filepath + file_number;
        };
        List<Long[]> first_and_last = switch(mode) {
            case COUNT -> this.first_and_last_counts;
            case UPDATE -> this.first_and_last_updates;
            case COUNT_BODY, UPDATE_BODY -> null;
            case FULL -> first_and_last_full_db_updates;
        };
        if(updates.isEmpty()) {
            System.out.println(filepath+" was not updated because there are no counts");
            return;
        }
        if(first_and_last != null) {
            Util.alwaysSet(first_and_last, new Long[]{((UpdateOrCountValue)updates.get(0)).getTimestamp(), ((UpdateOrCountValue)updates.get(updates.size() - 1)).getTimestamp()}, file_number);
        }
        serializeUpdateOrCountFile(updates, filepath);
    }
    protected void serializeUpdateOrCountFile(List<? extends Serializable> updates, String path) {
        File f = new File(path);
        int[] locs = new int[CORES];
        int current_loc = 0;
        for(int i=0;i<locs.length;i++) {
            if((i * MSGS_PER_FILE)/CORES > updates.size()) {
                locs[i] = 0;
            } else {
                locs[i] = (i * MSGS_PER_FILE) / CORES;
            }
        }
        int current_len = 0;
        for(int i=0;i<updates.size();i++) {
            if(current_loc < locs.length && i == locs[current_loc]) {
                locs[current_loc] = current_len+(CORES*4);
                current_loc++;
            }
            current_len += updates.get(i).toBytes().array().length;
        }
        try(FileOutputStream stream = new FileOutputStream(f)) {
            for(int i=0;i<CORES;i++) {
                stream.write(ByteBuffer.allocate(4).putInt(locs[i]).array());
            }
            for(Serializable update:updates) {
                stream.write(update.toBytes().array());
            }
            System.out.println(f+" was updated");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    public <K extends Serializable> List<K> readUpdateOrCountFile(int file_number, CountDataType mode) {
        System.out.println("Reading file "+file_number+" of thread "+thread.name);
        String filepath = switch(mode) {
            case COUNT -> this.count_filepath + file_number;
            case COUNT_BODY -> this.count_body_filepath + file_number;
            case UPDATE -> this.update_filepath + file_number;
            case UPDATE_BODY -> this.update_body_filepath + file_number;
            case FULL -> full_db_filepath + file_number;
        };
        List<Integer> entry_file = switch(mode) {
            case COUNT -> this.counts_per_file;
            case COUNT_BODY -> this.count_body_per_file;
            case UPDATE -> this.updates_per_file;
            case UPDATE_BODY ->  this.update_body_per_file;
            case FULL -> full_db_updates_per_file;
        };
        if(file_number >= entry_file.size()) return new ArrayList<>();
        int total_entries = entry_file.get(file_number);
        final BiFunction<ByteBuffer, IntWrapper, ? extends Serializable> from_bytes = switch(mode) {
            case COUNT -> CountValue::fromBytes;
            case UPDATE -> UpdateValue::fromBytes;
            case COUNT_BODY, UPDATE_BODY -> UpdateBody::fromBytes;
            case FULL -> FullUpdateValue::fromBytes;
        };
        try {
            File f = new File(filepath);
            List<K> counts = new ArrayList<>(total_entries);
            for(int i=0;i<total_entries;i++) {
                counts.add(null);
            }
            byte[] bytes = Files.readAllBytes(f.toPath());
            ByteBuffer data = ByteBuffer.wrap(bytes);
            CountDownLatch latch = new CountDownLatch(CORES);
            for(int i=0;i<CORES;i++) {
                int current_loc = data.getInt(i*4);
                int current_read_size = Math.min(((i+1) * MSGS_PER_FILE)/CORES, total_entries);
                int read_size = current_read_size - (i * MSGS_PER_FILE)/CORES;
                if(current_loc != 0) {
                    new Thread(new Runnable() {
                        final IntWrapper loc = new IntWrapper(current_loc);
                        final int read = read_size;
                        final int read_start = current_read_size-read_size;
                        @Override
                        public void run() {
                            for(int i=0;i<read;i++) {
                                counts.set(read_start+i,(K)(from_bytes.apply(data, loc)));
                            }
                            latch.countDown();
                        }
                    }).start();
                } else {
                    latch.countDown();
                }
            }
            latch.await();
            return counts;
        }
        catch(IOException|IndexOutOfBoundsException e) {
            return new ArrayList<>();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void deleteStatFiles() {
        String filename = "SidethreadData/" + thread.name + "/stats_data";
        File f = new File(filename);
        for(File subfile:f.listFiles()) {
            if(subfile.isDirectory()) {
                for(File subsubfile:subfile.listFiles()) {
                    subsubfile.delete();
                }
            }
            subfile.delete();
        }
    }

    private <K extends Serializable> List<K> getUpdatesOrCountsFromFile(int file, int start_loc_in_file, int end_loc_in_file, CountDataType mode) {
        if(file<0) return new ArrayList<>();
        List<Integer> entry_file = switch(mode) {
            case COUNT -> this.counts_per_file;
            case COUNT_BODY -> this.count_body_per_file;
            case UPDATE -> this.updates_per_file;
            case UPDATE_BODY ->  this.update_body_per_file;
            case FULL -> full_db_updates_per_file;
        };
        if(file >= entry_file.size()) return new ArrayList<>();
        if(start_loc_in_file<0) start_loc_in_file = 0;
        if(end_loc_in_file>entry_file.get(file)) end_loc_in_file = entry_file.get(file);
        if(mode==CountDataType.FULL || mode==CountDataType.COUNT_BODY || mode==CountDataType.UPDATE_BODY) {
            List<K> updates = readUpdateOrCountFile(file, mode);
            return updates.subList(start_loc_in_file, end_loc_in_file);
        }
        String filepath = switch(mode) {
            case COUNT -> this.count_filepath + file;
            case UPDATE -> this.update_filepath + file;
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
        final BiFunction<ByteBuffer, IntWrapper, ? extends Serializable> from_bytes = switch(mode) {
            case COUNT -> CountValue::fromBytes;
            case UPDATE -> UpdateValue::fromBytes;
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
        List<K> updates = new ArrayList<>();
        int update_size = mode == CountDataType.COUNT ? CountValue.size : UpdateValue.size;
        try (RandomAccessFile f = new RandomAccessFile(new File(filepath), "r")) {
            byte[] bytes = new byte[update_size*(end_loc_in_file-start_loc_in_file)];
            f.seek(4*CORES+ (long) update_size * start_loc_in_file);
            f.read(bytes);
            ByteBuffer data = ByteBuffer.wrap(bytes);
            IntWrapper loc = new IntWrapper(0);
            for(int i=0;i<end_loc_in_file-start_loc_in_file;i++) {
                updates.add((K)(from_bytes.apply(data, loc)));
            }
            return updates;
        }
        catch(IOException|IndexOutOfBoundsException e) {
            return new ArrayList<>();
        }
    }
    public <K extends Serializable> List<K> getUpdatesOrCounts(int start_update, int end_update, CountDataType mode) {
        Location start = getCountOrUpdateLocation(start_update, mode);
        Location end = getCountOrUpdateLocation(end_update, mode);
        if(start.getChatFile()==end.getChatFile()) return getUpdatesOrCountsFromFile(start.getChatFile(), start.loc_in_file(), end.loc_in_file(), mode);
        List<K> counts = getUpdatesOrCountsFromFile(start.getChatFile(), start.loc_in_file(), end_update, mode);
        int current_file = start.getChatFile()+1;
        while(current_file < end.getChatFile()) {
            counts.addAll(readUpdateOrCountFile(current_file, mode));
            current_file++;
        }
        counts.addAll(getUpdatesOrCountsFromFile(end.getChatFile(), 0, end.loc_in_file(), mode));
        return counts;
    }
    public <K extends Serializable> K getUpdateOrCount(int update, CountDataType mode) {
        List<K> count = getUpdatesOrCounts(update, update+1, mode);
        if(count.isEmpty()) return null;
        return count.get(0);
    }

    public int getTotalCounts() {
        return this.total_counts;
    }
    public int getTotalUpdates() {
        return this.total_updates;
    }
    public int getTotalUpdates(CountDataType mode) {
        return switch(mode) {
            case COUNT, COUNT_BODY -> this.total_counts;
            case UPDATE, UPDATE_BODY -> this.total_updates;
            case FULL -> full_db_total_updates;
        };
    }

    protected void updateThread(List<? extends Serializable> updates, CountDataType mode) {
        List<Integer> entries_per_file = switch(mode) {
            case COUNT -> this.counts_per_file;
            case COUNT_BODY -> this.count_body_per_file;
            case UPDATE -> this.updates_per_file;
            case UPDATE_BODY -> this.update_body_per_file;
            case FULL -> full_db_updates_per_file;
        };
        int msgs_per_file = switch(mode) {
            case COUNT, COUNT_BODY, UPDATE, UPDATE_BODY -> MSGS_PER_FILE;
            case FULL -> FULL_DB_MSGS_PER_FILE;
        };
        if(updates.isEmpty() || entries_per_file==null) return;
        System.out.println("Updating "+thread.name + " with "+updates.size()+" new updates of type "+mode+".");
        int chat_files = Math.max(entries_per_file.size()-1,0);
        if(!entries_per_file.isEmpty() && entries_per_file.get(chat_files) >= msgs_per_file) {
            chat_files++;
        }
        switch(mode) {
            case COUNT -> this.total_counts += updates.size();
            case UPDATE -> this.total_updates += updates.size();
            case FULL -> full_db_total_updates += updates.size();
        }
        if(entries_per_file.isEmpty()) entries_per_file.add(0);
        while(true) {
            List<Serializable> past_updates = this.readUpdateOrCountFile(chat_files, mode);
            int lf_updates = entries_per_file.get(entries_per_file.size()-1);
            if(lf_updates>=msgs_per_file) lf_updates=0;
            if(lf_updates + updates.size() > msgs_per_file) {
                past_updates.addAll(updates.subList(0, msgs_per_file-lf_updates));
                updates = updates.subList(msgs_per_file-lf_updates, updates.size());
                this.serializeUpdateOrCountFile(past_updates, chat_files, mode);
                Util.alwaysSet(entries_per_file, msgs_per_file, chat_files);
            } else {
                past_updates.addAll(updates);
                this.serializeUpdateOrCountFile(past_updates, chat_files, mode);
                Util.alwaysSet(entries_per_file, past_updates.size(), chat_files);
                break;
            }
            chat_files++;
        }
        if (mode == CountDataType.FULL) {
            updateFullDBFileData();
        } else {
            this.updateFileData();
        }

    }
    public Location getCountOrUpdateLocation(int update_number, CountDataType mode) {
        List<Integer> entries_per_file = switch(mode) {
            case COUNT -> this.counts_per_file;
            case COUNT_BODY -> this.count_body_per_file;
            case UPDATE -> this.updates_per_file;
            case UPDATE_BODY -> this.update_body_per_file;
            case FULL -> full_db_updates_per_file;
        };
        int entries_by_file = 0;
        int chat_file = 0;
        boolean broken = false;
        for (int counts : entries_per_file) {
            if (entries_by_file + counts <= update_number) {
                entries_by_file += counts;
                chat_file++;
            } else {
                broken = true;
                break;
            }
        }
        if(!broken) return this.getMaxCountOrUpdateLocation(mode);
        return new Location(entries_by_file, chat_file, update_number-entries_by_file);
    }

    public Location getMaxCountOrUpdateLocation(CountDataType mode) {
        List<Integer> entries_per_file = switch(mode) {
            case COUNT -> this.counts_per_file;
            case COUNT_BODY -> this.count_body_per_file;
            case UPDATE -> this.updates_per_file;
            case UPDATE_BODY -> this.update_body_per_file;
            case FULL -> full_db_updates_per_file;
        };
        int total_entries = switch(mode) {
            case COUNT, COUNT_BODY -> this.total_counts;
            case UPDATE, UPDATE_BODY -> this.total_updates;
            case FULL -> full_db_total_updates;
        };
        if(total_entries==0) return new Location(0,0,0);
        return new Location(total_entries-entries_per_file.get(entries_per_file.size()-1),entries_per_file.size()-1, entries_per_file.get(entries_per_file.size()-1));
    }

    public Location getLocationOfFirstCountOrUpdateAfterTimestamp(UUID uuid, CountDataType mode, boolean after) {
        List<Long[]> first_and_last = switch(mode) {
            case COUNT, COUNT_BODY -> this.first_and_last_counts;
            case UPDATE, UPDATE_BODY -> this.first_and_last_updates;
            case FULL -> first_and_last_full_db_updates;
        };
        List<Integer> entries_per_file = switch(mode) {
            case COUNT, COUNT_BODY -> this.counts_per_file;
            case UPDATE, UPDATE_BODY -> this.updates_per_file;
            case FULL -> full_db_updates_per_file;
        };
        if(mode==CountDataType.COUNT_BODY) mode = CountDataType.COUNT;
        else if(mode==CountDataType.UPDATE_BODY) mode = CountDataType.UPDATE;
        long timestamp = uuid.getTime();
        int low = 0;
        int high = entries_per_file.size() -1;
        while(low<=high) {
            int mid = (high+low)/2;
            long first = first_and_last.get(mid)[0];
            long last = first_and_last.get(mid)[1];
            if(last < timestamp)
                low = mid + 1;
            else if(first > timestamp) {
                if(mid==0)
                    return new Location(0,0, 0);
                high = mid - 1;
            } else {
                int counts_by_file = entries_per_file.subList(0,mid).stream().mapToInt(Integer::intValue).sum();
                return new Location(counts_by_file, mid, binarySearchCountsForTimestamp(timestamp, readUpdateOrCountFile(mid, mode), after));
            }
        }
        if(low<entries_per_file.size()) {
            int counts_by_file = entries_per_file.subList(0,low).stream().mapToInt(Integer::intValue).sum();
            return new Location(counts_by_file, low, 0);
        }
        return null;
    }
    private int binarySearchCountsForTimestamp(long timestamp, List<? extends UpdateOrCountValue> counts, boolean after) {
        int low = 0;
        int high = counts.size()-1;
        int mid = 0;
        while(low<=high) {
            mid = (high+low)/2;
            long count_time = counts.get(mid).getTimestamp();
            if(count_time < timestamp)
                low = mid + 1;
            else if(count_time > timestamp)
                high = mid - 1;
            else
                return mid;
        }
        if(after && counts.get(mid).getTimestamp() > timestamp || !after && counts.get(mid).getTimestamp() < timestamp)
            return mid;
        else
            return after ? mid + 1 : mid - 1;
    }

    public List<CounterData> getBanList() {
        List<CounterData> ban_list = new ArrayList<>();
        for(String uuid:thread.postBans) {
            if(uuid.isEmpty()) continue;
            ban_list.add(Counters.counters.get(Counters.uuid_lookup.get(new UUID(uuid))));
        }
        for(String uuid:thread.countBans) {
            if(uuid.isEmpty()) continue;
            ban_list.add(Counters.counters.get(Counters.uuid_lookup.get(new UUID(uuid))));
        }
        return ban_list;
    }
    //The 0th object is a dummy object used for saving/loading data. It is not returned.
    public List<Split> splits(int start_k, int end_k, int k_size, boolean gets) {
        String filename = "SidethreadData/"+thread.name+"/stats_data/"+(gets?"gets":"splits")+"_"+k_size;
        List<Split> splits = StatLoader.loadFromBytes(filename);
        if(splits==null) {
            splits = new ArrayList<>();
            splits.add(new Split(-1,-1,0, CountValue.getDefaultObject(),null, true));
        }
        UUID start = new UUID(splits.get(0).start_count().timestamp()+1,0);
        if(start.getTime() > getLastUpdateTimestamp(CountDataType.COUNT)) start = UUID.maxUUID();
        UpdateIterator counts = new UpdateIterator(List.of(this), start, UUID.maxUUID(), true).init();
        CountValue last_start = null;
        if(splits.size()>1) {
            Split last_split = splits.get(splits.size()-1);
            if(last_split.end_count()!=null) last_start = last_split.end_count();
            else last_start = last_split.start_count();
        }
        if(!splits.get(0).complete()) {
            splits.remove(splits.size()-1);
        }
        CountValue last_count = splits.get(0).start_count();
        int k_index = splits.get(0).k_index();
        while(counts.hasNext()) {
            CountValue count = (CountValue)counts.next();
            if(count.true_number()==1) {
                if(last_start!=null) {
                    int[] split = last_count.getSplit(k_size);
                    splits.add(new Split(split[0], gets ? 0 : split[1], k_index, last_start, last_count, false));
                    k_index++;
                }
                last_start = count;
            }
            if(!gets && count.isSplit(0,k_size)) {
                int[] split = count.getSplit(k_size);
                splits.add(new Split(split[0], split[1], k_index, last_start, count, true));
                last_start = count;
            } else if(gets && count.isGet(0, k_size)) {
                int k = count.getK(k_size);
                splits.add(new Split(k, 0, k_index, last_start, count, true));
                last_start = count;
            }
            last_count = count;
        }
        if(last_count!=null) {
            if(!(gets && last_count.isGet(0,k_size)) && !(!gets && last_count.isSplit(0,k_size)) && !(last_count.true_number()==1)) {
                int[] split = last_count.getSplit(k_size);
                splits.add(new Split(split[0], gets ? 0 : split[1], k_index, last_start, last_count, false));
                splits.set(0, new Split(-1,-1,k_index, last_count,null, false));
            } else {
                splits.set(0, new Split(-1,-1,k_index, last_count,null, true));
            }
            StatLoader.saveToBytes(splits, filename);
        }
        List<Split> output_splits = new ArrayList<>();
        for(Split s: splits) {
            if(s.k()>=start_k && s.k()<end_k) {
                output_splits.add(s);
            }
        }
        return output_splits;
    }
    public List<SplitWithHOC> completeDistinctSplitHocs(int start_k, int end_k, int k_size, boolean k_hoc, boolean use_counters) {
        if(!thread.resetOnMistakes) {
            List<SplitHOC> split_hoc = CombinedStats.splitHocs(List.of(this), start_k, end_k, k_size, k_hoc, true, use_counters, false);
            List<SplitWithHOC> output = new ArrayList<>();
            List<Split> splits = this.splits(start_k, end_k, k_size, k_hoc);
            for(int i=0;i<split_hoc.size(); i++) {
                SplitHOC split = split_hoc.get(i);
                Split also_split = splits.get(i);
                if(!also_split.complete()) break;
                output.add(new SplitWithHOC(split.k(), split.split(), 0, also_split.start_count(), also_split.end_count(), split.hoc()));
            }
            return output;
        } else {
            List<Split> splits = this.splits(start_k, end_k, k_size, k_hoc);
            List<SplitWithHOC> output = new ArrayList<>();
            for(Split split:splits) {
                if(!split.complete()) continue;
                int start_count = split.start_count().validCountNumber();
                if(split.start_count().true_number()==1) start_count--;
                int end_count = split.end_count().validCountNumber();
                List<UpdateOrCountValue> counts = getUpdatesOrCounts(start_count, end_count, CountDataType.COUNT);
                StatContainer hoc = CombinedStats.hoc(counts, true, use_counters, true, false);
                output.add(new SplitWithHOC(split.k(), split.split(), split.k_index(), split.start_count(), split.end_count(), hoc));
            }
            return output;
        }
    }
    public List<List<SplitWithHOC>> completeDistinctSplitHocsByK(int start_k, int end_k, int k_size, boolean use_counters) {
        List<SplitWithHOC> splits = completeDistinctSplitHocs(start_k, end_k, k_size, false, use_counters);
        List<List<SplitWithHOC>> split_hocs_by_k = new ArrayList<>();
        for(int i=0;i<splits.size();i++) {
            List<SplitWithHOC> splits_in_k = new ArrayList<>();
            SplitWithHOC start = splits.get(i);
            splits_in_k.add(start);
            while(++i<splits.size()) {
                SplitWithHOC current = splits.get(i);
                if(current.k()==start.k() && current.k_index()==start.k_index()) splits_in_k.add(current);
                else {
                    i--;
                    break;
                }
            }
            if(splits_in_k.size() == thread.splitsPerGet*k_size) split_hocs_by_k.add(splits_in_k);
        }
        return split_hocs_by_k;
    }
    public TimeUnit first_day(ParticipationDuration mode, String timezone) {
        return new TimeUnit(first_and_last_updates.get(0)[0], timezone, mode);
    }
    public TimeUnit last_day(ParticipationDuration mode, String timezone) {
        return new TimeUnit(first_and_last_updates.get(first_and_last_updates.size()-1)[1], timezone, mode).addTime(1);
    }
    public long getFirstUpdateTimestamp(CountDataType mode) {
        return switch(mode) {
            case FULL -> first_and_last_full_db_updates.get(0)[0];
            case COUNT, COUNT_BODY -> this.first_and_last_counts.isEmpty() ? 0L : this.first_and_last_counts.get(0)[0];
            case UPDATE, UPDATE_BODY -> this.first_and_last_updates.get(0)[0];
        };
    }
    public long getLastUpdateTimestamp(CountDataType mode) {
        return switch(mode) {
            case FULL -> first_and_last_full_db_updates.get(first_and_last_full_db_updates.size()-1)[1];
            case COUNT, COUNT_BODY -> this.first_and_last_counts.isEmpty() ? 0L : this.first_and_last_counts.get(this.first_and_last_counts.size()-1)[1];
            case UPDATE, UPDATE_BODY -> this.first_and_last_updates.get(this.first_and_last_updates.size()-1)[1];
        };
    }
    public static long getLastUpdateTimestamp() {
        return first_and_last_full_db_updates.get(first_and_last_full_db_updates.size()-1)[1];
    }
    public int doubleCountInterval() {
        return thread.allowDoublePosts ? 1 : 2;
    }
    public double countSuccessChance(UpdateOrCountValueWithComment current) {
        if(current.isCount()) return 1;
        return 0;
    }
    public void clearCountSuccessChance() {}
    public String getCount(int num) {
        throw new UnsupportedOperationException();
    }
    public int parse(String raw_count) {
        throw new UnsupportedOperationException();
    }
}
