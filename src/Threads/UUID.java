package Threads;

public class UUID {
    private final long s1;
    private final long s2;
    private final long timestamp;
    public UUID(long s1, long s2, long timestamp) {
        this.s1 = s1;
        this.s2 = s2;
        this.timestamp  = timestamp;
    }
    public UUID(String uuid) {
        if(uuid.equals("[SYSTEM]")) uuid="00000000-0000-4000-a000-000000000000";
        String parsed_uuid = uuid.replace("-","");
        this.s1 = Long.parseUnsignedLong(parsed_uuid.substring(0,16),16);
        this.s2 = Long.parseUnsignedLong(parsed_uuid.substring(16,32),16);
        this.timestamp = Long.parseLong(parsed_uuid.substring(0,12)+parsed_uuid.substring(13,16)+parsed_uuid.substring(17,19));
    }
    public String toString() {
        StringBuilder sb = new StringBuilder(36);
        sb.append(String.format("%016x",s1)).append(String.format("%016x",s2));
        sb.insert(20,'-').insert(16,'-').insert(12,'-').insert(8,'-');
        return sb.toString();
    }
    public String toDashlessString() {
        return Long.toUnsignedString(s1, 16) + Long.toUnsignedString(s2, 16);
    }
    public long getTime() {
        return this.timestamp;
    }
    public long getS1() {
        return s1;
    }
    public long getS2() {
        return s2;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof UUID p)) return false;
        return this.s1 == p.s1 && this.s2 == p.s2;
    }

    @Override
    public int hashCode() {
        return (int)(s1 * 31 + s2);
    }
}
