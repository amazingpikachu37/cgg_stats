package Stats;

public class UUID {
    private final long timestamp;
    private final int random;
    public UUID(long timestamp, int random) {
        this.timestamp  = timestamp;
        this.random = random;
    }
    public UUID(String uuid) {
        if(uuid.equals("[SYSTEM]")) uuid="00000000-0000-4000-a000-000000000000";
        String parsed_uuid = uuid.replace("-","");
        char random_char = switch(parsed_uuid.charAt(16)) {
            case '8' -> '1';
            case '9' -> '2';
            case 'a' -> '3';
            default -> '4'; //the 'b' case, which shouldn't exist due to a bug in the implementation
        };
        this.random = Integer.parseInt(random_char+parsed_uuid.substring(21,26));
        long ts = Long.parseUnsignedLong(parsed_uuid.substring(0,12)+parsed_uuid.substring(13,16)+parsed_uuid.substring(17,21));
        if(ts < 0) this.timestamp = Long.MAX_VALUE; //overflow
        else this.timestamp = ts;
    }
    public static UUID minUUID() {
        return new UUID("00000000-0000-4000-a000-000000000000");
    }
    public static UUID maxUUID() {
        return new UUID("92233720-3685-4477-a580-799999999999");
    }
    public String toString() {
        if(random==0) return "";
        StringBuilder sb = new StringBuilder(36);
        StringBuilder ts = new StringBuilder(String.valueOf(timestamp));
        while(ts.length() < 19) {
            ts.insert(0, "0");
        }
        String rd = String.valueOf(random);
        char random_char = switch(rd.charAt(0)) {
            case '1' -> '8';
            case '2' -> '9';
            case '3' -> 'a';
            default -> 'b'; //the 'b' case, which shouldn't exist due to a bug in the implementation
        };
        sb.append(ts).append(rd,1,6).append(sb, 12, 18);
        sb.insert(18,'-').insert(15,"-"+random_char).insert(12,"-4").insert(8,'-');
        return sb.toString();
    }
    public long getTime() {
        return this.timestamp;
    }
    public int getRandom() {
        return this.random;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof UUID p)) return false;
        return this.timestamp == p.timestamp && this.random == p.random;
    }

    @Override
    public int hashCode() {
        return (int)(timestamp * 31 + random);
    }
}
