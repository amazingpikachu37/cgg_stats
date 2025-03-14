package Threads;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UpdateValue implements Comparable<UpdateValue> {
    private long id;
    private UUID uuid;
    private String timestamp;
    private String timeSinceLastCount;
    private String timeSinceLastPost;
    private String rawText;
    private boolean isCount;
    private boolean isValidCount;
    private Integer validCountNumber;
    private String countContent;
    private String rawCount;
    private boolean stricken;
    private UUID thread;
    private boolean hasComment;
    private String comment;
    private UUID authorUUID;
    private boolean isDeleted;
    private boolean isCommentDeleted;
    private Map<String, List<UUID>> reactions;
    public long getId() {
        return this.id;
    }
    public UUID getUUID() {
        return this.uuid;
    }
    public boolean isStricken() {
        return this.stricken;
    }
    public void setStricken(boolean stricken) {
        this.stricken = stricken;
    }
    public UUID getAuthorUUID() {
        return this.authorUUID;
    }
    public String getAuthor() {
            return Counters.getCounters().get(this.authorUUID);
    }
    public String getRawText() {
        return this.rawText;
    }
    public String getRawCount() {
        return this.rawCount;
    }
    public Integer getValidCountNumber() {
        return this.validCountNumber;
    }
    public void setValidCountNumber(int validCountNumber) {
        this.validCountNumber = validCountNumber;
    }
    public boolean isIsValidCount() {
        return this.isValidCount;
    }
    public String getTimestamp() {
        return this.timestamp;
    }
    public String getTimeSinceLastCount() {
        return this.timeSinceLastCount;
    }
    public String getTimeSinceLastPost() {
        return this.timeSinceLastPost;
    }
    public boolean isIsCount() {
        return this.isCount;
    }

    public boolean isIsCommentDeleted() {
        return this.isCommentDeleted;
    }
    public boolean isIsDeleted() {
        return this.isDeleted;
    }
    public Map<String, List<UUID>> getReactions() {
        return this.reactions;
    }
    public boolean isHasComment() {
        return this.hasComment;
    }
    public String getComment() {
        return this.comment;
    }
    public String getCountContent() {
        return this.countContent;
    }
    public UUID getThread() {
        return this.thread;
    }

    public UpdateValue() {}
    public UpdateValue(long id, UUID uuid, String timestamp, String timeSinceLastCount, String timeSinceLastPost, String rawText, boolean isCount, boolean isValidCount, Integer validCountNumber, String countContent, String rawCount, boolean stricken, UUID thread, boolean hasComment, String comment, UUID authorUUID, boolean isDeleted, boolean isCommentDeleted, Map<String, List<UUID>> reactions) {
        this.id = id;
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.timeSinceLastCount = timeSinceLastCount;
        this.timeSinceLastPost = timeSinceLastPost;
        this.rawText = rawText;
        this.isCount = isCount;
        this.isValidCount = isValidCount;
        this.validCountNumber = validCountNumber;
        this.countContent = countContent;
        this.rawCount = rawCount;
        this.stricken = stricken;
        this.thread = thread;
        this.hasComment = hasComment;
        this.comment = comment;
        this.authorUUID = authorUUID;
        this.isDeleted = isDeleted;
        this.isCommentDeleted = isCommentDeleted;
        this.reactions = reactions;
    }
    /**
     * @param loc The location in the ByteBuffer to read an UpdateValue from. This uses an IntWrapper as opposed to an int so that the caller will have the next starting location.
     * */
    public static UpdateValue fromBytes(ByteBuffer data, IntWrapper loc) {
        byte bools = data.get(loc.getValue());
        loc.plus(1);
        long id = data.getLong(loc.getValue());
        loc.plus(8);
        long s1 = data.getLong(loc.getValue());
        loc.plus(8);
        long s2 = data.getLong(loc.getValue());
        loc.plus(8);
        long time_1 = data.getLong(loc.getValue());
        loc.plus(8);
        UUID uuid = new UUID(s1,s2,time_1);
        int timestamp_size = data.getInt(loc.getValue());
        loc.plus(4);
        String timestamp = null;
        if(timestamp_size!=-1) {
            timestamp = new String(data.array(), loc.getValue(), timestamp_size, StandardCharsets.UTF_8);
            loc.plus(timestamp_size);
        }
        int time_since_last_count_size = data.getInt(loc.getValue());
        loc.plus(4);
        String time_since_last_count = null;
        if(time_since_last_count_size!=-1) {
            time_since_last_count = new String(data.array(), loc.getValue(), time_since_last_count_size, StandardCharsets.UTF_8);
            loc.plus(time_since_last_count_size);
        }
        int time_since_last_post_size = data.getInt(loc.getValue());
        loc.plus(4);
        String time_since_last_post = null;
        if(time_since_last_post_size!=-1) {
            time_since_last_post = new String(data.array(), loc.getValue(), time_since_last_post_size, StandardCharsets.UTF_8);
            loc.plus(time_since_last_post_size);
        }
        int raw_text_size = data.getInt(loc.getValue());
        loc.plus(4);
        String raw_text = null;
        if(raw_text_size!=-1) {
            raw_text = new String(data.array(), loc.getValue(), raw_text_size, StandardCharsets.UTF_8);
            loc.plus(raw_text_size);
        }
        Integer valid_count_number = data.getInt(loc.getValue());
        if(valid_count_number==-1) valid_count_number = null;
        loc.plus(4);
        int count_content_size = data.getInt(loc.getValue());
        loc.plus(4);
        String count_content = null;
        if(count_content_size!=-1) {
            count_content = new String(data.array(), loc.getValue(), count_content_size, StandardCharsets.UTF_8);
            loc.plus(count_content_size);
        }
        int raw_count_size = data.getInt(loc.getValue());
        loc.plus(4);
        String raw_count = null;
        if(raw_count_size!=-1) {
            raw_count = new String(data.array(), loc.getValue(), raw_count_size, StandardCharsets.UTF_8);
            loc.plus(raw_count_size);
        }
        long t1 = data.getLong(loc.getValue());
        loc.plus(8);
        long t2 = data.getLong(loc.getValue());
        loc.plus(8);
        long time_2 = data.getLong(loc.getValue());
        loc.plus(8);
        UUID thread = new UUID(t1,t2,time_2);
        int comment_size = data.getInt(loc.getValue());
        loc.plus(4);
        String comment = null;
        if(comment_size!=-1) {
            comment = new String(data.array(), loc.getValue(), comment_size, StandardCharsets.UTF_8);
            loc.plus(comment_size);
        }
        long a1 = data.getLong(loc.getValue());
        loc.plus(8);
        long a2 = data.getLong(loc.getValue());
        loc.plus(8);
        long time_3 = data.getLong(loc.getValue());
        loc.plus(8);
        UUID author_uuid = new UUID(a1,a2,time_3);
        int reactions_size = data.getInt(loc.getValue());
        loc.plus(4);
        Map<String, List<UUID>> reactions = new HashMap<>(2*reactions_size);
        for(int k=0;k<reactions_size;k++) {
            int array_size = data.getInt(loc.getValue());
            loc.plus(4);
            int reaction_key_size = data.getInt(loc.getValue());
            loc.plus(4);
            String reaction_key = new String(data.array(), loc.getValue(), reaction_key_size, StandardCharsets.UTF_8);
            loc.plus(reaction_key_size);
            List<UUID> values = new ArrayList<>(array_size);
            for(int l=0;l<array_size;l++) {
                long r1 = data.getLong(loc.getValue());
                loc.plus(8);
                long r2 = data.getLong(loc.getValue());
                loc.plus(8);
                long time_4 = data.getLong(loc.getValue());
                loc.plus(8);
                values.add(new UUID(r1, r2, time_4));
            }
            reactions.put(reaction_key, values);
        }
        return new UpdateValue(id, uuid, timestamp, time_since_last_count, time_since_last_post, raw_text, (bools&0b1)!=0, (bools&0b10)!=0, valid_count_number, count_content, raw_count, (bools&0b100)!=0, thread, (bools&0b1000)!=0, comment, author_uuid, (bools&0b10000)!=0, (bools&0b100000)!=0, reactions);
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
        if(this.isCount) bools |= 0b1;
        if(this.isValidCount) bools |= 0b10;
        if(this.stricken) bools |= 0b100;
        if(this.hasComment) bools |= 0b1000;
        if(this.isDeleted) bools |= 0b10000;
        if(this.isCommentDeleted) bools |= 0b100000;
        int size = timestamp.length + timeSinceLastCount.length + timeSinceLastPost.length + rawText.length + countContent.length + rawCount.length + comment.length;
        size += 117; //8 ints (7 string, 1 map), 1 byte, 1 Integer (non-negative unless null), 10 longs
        for(String reaction: this.reactions.keySet()) {
            size += 8; //2 ints (array length, string length)
            byte[] reaction_bytes = reaction.getBytes(StandardCharsets.UTF_8);
            size += reaction_bytes.length;
            size += 24 * this.reactions.get(reaction).size(); //3 longs per reaction
            reactions.put(reaction, reaction_bytes);
        }
        ByteBuffer bytes = ByteBuffer.allocate(size);
        bytes.put(bools);
        bytes.putLong(id);
        bytes.putLong(uuid.getS1()).putLong(uuid.getS2()).putLong(uuid.getTime());
        if(this.timestamp!=null) bytes.putInt(timestamp.length).put(timestamp);
        else bytes.putInt(-1);
        if(this.timeSinceLastCount!=null) bytes.putInt(timeSinceLastCount.length).put(timeSinceLastCount);
        else bytes.putInt(-1);
        if(this.timeSinceLastPost!=null) bytes.putInt(timeSinceLastPost.length).put(timeSinceLastPost);
        else bytes.putInt(-1);
        if(this.rawText!=null) bytes.putInt(rawText.length).put(rawText);
        else bytes.putInt(-1);
        if(this.validCountNumber!=null) bytes.putInt(this.validCountNumber);
        else bytes.putInt(-1);
        if(this.countContent!=null) bytes.putInt(countContent.length).put(countContent);
        else bytes.putInt(-1);
        if(this.rawCount!=null) bytes.putInt(rawCount.length).put(rawCount);
        else bytes.putInt(-1);
        bytes.putLong(thread.getS1()).putLong(thread.getS2()).putLong(thread.getTime());
        if(this.comment!=null) bytes.putInt(comment.length).put(comment);
        else bytes.putInt(-1);
        bytes.putLong(authorUUID.getS1()).putLong(authorUUID.getS2()).putLong(authorUUID.getTime());
        bytes.putInt(reactions.size());
        for(String reaction:this.reactions.keySet()) {
            bytes.putInt(this.reactions.get(reaction).size());
            bytes.putInt(reactions.get(reaction).length).put(reactions.get(reaction));
            for(UUID user:this.reactions.get(reaction)) {
                bytes.putLong(user.getS1()).putLong(user.getS2()).putLong(user.getTime());
            }
        }
        return bytes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":").append(this.id).append(",");
        sb.append("\"uuid\":\"").append(Util.escapeJava(this.uuid.toString())).append("\",");
        sb.append("\"timestamp\":\"").append(Util.escapeJava(this.timestamp)).append("\",");
        if(this.timeSinceLastPost == null)
            sb.append("\"timeSinceLastCount\":null,");
        else
            sb.append("\"timeSinceLastCount\":\"").append(Util.escapeJava(this.timeSinceLastCount)).append("\",");
        if(this.timeSinceLastPost == null)
            sb.append("\"timeSinceLastPost\":null,");
        else
            sb.append("\"timeSinceLastPost\":\"").append(Util.escapeJava(this.timeSinceLastPost)).append("\",");
        sb.append("\"rawText\":\"").append(Util.escapeJava(this.rawText)).append("\",");
        sb.append("\"isCount\":").append(this.isCount).append(",");
        sb.append("\"isValidCount\":").append(this.isValidCount).append(",");
        sb.append("\"validCountNumber\":").append(this.validCountNumber).append(",");
        if(this.countContent == null)
            sb.append("\"countContent\":null,");
        else
            sb.append("\"countContent\":\"").append(Util.escapeJava(this.countContent)).append("\",");
        if(this.rawCount == null)
            sb.append("\"rawCount\":null,");
        else
            sb.append("\"rawCount\":\"").append(Util.escapeJava(this.rawCount)).append("\",");
        sb.append("\"stricken\":" ).append(this.stricken).append(",");
        sb.append("\"thread\":\"").append(Util.escapeJava(this.thread.toString())).append("\",");
        sb.append("\"hasComment\":").append(this.hasComment).append(",");
        if(this.comment == null)
            sb.append("\"comment\":null,");
        else
            sb.append("\"comment\":\"").append(Util.escapeJava(this.comment)).append("\",");
        sb.append("\"authorUUID\":\"").append(Util.escapeJava(this.authorUUID.toString())).append("\",");
        sb.append("\"isDeleted\":").append(this.isDeleted).append(",");
        sb.append("\"isCommentDeleted\":").append(this.isCommentDeleted).append(",");
        sb.append("\"reactions\":{");
        for(String reaction:reactions.keySet()) {
            sb.append("\"").append(reaction).append("\":[\"").append(reactions.get(reaction).get(0)).append("\"");
            for(int i=1;i<reactions.get(reaction).size();i++) {
                sb.append(",\"").append(reactions.get(reaction).get(i)).append("\"");
            }
            sb.append("],");
        }
        if(sb.charAt(sb.length()-1)==',') {
            sb.deleteCharAt(sb.length()-1);
        }
        sb.append("}}");
        return sb.toString();
    }

    @Override
    public int compareTo(UpdateValue o) {
        return Long.signum(o.uuid.getTime()-this.uuid.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof UpdateValue)) return false;
        return ((UpdateValue) o).id == this.id;
    }

    public boolean strongerEquals(UpdateValue o) {
        if(this.id!=o.id) return false;
        if(!this.uuid.equals(o.uuid)) return false;
        if(!Objects.equals(this.timestamp, o.timestamp)) return false;
        if(!Objects.equals(this.timeSinceLastCount, o.timeSinceLastCount)) return false;
        if(!Objects.equals(this.timeSinceLastPost, o.timeSinceLastPost)) return false;
        if(!Objects.equals(this.rawText, o.rawText)) return false;
        if(this.isCount!=o.isCount) return false;
        if(this.isValidCount!=o.isValidCount) return false;
        if(!Objects.equals(this.validCountNumber, o.validCountNumber)) return false;
        if(!Objects.equals(this.countContent, o.countContent)) return false;
        if(!Objects.equals(this.rawCount, o.rawCount)) return false;
        if(this.stricken!=o.stricken) return false;
        if(!Objects.equals(this.thread, o.thread)) return false;
        if(this.hasComment!=o.hasComment) return false;
        if(!Objects.equals(this.comment, o.comment)) return false;
        if(!Objects.equals(this.authorUUID, o.authorUUID)) return false;
        if(this.isDeleted!=o.isDeleted) return false;
        if(this.isCommentDeleted!=o.isCommentDeleted) return false;
        return Objects.equals(this.reactions, o.reactions);
    }
}
