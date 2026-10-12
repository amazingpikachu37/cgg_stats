package Threads;

import Stats.*;

public class IncrementalOdds extends CountingThread {
    public IncrementalOdds(ThreadData t) {
        super(t);
    }
    private UpdateOrCountValueWithComment prev_prev = null;
    private UpdateOrCountValueWithComment prev = null;

    @Override
    public double countSuccessChance(UpdateOrCountValueWithComment current) {
        if(prev == null) {
            Location loc = getLocationOfFirstCountOrUpdateAfterTimestamp(new UUID(current.getTimestamp()-1, 0), CountDataType.COUNT, false);
            UpdateOrCountValue prev_count = getUpdateOrCount(loc.counts_by_file()+loc.loc_in_file(), CountDataType.COUNT);
            if(prev_count.getTimestamp() < current.getTimestamp()) {
                UpdateBody body = getUpdateOrCount(loc.counts_by_file() + loc.loc_in_file(), CountDataType.COUNT_BODY);
                prev = new UpdateOrCountValueWithComment(prev_count, body);
                loc = getLocationOfFirstCountOrUpdateAfterTimestamp(new UUID(prev_count.getTimestamp() - 1, 0), CountDataType.COUNT, false);
                UpdateOrCountValue prev_prev_count = getUpdateOrCount(loc.counts_by_file()+loc.loc_in_file(), CountDataType.COUNT);
                if(prev_prev_count.getTimestamp() < prev_count.getTimestamp()) {
                    UpdateBody body2 = getUpdateOrCount(loc.counts_by_file() + loc.loc_in_file(), CountDataType.COUNT_BODY);
                    prev_prev = new UpdateOrCountValueWithComment(prev_prev_count, body2);
                }
            }
        }
        double output;
        if(current.getRawCount()==null || parseMain(current.getRawCount()) < 1) output = 0.0;
        else if(prev==null) output = 1.0/parseMain(current.getRawCount());
        else if(prev.getAuthorId() == current.getAuthorId() && !thread.allowDoublePosts) output = 0.0;
        else {
            int prev_prev_num = (prev_prev == null) ? 0 : parseMain(prev_prev.getRawCount());
            int previous_num = parseMain(prev.getRawCount());
            int current_num = parseMain(current.getRawCount());
            int increment = previous_num - prev_prev_num;
            if (current_num < previous_num + increment) output = 0.0;
            else output = 1.0 / (current_num - previous_num);
        }
        if(current.isCount()) {
            prev_prev = prev;
            prev = current;
        }
        return output;
    }

    public void clearCountSuccessChance() {
        prev = null;
        prev_prev = null;
    }

    private int parseMain(String raw_count) {
        CountingThread main = AllThreads.all_threads_with_combined.get(3);
        return main.parse(raw_count);
    }
}
