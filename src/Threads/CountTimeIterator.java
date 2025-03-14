package Threads;

import java.util.List;
import java.util.NoSuchElementException;

public class CountTimeIterator extends CountIterator {
    private final Statable thread;
    private final UUID uuid1;
    private final UUID uuid2;
    private int start_count;
    private int end_count;
    private List<CountValue> counts;
    private int file_num;
    private int index;
    private int end_file_num = 0;
    private int end_index = 0;
    public CountTimeIterator(Statable thread, UUID uuid1, UUID uuid2) {
        this.uuid1 = uuid1;
        this.uuid2 = uuid2;
        this.start_count = this.end_count = 0;
        this.thread = thread;
    }
    public CountTimeIterator(Statable thread, int start_count, int end_count) {
        this.start_count = start_count;
        this.end_count = end_count;
        this.uuid1 = this.uuid2 = null;
        this.thread = thread;
    }
    public CountTimeIterator init() {
        Location start;
        Location end;
        if(uuid1 == null) {
            start_count--;
            end_count--;
            if(start_count < 0) start_count = 0;
            if(start_count > thread.getTotal_counts()) start_count = thread.getTotal_counts();
            if(end_count < 0) end_count = 0;
            if(end_count > thread.getTotal_counts()) end_count = thread.getTotal_counts();
            start = thread.getCountLocation(start_count);
            end = thread.getCountLocation(end_count);
        } else {
            start = thread.getLocationOfFirstCountAfterTimestamp(uuid1);
            end = thread.getLocationOfFirstCountAfterTimestamp(uuid2);
            if(end==null)
                end = thread.getMaxCountLocation();
            if(start==null) {
                start = thread.getMaxCountLocation();
            }
        }
        file_num = start.getChatFile();
        index = start.getLocInFile();
        end_file_num = end.getChatFile();
        end_index = end.getLocInFile();
        counts = thread.getCountsTimeByFile(file_num);
        return this;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return file_num < end_file_num || index < end_index;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public CountValue next() {
        if(this.hasNext()) {
            CountValue output = counts.get(index);
            index++;
            if(index >= counts.size() && file_num!=end_file_num) {
                counts = thread.getCountsTimeByFile(++file_num);
                index = 0;
            }
            return output;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public Statable getThread() {
        return thread;
    }
}
