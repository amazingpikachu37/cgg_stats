package Threads;

import Stats.*;

public class OneInX extends BaseN {
    public OneInX(ThreadData data, int n, int offset, int increment, String base_string, boolean contains_zero) {
        super(data, n, offset, increment, base_string, contains_zero);
    }
    private UpdateOrCountValueWithComment prev = null;
    public double countSuccessChance(UpdateOrCountValueWithComment current) {
        if(prev == null) {
            Location loc = getLocationOfFirstCountOrUpdateAfterTimestamp(new UUID(current.getTimestamp()-1, 0), CountDataType.COUNT, false);
            UpdateOrCountValue prev_count = getUpdateOrCount(loc.counts_by_file()+loc.loc_in_file(), CountDataType.COUNT);
            if(prev_count.getTimestamp() < current.getTimestamp()) {
                UpdateBody body = getUpdateOrCount(loc.counts_by_file() + loc.loc_in_file(), CountDataType.COUNT_BODY);
                prev = new UpdateOrCountValueWithComment(prev_count, body);
            }
        }
        double output;
        if(current.getRawCount()==null) output = 0.0;
        else if(prev==null) output = (parse(current.getRawCount()) == 1) ? 1.0 : 0.0;
        else if(prev.getAuthorId() == current.getAuthorId() && !thread.allowDoublePosts) output = 0.0;
        else {
            int previous_num = parse(prev.getRawCount());
            int current_num = parse(current.getRawCount());
            if (current_num != previous_num + 1) output = 0.0;
            else output = 1.0 / current_num;
        }
        if(current.isCount()) prev = current;
        return output;
    }
    public void clearCountSuccessChance() {
        prev = null;
    }
}
