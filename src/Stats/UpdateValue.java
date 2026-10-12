package Stats;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @param update_id Global update id
 */
public record UpdateValue(UUID uuid, boolean is_count, boolean stricken, boolean comment, Integer number,
                          int author_id, int thread_id,
                          int update_id) implements UpdateOrCountValue, Serializable, Comparable<UpdateOrCountValue> {
    public static final int size = 29;
    public ByteBuffer toBytes() {
        byte bools = 0;
        if (this.is_count) bools |= 0b1;
        if (this.stricken) bools |= 0b10;
        if (this.comment) bools |= 0b100;
        ByteBuffer bytes = ByteBuffer.allocate(size); // = 1 + (8+4) + 4*4
        bytes.put(bools);
        bytes.putLong(this.uuid.getTime());
        bytes.putInt(this.uuid.getRandom());
        bytes.putInt(Objects.requireNonNullElse(this.number, -1));
        bytes.putInt(this.author_id);
        bytes.putInt(this.thread_id);
        bytes.putInt(this.update_id);
        return bytes;
    }

    /**
     * @param loc The location in the ByteBuffer to read a CountValue from. This uses an IntWrapper as opposed to an int so that the caller will have the next starting location.
     *
     */
    public static UpdateValue fromBytes(ByteBuffer data, IntWrapper loc) {
        byte bools = data.get(loc.getValue());
        loc.plus(1);
        long timestamp = data.getLong(loc.getValue());
        loc.plus(8);
        int random = data.getInt(loc.getValue());
        loc.plus(4);
        UUID uuid = new UUID(timestamp, random);
        Integer number = data.getInt(loc.getValue());
        if (number == -1) number = null;
        loc.plus(4);
        int author_id = data.getInt(loc.getValue());
        loc.plus(4);
        int thread_id = data.getInt(loc.getValue());
        loc.plus(4);
        int update_id = data.getInt(loc.getValue());
        loc.plus(4);
        return new UpdateValue(uuid, (bools & 0b1) != 0, (bools & 0b10) != 0, (bools & 0b100) != 0, number, author_id, thread_id, update_id);
    }

    @Override
    @Nonnull
    public String toString() {
        return "{\"uuid\":\"" + uuid.toString() + "\"," +
                "\"is_count\":\"" + is_count + "\"," +
                "\"stricken\":\"" + stricken + "\"," +
                "\"comment\":\"" + comment + "\"," +
                "\"number\":\"" + number + "\"," +
                "\"author_id\":" + author_id + "," +
                "\"thread_id\":\"" + thread_id + "\"," +
                "\"update_id\":" + update_id + "}";
    }

    @Override
    public int compareTo(UpdateOrCountValue o) {
        return Long.signum(this.getTimestamp() - o.getTimestamp());
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }
    public long getTimestamp() {
        return uuid.getTime();
    }

    @Override
    public Integer validCountNumber() {
        return number;
    }

    @Override
    public boolean isCount() {
        return is_count;
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
    public static UpdateValue getDefaultObject() {
        return new UpdateValue(new UUID(0,0),false,false,false,0,0,0,0);
    }
}
