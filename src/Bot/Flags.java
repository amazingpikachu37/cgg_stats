package Bot;

import Threads.ParticipationDuration;
import Threads.TimeUnit;
import Threads.UUID;

public class Flags {
    public String username;
    public boolean username_modified = false;
    public int position = 1;
    public int context = 2;
    public UUID start_uuid = new UUID("00000000-0000-4000-a000-000000000000");
    public UUID end_uuid = new UUID("99999999-9999-4999-a999-999999999999");
    public String start_date = null;
    public String end_date = null;
    public boolean is_current = false;
    public int mode = 0;
    public int gets_per_k = 1;
    public int start_k = 0;
    public int end_k = Integer.MAX_VALUE;
    public int min_parts = 0;
    public int duration_seconds = 3600;
    public boolean thread_stats_combined = false;
    public float percentage = 0.98f;
    public int start_count = 0;
    public int end_count = Integer.MAX_VALUE;
    public int counts_between_replies = 1;
    public int cap = 0;
    public int offset = 0;
    public boolean is_exact = false;
    public boolean offset_modified = false;
    private final UUID timestamp;
    public Flags(String active_user, UUID timestamp) {
        username = active_user;
        this.timestamp = timestamp;
    }
    public String getStartDate(ParticipationDuration mode) {
        if(start_date == null) {
            start_date = switch(mode) {
                case DAY -> "2023-2-22";
                case MONTH -> "2023-2";
                case YEAR -> "2023";
            };
        }
        return new TimeUnit(start_date,mode).toString();
    }
    public String getEndDate(ParticipationDuration mode) {
        if(end_date == null) {
            end_date = new TimeUnit(timestamp,"America/Montreal", mode).addTime(1).toString();
        }
        return new TimeUnit(end_date,mode).toString();
    }
}
