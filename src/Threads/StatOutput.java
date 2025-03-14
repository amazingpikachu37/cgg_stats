package Threads;

import java.lang.reflect.Field;

public class StatOutput {
    public final Integer start;
    public final Integer end;
    public final Integer k_size;
    public final String start_uuid;
    public final String end_uuid;
    public final String start_thread;
    public final String end_thread;
    public final String user1;
    public final String user2;
    public final Integer user1_counts;
    public final Integer user2_counts;
    public final String[] users;
    public final Integer[] user_counts;
    public final String start_date;
    public final String end_date;
    public final Long time;
    public final Float percentage;
    public final Integer counts;
    public final Integer streak;
    public final Integer counts_in_k;
    public final Integer counts_by_users;
    public final Boolean artbn;
    public final Long[] splits;
    public final Float stdDev;
    public final Integer thread_num;
    public final Integer trifecta;
    public final Integer chalupa_exacta;
    public final Integer poonxacta;
    public final Integer trejexacta;
    public final Integer start_hoc;
    public final Integer end_hoc;
    public final CountValue count;

    private StatOutput(Builder builder) {
        this.start = builder.start;
        this.end = builder.end;
        this.k_size = builder.k_size;
        this.start_uuid = builder.start_uuid;
        this.end_uuid = builder.end_uuid;
        this.start_thread = builder.start_thread;
        this.end_thread = builder.end_thread;
        this.user1 = builder.user1;
        this.user2 = builder.user2;
        this.user1_counts = builder.user1_counts;
        this.user2_counts = builder.user2_counts;
        this.users = builder.users;
        this.user_counts = builder.user_counts;
        this.start_date = builder.start_date;
        this.end_date = builder.end_date;
        this.time = builder.time;
        this.percentage = builder.percentage;
        this.counts = builder.counts;
        this.streak = builder.streak;
        this.counts_in_k = builder.counts_in_k;
        this.counts_by_users = builder.counts_by_users;
        this.artbn = builder.artbn;
        this.splits = builder.splits;
        this.stdDev = builder.stdDev;
        this.thread_num = builder.thread_num;
        this.trifecta = builder.trifecta;
        this.chalupa_exacta = builder.chalupa_exacta;
        this.poonxacta = builder.poonxacta;
        this.trejexacta = builder.trejexacta;
        this.start_hoc = builder.start_hoc;
        this.end_hoc = builder.end_hoc;
        this.count = builder.count;
    }
    public Object getFieldValue(String fieldName) {
        try {
            Field field = this.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Invalid field name: " + fieldName, e);
        }
    }

    public static class Builder {
        private Integer start;
        private Integer end;
        private Integer k_size;
        private String start_uuid;
        private String end_uuid;
        private String start_thread;
        private String end_thread;
        private String user1;
        private String user2;
        private Integer user1_counts;
        private Integer user2_counts;
        private String[] users;
        private Integer[] user_counts;
        private String start_date;
        private String end_date;
        private Long time;
        private Float percentage;
        private Integer counts;
        private Integer streak;
        private Integer counts_in_k;
        private Integer counts_by_users;
        private Boolean artbn;
        private Long[] splits;
        private Float stdDev;
        private Integer thread_num;
        private Integer trifecta;
        private Integer chalupa_exacta;
        private Integer poonxacta;
        private Integer trejexacta;
        private Integer start_hoc;
        private Integer end_hoc;
        private CountValue count;

        public Builder setStart(Integer start) {
            this.start = start;
            return this;
        }

        public Builder setEnd(Integer end) {
            this.end = end;
            return this;
        }

        public Builder setKSize(Integer k_size) {
            this.k_size = k_size;
            return this;
        }

        public Builder setStartUuid(String start_uuid) {
            this.start_uuid = start_uuid;
            return this;
        }

        public Builder setEndUuid(String end_uuid) {
            this.end_uuid = end_uuid;
            return this;
        }

        public Builder setStartThread(String start_thread) {
            this.start_thread = start_thread;
            return this;
        }

        public Builder setEndThread(String end_thread) {
            this.end_thread = end_thread;
            return this;
        }
        public Builder setUser1(String user1) {
            this.user1 = user1;
            return this;
        }

        public Builder setUser2(String user2) {
            this.user2 = user2;
            return this;
        }

        public Builder setUser1Counts(Integer user1_counts) {
            this.user1_counts = user1_counts;
            return this;
        }

        public Builder setUser2Counts(Integer user2_counts) {
            this.user2_counts = user2_counts;
            return this;
        }
        public Builder setUsers(String[] users) {
            this.users = users;
            return this;
        }

        public Builder setUserCounts(Integer[] user_counts) {
            this.user_counts = user_counts;
            return this;
        }

        public Builder setStartDate(String date) {
            this.start_date = date;
            return this;
        }
        public Builder setEndDate(String date) {
            this.end_date = date;
            return this;
        }

        public Builder setTime(Long time) {
            this.time = time;
            return this;
        }

        public Builder setPercentage(Float percentage) {
            this.percentage = percentage;
            return this;
        }

        public Builder setCounts(Integer counts) {
            this.counts = counts;
            return this;
        }

        public Builder setStreak(Integer streak) {
            this.streak = streak;
            return this;
        }

        public Builder setCountsInK(Integer counts_in_k) {
            this.counts_in_k = counts_in_k;
            return this;
        }

        public Builder setCountsByUsers(Integer counts_by_users) {
            this.counts_by_users = counts_by_users;
            return this;
        }

        public Builder setArtbn(Boolean artbn) {
            this.artbn = artbn;
            return this;
        }

        public Builder setSplits(Long[] splits) {
            this.splits = splits;
            return this;
        }

        public Builder setStdDev(Float stdDev) {
            this.stdDev = stdDev;
            return this;
        }

        public Builder setThreadNum(Integer thread_num) {
            this.thread_num = thread_num;
            return this;
        }

        public Builder setTrifecta(Integer trifecta) {
            this.trifecta = trifecta;
            return this;
        }

        public Builder setChalupaExacta(Integer chalupa_exacta) {
            this.chalupa_exacta = chalupa_exacta;
            return this;
        }

        public Builder setPoonxacta(Integer poonxacta) {
            this.poonxacta = poonxacta;
            return this;
        }

        public Builder setTrejexacta(Integer trejexacta) {
            this.trejexacta = trejexacta;
            return this;
        }

        public Builder setStartHoc(Integer start_hoc) {
            this.start_hoc = start_hoc;
            return this;
        }

        public Builder setEndHoc(Integer end_hoc) {
            this.end_hoc = end_hoc;
            return this;
        }

        public Builder setCount(CountValue count) {
            this.count = count;
            return this;
        }

        public StatOutput build() {
            return new StatOutput(this);
        }
    }
}
