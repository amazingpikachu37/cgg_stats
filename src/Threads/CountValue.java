package Threads;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@JsonIgnoreProperties({"id","timestamp","timeSinceLastCount", "timeSinceLastPost","isCount","isValidCount","countContent","thread","hasComment","comment","isDeleted","isCommentDeleted","reactions"})
public class CountValue implements Comparable<CountValue> {
    private static final ObjectMapper mapper = new ObjectMapper();
    private UUID uuid;
    private String rawText;
    private int validCountNumber;
    private UUID authorUUID;
    private String rawCount;
    private boolean stricken;
    private int update_id;
    public CountValue() {}
    public CountValue(String uuid, String rawText, int validCountNumber, String authorUUID, String rawCount, boolean stricken, int update_id) {
        this.uuid = new UUID(uuid);
        this.rawText = rawText;
        this.validCountNumber = validCountNumber;
        this.authorUUID = new UUID(authorUUID);
        this.rawCount = rawCount;
        this.stricken = stricken;
        this.update_id = update_id;
    }
    public CountValue(UUID uuid, String rawText, int validCountNumber, UUID authorUUID, String rawCount, boolean stricken, int update_id) {
        this.uuid = uuid;
        this.rawText = rawText;
        this.validCountNumber = validCountNumber;
        this.authorUUID = authorUUID;
        this.rawCount = rawCount;
        this.stricken = stricken;
        this.update_id = update_id;
    }
    public String getRawText() {
        return rawText;
    }
    public UUID getAuthorUUID() {
        return authorUUID;
    }
    public String getAuthor() {
        String author = Counters.getCounters().get(this.authorUUID);
        if(author == null) return "[deleted]";
        return author;
    }
    public boolean isStricken() {
        return stricken;
    }
    public UUID getUUID() {
        return this.uuid;
    }
    public String getRawCount() {
        return this.rawCount;
    }
    public int getUpdate_id() {
        return this.update_id;
    }
    public int getValidCountNumber() {
        return this.validCountNumber;
    }
    public void setValidCountNumber(int number) {
        this.validCountNumber = number;
    }
    public ByteBuffer toBytes() {
        byte[] rawText = this.rawText.getBytes(StandardCharsets.UTF_8);
        byte[] rawCount = this.rawCount.getBytes(StandardCharsets.UTF_8);
        byte stricken = 0;
        if(this.stricken) stricken = 1;
        ByteBuffer bytes = ByteBuffer.allocate(rawText.length+rawCount.length+65); // = 24+4+rawText.length+4+24+4+rawCount.length+1+4
        bytes.putLong(uuid.getS1());
        bytes.putLong(uuid.getS2());
        bytes.putLong(uuid.getTime());
        bytes.putInt(rawText.length).put(rawText);
        bytes.putInt(this.validCountNumber);
        bytes.putLong(authorUUID.getS1());
        bytes.putLong(authorUUID.getS2());
        bytes.putLong(authorUUID.getTime());
        bytes.putInt(rawCount.length).put(rawCount);
        bytes.put(stricken);
        bytes.putInt(this.update_id);
        return bytes;
    }
    /**
     * @param loc The location in the ByteBuffer to read a CountValue from. This uses an IntWrapper as opposed to an int so that the caller will have the next starting location.
     * */
    public static CountValue fromBytes(ByteBuffer data, IntWrapper loc) {
        long s1 = data.getLong(loc.getValue());
        loc.plus(8);
        long s2 = data.getLong(loc.getValue());
        loc.plus(8);
        long time_1 = data.getLong(loc.getValue());
        loc.plus(8);
        UUID uuid = new UUID(s1,s2,time_1);
        int body_size = data.getInt(loc.getValue());
        loc.plus(4);
        String raw_text = new String(data.array(), loc.getValue(), body_size, StandardCharsets.UTF_8);
        loc.plus(body_size);
        int number = data.getInt(loc.getValue());
        loc.plus(4);
        long a1 = data.getLong(loc.getValue());
        loc.plus(8);
        long a2 = data.getLong(loc.getValue());
        loc.plus(8);
        long time_2 = data.getLong(loc.getValue());
        loc.plus(8);
        UUID author_uuid = new UUID(a1,a2,time_2);
        int raw_count_size = data.getInt(loc.getValue());
        loc.plus(4);
        String raw_count = new String(data.array(), loc.getValue(), raw_count_size, StandardCharsets.UTF_8);
        loc.plus(raw_count_size);
        boolean stricken = data.get(loc.getValue()) == 1;
        loc.plus(1);
        int update_id = data.getInt(loc.getValue());
        loc.plus(4);
        return new CountValue(uuid, raw_text, number, author_uuid, raw_count, stricken, update_id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"uuid\":\"").append(Util.escapeJava(this.uuid.toString())).append("\",");
        sb.append("\"rawText\":\"").append(Util.escapeJava(this.rawText)).append("\",");
        sb.append("\"validCountNumber\":").append(validCountNumber).append(",");
        if(this.authorUUID == null)
            sb.append("\"authorUUID\":null,");
        else
            sb.append("\"authorUUID\":\"").append(Util.escapeJava(this.authorUUID.toString())).append("\",");
        sb.append("\"rawCount\":\"").append(Util.escapeJava(this.rawCount)).append("\",");
        sb.append("\"stricken\":").append(this.stricken).append(",");
        sb.append("\"update_id\":").append(update_id).append("}");
        return sb.toString();
    }
    public static CountValue parseCountValue(String s) {
        try {
            return mapper.readValue(s, CountValue.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int compareTo(CountValue o) {
        return this.validCountNumber-o.validCountNumber;
    }
}
