package Threads;

import java.util.List;
import java.util.NoSuchElementException;

public class UpdateIterator extends UpdateIteratorAbstraction {
    private final Statable thread;
    private final UUID uuid1;
    private final UUID uuid2;
    private List<UpdateValue> counts;
    private int file_num;
    private int index;
    private int end_file_num;
    private int end_index;
    public UpdateIterator(Statable thread, UUID uuid1, UUID uuid2) {
        this.thread = thread;
        this.uuid1 = uuid1;
        this.uuid2 = uuid2;
    }
    @Override
    public UpdateIterator init() {
        Location start = thread.getLocationOfFirstUpdateAfterTimestamp(uuid1);
        Location end = thread.getLocationOfFirstUpdateAfterTimestamp(uuid2);
        if(end==null)
            end = thread.getMaxUpdateLocation();
        if(start==null) {
            start = thread.getMaxUpdateLocation();
        }
        file_num = start.getChatFile();
        end_file_num = end.getChatFile();
        counts = thread.getUpdatesByFile(file_num);
        index = counts.size()-start.getLocInFile()-1;
        end_index = end.getLocInFile();
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
        return file_num < end_file_num || index >= counts.size()-end_index;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public UpdateValue next() {
        if(this.hasNext()) {
            UpdateValue output = counts.get(index);
            index--;
            if(index<0 && file_num!=end_file_num) {
                counts = thread.getUpdatesByFile(++file_num);
                index = counts.size()-1;
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
