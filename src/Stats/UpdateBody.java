package Stats;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record UpdateBody(String rawCount, String comment) implements Serializable {
    public ByteBuffer toBytes() {
        byte[] rawCount = this.rawCount == null ? new byte[0] : this.rawCount.getBytes(StandardCharsets.UTF_8);
        byte[] comment = this.comment == null ? new byte[0] : this.comment.getBytes(StandardCharsets.UTF_8);
        ByteBuffer bytes = ByteBuffer.allocate(rawCount.length + comment.length + 8);
        if (this.rawCount != null) bytes.putInt(rawCount.length).put(rawCount);
        else bytes.putInt(-1);
        if (this.comment != null) bytes.putInt(comment.length).put(comment);
        else bytes.putInt(-1);
        return bytes;
    }

    public static UpdateBody fromBytes(ByteBuffer data, IntWrapper loc) {
        int raw_count_size = data.getInt(loc.getValue());
        loc.plus(4);
        String raw_count = null;
        if (raw_count_size != -1) {
            raw_count = new String(data.array(), loc.getValue(), raw_count_size, StandardCharsets.UTF_8);
            loc.plus(raw_count_size);
        }
        int comment_size = data.getInt(loc.getValue());
        loc.plus(4);
        String comment = null;
        if (comment_size != -1) {
            comment = new String(data.array(), loc.getValue(), comment_size, StandardCharsets.UTF_8);
            loc.plus(comment_size);
        }
        return new UpdateBody(raw_count, comment);
    }
}