package Stats;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public record FullUpdateValue(@JsonProperty("id") int id, @JsonProperty("uuid") UUID uuid, @JsonProperty("timestamp") String timestamp,
                              @JsonProperty("timeSinceLastCount") String timeSinceLastCount, @JsonProperty("timeSinceLastPost") String timeSinceLastPost,
                              @JsonProperty("rawText") String rawText, @JsonProperty("isCount") boolean isCount, @JsonProperty("isValidCount") boolean isValidCount,
                              @JsonProperty("validCountNumber") Integer validCountNumber, @JsonProperty("countContent") String countContent,
                              @JsonProperty("rawCount") String rawCount, @JsonProperty("stricken") boolean stricken, @JsonProperty("thread") UUID thread,
                              @JsonProperty("threadId") int threadId, @JsonProperty("hasComment") boolean hasComment,
                              @JsonProperty("hasThreeCharComment") boolean hasThreeCharComment, @JsonProperty("comment") String comment,
                              @JsonProperty("authorUUID") UUID authorUUID, @JsonProperty("authorId") Integer authorId, @JsonProperty("isDeleted") boolean isDeleted,
                              @JsonProperty("isCommentDeleted") boolean isCommentDeleted, @JsonProperty("reactions") Map<String, List<UUID>> reactions,
                              @JsonProperty("macroPresetId") Integer macroPresetId, @JsonProperty("macroPresetVersionId") Integer macroPresetVersionId,
                              @JsonProperty("phyTotal") Long phyTotal, @JsonProperty("phyMin") Long phyMin, @JsonProperty("phyMax") Long phyMax,
                              @JsonProperty("phyCount") Integer phyCount, @JsonProperty("phyWait") Long phyWait, @JsonProperty("phyAdded") Integer phyAdded,
                              @JsonProperty("phyDwell") Long phyDwell) implements UpdateOrCountValue, Serializable, Comparable<UpdateOrCountValue> {
    /**
     * @param loc The location in the ByteBuffer to read an UpdateValue from. This uses an IntWrapper as opposed to an int so that the caller will have the next starting location.
     *
     */
    public static FullUpdateValue fromBytes(ByteBuffer data, IntWrapper loc) {
        byte bools = data.get(loc.getValue());
        loc.plus(1);
        int id = data.getInt(loc.getValue());
        loc.plus(4);
        long time_1 = data.getLong(loc.getValue());
        loc.plus(8);
        int random_1 = data.getInt(loc.getValue());
        loc.plus(4);
        UUID uuid = new UUID(time_1, random_1);
        int timestamp_size = data.getInt(loc.getValue());
        loc.plus(4);
        String timestamp = null;
        if (timestamp_size != -1) {
            timestamp = new String(data.array(), loc.getValue(), timestamp_size, StandardCharsets.UTF_8);
            loc.plus(timestamp_size);
        }
        int time_since_last_count_size = data.getInt(loc.getValue());
        loc.plus(4);
        String time_since_last_count = null;
        if (time_since_last_count_size != -1) {
            time_since_last_count = new String(data.array(), loc.getValue(), time_since_last_count_size, StandardCharsets.UTF_8);
            loc.plus(time_since_last_count_size);
        }
        int time_since_last_post_size = data.getInt(loc.getValue());
        loc.plus(4);
        String time_since_last_post = null;
        if (time_since_last_post_size != -1) {
            time_since_last_post = new String(data.array(), loc.getValue(), time_since_last_post_size, StandardCharsets.UTF_8);
            loc.plus(time_since_last_post_size);
        }
        int raw_text_size = data.getInt(loc.getValue());
        loc.plus(4);
        String raw_text = null;
        if (raw_text_size != -1) {
            raw_text = new String(data.array(), loc.getValue(), raw_text_size, StandardCharsets.UTF_8);
            loc.plus(raw_text_size);
        }
        Integer valid_count_number = data.getInt(loc.getValue());
        if (valid_count_number == -1) valid_count_number = null;
        loc.plus(4);
        int count_content_size = data.getInt(loc.getValue());
        loc.plus(4);
        String count_content = null;
        if (count_content_size != -1) {
            count_content = new String(data.array(), loc.getValue(), count_content_size, StandardCharsets.UTF_8);
            loc.plus(count_content_size);
        }
        int raw_count_size = data.getInt(loc.getValue());
        loc.plus(4);
        String raw_count = null;
        if (raw_count_size != -1) {
            raw_count = new String(data.array(), loc.getValue(), raw_count_size, StandardCharsets.UTF_8);
            loc.plus(raw_count_size);
        }
        long time_2 = data.getLong(loc.getValue());
        loc.plus(8);
        int random_2 = data.getInt(loc.getValue());
        loc.plus(4);
        UUID thread = new UUID(time_2, random_2);
        int thread_id = data.getInt(loc.getValue());
        loc.plus(4);
        int comment_size = data.getInt(loc.getValue());
        loc.plus(4);
        String comment = null;
        if (comment_size != -1) {
            comment = new String(data.array(), loc.getValue(), comment_size, StandardCharsets.UTF_8);
            loc.plus(comment_size);
        }
        long time_3 = data.getLong(loc.getValue());
        loc.plus(8);
        int random_3 = data.getInt(loc.getValue());
        loc.plus(4);
        UUID author_uuid = new UUID(time_3, random_3);
        Integer author_id = data.getInt(loc.getValue());
        if (author_id == -1) author_id = null;
        loc.plus(4);
        int reactions_size = data.getInt(loc.getValue());
        loc.plus(4);
        Map<String, List<UUID>> reactions = new HashMap<>(2 * reactions_size);
        for (int k = 0; k < reactions_size; k++) {
            int array_size = data.getInt(loc.getValue());
            loc.plus(4);
            int reaction_key_size = data.getInt(loc.getValue());
            loc.plus(4);
            String reaction_key = new String(data.array(), loc.getValue(), reaction_key_size, StandardCharsets.UTF_8);
            loc.plus(reaction_key_size);
            List<UUID> values = new ArrayList<>(array_size);
            for (int l = 0; l < array_size; l++) {
                long time_4 = data.getLong(loc.getValue());
                loc.plus(8);
                int random_4 = data.getInt(loc.getValue());
                loc.plus(4);
                values.add(new UUID(time_4, random_4));
            }
            reactions.put(reaction_key, values);
        }
        Integer macroPresetId = data.getInt(loc.getValue());
        if (macroPresetId == -1) macroPresetId = null;
        loc.plus(4);
        Integer macroPresetVersionId = data.getInt(loc.getValue());
        if (macroPresetVersionId == -1) macroPresetVersionId = null;
        loc.plus(4);
        Long phyTotal = data.getLong(loc.getValue());
        if (phyTotal == -1) phyTotal = null;
        loc.plus(8);
        Long phyMin = data.getLong(loc.getValue());
        if (phyMin == -1) phyMin = null;
        loc.plus(8);
        Long phyMax = data.getLong(loc.getValue());
        if (phyMax == -1) phyMax = null;
        loc.plus(8);
        Integer phyCount = data.getInt(loc.getValue());
        if (phyCount == -1) phyCount = null;
        loc.plus(4);
        Long phyWait = data.getLong(loc.getValue());
        if (phyWait == -1) phyWait = null;
        loc.plus(8);
        Integer phyAdded = data.getInt(loc.getValue());
        if (phyAdded == -1) phyAdded = null;
        loc.plus(4);
        Long phyDwell = data.getLong(loc.getValue());
        if (phyDwell == -1) phyDwell = null;
        loc.plus(8);

        return new FullUpdateValue(id, uuid, timestamp, time_since_last_count, time_since_last_post, raw_text, (bools & 0b1) != 0, (bools & 0b10) != 0, valid_count_number, count_content, raw_count, (bools & 0b100) != 0, thread, thread_id, (bools & 0b1000) != 0, (bools & 0b1000000) != 0, comment, author_uuid, author_id, (bools & 0b10000) != 0, (bools & 0b100000) != 0, reactions, macroPresetId, macroPresetVersionId, phyTotal, phyMin, phyMax, phyCount, phyWait, phyAdded, phyDwell);
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }
    public long getTimestamp() {
        return uuid.getTime();
    }
    public int getAuthorId() {
        return this.authorId;
    }
    public int getThreadId() {
        return this.threadId;
    }
    public int getUpdateId() {
        return this.id;
    }

    public ByteBuffer toBytes() {
        byte[] timestamp = this.timestamp == null ? new byte[0] : this.timestamp.getBytes(StandardCharsets.UTF_8);
        byte[] timeSinceLastCount = this.timeSinceLastCount == null ? new byte[0] : this.timeSinceLastCount.getBytes(StandardCharsets.UTF_8);
        byte[] timeSinceLastPost = this.timeSinceLastPost == null ? new byte[0] : this.timeSinceLastPost.getBytes(StandardCharsets.UTF_8);
        byte[] rawText = this.rawText == null ? new byte[0] : this.rawText.getBytes(StandardCharsets.UTF_8);
        byte[] countContent = this.countContent == null ? new byte[0] : this.countContent.getBytes(StandardCharsets.UTF_8);
        byte[] rawCount = this.rawCount == null ? new byte[0] : this.rawCount.getBytes(StandardCharsets.UTF_8);
        byte[] comment = this.comment == null ? new byte[0] : this.comment.getBytes(StandardCharsets.UTF_8);
        Map<String, byte[]> reactions = new HashMap<>();
        byte bools = 0;
        if (this.isCount) bools |= 0b1;
        if (this.isValidCount) bools |= 0b10;
        if (this.stricken) bools |= 0b100;
        if (this.hasComment) bools |= 0b1000;
        if (this.isDeleted) bools |= 0b10000;
        if (this.isCommentDeleted) bools |= 0b100000;
        int size = timestamp.length + timeSinceLastCount.length + timeSinceLastPost.length + rawText.length + countContent.length + rawCount.length + comment.length;
        size += 141; //10 ints (7 string, 1 map, 5 other), 1 byte, 6 Integers (non-negative unless null), 3 longs, 5 Longs (non-negative unless null)
        for (String reaction : this.reactions.keySet()) {
            size += 8; //2 ints (array length, string length)
            byte[] reaction_bytes = reaction.getBytes(StandardCharsets.UTF_8);
            size += reaction_bytes.length;
            size += 12 * this.reactions.get(reaction).size(); //1 long, 1 int per reaction
            reactions.put(reaction, reaction_bytes);
        }
        ByteBuffer bytes = ByteBuffer.allocate(size);
        bytes.put(bools);
        bytes.putInt(id);
        bytes.putLong(uuid.getTime()).putInt(uuid.getRandom());
        if (this.timestamp != null) bytes.putInt(timestamp.length).put(timestamp);
        else bytes.putInt(-1);
        if (this.timeSinceLastCount != null) bytes.putInt(timeSinceLastCount.length).put(timeSinceLastCount);
        else bytes.putInt(-1);
        if (this.timeSinceLastPost != null) bytes.putInt(timeSinceLastPost.length).put(timeSinceLastPost);
        else bytes.putInt(-1);
        if (this.rawText != null) bytes.putInt(rawText.length).put(rawText);
        else bytes.putInt(-1);
        bytes.putInt(Objects.requireNonNullElse(this.validCountNumber, -1));
        if (this.countContent != null) bytes.putInt(countContent.length).put(countContent);
        else bytes.putInt(-1);
        if (this.rawCount != null) bytes.putInt(rawCount.length).put(rawCount);
        else bytes.putInt(-1);
        bytes.putLong(thread.getTime()).putInt(thread.getRandom());
        bytes.putInt(threadId);
        if (this.comment != null) bytes.putInt(comment.length).put(comment);
        else bytes.putInt(-1);
        bytes.putLong(authorUUID.getTime()).putInt(authorUUID.getRandom());
        bytes.putInt(this.authorId == null ? 0 : this.authorId);
        bytes.putInt(reactions.size());
        for (String reaction : this.reactions.keySet()) {
            bytes.putInt(this.reactions.get(reaction).size());
            bytes.putInt(reactions.get(reaction).length).put(reactions.get(reaction));
            for (UUID user : this.reactions.get(reaction)) {
                bytes.putLong(user.getTime()).putInt(user.getRandom());
            }
        }
        bytes.putInt(Objects.requireNonNullElse(this.macroPresetId, -1));
        bytes.putInt(Objects.requireNonNullElse(this.macroPresetVersionId, -1));
        bytes.putLong(Objects.requireNonNullElse(this.phyTotal, -1L));
        bytes.putLong(Objects.requireNonNullElse(this.phyMin, -1L));
        bytes.putLong(Objects.requireNonNullElse(this.phyMax, -1L));
        bytes.putInt(Objects.requireNonNullElse(this.phyCount, -1));
        bytes.putLong(Objects.requireNonNullElse(this.phyWait, -1L));
        bytes.putInt(Objects.requireNonNullElse(this.phyAdded, -1));
        bytes.putLong(Objects.requireNonNullElse(this.phyDwell, -1L));
        return bytes;
    }

    @Override
    @Nonnull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":").append(this.id).append(",");
        sb.append("\"uuid\":\"").append(Util.escapeJava(this.uuid.toString())).append("\",");
        sb.append("\"timestamp\":\"").append(Util.escapeJava(this.timestamp)).append("\",");
        if (this.timeSinceLastPost == null)
            sb.append("\"timeSinceLastCount\":null,");
        else
            sb.append("\"timeSinceLastCount\":\"").append(Util.escapeJava(this.timeSinceLastCount)).append("\",");
        if (this.timeSinceLastPost == null)
            sb.append("\"timeSinceLastPost\":null,");
        else
            sb.append("\"timeSinceLastPost\":\"").append(Util.escapeJava(this.timeSinceLastPost)).append("\",");
        sb.append("\"rawText\":\"").append(Util.escapeJava(this.rawText)).append("\",");
        sb.append("\"isCount\":").append(this.isCount).append(",");
        sb.append("\"isValidCount\":").append(this.isValidCount).append(",");
        sb.append("\"validCountNumber\":").append(this.validCountNumber).append(",");
        if (this.countContent == null)
            sb.append("\"countContent\":null,");
        else
            sb.append("\"countContent\":\"").append(Util.escapeJava(this.countContent)).append("\",");
        if (this.rawCount == null)
            sb.append("\"rawCount\":null,");
        else
            sb.append("\"rawCount\":\"").append(Util.escapeJava(this.rawCount)).append("\",");
        sb.append("\"stricken\":").append(this.stricken).append(",");
        sb.append("\"thread\":\"").append(Util.escapeJava(this.thread.toString())).append("\",");
        sb.append("\"threadId\":\"").append(this.threadId).append("\",");
        sb.append("\"hasComment\":").append(this.hasComment).append(",");
        sb.append("\"hasThreeCharComment\":").append(this.hasThreeCharComment).append(",");
        if (this.comment == null)
            sb.append("\"comment\":null,");
        else
            sb.append("\"comment\":\"").append(Util.escapeJava(this.comment)).append("\",");
        sb.append("\"authorUUID\":\"").append(Util.escapeJava(this.authorUUID.toString())).append("\",");
        sb.append("\"authorId\":\"").append(this.authorId).append("\",");
        sb.append("\"isDeleted\":").append(this.isDeleted).append(",");
        sb.append("\"isCommentDeleted\":").append(this.isCommentDeleted).append(",");
        sb.append("\"reactions\":{");
        for (String reaction : reactions.keySet()) {
            sb.append("\"").append(reaction).append("\":[\"").append(reactions.get(reaction).get(0)).append("\"");
            for (int i = 1; i < reactions.get(reaction).size(); i++) {
                sb.append(",\"").append(reactions.get(reaction).get(i)).append("\"");
            }
            sb.append("],");
        }
        sb.append("}}");
        sb.append("\"macroPresetId\":").append(this.macroPresetId).append(",");
        sb.append("\"macroPresetVersionId\":").append(this.macroPresetVersionId).append(",");
        sb.append("\"phyTotal\":").append(this.phyTotal).append(",");
        sb.append("\"phyMin\":").append(this.phyMin).append(",");
        sb.append("\"phyMax\":").append(this.phyMax).append(",");
        sb.append("\"phyCount\":").append(this.phyCount).append(",");
        sb.append("\"phyWait\":").append(this.phyWait).append(",");
        sb.append("\"phyAdded\":").append(this.phyAdded).append(",");
        sb.append("\"phyDwell\":").append(this.phyAdded).append("}");
        return sb.toString();
    }

    @Override
    public int compareTo(UpdateOrCountValue o) {
        return Long.signum(this.getTimestamp()-o.getTimestamp());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FullUpdateValue)) return false;
        return ((FullUpdateValue) o).id == this.id;
    }

    public boolean strongerEquals(FullUpdateValue o) {
        if (this.id != o.id) return false;
        if (!this.uuid.equals(o.uuid)) return false;
        if (!Objects.equals(this.timestamp, o.timestamp)) return false;
        if (!Objects.equals(this.timeSinceLastCount, o.timeSinceLastCount)) return false;
        if (!Objects.equals(this.timeSinceLastPost, o.timeSinceLastPost)) return false;
        if (!Objects.equals(this.rawText, o.rawText)) return false;
        if (this.isCount != o.isCount) return false;
        if (this.isValidCount != o.isValidCount) return false;
        if (!Objects.equals(this.validCountNumber, o.validCountNumber)) return false;
        if (!Objects.equals(this.countContent, o.countContent)) return false;
        if (!Objects.equals(this.rawCount, o.rawCount)) return false;
        if (this.stricken != o.stricken) return false;
        if (!Objects.equals(this.thread, o.thread)) return false;
        if (this.hasComment != o.hasComment) return false;
        if (!Objects.equals(this.comment, o.comment)) return false;
        if (!Objects.equals(this.authorUUID, o.authorUUID)) return false;
        if (this.isDeleted != o.isDeleted) return false;
        if (this.isCommentDeleted != o.isCommentDeleted) return false;
        return Objects.equals(this.reactions, o.reactions);
    }
}
