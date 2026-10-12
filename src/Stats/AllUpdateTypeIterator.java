package Stats;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class AllUpdateTypeIterator<K extends Serializable> implements Iterator<K> {
    private final CountingThread thread;
    private final CountDataType mode;
    private Location start;
    private Location end;
    private List<K> updates;
    private int file_num;
    private int index;
    private int end_file_num;
    private int end_index;
    public AllUpdateTypeIterator(CountingThread thread, int start_update, int end_update, CountDataType mode) {
        this.thread = thread;
        if(start_update < 0) start_update = 0;
        if(start_update > thread.getTotalUpdates(mode)) start_update = thread.getTotalUpdates(mode);
        if(end_update < 0) end_update = 0;
        if(end_update > thread.getTotalUpdates(mode)) end_update = thread.getTotalUpdates(mode);
        start = thread.getCountOrUpdateLocation(start_update, mode);
        end = thread.getCountOrUpdateLocation(end_update, mode);
        this.mode = mode;
    }
    public AllUpdateTypeIterator(CountingThread thread, UUID uuid1, UUID uuid2, CountDataType mode) {
        this.thread = thread;
        start = thread.getLocationOfFirstCountOrUpdateAfterTimestamp(uuid1, mode, true);
        end = thread.getLocationOfFirstCountOrUpdateAfterTimestamp(uuid2, mode, true);
        if(end==null)
            end = thread.getMaxCountOrUpdateLocation(mode);
        if(start==null) {
            start = thread.getMaxCountOrUpdateLocation(mode);
        }
        this.mode = mode;
    }
    public AllUpdateTypeIterator<K> init() {
        file_num = start.getChatFile();
        index = start.getLocInFile();
        end_file_num = end.getChatFile();
        end_index = end.getLocInFile();
        updates = thread.readUpdateOrCountFile(file_num, mode);
        return this;
    }
    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    public boolean hasNext() {
        return file_num < end_file_num || index < end_index;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    public K next() {
        if(this.hasNext()) {
            K output = updates.get(index);
            index++;
            if(index >= updates.size() && file_num!=end_file_num) {
                updates = thread.readUpdateOrCountFile(++file_num, mode);
                index = 0;
            }
            return output;
        } else {
            throw new NoSuchElementException();
        }
    }
}
