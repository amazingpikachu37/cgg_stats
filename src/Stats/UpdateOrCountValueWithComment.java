package Stats;

import java.nio.ByteBuffer;

public record UpdateOrCountValueWithComment(UpdateOrCountValue update,
                                            UpdateBody comment) implements UpdateOrCountValue {
    static UpdateOrCountValueWithComment getDefaultObjectWithMaxTimestamp(boolean use_counts) {
        return new UpdateOrCountValueWithComment(UpdateOrCountValue.getDefaultObjectWithMaxTimestamp(use_counts), new UpdateBody(null, null));
    }

    public String getRawCount() {
        return comment.rawCount();
    }

    public String getComment() {
        return comment.comment();
    }

    @Override
    public boolean stricken() {
        return update.stricken();
    }

    @Override
    public UUID getUUID() {
        return update.getUUID();
    }

    @Override
    public long getTimestamp() {
        return update.getTimestamp();
    }

    @Override
    public Integer validCountNumber() {
        return update.validCountNumber();
    }

    @Override
    public boolean isCount() {
        return update.isCount();
    }

    @Override
    public int getThreadId() {
        return update.getThreadId();
    }

    @Override
    public int getAuthorId() {
        return update.getAuthorId();
    }

    @Override
    public int getUpdateId() {
        return update.getUpdateId();
    }

    @Override
    public int compareTo(UpdateOrCountValue u) {
        return update.compareTo(u);
    }

    @Override
    public ByteBuffer toBytes() {
        return update.toBytes();
    }
}
