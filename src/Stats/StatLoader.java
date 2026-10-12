package Stats;

import org.apache.fury.Fury;
import org.apache.fury.config.Language;
import org.apache.fury.logging.LoggerFactory;
import org.apache.fury.memory.MemoryBuffer;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class StatLoader {
    private final static Fury fury;
    static {
        LoggerFactory.disableLogging();
        fury = Fury.builder()
                .withLanguage(Language.JAVA)
                .requireClassRegistration(true)
                .build();
        fury.register(UUID.class);
        fury.register(CounterData.class);
        fury.register(ThreadData.class);
        fury.register(CountValue.class);
        fury.register(TimeOutput.class);
        fury.register(TimeUnit.class);
        fury.register(ParticipationDuration.class);
        fury.register(DayHOC.class);
        fury.register(SplitHOC.class);
        fury.register(CountValue.class);
        fury.register(UpdateValue.class);
        fury.register(UpdateOrCountValue.class);
        fury.register(UpdateOrCountValueWithComment.class);
        fury.register(UpdateBody.class);
        fury.register(TransitionMatrixEntry.class);
        fury.register(StatContainer.class);
        fury.register(StatDataType.class);
        fury.register(SaveType.class);
        fury.register(StatContainer.StatSaveMode.class);
        fury.register(BalancedBinarySearchTree.class);
        fury.register(BalancedBinarySearchTree.TreeNode.class);
        fury.register(Split.class);
    }

    public static <T> void saveToBytes(T data, String filepath) {
        File f = new File(filepath);
        try(FileOutputStream stream = new FileOutputStream(f)) {
            stream.write(fury.serialize(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    public static <T> T loadFromBytes(String filepath) {
        File f = new File(filepath);
        try {
            byte[] byte_data = Files.readAllBytes(f.toPath());
            ByteBuffer wrapper = ByteBuffer.wrap(byte_data);
            return (T) fury.deserialize(MemoryBuffer.fromByteBuffer(wrapper));
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
    public enum SaveType {UUID, COUNT}
    public static <T> void saveStatToBytes(CountingThread thread, T stat_data, String filename, UpdateOrCountValue last_count, boolean overwrite, SaveType save_as) {
        File f = new File("SidethreadData/" + thread.thread.name +"/stats_data/"+filename);
        if(f.exists() && !overwrite || last_count==null) return;
        try(FileOutputStream stream = new FileOutputStream(f)) {
            ByteBuffer data = ByteBuffer.allocate(8);
            data.putLong(last_count.getTimestamp());
            stream.write(data.array());
            stream.write(fury.serialize(save_as));
            stream.write(fury.serialize(stat_data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> T loadStatFromBytes(CountingThread thread, String filename, CountingIterator updates, CountDataType mode, boolean complete) {
        File f = new File("SidethreadData/" + thread.thread.name +"/stats_data/"+filename);
        try {
            byte[] byte_data = Files.readAllBytes(f.toPath());
            ByteBuffer wrapper = ByteBuffer.wrap(byte_data);
            long timestamp = wrapper.getLong();
            if(timestamp >= thread.getLastUpdateTimestamp(mode)) complete = true;
            MemoryBuffer buffer = MemoryBuffer.fromByteBuffer(wrapper);
            SaveType save_as = (SaveType) fury.deserialize(buffer);
            T output = (T) fury.deserialize(buffer);
            switch(save_as) {
                case UUID -> {
                    if(!complete) {
                        updates.changeStartUUID(new UUID(timestamp + 1, 0));
                    } else {
                        updates.changeStartUUID(UUID.maxUUID());
                    }
                }
                case COUNT -> {
                    if(!complete) {
                        Location loc = thread.getLocationOfFirstCountOrUpdateAfterTimestamp(new UUID(timestamp, 0), mode, true);
                        updates.changeStartUpdate(loc.counts_by_file() + loc.loc_in_file() + 1);
                    } else {
                        updates.changeStartUpdate(Integer.MAX_VALUE);
                    }
                }
            }
            return output;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    public record DayHOC(TimeUnit date, StatContainer hoc) {
        @Nonnull
        public String toString() {
            return date+"="+hoc.toString();
        }
    }
    public record SplitHOC(int k, int split, StatContainer hoc) {
        @Nonnull
        public String toString() {
            return "{"+k+","+split+","+hoc.toString()+"}";
        }
    }

    /**
     *
     * @param k the nth k as determined by CountValue.true_number;
     * @param split 0 if using this for ks, otherwise the index of the split in the k (0 -> thread.splitsPerGet-1)
     * @param k_index always 0 for threads that don't reset. Represents the nth time repeating the same k.
     * @param start_count the start count is the end count of the previous k of the same k_index if it exists, otherwise has start_count.true_number==1.
     * @param end_count null if the split/k is unfinished
     */
    public record Split(int k, int split, int k_index, CountValue start_count, CountValue end_count, boolean complete) {
        @Nonnull
        public String toString() {
            return "{"+k+","+split+","+k_index+","+start_count+","+end_count+","+complete+"}";
        }
    }
    /**
     *
     * @param k the nth k as determined by CountValue.true_number;
     * @param split 0 if using this for ks, otherwise the index of the split in the k (0 -> thread.splitsPerGet-1)
     * @param k_index always 0 for threads that don't reset. Represents the nth time repeating the same k.
     * @param start_count the start count is the end count of the previous k of the same k_index if it exists, otherwise has start_count.true_number==1.
     * @param end_count null if the split/k is unfinished
     */
    public record SplitWithHOC(int k, int split, int k_index, CountValue start_count, CountValue end_count, StatContainer hoc) {
        @Nonnull
        public String toString() {
            return "{"+k+","+split+","+k_index+","+start_count+","+end_count+","+hoc.toString()+"}";
        }
        public long time() {
            return end_count.timestamp()-start_count.timestamp();
        }
    }

    public record TransitionMatrixEntry(float chance, String output){
        @Nonnull
        public String toString() {
            return chance+" "+output;
        }
    }
}
