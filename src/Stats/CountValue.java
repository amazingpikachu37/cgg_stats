package Stats;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

/**
 * @param update_id Specifically what update it is in the current thread
 */
public record CountValue(long timestamp, int number, int true_number, int author_id, int thread_id,
                         int update_id) implements UpdateOrCountValue, Serializable, Comparable<UpdateOrCountValue> {

    public static CountValue getDefaultObject() {
        return new CountValue(0,0,0, 0,0,0);
    }
    public static final int size = 28;
    public ByteBuffer toBytes() {
        ByteBuffer bytes = ByteBuffer.allocate(size); // = 12 + 4*4
        bytes.putLong(timestamp);
        bytes.putInt(this.number);
        bytes.putInt(this.true_number);
        bytes.putInt(this.author_id);
        bytes.putInt(this.thread_id);
        bytes.putInt(this.update_id);
        return bytes;
    }

    /**
     * @param loc The location in the ByteBuffer to read a CountValue from. This uses an IntWrapper as opposed to an int so that the caller will have the next starting location.
     *
     */
    public static CountValue fromBytes(ByteBuffer data, IntWrapper loc) {
        long timestamp = data.getLong(loc.getValue());
        loc.plus(8);
        int number = data.getInt(loc.getValue());
        loc.plus(4);
        int true_number = data.getInt(loc.getValue());
        loc.plus(4);
        int author_id = data.getInt(loc.getValue());
        loc.plus(4);
        int thread_id = data.getInt(loc.getValue());
        loc.plus(4);
        int update_id = data.getInt(loc.getValue());
        loc.plus(4);
        return new CountValue(timestamp, number, true_number, author_id, thread_id, update_id);
    }

    @Override
    @Nonnull
    public String toString() {
        return "{\"timestamp\":\"" + timestamp + "\"," +
                "\"number\":\"" + number + "\"," +
                "\"true_number\":\"" + true_number + "\"," +
                "\"author_id\":" + author_id + "," +
                "\"thread_id\":\"" + thread_id + "\"," +
                "\"update_id\":" + update_id + "}";
    }

    @Override
    public int compareTo(UpdateOrCountValue o) {
        return Long.signum(this.timestamp - o.getTimestamp());
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean stricken() {
        return false;
    }

    @Override
    public UUID getUUID() {
        return ((UpdateValue)AllThreads.all_threads_with_combined.get(thread_id).getUpdateOrCount(update_id, CountDataType.UPDATE)).uuid();
    }

    @Override
    public Integer validCountNumber() {
        return number;
    }

    @Override
    public boolean isCount() {
        return true;
    }
    public boolean isGet(int offset, int k_size) {
        ThreadData thread = AllThreads.all_threads_with_combined.get(this.thread_id).thread;
        return this.true_number > thread.splitOffset && this.true_number > offset && (this.true_number + offset) % (thread.countsPerSplit*thread.splitsPerGet*k_size) == thread.splitOffset;
    }
    public boolean isSplit(int offset, int k_size) {
        ThreadData thread = AllThreads.all_threads_with_combined.get(this.thread_id).thread;
        return this.true_number > thread.splitOffset && this.true_number > offset && (this.true_number + offset) % (thread.countsPerSplit) == thread.splitOffset;
    }
    public int getK(int k_size) {
        ThreadData thread = AllThreads.all_threads_with_combined.get(this.thread_id).thread;
        if(this.true_number <= thread.splitOffset) return 0;
        return (this.true_number - thread.splitOffset - 1) / (thread.countsPerSplit*thread.splitsPerGet*k_size);
    }

    /**
     * @return Returns a 2-element array with the k in slot 0, and the split of that k in slot 1.
     */
    public int[] getSplit(int k_size) {
        int[] output = new int[2];
        ThreadData thread = AllThreads.all_threads_with_combined.get(this.thread_id).thread;
        if(this.true_number <= thread.splitOffset) return output;
        else {
            int total_splits = (this.true_number - thread.splitOffset - 1) / (thread.countsPerSplit);
            output[0] = total_splits / (thread.splitsPerGet*k_size);
            output[1] = total_splits % (thread.splitsPerGet*k_size);
        }
        return output;
    }
    public int getAuthorId() {
        return this.author_id;
    }
    public int getThreadId() {
        return this.thread_id;
    }
    public int getUpdateId() {
        return this.update_id;
    }
}
