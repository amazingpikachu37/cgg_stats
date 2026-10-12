package Bot;

import Stats.*;

public class Flags {
    public String username;
    public boolean username_modified = false;
    public int position = 0;
    public int context = 2;

    public UUID start_uuid = UUID.minUUID();
    public UUID end_uuid = UUID.maxUUID();

    public TimeUnit start_date = getStartDate(ParticipationDuration.DAY);
    public TimeUnit end_date = getEndDate(ParticipationDuration.DAY);
    public ParticipationDuration time_period = ParticipationDuration.DAY;

    public int start_k = 0;
    public int end_k = Integer.MAX_VALUE;
    public int k_size = 1;

    public int start_count = 0;
    public int end_count = Integer.MAX_VALUE;
    public int start_update = 0;
    public int end_update = Integer.MAX_VALUE;

    public int min_counts = 1;
    public int offset = 0;
    public float percentage = 0.98f;
    public int mode = 0; //Trifecta Mode and Sort Mode
    public int duration = 3600; //Can be seconds or milliseconds
    public boolean duration_modified = false;
    public int counts_between_replies = 1;

    public boolean unique = false;
    public boolean use_counters = true;
    public boolean use_counts = true;
    public boolean thread_counts_combined = false;
    public boolean add_thread_stats = false;
    public boolean allow_duplicate_counters = false;
    public boolean use_ks = true; //Ks vs Splits
    public boolean first = true;
    public boolean include_current = false;
    public boolean exclude_100 = false;


    public Flags(String active_user) {
        username = active_user;
    }
    public static TimeUnit getStartDate(ParticipationDuration mode) {
        return AllThreads.combined_thread.first_day(mode, CountingThread.timezone);
    }
    public static TimeUnit getEndDate(ParticipationDuration mode) {
        return AllThreads.combined_thread.last_day(mode, CountingThread.timezone);
    }
}
