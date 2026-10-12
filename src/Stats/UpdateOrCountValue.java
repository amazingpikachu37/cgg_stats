package Stats;

public interface UpdateOrCountValue extends Serializable {

    boolean stricken();
    UUID getUUID();
    long getTimestamp();
    Integer validCountNumber();
    boolean isCount();

    int getThreadId();
    int getAuthorId();
    int getUpdateId();
    default String getAuthorUsername() {
        return Counters.counters.get(this.getAuthorId()).username();
    }
    default CounterData getAuthor() {
        return Counters.counters.get(this.getAuthorId());
    }
    default String getAuthorName() {
        return Counters.counters.get(this.getAuthorId()).name();
    }
    default CountingThread getThread() {
        return AllThreads.all_threads.get(this.getThreadId()-1);
    }
    default String getThreadTitle() {
        return AllThreads.all_threads.get(this.getThreadId()-1).thread.title;
    }
    default String getThreadName() {
        return AllThreads.all_threads.get(this.getThreadId()-1).thread.name;
    }
    static UpdateOrCountValue getDefaultObject(boolean use_counts) {
        return use_counts?CountValue.getDefaultObject():UpdateValue.getDefaultObject();
    }
    static UpdateOrCountValue getDefaultObjectWithMaxTimestamp(boolean use_counts) {
        if(use_counts) return new CountValue(Long.MAX_VALUE,0,0,0,0,0);
        return new UpdateValue(UUID.maxUUID(),false, false, false, 0,0,0,0);
    }
    static UpdateOrCountValue getDummyObject(boolean use_counts, int author_id, int thread_id) {
        if(use_counts) return new CountValue(0L,0,0,author_id,thread_id,0);
        else return new UpdateValue(UUID.minUUID(), false, false, false, 0, author_id, thread_id, 0);
    }

    int compareTo(UpdateOrCountValue u);

}
