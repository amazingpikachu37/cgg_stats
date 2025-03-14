package Threads;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;

public class CountNumIterator implements Iterator<CountValue> {
    private final Statable thread;
    private CountDownLatch latch;
    private List<CountValue> counts;
    private List<CountValue> upcoming_counts;
    private int file_num;
    private int index;
    private final int end_file_num;
    private int start;
    private final int end;
    public CountNumIterator(Statable thread, int start_count, int end_count) {
        start = Math.max(start_count, 0);
        end = Math.min(end_count, thread.getLast_number());
        file_num = thread.getCountNumFileNumber(start_count);
        end_file_num = thread.getCountNumFileNumber(end_count);
        this.thread = thread;
        counts = thread.getFlattenedCountsNumByFile(file_num, start_count, end_count);
        latch = new CountDownLatch(1);
        index = 0;
        new Thread(() -> {
            if(file_num < end_file_num) {
                upcoming_counts = thread.getFlattenedCountsNumByFile(file_num+1, start, end);
            }
            latch.countDown();
        }).start();
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
        return start < end;
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
            if(index < counts.size()) {
                CountValue output = counts.get(index);
                index++;
                start++;
                return output;
            } else {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                counts = upcoming_counts;
                latch = new CountDownLatch(1);
                file_num++;
                new Thread(() -> {
                    if(file_num < end_file_num) {
                        upcoming_counts = thread.getFlattenedCountsNumByFile(file_num+1, start, end);
                    }
                    latch.countDown();
                }).start();
                index = 1;
                start++;
                return counts.get(0);
            }
        } else {
            throw new NoSuchElementException();
        }
    }
}
