package Threads;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class CountingThread {
    public static final int MSGS_PER_FILE = 100000;
    public final String timezone;
    public final int CORES = 6;
    protected final ObjectMapper mapper = new ObjectMapper();
    private final ObjectMapper num_mapper = new ObjectMapper();
    private final ArrayList<Logger> EX_COUNTS_LOGGER = new ArrayList<>();
    private final ArrayList<Logger> SET_COUNTS_LOGGER = new ArrayList<>();
    private final ArrayList<String> update_files = new ArrayList<>();
    private final ArrayList<String> count_num_files = new ArrayList<>();
    private final ArrayList<String> count_time_files = new ArrayList<>();
    private final ArrayList<String> name = new ArrayList<>();
    private final ArrayList<String> live_thread = new ArrayList<>();
    private final ArrayList<Integer> total_updates = new ArrayList<>();
    private final ArrayList<Integer> total_counts = new ArrayList<>();
    private final ArrayList<Integer> last_num_update = new ArrayList<>();
    private final ArrayList<Integer> last_number = new ArrayList<>();
    private final ArrayList<ArrayList<Integer>> updates_per_file = new ArrayList<>();
    private final ArrayList<ArrayList<Integer>> counts_per_file = new ArrayList<>();
    private final ArrayList<List<String[]>> first_and_last_updates = new ArrayList<>();
    private final ArrayList<List<String[]>> first_and_last_counts = new ArrayList<>();
    public CountingThread(String name, List<String> live_threads, String timezone) {
        if(live_threads == null || live_threads.size() == 0) throw new IllegalArgumentException();
        this.timezone = timezone;
        for(int i=0;i<live_threads.size();i++) {
            this.name.add(name+"_v"+i);
            this.live_thread.add(live_threads.get(i));
            String file_dir = "SidethreadData\\" + live_threads.get(i);
            this.update_files.add(file_dir + "\\updates\\chat");
            this.count_time_files.add(file_dir + "\\count_time\\chat");
            this.count_num_files.add(file_dir + "\\count_num\\chat");
            Util.createFileStructure(live_threads.get(i));
            this.EX_COUNTS_LOGGER.add(Util.setupLogger(Logger.getLogger("EX_COUNTS_LOGGER_"+name+"_"+i), file_dir + "\\extra_counts.log", name));
            this.SET_COUNTS_LOGGER.add(Util.setupLogger(Logger.getLogger("SET_COUNTS_LOGGER_"+name+"_"+i), file_dir + "\\set_counts.log", name));
            FileData data = FileParser.getFileData(live_threads.get(i));
            if (data == null) {
                data = new FileData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(Arrays.asList(0, 0, 0, 0)), new ArrayList<>(), new ArrayList<>());
                FileParser.updateFileData(live_threads.get(i), data);
            }
            List<Integer> count_values = data.count_values();
            this.total_updates.add(count_values.get(0));
            this.total_counts.add(count_values.get(1));
            this.last_num_update.add(count_values.get(2));
            this.last_number.add(count_values.get(3));
            this.updates_per_file.add(data.updates_per_file());
            this.counts_per_file.add(data.counts_per_file());
            this.first_and_last_updates.add(data.first_and_last_updates());
            this.first_and_last_counts.add(data.first_and_last_counts());
        }
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ArrayList.class, new CountNumDeserializer());
        num_mapper.registerModule(module);
    }
    private void updateFileData(int thread_id) {
        ArrayList<Integer> a = new ArrayList<>(Arrays.asList(this.total_updates.get(thread_id), this.total_counts.get(thread_id), this.last_num_update.get(thread_id), this.last_number.get(thread_id)));
        FileParser.updateFileData(live_thread.get(thread_id), new FileData(this.updates_per_file.get(thread_id), this.counts_per_file.get(thread_id), a, this.first_and_last_updates.get(thread_id), this.first_and_last_counts.get(thread_id)));
    }
    public ArrayList<UpdateValue> readUpdateFile_OLD(int thread_id, int file_number) {
        TypeFactory tf = mapper.getTypeFactory();
        CollectionType collectionType = tf.constructCollectionType(ArrayList.class, UpdateValue.class);
        File f = new File(this.update_files.get(thread_id) + file_number+".json");
        try {
            return mapper.readValue(f, collectionType);
        }
        catch(IOException e) {
            System.out.println(file_number);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    private ArrayList<CountValue> readJSONCountTimeFile(int thread_id, int file_number) {
        TypeFactory tf = mapper.getTypeFactory();
        CollectionType collectionType = tf.constructCollectionType(ArrayList.class, CountValue.class);
        File f = new File(this.count_time_files.get(thread_id) + file_number+".json");
        try {
            return mapper.readValue(f, collectionType);
        }
        catch(IOException e) {
            return new ArrayList<>();
        }
    }
    public ArrayList<CountValue> readCountTimeFile_OLD(int thread_id, int file_number) {
        try {
            File f = new File(this.count_time_files.get(thread_id) + file_number);
            ArrayList<CountValue> counts = new ArrayList<>();
            int total = this.counts_per_file.get(thread_id).get(file_number);
            int bytes_read = 0;
            byte[] bytes = Files.readAllBytes(f.toPath());
            ByteBuffer data = ByteBuffer.wrap(bytes);
            for(int i=0;i<total;i++) {
                String uuid = new String(data.array(),bytes_read,36, StandardCharsets.ISO_8859_1);
                bytes_read+=36;
                int body_size = data.getInt(bytes_read);
                bytes_read+=4;
                String raw_text = new String(data.array(), bytes_read, body_size, StandardCharsets.UTF_8);
                bytes_read+=body_size;
                int number = data.getInt(bytes_read);
                bytes_read+=4;
                String author_uuid = new String(data.array(),bytes_read,36, StandardCharsets.ISO_8859_1);
                bytes_read+=36;
                int raw_count_size = data.getInt(bytes_read);
                bytes_read+=4;
                String raw_count = new String(data.array(), bytes_read, raw_count_size, StandardCharsets.UTF_8);
                bytes_read+=raw_count_size;
                boolean stricken = data.get(bytes_read) == 1;
                bytes_read+=1;
                int update_id = data.getInt(bytes_read);
                bytes_read+=4;
                counts.add(new CountValue(uuid, raw_text, number, author_uuid, raw_count, stricken, update_id));
            }
            return counts;
        }
        catch(IOException|IndexOutOfBoundsException e) {
            return new ArrayList<>();
        }
    }
    @SuppressWarnings("unchecked")
    private ArrayList<CountValue>[] readJSONCountNumFile(int thread_id, int file_number) {
        File f = new File(this.count_num_files.get(thread_id) + file_number+".json");
        try {
            return num_mapper.readValue(f, ArrayList[].class);
        }
        catch(IOException e) {
            ArrayList<CountValue>[] empty = new ArrayList[MSGS_PER_FILE];
            for(int i=0;i<MSGS_PER_FILE;i++) {
                empty[i] = new ArrayList<>();
            }
            return empty;
        }
    }
    @SuppressWarnings("unchecked")
    public ArrayList<CountValue>[] readCountNumFile_OLD(int thread_id, int file_number) {
        File f = new File(this.count_num_files.get(thread_id) + file_number);
        ArrayList<CountValue>[] numbers = new ArrayList[MSGS_PER_FILE];
        for(int i=0;i<MSGS_PER_FILE;i++) {
            numbers[i] = new ArrayList<>();
        }
        try {
            int bytes_read = 0;
            byte[] bytes = Files.readAllBytes(f.toPath());
            ByteBuffer data = ByteBuffer.wrap(bytes);
            while(bytes_read<bytes.length) {
                String uuid = new String(data.array(),bytes_read,36, StandardCharsets.ISO_8859_1);
                bytes_read+=36;
                int body_size = data.getInt(bytes_read);
                bytes_read+=4;
                String raw_text = new String(data.array(), bytes_read, body_size, StandardCharsets.UTF_8);
                bytes_read+=body_size;
                int number = data.getInt(bytes_read);
                bytes_read+=4;
                String author_uuid = new String(data.array(),bytes_read,36, StandardCharsets.ISO_8859_1);
                bytes_read+=36;
                int raw_count_size = data.getInt(bytes_read);
                bytes_read+=4;
                String raw_count = new String(data.array(), bytes_read, raw_count_size, StandardCharsets.UTF_8);
                bytes_read+=raw_count_size;
                boolean stricken = data.get(bytes_read) == 1;
                bytes_read+=1;
                int update_id = data.getInt(bytes_read);
                bytes_read+=4;
                numbers[(number-1)%MSGS_PER_FILE].add(new CountValue(uuid, raw_text, number, author_uuid, raw_count, stricken, update_id));
            }
            return numbers;
        }
        catch(IOException e) {
            return numbers;
        }
    }
    private void serializeUpdateFile_OLD(List<UpdateValue> updates, int thread_id, int file_number) {
        File f = new File(this.update_files.get(thread_id)+file_number+".json");
        try(FileOutputStream stream = new FileOutputStream(f)) {
            StringBuilder sb = new StringBuilder("[");
            if(updates.size()>0) {
                this.alwaysAppend(this.first_and_last_updates.get(thread_id), new String[]{updates.get(0).getUUID().toString(),updates.get(updates.size()-1).getUUID().toString()}, file_number);
                sb.append(updates.get(0));
                for (int i = 1; i < updates.size(); i++) {
                    sb.append(",").append(updates.get(i));
                }
            }
            sb.append("]");
            byte[] bytes = sb.toString().getBytes();
            stream.write(bytes);
            System.out.println(f+" was updated");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    public void serializeUpdateFile(List<UpdateValue> updates, int thread_id, int file_number) {
        File f = new File(this.update_files.get(thread_id)+file_number);
        if(updates.size()==0) {
            System.out.println(f+" was not updated because there are no counts");
            return;
        }
        this.alwaysAppend(this.first_and_last_updates.get(thread_id), new String[]{updates.get(0).getUUID().toString(),updates.get(updates.size()-1).getUUID().toString()}, file_number);
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
            for(UpdateValue update:updates) {
                stream.write(update.toBytes().array());
            }
            System.out.println(f+" was updated");
        } catch(IOException e) {
            e.printStackTrace();
        }
        this.updateFileData(thread_id);
    }
    public List<UpdateValue> readUpdateFile(int thread_id, int file_number) {
        File f = new File(this.update_files.get(thread_id) + file_number);
        try {
            int total = this.updates_per_file.get(thread_id).get(file_number);
            List<UpdateValue> counts = new ArrayList<>(total);
            for(int i=0;i<total;i++) {
                counts.add(null);
            }
            byte[] bytes = Files.readAllBytes(f.toPath());
            ByteBuffer data = ByteBuffer.wrap(bytes);
            CountDownLatch latch = new CountDownLatch(CORES);
            for(int i=0;i<CORES;i++) {
                int current_loc = data.getInt(i*4);
                int current_read_size = Math.min(((i+1) * MSGS_PER_FILE)/CORES, total);
                int read_size = current_read_size - (i * MSGS_PER_FILE)/CORES;
                if(current_loc != 0) {
                    new Thread(new Runnable() {
                        final IntWrapper loc = new IntWrapper(current_loc);
                        final int read = read_size;
                        final int read_start = current_read_size-read_size;
                        @Override
                        public void run() {
                            for(int i=0;i<read;i++) {
                                counts.set(read_start+i,UpdateValue.fromBytes(data, loc));
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
    private void serializeJSONCountTimeFile(List<CountValue> updates, int thread_id, int file_number) {
        File f = new File(this.count_time_files.get(thread_id)+file_number+".json");
        try(FileOutputStream stream = new FileOutputStream(f)) {
            StringBuilder sb = new StringBuilder("[");
            if(updates.size()>0) {
                this.alwaysAppend(this.first_and_last_counts.get(thread_id), new String[]{updates.get(0).getUUID().toString(),updates.get(updates.size()-1).getUUID().toString()}, file_number);
                sb.append(updates.get(0));
                for (int i = 1; i < updates.size(); i++) {
                    sb.append(",").append(updates.get(i));
                }
            }
            sb.append("]");
            byte[] bytes = sb.toString().getBytes();
            stream.write(bytes);
            System.out.println(f+" was updated");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    private void serializeCountTimeFile_OLD(List<CountValue> updates, int thread_id, int file_number) {
        File f = new File(this.count_time_files.get(thread_id)+file_number);
        try(FileOutputStream stream = new FileOutputStream(f)) {
            if(updates.size()>0) {
                this.alwaysAppend(this.first_and_last_counts.get(thread_id), new String[]{updates.get(0).getUUID().toString(),updates.get(updates.size()-1).getUUID().toString()}, file_number);
            }
            for(CountValue update:updates) {
                stream.write(update.toBytes().array());
            }
            System.out.println(f+" was updated");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    public void serializeCountTimeFile(List<CountValue> updates, int thread_id, int file_number) {
        File f = new File(this.count_time_files.get(thread_id)+file_number);
        if(updates.size()==0) {
            System.out.println(f+" was not updated because there are no counts");
            return;
        }
        this.alwaysAppend(this.first_and_last_counts.get(thread_id), new String[]{updates.get(0).getUUID().toString(),updates.get(updates.size()-1).getUUID().toString()}, file_number);
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
            for(CountValue update:updates) {
                stream.write(update.toBytes().array());
            }
            System.out.println(f+" was updated");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<CountValue> readCountTimeFile(int thread_id, int file_number) {
        try {
            File f = new File(this.count_time_files.get(thread_id) + file_number);
            int total = this.counts_per_file.get(thread_id).get(file_number);
            ArrayList<CountValue> counts = new ArrayList<>(total);
            for(int i=0;i<total;i++) {
                counts.add(null);
            }
            byte[] bytes = Files.readAllBytes(f.toPath());
            ByteBuffer data = ByteBuffer.wrap(bytes);
            CountDownLatch latch = new CountDownLatch(CORES);
            for(int i=0;i<CORES;i++) {
                int current_loc = data.getInt(i*4);
                int current_read_size = Math.min(((i+1) * MSGS_PER_FILE)/CORES, total);
                int read_size = current_read_size - (i * MSGS_PER_FILE)/CORES;
                if(current_loc != 0) {
                    new Thread(new Runnable() {
                        final IntWrapper loc = new IntWrapper(current_loc);
                        final int read = read_size;
                        final int read_start = current_read_size-read_size;
                        @Override
                        public void run() {
                            for(int i=0;i<read;i++) {
                                counts.set(read_start+i,CountValue.fromBytes(data, loc));
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
    private void serializeJSONCountNumFile(List<CountValue>[] counts, int thread_id, int file_number) {
        File f = new File(this.count_num_files.get(thread_id)+file_number+".json");
        try(FileOutputStream stream = new FileOutputStream(f)) {
            byte[] bytes = Arrays.toString(counts).getBytes();
            stream.write(bytes);
            System.out.println(f+" was updated");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    public void serializeCountNumFile(List<CountValue>[] counts, int thread_id, int file_number) {
        File f = new File(this.count_num_files.get(thread_id)+file_number);
        try(FileOutputStream stream = new FileOutputStream(f)) {
            for(List<CountValue> updates:counts) {
                for(CountValue update:updates) {
                    stream.write(update.toBytes().array());
                }
            }
            System.out.println(f+" was updated");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    public ArrayList<CountValue>[] readCountNumFile(int thread_id, int file_number) {
        File f = new File(this.count_num_files.get(thread_id) + file_number);
        ArrayList<CountValue>[] numbers = new ArrayList[MSGS_PER_FILE];
        for(int i=0;i<MSGS_PER_FILE;i++) {
            numbers[i] = new ArrayList<>();
        }
        try {
            IntWrapper loc = new IntWrapper(0);
            byte[] bytes = Files.readAllBytes(f.toPath());
            ByteBuffer data = ByteBuffer.wrap(bytes);
            while(loc.getValue()<bytes.length) {
                CountValue num = CountValue.fromBytes(data, loc);
                numbers[(num.getValidCountNumber()-1)%MSGS_PER_FILE].add(num);
            }
            return numbers;
        }
        catch(IOException e) {
            return numbers;
        }
    }
    public void serializeCountNumFileV2(List<CountValue>[] counts, int thread_id, int file_number) {
        File f = new File(this.count_num_files.get(thread_id)+file_number);
        if(counts.length==0) {
            System.out.println(f+" was not updated because there are no counts");
            return;
        }
        int[] locs = new int[CORES];
        int current_loc = 0;
        for(int i=0;i<locs.length;i++) {
            if((i * MSGS_PER_FILE)/CORES > counts.length) {
                locs[i] = 0;
            } else {
                locs[i] = (i * MSGS_PER_FILE) / CORES;
            }
        }
        int current_len = 0;
        for(int i=0;i<counts.length;i++) {
            if(current_loc < locs.length && i == locs[current_loc]) {
                locs[current_loc] = current_len+(CORES*4);
                current_loc++;
            }
            for(CountValue update:counts[i]) {
                current_len += update.toBytes().array().length;
            }
        }
        try(FileOutputStream stream = new FileOutputStream(f)) {
            for(int i=0;i<CORES;i++) {
                stream.write(ByteBuffer.allocate(4).putInt(locs[i]).array());
                System.out.println(locs[i]);
            }
            for(List<CountValue> updates:counts) {
                for(CountValue update:updates) {
                    stream.write(update.toBytes().array());
                }
            }
            System.out.println(f+" was updated");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    public ArrayList<CountValue>[] readCountNumFileV2(int thread_id, int file_number) {
        File f = new File(this.count_num_files.get(thread_id) + file_number);
        ArrayList<CountValue>[] numbers = new ArrayList[MSGS_PER_FILE];
        for(int i=0;i<MSGS_PER_FILE;i++) {
            numbers[i] = new ArrayList<>();
        }
        try {
            byte[] bytes = Files.readAllBytes(f.toPath());
            ByteBuffer data = ByteBuffer.wrap(bytes);
            CountDownLatch latch = new CountDownLatch(CORES);
            int current_loc = 0;
            int next_loc = data.getInt(0);
            for(int i=0;i<CORES;i++) {
                current_loc = next_loc;
                next_loc = data.getInt((i+1)*4);
                int end_loc;
                if(next_loc==0 || i == CORES-1) {
                    end_loc = bytes.length;
                } else {
                    end_loc = next_loc;
                }
                if(current_loc != 0) {
                    int finalCurrent_loc = current_loc;
                    new Thread(new Runnable() {
                        int loc = finalCurrent_loc;
                        final int end = end_loc;
                        @Override
                        public void run() {
                            while(loc < end) {
                                String uuid = new String(data.array(),loc,36, StandardCharsets.ISO_8859_1);
                                loc+=36;
                                int body_size = data.getInt(loc);
                                loc+=4;
                                String raw_text = new String(data.array(), loc, body_size, StandardCharsets.UTF_8);
                                loc+=body_size;
                                int number = data.getInt(loc);
                                loc+=4;
                                String author_uuid = new String(data.array(),loc,36, StandardCharsets.ISO_8859_1);
                                loc+=36;
                                int raw_count_size = data.getInt(loc);
                                loc+=4;
                                String raw_count = new String(data.array(), loc, raw_count_size, StandardCharsets.UTF_8);
                                loc+=raw_count_size;
                                boolean stricken = data.get(loc) == 1;
                                loc+=1;
                                int update_id = data.getInt(loc);
                                loc+=4;
                                numbers[(number-1)%MSGS_PER_FILE].add(new CountValue(uuid, raw_text, number, author_uuid, raw_count, stricken, update_id));
                            }
                            latch.countDown();
                        }
                    }).start();
                } else {
                    latch.countDown();
                }
            }
            latch.await();
            return numbers;
        }
        catch(IOException e) {
            return numbers;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public List<UpdateValue> getUpdatesByFile(int file_number) {
        int thread_id = 0;
        int total_files = 0;
        int current = this.updates_per_file.get(0).size();
        while(file_number >= total_files + current) {
            total_files += current;
            thread_id++;
            if(thread_id >= this.live_thread.size()) break;
            current = this.updates_per_file.get(thread_id).size();
        }
        return this.readUpdateFile(thread_id, file_number-total_files);
    }
    public ArrayList<CountValue> getCountsTimeByFile(int file_number) {
        int thread_id = 0;
        int total_files = 0;
        int current = this.counts_per_file.get(0).size();
        while(file_number >= total_files + current) {
            total_files += current;
            thread_id++;
            if(thread_id >= this.live_thread.size()) break;
            current = this.counts_per_file.get(thread_id).size();
        }
        return this.readCountTimeFile(thread_id, file_number-total_files);
    }
    public ArrayList<CountValue>[] getCountsNumByFile(int file_number) {
        ArrayList<ArrayList<CountValue>[]> arrays = new ArrayList<>();
        for(int i=0;i<live_thread.size();i++) {
            arrays.add(this.readCountNumFile(i, file_number));
        }
        return mergeCountNumArrays(arrays);
    }

    @SuppressWarnings("unchecked")
    private ArrayList<CountValue>[] mergeCountNumArrays(ArrayList<ArrayList<CountValue>[]> arrays) {
        int current_size = 0;
        for(ArrayList<CountValue>[] arr:arrays) {
            int prev_size = current_size;
            current_size = arr.length;
            if(prev_size != 0 && current_size != prev_size) throw new IndexOutOfBoundsException("Arrays must have the same length");
        }
        ArrayList<CountValue>[] merged = new ArrayList[current_size];
        for(ArrayList<CountValue>[] arr:arrays) {
            for(int i = 0; i < current_size; i++) {
                if(arr[i] == null) continue;
                if(merged[i]==null) {
                    merged[i] = arr[i];
                } else {
                    merged[i].addAll(arr[i]);
                }
            }
        }
        return merged;
    }

    /**
     *
     * @param file_number the file you want numbers from. This will return counts in this file index from all parts
     *                    of this thread.
     * @param start_number the first number you want (zero-indexed). If start_number is less than the first number,
     *                     this will return the entire file. If start_number is greater than the last number,
     *                     no counts will be returned.
     * @param end_number One more than the last number you want (zero-indexed). If end_number is greater than the last number, this will
     *                   return the entire file. If end_number is less than the first number, no counts
     *                   will be returned.
     * @return the counts that exist in the file between start_number (inclusive) and end_number (exclusive), zero-indexed
     *         or alternatively, the counts that exist between start_number (exclusive) and end_number (inclusive), one-indexed
     */
    public List<CountValue> getFlattenedCountsNumByFile(int file_number, int start_number, int end_number) {
        int default_start = file_number*MSGS_PER_FILE;
        int default_end = (file_number+1)*MSGS_PER_FILE;
        if(start_number > default_end || end_number < default_start) return new ArrayList<>();
        return this.getCountsNumSort(Math.max(default_start, start_number), Math.min(Math.min(this.getLast_number(), default_end),end_number));
    }
    public Integer updateDatabase() throws IOException, InterruptedException, URISyntaxException {
        for(int i=0;i<this.live_thread.size();i++) {
            int initial_updates = this.total_updates.get(i);
            if(this.total_updates.get(i)!=this.sumIntList(this.updates_per_file.get(i)) || this.total_counts.get(i)!=this.sumIntList(this.counts_per_file.get(i))) {
                throw new RuntimeException("Error: filedata desync");
            }
            this.handleThreadID(i);
            if(this.total_updates.get(i)!=this.sumIntList(this.updates_per_file.get(i)) || this.total_counts.get(i)!=this.sumIntList(this.counts_per_file.get(i))) {
                throw new RuntimeException("Error: filedata desync");
            }
            this.threadToCount(i);
            this.timeToNum(i);
            if(initial_updates!=this.total_updates.get(i)) {
                File[] files = new File("SidethreadData\\" + live_thread.get(i) + "\\stats_data").listFiles();
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
            }
        }
        return 0;
    }
   /**Note: Untested since I implemented multipart thread support*/
    public void updateSingleThreadDatabase(int thread_id, Long[] stop_point, String[] parameters) throws IOException, InterruptedException, URISyntaxException {
        this.handleThreadID(thread_id, stop_point, parameters);
        this.threadToCount(thread_id);
        this.timeToNum(thread_id);
    }

    private void handleThreadID(int thread_id) throws IOException, InterruptedException, URISyntaxException {
        handleThreadID(thread_id, new Long[10], new String[]{"limit=1000","commentsOnly=false","validCountsOnly=false"});
    }
    private void handleThreadID(int thread_id, Long[] stop_point, String[] parameters) throws IOException, InterruptedException, URISyntaxException {
        StringBuilder query = new StringBuilder("thread_name="+this.getName());
        for(String parameter:parameters) {
            query.append("&&").append(parameter);
        }
        int chat_file = this.updates_per_file.get(thread_id).size();
        if(chat_file > 0 && stop_point[0] == null) {
            List<UpdateValue> stops = this.readUpdateFile(thread_id, chat_file-1);
            for(int i=0;i<stop_point.length;i++) {
                if(stops.size() == 0) {
                    chat_file--;
                    if(chat_file > 0)
                        stops = this.readUpdateFile(thread_id, chat_file-1);
                    else break;
                }
                stop_point[i] = stops.remove(0).getId();
            }
        }
        List<UpdateValue> updates = new ArrayList<>();
        long least_id = Long.MAX_VALUE;
        while(true) {
            URL u = new URI("https", "api.counting.gg", "/api/thread/loadOlderCounts", query.toString(), null).toURL();
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "{:}");
            int status = connection.getResponseCode();
            if(status > 299) {
                System.out.println("Connection failed. Retrying");
                java.lang.Thread.sleep(55000);
                continue;
            }
            String text = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))
                    .lines()
                    .collect(Collectors.joining("\n"));
            Data1 data = mapper.readValue(text, Data1.class);
            UpdateValue[] messages = data.getCounts();
            if(messages.length == 0) {
                connection.disconnect();
                updateThread(updates, thread_id);
                return;
            }
            for(UpdateValue message:messages) {
                if(message.getId() >= least_id) {
                    continue;
                } else {
                    least_id = message.getId();
                }
                if(Arrays.asList(stop_point).contains(message.getId())) {
                    System.out.println("Received "+updates.size()+" messages for " + this.name.get(thread_id));
                    connection.disconnect();
                    updateThread(updates, thread_id);
                    return;
                }
                updates.add(message);
            }
            System.out.println("Received "+updates.size()+" messages for " + this.name.get(thread_id));
            if(data.isLoadedOldest()) {
                connection.disconnect();
                updateThread(updates, thread_id);
                return;
            }
            query = new StringBuilder("thread_name=").append(this.getName());
            query.append("&&uuid=").append(messages[messages.length-1].getUUID());
            query.append("&&limit=1000&&commentsOnly=false&&validCountsOnly=false");
        }
    }
    private void updateThread(List<UpdateValue> updates, int thread_id) {
        System.out.println("Completed "+this.name.get(thread_id));
        if(updates.size()==0) return;
        int chat_files = Math.max(this.updates_per_file.get(thread_id).size()-1,0);
        if(this.updates_per_file.get(thread_id).size() > 0 && this.updates_per_file.get(thread_id).get(chat_files) >= MSGS_PER_FILE) {
            chat_files++;
        }
        this.total_updates.set(thread_id, this.total_updates.get(thread_id) + updates.size());
        if(this.updates_per_file.get(thread_id).size()==0) this.updates_per_file.get(thread_id).add(0);
        while(true) {
            List<UpdateValue> past_updates = this.readUpdateFile(thread_id, chat_files);
            int lf_updates = this.updates_per_file.get(thread_id).get(this.updates_per_file.get(thread_id).size()-1);
            if(lf_updates>=MSGS_PER_FILE) lf_updates=0;
            if(lf_updates + updates.size() > MSGS_PER_FILE) {
                past_updates.addAll(0, updates.subList(updates.size()-(MSGS_PER_FILE-lf_updates),updates.size()));
                updates = updates.subList(0, updates.size()-(MSGS_PER_FILE-lf_updates));
                this.serializeUpdateFile(past_updates, thread_id, chat_files);
                alwaysAppend(this.updates_per_file.get(thread_id), MSGS_PER_FILE, chat_files);
            } else {
                past_updates.addAll(0, updates);
                this.serializeUpdateFile(past_updates, thread_id, chat_files);
                alwaysAppend(this.updates_per_file.get(thread_id), past_updates.size(), chat_files);
                break;
            }
            chat_files++;
        }
        this.updateFileData(thread_id);
    }
    private <T> void alwaysAppend(List<T> a, T datum, int loc) {
        if(a.size() <= loc)
            a.add(datum);
        else
            a.set(loc, datum);
    }
    private void threadToCount(int thread_id) {
        int chat_files = Math.max(this.counts_per_file.get(thread_id).size()-1,0);
        if(this.counts_per_file.get(thread_id).size() > 0 && this.counts_per_file.get(thread_id).get(chat_files) >= MSGS_PER_FILE) {
            chat_files++;
        }
        int last_update_id;
        if(this.total_counts.get(thread_id) == 0)
            last_update_id = -1;
        else
            last_update_id = getCountsTimeSortForSpecificThread(thread_id, this.total_counts.get(thread_id)-1).getUpdate_id();
        Location update_loc = getUpdateLocation(thread_id, last_update_id+1);
        int update_file = update_loc.getChatFile();
        int difference = last_update_id+1 - update_loc.getCountsByFile();
        if(this.counts_per_file.get(thread_id).size()==0) this.counts_per_file.get(thread_id).add(0);
        while(true) {
            List<CountValue> counts = getCountsFromUpdates(thread_id, last_update_id);
            this.total_counts.set(thread_id, this.total_counts.get(thread_id)+counts.size());
            if(this.total_updates.get(thread_id)-1==last_update_id && counts.size()==0) break;
            last_update_id += this.updates_per_file.get(thread_id).get(update_file) - difference;
            difference=0;
            update_file++;
            List<CountValue> past_counts = this.readCountTimeFile(thread_id, chat_files);
            int lf_counts = this.counts_per_file.get(thread_id).get(this.counts_per_file.get(thread_id).size()-1);
            if(lf_counts>=MSGS_PER_FILE) lf_counts=0;
            if(lf_counts + counts.size() > MSGS_PER_FILE) {
                past_counts.addAll(counts.subList(0, MSGS_PER_FILE-lf_counts));
                counts = new ArrayList<>(counts.subList(MSGS_PER_FILE-lf_counts, counts.size()));
                this.serializeCountTimeFile(past_counts, thread_id, chat_files);
                alwaysAppend(this.counts_per_file.get(thread_id), MSGS_PER_FILE, chat_files);
                chat_files++;
                this.serializeCountTimeFile(counts, thread_id, chat_files);
                alwaysAppend(this.counts_per_file.get(thread_id), counts.size(), chat_files);
            } else if(counts.size() > 0) {
                past_counts.addAll(counts);
                this.serializeCountTimeFile(past_counts, thread_id, chat_files);
                alwaysAppend(this.counts_per_file.get(thread_id), past_counts.size(), chat_files);
                if(past_counts.size()>=MSGS_PER_FILE) {
                    chat_files++;
                    this.counts_per_file.get(thread_id).add(0);
                }
            }
        }
        if(this.counts_per_file.get(thread_id).get(this.counts_per_file.get(thread_id).size()-1)==0)
            this.counts_per_file.get(thread_id).remove(this.counts_per_file.get(thread_id).size()-1);
        this.updateFileData(thread_id);
        System.out.println("Completed time ordering for "+this.name.get(thread_id));
    }
    protected List<CountValue> getCountsFromUpdates(int thread_id, int last_update_id) {
        Location loc = getUpdateLocation(thread_id, last_update_id+1);
        List<UpdateValue> nums = readUpdateFile(thread_id, loc.getChatFile());
        if(last_update_id+1>loc.getCountsByFile())
            nums = nums.subList(0, this.updates_per_file.get(thread_id).get(loc.getChatFile()) - (last_update_id+1 - loc.getCountsByFile()));
        List<CountValue> counts = new ArrayList<>();
        for(int i=nums.size()-1;i>=0;i--) {
            UpdateValue num = nums.get(i);
            last_update_id++;
            if(!num.isIsValidCount()) continue;
            modifyValidCountNumber(num);
            counts.add(new CountValue(num.getUUID(),num.getRawText(),num.getValidCountNumber(),num.getAuthorUUID(),num.getRawCount(),num.isStricken(),last_update_id));
        }
        return counts;
    }
    protected void modifyValidCountNumber(UpdateValue num) {}
    private Location getCountLocation(int thread_id, int count_number) {
        int counts_by_file = 0;
        int chat_file = 0;
        for(int counts:this.counts_per_file.get(thread_id)) {
            if(counts_by_file+counts<=count_number) {
                counts_by_file+=counts;
                chat_file++;
            }
            else break;
        }
        return new Location(counts_by_file, chat_file, count_number-counts_by_file);
    }
    /**
     * Only valid for count_num under the assumption that all counts exist in the proper order. (which they should)
     * */
    public Location getCountLocation(int count_number) {
        int counts_by_file = 0;
        int chat_file = 0;
        boolean broken = false;
        for(int i=0;i<this.counts_per_file.size();i++) {
            for (int counts : this.counts_per_file.get(i)) {
                if (counts_by_file + counts <= count_number) {
                    counts_by_file += counts;
                    chat_file++;
                } else {
                    broken = true;
                    break;
                }
            }
            if(broken) break;
        }
        if(!broken) return this.getMaxCountLocation();
        return new Location(counts_by_file, chat_file, count_number-counts_by_file);
    }
    private Location getUpdateLocation(int thread_id, int count_number) {
        int counts_by_file = 0;
        int chat_file = 0;
        for(int counts:this.updates_per_file.get(thread_id)) {
            if(counts_by_file+counts<=count_number) {
                counts_by_file+=counts;
                chat_file++;
            }
            else break;
        }
        return new Location(counts_by_file, chat_file, count_number-counts_by_file);
    }
    public UpdateValue getUpdates(int id_start) {
        return this.getUpdates(id_start, id_start+1).get(0);
    }
    private UpdateValue getUpdatesForSpecificThread(int thread_id, int id_start) {
        return this.getUpdatesForSpecificThread(thread_id, id_start, id_start+1).get(0);
    }
    private List<UpdateValue> getUpdatesForSpecificThread(int thread_id, int id_start, int id_end) {
        if(id_end>this.total_updates.get(thread_id)||id_start>id_end) throw new IndexOutOfBoundsException();
        if(id_start==id_end||this.total_updates.get(thread_id)==0) return new ArrayList<>();
        Location loc = getUpdateLocation(thread_id, id_start);
        int updates_by_file = loc.getCountsByFile();
        int chat_file = loc.getChatFile();
        List<UpdateValue> updates = new ArrayList<>();
        int id_temp = id_start;
        while(true) {
            boolean completed = true;
            List<UpdateValue> msgs = readUpdateFile(thread_id, chat_file);
            int next_cbf = this.updates_per_file.get(thread_id).get(chat_file);
            for(int i=id_temp;i<id_end;i++) {
                updates.add(msgs.get(msgs.size()-(i-updates_by_file+1)));
                if(updates_by_file+next_cbf-1==i && i!=id_end-1) {
                    updates_by_file += next_cbf;
                    id_temp = updates_by_file;
                    completed = false;
                    break;
                }
            }
            if(completed) break;
            chat_file++;
        }
        return updates;
    }
    public List<UpdateValue> getUpdates(int id_start, int id_end) {
        if(id_end>this.getTotal_updates()||id_start>id_end) throw new IndexOutOfBoundsException();
        List<UpdateValue> updates = new ArrayList<>();
        int border = 0;
        for(int i=0;i<this.live_thread.size();i++) {
            int next = this.total_updates.get(i);
            if (border >= id_end) break;
            border += next;
            if (border < id_start) continue;
            updates.addAll(this.getUpdatesForSpecificThread(i, Math.max(id_start-(border-next),0), Math.min(id_end-(border-next),this.total_updates.get(i))));
        }
        return updates;
    }
    public CountValue getCountsTimeSort(int id_start) {
        return getCountsTimeSort(id_start, id_start+1).get(0);
    }
    private CountValue getCountsTimeSortForSpecificThread(int thread_id, int id_start) {
        return getCountsTimeSortForSpecificThread(thread_id, id_start, id_start+1).get(0);
    }
    private List<CountValue> getCountsTimeSortForSpecificThread(int thread_id, int id_start, int id_end) {
        if(id_end>this.total_counts.get(thread_id)||id_start>id_end) throw new IndexOutOfBoundsException();
        if(id_start==id_end||this.total_counts.get(thread_id)==0) return new ArrayList<>();
        Location loc = getCountLocation(thread_id, id_start);
        int counts_by_file = loc.getCountsByFile();
        int chat_file = loc.getChatFile();
        List<CountValue> counts = new ArrayList<>();
        int id_temp = id_start;
        while(true) {
            List<CountValue> msgs = readCountTimeFile(thread_id, chat_file);
            int end1 = id_end-counts_by_file;
            int end2 = msgs.size();
            counts.addAll(msgs.subList(id_temp-counts_by_file,Math.min(end1, end2)));
            if(end2 >= end1) return counts;
            counts_by_file += this.counts_per_file.get(thread_id).get(chat_file);
            id_temp = counts_by_file;
            chat_file++;
        }
    }
    public List<CountValue> getCountsTimeSort(int id_start, int id_end) {
        if(id_end>this.getTotal_counts()||id_start>id_end) throw new IndexOutOfBoundsException();
        List<CountValue> counts = new ArrayList<>();
        int border = 0;
        for(int i=0;i<this.live_thread.size();i++) {
            int next = this.total_counts.get(i);
            if (border >= id_end) break;
            border+=next;
            if (border < id_start) continue;
            counts.addAll(this.getCountsTimeSortForSpecificThread(i, Math.max(id_start-(border-next),0), Math.min(id_end-(border-next),this.total_counts.get(i))));
        }
        return counts;
    }

    public List<CountValue> getCountsNumSort(int id_start) {
        return this.getCountsNumSort(id_start, id_start+1);
    }
    private List<CountValue> getCountsNumSortForSpecificThread(int thread_id, int id_start, int id_end) {
        if(id_end>this.last_number.get(thread_id)||id_start>id_end) throw new IndexOutOfBoundsException();
        if(id_start==id_end||this.last_number.get(thread_id)==0) return new ArrayList<>();
        List<CountValue> counts = new ArrayList<>();
        int id_temp = id_start;
        while(true) {
            boolean completed = true;
            List<CountValue>[] msgs = readCountNumFile(thread_id, id_temp/MSGS_PER_FILE);
            for(int i=id_temp;i<id_end;i++) {
                counts.addAll(msgs[i % MSGS_PER_FILE]);
                if(i%MSGS_PER_FILE==MSGS_PER_FILE-1 && i!=id_end-1) {
                    id_temp = MSGS_PER_FILE*(1+id_temp/MSGS_PER_FILE);
                    completed = false;
                    break;
                }
            }
            if(completed) break;
        }
        return counts;
    }
    public List<CountValue> getCountsNumSort(int id_start, int id_end) {
        if(id_end > this.getLast_number() || id_start > id_end) throw new IndexOutOfBoundsException();
        List<CountValue> counts = new ArrayList<>();
        for(int i=0;i<live_thread.size();i++) {
            int current_last = this.last_number.get(i);
            if(id_start < current_last) {
                counts.addAll(this.getCountsNumSortForSpecificThread(i, id_start, Math.min(id_end, current_last)));
            }
        }
        Collections.sort(counts);
        return counts;
    }

    public List<CountValue> getCountsFromList(List<Integer> counts) {
        Collections.sort(counts);
        if(counts.size() == 0 || this.getLast_number() < counts.get(counts.size()-1)) return new ArrayList<>();
        int counter = 0;
        List<CountValue> current_counts = new ArrayList<>();
        while(counter < counts.size()) {
            int last = (counts.get(counter)-1)/MSGS_PER_FILE;
            List<CountValue>[] current_nums = this.getCountsNumByFile(last);
            for(int i=counter;i<counts.size();i++) {
                if((counts.get(i)-1)/MSGS_PER_FILE == last) {
                    current_counts.addAll(current_nums[(counts.get(i)-1) % MSGS_PER_FILE]);
                    counter++;
                } else break;
            }
        }
        return current_counts;
    }
    /**
     * Indexed from 1
     * */
    public int getCountNumFileNumber(int count) {
        return (count-1)/MSGS_PER_FILE;
    }
    private Location getLocationOfFirstCountInSpecificThreadAfterTimestamp(int thread_id, UUID uuid) {
        long timestamp = uuid.getTime();
        int low = 0;
        int high = this.counts_per_file.get(thread_id).size() -1;
        while(low<=high) {
            int mid = (high+low)/2;
            long first = new UUID(first_and_last_counts.get(thread_id).get(mid)[0]).getTime();
            long last = new UUID(first_and_last_counts.get(thread_id).get(mid)[1]).getTime();
            if(last < timestamp)
                low = mid + 1;
            else if(first > timestamp) {
                if(mid==0)
                    return new Location(0,0, 0);
                high = mid - 1;
            } else {
                int counts_by_file = this.counts_per_file.get(thread_id).subList(0,mid).stream().mapToInt(Integer::intValue).sum();
                return new Location(counts_by_file, mid, binarySearchCountsForTimestamp(timestamp, readCountTimeFile(thread_id, mid)));
            }
        }
        if(low<this.counts_per_file.get(thread_id).size()) {
            int counts_by_file = this.counts_per_file.get(thread_id).subList(0,low).stream().mapToInt(Integer::intValue).sum();
            return new Location(counts_by_file, low, 0);
        }
        return null;
    }
    public Location getLocationOfFirstCountAfterTimestamp(UUID uuid) {
        Location loc = null;
        int counts_by_file = 0;
        int chat_files = 0;
        for(int i=0;i<live_thread.size();i++) {
            loc = getLocationOfFirstCountInSpecificThreadAfterTimestamp(i, uuid);
            if(loc != null) break;
            counts_by_file += this.total_counts.get(i);
            chat_files += this.counts_per_file.get(i).size();
        }
        if(loc == null) return null;
        return new Location(loc.getCountsByFile()+counts_by_file, loc.getChatFile()+chat_files, loc.getLocInFile());
    }
    private int binarySearchCountsForTimestamp(long timestamp, List<CountValue> counts) {
        int low = 0;
        int high = counts.size()-1;
        int mid = 0;
        while(low<=high) {
            mid = (high+low)/2;
            long count_time = counts.get(mid).getUUID().getTime();
            if(count_time < timestamp)
                low = mid + 1;
            else if(count_time > timestamp)
                high = mid - 1;
            else
                return mid;
        }
        if(counts.get(mid).getUUID().getTime() > timestamp)
            return mid;
        else
            return mid + 1;
    }
    private Location getLocationOfFirstUpdateInSpecificThreadAfterTimestamp(int thread_id, UUID uuid) {
        long timestamp = uuid.getTime();
        int low = 0;
        int high = this.updates_per_file.get(thread_id).size() - 1;
        while(low<=high) {
            int mid = (high+low)/2;
            long last = new UUID(first_and_last_updates.get(thread_id).get(mid)[0]).getTime();
            long first = new UUID(first_and_last_updates.get(thread_id).get(mid)[1]).getTime();
            if(last < timestamp)
                low = mid + 1;
            else if(first > timestamp) {
                if(mid==0)
                    return new Location(0,0, 0);
                high = mid - 1;
            } else {
                int updates_by_file = this.updates_per_file.get(thread_id).subList(0,mid).stream().mapToInt(Integer::intValue).sum();
                return new Location(updates_by_file, mid, this.updates_per_file.get(thread_id).get(mid)-1-binarySearchUpdatesForTimestamp(timestamp, readUpdateFile(thread_id, mid)));
            }
        }
        if(low<this.updates_per_file.get(thread_id).size()) {
            int updates_by_file = this.updates_per_file.get(thread_id).subList(0,low).stream().mapToInt(Integer::intValue).sum();
            return new Location(updates_by_file, low, 0);
        }
        return null;
    }
    public Location getLocationOfFirstUpdateAfterTimestamp(UUID uuid) {
        Location loc = null;
        int counts_by_file = 0;
        int chat_files = 0;
        for(int i=0;i<live_thread.size();i++) {
            loc = getLocationOfFirstUpdateInSpecificThreadAfterTimestamp(i, uuid);
            if(loc != null) break;
            counts_by_file += this.total_updates.get(i);
            chat_files += this.updates_per_file.get(i).size();
        }
        if(loc == null) return null;
        return new Location(loc.getCountsByFile()+counts_by_file, loc.getChatFile()+chat_files, loc.getLocInFile());
    }
    private int binarySearchUpdatesForTimestamp(long timestamp, List<UpdateValue> updates) {
        int high = 0;
        int low = updates.size()-1;
        int mid = 0;
        while(high<=low) {
            mid = (high+low)/2;
            long count_time = updates.get(mid).getUUID().getTime();
            if(count_time < timestamp)
                low = mid - 1;
            else if(count_time > timestamp)
                high = mid + 1;
            else
                return mid;
        }
        if(updates.get(mid).getUUID().getTime() < timestamp)
            return mid - 1;
        else
            return mid;
    }

    private enum Mode {INSERT, REMOVE, DELETE, EDIT}

    /**
     * Calling function is expected to update file data afterwards.
     */
    private void modifyCountFromTimestamp(int thread_id, UUID uuid, Mode mode, CountValue count) {
        boolean changed = false;
        Location loc = getLocationOfFirstCountInSpecificThreadAfterTimestamp(thread_id, uuid);
        if(loc == null) throw new NullPointerException("The count didn't exist");
        List<CountValue> counts = readCountTimeFile(thread_id, loc.getChatFile());
        switch (mode) {
            case INSERT -> {
                changed = true;
                counts.add(loc.getLocInFile(), count);
                this.total_counts.set(thread_id, this.total_counts.get(thread_id)+1);
                this.counts_per_file.get(thread_id).set(loc.getChatFile(), this.counts_per_file.get(thread_id).get(loc.getChatFile()) + 1);
            }
            case REMOVE -> {
                if (uuid.equals(counts.get(loc.getLocInFile()).getUUID())) {
                    changed = true;
                    counts.remove(loc.getLocInFile());
                    this.total_counts.set(thread_id, this.total_counts.get(thread_id)-1);
                    this.counts_per_file.get(thread_id).set(loc.getChatFile(), this.counts_per_file.get(thread_id).get(loc.getChatFile()) - 1);
                }
            }
            case DELETE -> {
                this.removeCountsFromList(thread_id, this.splitExtraCounts(counts.subList(loc.getLocInFile(), counts.size())));
                counts = counts.subList(0, loc.getLocInFile());
                this.counts_per_file.get(thread_id).set(loc.getChatFile(), counts.size());
                changed = true;
            }
            case EDIT -> {
                if (uuid.equals(counts.get(loc.getLocInFile()).getUUID())) {
                    changed = true;
                    counts.set(loc.getLocInFile(), count);
                }
            }
        }
        if(changed) {
            this.serializeCountTimeFile(counts, thread_id, loc.getChatFile());
        }
    }
    private enum Mode2 {STRIKE, DELETE, EDIT}
    private void modifyUpdateFromTimestamp(int thread_id, UUID uuid, Mode2 mode, UpdateValue update) {
        boolean changed = false;
        Location loc = getLocationOfFirstUpdateInSpecificThreadAfterTimestamp(thread_id, uuid);
        if(loc == null) throw new NullPointerException("The location didn't exist");
        List<UpdateValue> updates = readUpdateFile(thread_id, loc.getChatFile());
        int update_size = updates.size();
        switch (mode) {
            case STRIKE -> {
                UpdateValue to_strike = updates.get(update_size - 1 - loc.getLocInFile());
                if (update.getId() == to_strike.getId()) {
                    changed = true;
                    to_strike.setStricken(true);
                }
            }
            case DELETE -> {
                updates = updates.subList(update_size - loc.getLocInFile(), updates.size());
                this.updates_per_file.get(thread_id).set(loc.getChatFile(), updates.size());
                changed = true;
            }
            case EDIT -> {
                UpdateValue comparison = updates.get(update_size - 1 - loc.getLocInFile());
                if (update.getId() == comparison.getId()) {
                    changed = true;
                    updates.set(update_size - 1 - loc.getLocInFile(), update);
                }
            }
        }
        if(changed) {
            this.serializeUpdateFile(updates, thread_id, loc.getChatFile());
        }
    }
    private ArrayList<ArrayList<CountValue>> splitExtraCounts(List<CountValue> extra_counts) {
        if(extra_counts.isEmpty()) return new ArrayList<>();
        extra_counts.sort(Comparator.comparing(CountValue::getValidCountNumber));
        ArrayList<ArrayList<CountValue>> extra_2d = new ArrayList<>();
        extra_2d.add(new ArrayList<>());
        extra_2d.get(0).add(extra_counts.get(0));
        for(int i=1;i<extra_counts.size();i++) {
            int num1 = extra_counts.get(i).getValidCountNumber();
            int num2 = extra_counts.get(i-1).getValidCountNumber();
            if(num1-num2<MSGS_PER_FILE && (num1-1)%MSGS_PER_FILE-(num2-1)%MSGS_PER_FILE>=0)
                extra_2d.get(extra_2d.size()-1).add(extra_counts.get(i));
            else {
                extra_2d.add(new ArrayList<>());
                extra_2d.get(extra_2d.size()-1).add(extra_counts.get(i));
            }
        }
        return extra_2d;
    }
    private record BadCountData(List<Integer> files, ArrayList<ArrayList<CountValue>> updates){}
    private BadCountData splitBadCounts(List<CountValue> bad_counts, int thread_id) {
        if(bad_counts.isEmpty()) return new BadCountData(new ArrayList<>(), new ArrayList<>());
        bad_counts.sort(Comparator.comparing(a -> (a.getUUID().getTime())));
        ArrayList<ArrayList<CountValue>> bad_2d = new ArrayList<>();
        List<Integer> files = new ArrayList<>();
        int current_file = 0;
        bad_2d.add(new ArrayList<>());
        for(int j=current_file;j<first_and_last_counts.get(thread_id).size();j++) {
            if(new UUID(first_and_last_counts.get(thread_id).get(current_file)[1]).getTime() >= bad_counts.get(0).getUUID().getTime()) break;
            current_file++;
        }
        files.add(current_file);
        bad_2d.get(0).add(bad_counts.get(0));
        for(int i=1;i<bad_counts.size();i++) {
            long num1 = bad_counts.get(i).getUUID().getTime();
            long num2 = new UUID(first_and_last_counts.get(thread_id).get(current_file)[1]).getTime();
            if(num2 >= num1)
                bad_2d.get(bad_2d.size()-1).add(bad_counts.get(i));
            else {
                for(int j=current_file;j<first_and_last_counts.get(thread_id).size();j++) {
                    current_file++;
                    if(new UUID(first_and_last_counts.get(thread_id).get(current_file)[1]).getTime() >= num1) break;
                }
                files.add(current_file);
                bad_2d.add(new ArrayList<>());
                bad_2d.get(bad_2d.size()-1).add(bad_counts.get(i));
            }
        }
        return new BadCountData(files, bad_2d);
    }
    //Note: The desync will be especially bad if any two part threads exceed MSGS_PER_FILE
    private void timeToNum(int thread_id) {
        List<CountValue> extra_counts = new ArrayList<>();
        Location loc = getCountLocation(thread_id, this.last_num_update.get(thread_id));
        int counts_by_file = loc.getCountsByFile();
        int chat_file = loc.getChatFile();
        for(int i=chat_file;i<this.counts_per_file.get(thread_id).size();i++) {
            int nums_added = 0;
            List<CountValue> counts = readCountTimeFile(thread_id, chat_file);
            if(this.last_num_update.get(thread_id)!=counts_by_file)
                counts = counts.subList(this.last_num_update.get(thread_id)-counts_by_file, counts.size());
            List<CountValue>[] current_nums = readCountNumFile(thread_id, chat_file);
            for(CountValue count:counts) {
                int index = (count.getValidCountNumber()-1)-(chat_file*MSGS_PER_FILE);
                if(index < 0 || index >= MSGS_PER_FILE) {
                    extra_counts.add(count);
                } else {
                    nums_added++;
                    current_nums[index].add(count);
                    if(count.getValidCountNumber() > this.last_number.get(thread_id)) {
                        this.last_number.set(thread_id,count.getValidCountNumber());
                    }
                }
            }
            if(nums_added>0)
                this.serializeCountNumFile(current_nums, thread_id, chat_file);
            counts_by_file += this.counts_per_file.get(thread_id).get(i);
            this.last_num_update.set(thread_id, counts_by_file);
            chat_file++;
        }
        ArrayList<ArrayList<CountValue>> extra_2d = this.splitExtraCounts(extra_counts);
        extra_counts.clear();
        for(ArrayList<CountValue> counts: extra_2d) {
            ArrayList<CountValue>[] current_nums = readCountNumFile(thread_id, (counts.get(0).getValidCountNumber()-1)/MSGS_PER_FILE);
            ArrayList<CountValue>[] comparison_nums = getCountsNumByFile((counts.get(0).getValidCountNumber()-1)/MSGS_PER_FILE);
            int nums_added = 0;
            for(CountValue count:counts) {
                if(count.getValidCountNumber()>this.getLast_number()+MSGS_PER_FILE) {
                    extra_counts.add(count);
                    this.EX_COUNTS_LOGGER.get(thread_id).info(String.format("%d was not converted to a count due to being too far above the last count",count.getValidCountNumber()));
                    continue;
                } else if(count.getValidCountNumber() > this.last_number.get(thread_id)) {
                    this.last_number.set(thread_id, count.getValidCountNumber());
                } else if((count.getValidCountNumber()-1)%MSGS_PER_FILE<0||(count.getValidCountNumber()<this.getLast_number()-MSGS_PER_FILE)&&comparison_nums[(count.getValidCountNumber()-1)%MSGS_PER_FILE].size() != 0) {
                    extra_counts.add(count);
                    this.EX_COUNTS_LOGGER.get(thread_id).info(String.format("%d was not converted to a count due to being too far below the last count where there already existed a count",count.getValidCountNumber()));
                    continue;
                }
                nums_added++;
                current_nums[(count.getValidCountNumber()-1)%MSGS_PER_FILE].add(count);
            }
            if(nums_added>0)
                serializeCountNumFile(current_nums, thread_id, (counts.get(0).getValidCountNumber()-1)/MSGS_PER_FILE);
        }
        BadCountData to_remove = this.splitBadCounts(extra_counts, thread_id);
        this.removeCountsTimeFromList(thread_id, to_remove);
        this.last_num_update.set(thread_id, this.total_counts.get(thread_id));
        this.updateFileData(thread_id);
        System.out.println("Completed num ordering of "+this.name.get(thread_id));
    }
    private AddCountCheckedStricken addCountByURL(int number, URL url, boolean set_mode) throws IOException {
        String[] s = url.getPath().split("/");
        int thread_id = -1;
        for(int i=0;i<live_thread.size();i++) {
            if(s.length == 5 && s[2].equals(this.live_thread.get(i))) {
                thread_id = i;
                break;
            }
        }
        if(s.length != 5 || !s[1].equals("live") || thread_id == -1 || !s[3].equals("updates") || !s[4].matches("^[\\da-f]{8}(?:-[\\da-f]{4}){3}-[\\da-f]{12}.json$")) {
            if(thread_id == -1) {
                thread_id = live_thread.size()-1;
            }
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Adding of number %d failed. Invalid URL syntax.", number));
            throw new MalformedURLException(String.format("Adding of number %d failed. Invalid URL syntax.", number));
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        UpdateValue msg = this.checkStricken(thread_id, number, connection, Operation.Adding, ValueType.number);
        connection.disconnect();
        Location msg_loc = this.getLocationOfFirstUpdateInSpecificThreadAfterTimestamp(thread_id, msg.getUUID());
        UpdateValue in_database = null;
        if(msg_loc != null) {
            in_database = this.getUpdatesForSpecificThread(thread_id, msg_loc.getCountsByFile() + msg_loc.getLocInFile());
        }
        if(in_database == null || msg.getId() != in_database.getId()) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Adding of number %d failed. Update does not exist in database", number));
            throw new IllegalArgumentException(String.format("Adding of number %d failed. Update does not exist in database", number));
        }
        return this.addCount(thread_id, new CountValue(msg.getUUID(), msg.getRawText(), number, in_database.getAuthorUUID(), msg.getRawCount(), msg.isStricken(), msg_loc.getCountsByFile() + msg_loc.getLocInFile()), set_mode);
    }
    private record AddCountCheckedStricken(CountValue count, boolean checked) {}
    private AddCountCheckedStricken addCount(int thread_id, CountValue count, boolean set_mode) throws IOException {
        if(count.getValidCountNumber() < 1) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Adding of number %d failed. You cannot add a number before the first count.", count.getValidCountNumber()));
            throw new IllegalArgumentException(String.format("Adding of number %d failed. You cannot add a number before the first count.", count.getValidCountNumber()));
        }
        if(count.getValidCountNumber() > this.last_number.get(thread_id)) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Adding of number %d failed. You cannot add a number after the last count.", count.getValidCountNumber()));
            throw new IllegalArgumentException(String.format("Adding of number %d failed. You cannot add a number after the last count.", count.getValidCountNumber()));
        }
        if(count.isStricken()) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Adding of number %d failed. You are trying to add a stricken number",count.getValidCountNumber()));
            throw new IllegalArgumentException(String.format("Adding of number %d failed. You are trying to add a stricken number",count.getValidCountNumber()));
        }
        Location val = this.getLocationOfFirstCountInSpecificThreadAfterTimestamp(thread_id, count.getUUID());
        if(val==null) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Adding of number %d failed. The count_time files need to be updated.", count.getValidCountNumber()));
            throw new IllegalArgumentException(String.format("Adding of number %d failed. The count_time files need to be updated.", count.getValidCountNumber()));
        }
        CountValue compare = this.getCountsTimeSortForSpecificThread(thread_id, val.getLocInFile()+val.getCountsByFile());
        if(Objects.equals(compare.getUUID(), count.getUUID())) {
            if(compare.getValidCountNumber()!=count.getValidCountNumber()) {
                this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Adding of number %d failed. The count is already recorded as %s.", count.getValidCountNumber(), compare.getValidCountNumber()));
                throw new IllegalArgumentException(String.format("Adding of number %d failed. The count is already recorded as %s.", count.getValidCountNumber(), compare.getValidCountNumber()));
            } else {
                this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Adding of number %d 'succeeded'. The count already existed at the desired location.", count.getValidCountNumber()));
                return new AddCountCheckedStricken(count, false);
            }
        }
        List<CountValue> vals = this.getCountsNumSort(count.getValidCountNumber()-1);
        if(vals.size() > 0) {
            if(set_mode) {
                for(CountValue num:vals) {
                    int current_count_thread_id = this.getThreadIDFromCountValue(num);
                    URL url = new URL("https://www.reddit.com/live/"+this.live_thread.get(current_count_thread_id)+"/updates/"+num.getUUID()+".json");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    this.checkStricken(current_count_thread_id, count.getValidCountNumber(), connection, Operation.Removal, ValueType.number);
                    this.modifyUpdateFromTimestamp(current_count_thread_id, num.getUUID(), Mode2.STRIKE, getUpdatesForSpecificThread(current_count_thread_id, num.getUpdate_id()));
                }
            }
            else if(vals.size() == 1) {
                this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Adding of number %d failed. 1 count already exists at that location.", count.getValidCountNumber()));
                throw new IllegalArgumentException(String.format("Adding of number %d failed. 1 count already exists at that location.", count.getValidCountNumber()));
            } else {
                this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Adding of number %d failed. %d counts already exist at that location.", count.getValidCountNumber(), vals.size()));
                throw new IllegalArgumentException(String.format("Adding of number %d failed. %d counts already exist at that location.", count.getValidCountNumber(), vals.size()));
            }
        }
        int chat_file = (count.getValidCountNumber()-1)/MSGS_PER_FILE;
        List<CountValue>[] to_update = this.readCountNumFile(thread_id, chat_file);
        to_update[(count.getValidCountNumber()-1)%MSGS_PER_FILE].add(count);
        this.serializeCountNumFile(to_update, thread_id, chat_file);
        this.modifyCountFromTimestamp(thread_id, count.getUUID(), Mode.INSERT, count);
        this.last_num_update.set(thread_id, this.last_num_update.get(thread_id)+1);
        this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Added %s.",count));
        this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Adding of number %d succeeded.",count.getValidCountNumber()));
        this.updateFileData(thread_id);
        return new AddCountCheckedStricken(count, true);
    }
    private enum Operation {Adding, Removal}
    private enum ValueType {update, number}
    private UpdateValue checkStricken(int thread_id, int number, HttpURLConnection connection, Operation operation, ValueType value_type) throws IOException {
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "LCStatBot/1.0 by /u/amazingpikachu_38");
        int status = connection.getResponseCode();
        if(status==404) {
            if(!operation.equals(Operation.Removal)) {
                String format = String.format("%s of %s %d failed. You are trying to add a deleted %s or you have an invalid URL.", operation, value_type, number, value_type);
                this.SET_COUNTS_LOGGER.get(thread_id).info(format);
                throw new MalformedURLException(format);
            } else return null;
        } else if(status > 299) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("%s of %s %d failed due to a connection issue. Please try again.", operation, value_type, number));
            throw new HttpRetryException(String.format("%s of %s %d failed due to a connection issue. Please try again.", operation, value_type, number),status);
        }
        String text = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))
                .lines()
                .collect(Collectors.joining("\n"));
        UpdateValue msg = mapper.readValue(text, Data1.class).getCounts()[0];
        if(!operation.equals(Operation.Removal) && msg.isStricken()) {
            String format = String.format("%s of %s %d failed. You are trying to add a stricken %s.", operation, value_type, number, value_type);
            this.SET_COUNTS_LOGGER.get(thread_id).info(format);
            throw new IllegalArgumentException(format);
        } else if(operation.equals(Operation.Removal) && !msg.isStricken()) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Removal of %s %d failed. Not all %ss wanting to be removed have been stricken or deleted.", value_type, number, value_type));
            throw new IllegalArgumentException(String.format("Removal of %s %d failed. Not all %ss wanting to be removed have been stricken or deleted.", value_type, number, value_type));
        }
        return msg;
    }
    private List<CountValue> removeCount(int thread_id, int number, boolean strike_override, boolean set_mode, CountValue set_count) throws IOException {
        if(number < 1) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Removal of number %d failed. You cannot remove a number before the first count.", number));
            throw new IllegalArgumentException(String.format("Removal of number %d failed. You cannot remove a number before the first count.", number));
        }
        if(number > this.last_number.get(thread_id)) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Removal of number %d failed. You cannot remove a number after the last count.", number));
            throw new IllegalArgumentException(String.format("Removal of number %d failed. You cannot remove a number after the last count.", number));
        }
        List<CountValue>[] counts = this.readCountNumFile(thread_id, (number-1)/MSGS_PER_FILE);
        List<CountValue> nums_to_remove = new ArrayList<>(counts[(number-1)%MSGS_PER_FILE]);
        counts[(number-1)%MSGS_PER_FILE].clear();
        if(set_mode && this.getThreadIDFromCountValue(set_count) == thread_id) {
            counts[(number - 1) % MSGS_PER_FILE].add(set_count);
            for(CountValue num:nums_to_remove) {
                if(num.getUUID().equals(set_count.getUUID())) {
                    nums_to_remove.remove(num);
                    break;
                }
            }
        }
        if(nums_to_remove.size() == 0) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Successfully removed 0 counts at %d.",number));
            return nums_to_remove;
        }
        if(!strike_override) {
            for(CountValue num:nums_to_remove) {
                URL url = new URL("https://www.reddit.com/live/"+this.live_thread.get(thread_id)+"/updates/"+num.getUUID()+".json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                this.checkStricken(thread_id, number, connection, Operation.Removal, ValueType.number);
                this.modifyUpdateFromTimestamp(thread_id, num.getUUID(), Mode2.STRIKE, getUpdatesForSpecificThread(thread_id, num.getUpdate_id()));
            }
        }
        this.serializeCountNumFile(counts, thread_id, (number-1)/MSGS_PER_FILE);
        for(CountValue num:nums_to_remove) {
            this.last_num_update.set(thread_id, this.last_num_update.get(thread_id) - 1);
            this.modifyCountFromTimestamp(thread_id, num.getUUID(), Mode.REMOVE, null);
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Removed %s.",num));
        }
        if(nums_to_remove.size() == 1)
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Successfully removed 1 count at %d.",number));
        else
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Successfully removed %d counts at %d.",nums_to_remove.size(), number));
        if(number == this.last_number.get(thread_id)) {
            this.last_number.set(thread_id,this.findLastCount(thread_id));
        }
        this.updateFileData(thread_id);
        return nums_to_remove;
    }
    public List<CountValue> setCountByURL(int number, URL url) throws IOException {
        AddCountCheckedStricken set_count = this.addCountByURL(number, url, true);
        List<CountValue> counts = new ArrayList<>();
        counts.add(set_count.count());
        for(int i=0;i<live_thread.size();i++) {
            if(number <= this.last_number.get(i)) {
                counts.addAll(this.removeCount(i, number, set_count.checked(), true, set_count.count()));
            }
        }
        return counts;
    }
    public List<CountValue> removeCount(int number, boolean force) throws IOException {
        List<CountValue> counts = new ArrayList<>();
        for(int i=0;i<live_thread.size();i++) {
            if(number <= this.last_number.get(i)) {
                counts.addAll(this.removeCount(i, number, force, false, null));
            }
        }
        return counts;
    }
    private int getThreadIDFromCountValue(CountValue count) {
        if(live_thread.size()==1) return 0;
        for(int i=0;i<live_thread.size();i++) {
            Location loc = getLocationOfFirstUpdateInSpecificThreadAfterTimestamp(i, count.getUUID());
            if(loc != null) return i;
        }
        throw new IndexOutOfBoundsException("No thread found");
    }
    private int getThreadIDFromUpdateValue(UpdateValue count) {
        if(live_thread.size()==1) return 0;
        for(int i=0;i<live_thread.size();i++) {
            Location loc = getLocationOfFirstUpdateInSpecificThreadAfterTimestamp(i, count.getUUID());
            if(loc != null) return i;
        }
        throw new IndexOutOfBoundsException("No thread found");
    }
    private int getThreadIDFromUUID(UUID uuid) {
        if(live_thread.size()==1) return 0;
        for(int i=0;i<live_thread.size();i++) {
            Location loc = getLocationOfFirstUpdateInSpecificThreadAfterTimestamp(i, uuid);
            if(loc != null) return i;
        }
        return live_thread.size()-1;
    }
    public String getURLFromCountValue(CountValue count) {
        int thread_id = this.getThreadIDFromCountValue(count);
        return "https://www.counting.gg/thread/"+live_thread.get(thread_id)+"/"+count.getUUID();
    }
    public String getURLFromUpdateValue(UpdateValue count) {
        int thread_id = this.getThreadIDFromUpdateValue(count);
        return "https://www.counting.gg/thread/"+live_thread.get(thread_id)+"/"+count.getUUID();
    }
    /**
     * @param context A value of 0 provides the standard update link.
     *                A value greater than 0 provides after context.
     *                A value less than 0 provides before context.
    */
    public String getURLFromUUID(UUID uuid, int context) {
        int thread_id = this.getThreadIDFromUUID(uuid);
        if(context==0) {
            return "https://www.reddit.com/live/" + live_thread.get(thread_id) + "/updates/" + uuid;
        } else if(context > 0) {
            return "https://www.reddit.com/live/" + live_thread.get(thread_id) + "?after=LiveUpdate_" + uuid;
        } else {
            return "https://www.reddit.com/live/" + live_thread.get(thread_id) + "?before=LiveUpdate_" + uuid;
        }
    }
    public CountValue resetCountFromTimestamp(UUID uuid, int new_num, boolean force) {
        Location msg_loc = this.getLocationOfFirstCountAfterTimestamp(uuid);
        if(msg_loc == null) {
            this.SET_COUNTS_LOGGER.get(this.live_thread.size()-1).info(String.format("Reset of number %d failed. Update does not exist in database", new_num));
            throw new IllegalArgumentException(String.format("Reset of number %d failed. Update does not exist in database", new_num));
        }
        CountValue count = this.getCountsTimeSort(msg_loc.getCountsByFile() + msg_loc.getLocInFile());
        int thread_id = this.getThreadIDFromCountValue(count);
        if(!Objects.equals(uuid, count.getUUID())) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Reset of number %d failed. Update does not exist in database", new_num));
            throw new IllegalArgumentException(String.format("Reset of number %d failed. Update does not exist in database", new_num));
        }
        if(count.getValidCountNumber() == new_num) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Reset of number %d \"succeeded\". Count already existed at desired location.", new_num));
            return count;
        }
        return this.reSetCount(thread_id, count, new_num, force);
    }
    private CountValue reSetCount(int thread_id, CountValue count, int new_num, boolean force) {
        if(count.getValidCountNumber() == new_num) return count;
        if(new_num < 1) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Reset of number %d failed. You cannot add a number before the first count.", new_num));
            throw new IllegalArgumentException(String.format("Reset of number %d failed. You cannot add a number before the first count.", new_num));
        }
        if(new_num > this.last_number.get(thread_id)) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Reset of number %d failed. You cannot add a number after the last count.", new_num));
            throw new IllegalArgumentException(String.format("Reset of number %d failed. You cannot add a number after the last count.", new_num));
        }
        if(count.isStricken()) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Reset of number %d failed. You are trying to add a stricken number", new_num));
            throw new IllegalArgumentException(String.format("Reset of number %d failed. You are trying to add a stricken number", new_num));
        }
        if(!force && this.getCountsNumSort(new_num-1).size() > 0) {
            this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Reset of number %d failed. You are trying to reset a number to a location where there already exists at least one number", new_num));
            throw new IllegalArgumentException(String.format("Reset of number %d failed. You are trying to reset a number to a location where there already exists at least one number", new_num));
        }
        int old_num = count.getValidCountNumber();
        this.removeCountsFromList(thread_id, new ArrayList<>(List.of(new ArrayList<>(Collections.singletonList(count)))));
        count.setValidCountNumber(new_num);
        int chat_file = (count.getValidCountNumber()-1)/MSGS_PER_FILE;
        List<CountValue>[] to_update = this.readCountNumFile(thread_id, chat_file);
        to_update[(count.getValidCountNumber()-1)%MSGS_PER_FILE].add(count);
        this.serializeCountNumFile(to_update, thread_id, chat_file);
        this.modifyCountFromTimestamp(thread_id, count.getUUID(), Mode.EDIT, count);
        if(old_num==this.last_number.get(thread_id)) {
            this.last_number.set(thread_id, this.findLastCount(thread_id));
            this.updateFileData(thread_id);
        }
        this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Reset %s.",count));
        this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("Reset of number %d succeeded.",count.getValidCountNumber()));
        return count;
    }
    private int findLastCount(int thread_id) {
        int chat_files = (this.last_number.get(thread_id)-1)/MSGS_PER_FILE;
        for(int i=chat_files;i>=0;i--) {
            List<CountValue> counts = this.getCountsNumSortForSpecificThread(thread_id, MSGS_PER_FILE*i, Math.min(MSGS_PER_FILE*(i+1), this.last_number.get(thread_id)));
            if(counts.size()>0)
                return counts.get(counts.size()-1).getValidCountNumber();
            else {
                File f = new File(this.count_num_files.get(thread_id)+i+".json");
                if (f.delete())
                    System.out.println(f + " was deleted");
                else
                    this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("File %s failed to delete", f));
            }
        }
        return 0;
    }
    private void removeAllUpdatesAfterTimestamp(int thread_id, UUID uuid) {
        Location loc = this.getLocationOfFirstUpdateInSpecificThreadAfterTimestamp(thread_id, uuid);
        if(loc==null) return;
        this.modifyUpdateFromTimestamp(thread_id, uuid, Mode2.DELETE, null);
        int chat_file = loc.getChatFile();
        int total_files = updates_per_file.get(thread_id).size();
        for(int i=chat_file+1;i<total_files;i++) {
            File f = new File(this.update_files.get(thread_id)+i+".json");
            if(f.delete()) {
                System.out.println(f + " was deleted");
                updates_per_file.get(thread_id).remove(updates_per_file.get(thread_id).size()-1);
            } else
                this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("File %s failed to delete", f));
        }
        if(this.updates_per_file.get(thread_id).get(this.updates_per_file.get(thread_id).size()-1) == 0) {
            File f = new File(this.update_files.get(thread_id)+chat_file+".json");
            if(f.delete()) {
                System.out.println(f + " was deleted");
                updates_per_file.get(thread_id).remove(updates_per_file.get(thread_id).size()-1);
            } else
                this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("File %s failed to delete", f));
        }
        this.total_updates.set(thread_id, this.updates_per_file.get(thread_id).stream().mapToInt(Integer::intValue).sum());
    }
    public void removeAllCountsAfterTimestamp(UUID uuid) {
        for(int i=0;i<this.live_thread.size();i++) {
            this.removeAllCountsAfterTimestampForSpecificThread(i, uuid);
        }
    }
    private void removeAllCountsAfterTimestampForSpecificThread(int thread_id, UUID uuid) {
        this.removeAllUpdatesAfterTimestamp(thread_id, uuid);
        Location loc = this.getLocationOfFirstCountInSpecificThreadAfterTimestamp(thread_id, uuid);
        if(loc == null) return;
        int chat_file = loc.getChatFile();
        int total_files = this.counts_per_file.get(thread_id).size();
        this.modifyCountFromTimestamp(thread_id, uuid, Mode.DELETE, null);
        for(int i=chat_file+1;i<total_files;i++) {
            List<CountValue> to_delete = this.readCountTimeFile(thread_id, i);
            this.removeCountsFromList(thread_id, this.splitExtraCounts(to_delete));
            File f = new File(this.count_time_files.get(thread_id)+i+".json");
            if(f.delete()) {
                System.out.println(f + " was deleted");
                counts_per_file.get(thread_id).remove(counts_per_file.get(thread_id).size()-1);
                first_and_last_updates.get(thread_id).remove(first_and_last_updates.get(thread_id).size()-1);
                first_and_last_counts.get(thread_id).remove(first_and_last_counts.get(thread_id).size()-1);
            } else
                this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("File %s failed to delete", f));
        }
        if(this.counts_per_file.get(thread_id).get(this.counts_per_file.get(thread_id).size()-1) == 0) {
            File f = new File(this.count_time_files.get(thread_id)+chat_file+".json");
            if(f.delete()) {
                System.out.println(f + " was deleted");
                counts_per_file.get(thread_id).remove(counts_per_file.get(thread_id).size()-1);
            } else
                this.SET_COUNTS_LOGGER.get(thread_id).info(String.format("File %s failed to delete", f));
        }
        this.total_counts.set(thread_id, this.counts_per_file.get(thread_id).stream().mapToInt(Integer::intValue).sum());
        this.last_num_update.set(thread_id, this.total_counts.get(thread_id));
        this.last_number.set(thread_id, this.findLastCount(thread_id));
        this.updateFileData(thread_id);
    }
    private void removeCountsFromList(int thread_id, ArrayList<ArrayList<CountValue>> updates) {
        for(List<CountValue> counts:updates) {
            if(counts.size()==0) continue;
            List<CountValue>[] current_nums = this.readCountNumFile(thread_id, (counts.get(0).getValidCountNumber()-1)/MSGS_PER_FILE);
            for(CountValue count: counts) {
                List<CountValue> current_index = current_nums[(count.getValidCountNumber()-1)%MSGS_PER_FILE];
                for(int i=0;i<current_index.size();i++) {
                    if(current_index.get(i).getUUID().equals(count.getUUID())) {
                        System.out.println("removed "+count.getRawText());
                        current_index.remove(i);
                        break;
                    }
                }
            }
            this.serializeCountNumFile(current_nums, thread_id, (counts.get(0).getValidCountNumber()-1)/MSGS_PER_FILE);
        }
    }
    private void removeCountsTimeFromList(int thread_id, BadCountData to_remove) {
        ArrayList<ArrayList<CountValue>> updates = to_remove.updates();
        for(int i=0;i<to_remove.files().size();i++) {
            List<CountValue> current_nums = this.readCountTimeFile(thread_id, to_remove.files().get(i));
            int counter = current_nums.size()-1;
            for(int k=updates.get(i).size()-1;k>=0;k--) {
                for(int j=counter;j>=0;j--) {
                    if (current_nums.get(j).getUUID().equals(updates.get(i).get(k).getUUID())) {
                        this.EX_COUNTS_LOGGER.get(thread_id).info(String.format("Removed %s",updates.get(i).get(k)));
                        current_nums.remove(j);
                        counter--;
                        this.total_counts.set(thread_id, this.total_counts.get(thread_id)-1);
                        this.counts_per_file.get(thread_id).set(to_remove.files().get(i),this.counts_per_file.get(thread_id).get(to_remove.files().get(i))-1);
                        break;
                    }
                }
            }
            this.serializeCountTimeFile(current_nums, thread_id, to_remove.files().get(i));
        }
    }
    private Location getMaxCountLocationForSpecificThread(int thread_id) {
        if(this.total_counts.get(thread_id)==0) return new Location(0,0,0);
        return new Location(this.total_counts.get(thread_id)-this.counts_per_file.get(thread_id).get(this.counts_per_file.get(thread_id).size()-1),this.counts_per_file.get(thread_id).size()-1, this.counts_per_file.get(thread_id).get(this.counts_per_file.get(thread_id).size()-1));
    }
    public Location getMaxCountLocation() {
        int total_files = 0;
        int counts_by_file = 0;
        Location loc = null;
        for(int i=0;i<live_thread.size();i++) {
            loc = this.getMaxCountLocationForSpecificThread(i);
            total_files += this.counts_per_file.get(i).size();
            counts_by_file += this.total_counts.get(i);
        }
        assert loc != null;
        return new Location(counts_by_file-loc.getLocInFile(), total_files-1, loc.getLocInFile());
    }
    private Location getMaxUpdateLocationForSpecificThread(int thread_id) {
        return new Location(this.total_updates.get(thread_id)-this.updates_per_file.get(thread_id).get(this.updates_per_file.get(thread_id).size()-1),this.updates_per_file.get(thread_id).size()-1, this.updates_per_file.get(thread_id).get(this.updates_per_file.get(thread_id).size()-1));
    }
    public Location getMaxUpdateLocation(){
        int total_files = 0;
        int counts_by_file = 0;
        Location loc = null;
        for(int i=0;i<live_thread.size();i++) {
            loc = this.getMaxUpdateLocationForSpecificThread(i);
            total_files += this.updates_per_file.get(i).size();
            counts_by_file += this.total_updates.get(i);
        }
        assert loc != null;
        return new Location(counts_by_file-loc.getLocInFile(), total_files-1, loc.getLocInFile());
    }
    /**
     * Note: Untested since I implemented multipart thread support
     * @param thread_id Which (zero-indexed) partition of the thread you want to use. This is normally 0,
     *                  but it can be larger for multipart threads.
     * @param directory The location where you have the files you want rebalanced
     * @param files How many files you have to rebalance
     */
    public void rebalanceUpdateFiles(int thread_id, String directory, int files) throws IOException {
        TypeFactory tf = mapper.getTypeFactory();
        CollectionType collectionType = tf.constructCollectionType(ArrayList.class, UpdateValue.class);
        List<UpdateValue> unbalanced = new ArrayList<>();
        int counter=0;
        for(int i=0;i<files;i++) {
            unbalanced.addAll(0, mapper.readValue(new File(directory+"chat"+i+".json"), collectionType));
            while(unbalanced.size() >= MSGS_PER_FILE) {
                this.serializeUpdateFile(unbalanced.subList(unbalanced.size()-MSGS_PER_FILE, unbalanced.size()), thread_id, counter);
                unbalanced = new ArrayList<>(unbalanced.subList(0, unbalanced.size()-MSGS_PER_FILE));
                counter++;
            }
        }
        if(unbalanced.size() > 0)
            this.serializeUpdateFile(unbalanced, thread_id, counter);
    }
    public String getName() {
        return this.name.get(0).substring(0,this.name.get(0).length()-3);
    }
    public int getLast_number() {
        int max = 0;
        for(Integer i:this.last_number) {
            max = Math.max(max, i);
        }
        return max;
    }
    public int getTotal_counts() {
        return this.sumIntList(this.total_counts);
    }
    public int getTotal_updates() {
        return this.sumIntList(this.total_updates);
    }
    private int sumIntList(List<Integer> to_sum) {
        int sum = 0;
        for(Integer i:to_sum) {
            sum += i;
        }
        return sum;
    }
    public int getUpdateIdFromCountValue(CountValue num) {
        int thread = this.getThreadIDFromCountValue(num);
        int prior_updates = this.sumIntList(total_updates.subList(0,thread));
        return prior_updates+num.getUpdate_id();
    }

    public abstract String getNextGet();
    public abstract String getCount(int num);
    public abstract String[] link();
    //Exclusively for adding in large numbers of updates that were missing for some reason
    public int sortIndividualFile(int file) {
        List<UpdateValue> updates = this.getUpdatesByFile(file);
        List<UpdateValue> copy = this.getUpdatesByFile(file);
        Collections.sort(updates);
        if(updates.equals(copy)) return 0;
        this.serializeUpdateFile(updates, 0, file);
        return 1;
    }
    public int totalUpdateFiles() {
        int total = 0;
        for(ArrayList<Integer> files:this.updates_per_file) {
            total+=files.size();
        }
        return total;
    }
    public TimeUnit first_day(ParticipationDuration mode, String timezone) {
        return new TimeUnit(new UUID(first_and_last_updates.get(0).get(0)[1]), timezone, mode);
    }
    public TimeUnit last_day(ParticipationDuration mode, String timezone) {
        int thread_total = this.live_thread.size();
        int last_updates_size = this.first_and_last_updates.get(thread_total-1).size();
        return new TimeUnit(new UUID(first_and_last_updates.get(thread_total-1).get(last_updates_size-1)[0]), timezone, mode).addTime(1);
    }
}
