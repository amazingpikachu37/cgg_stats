package Threads;

import Stats.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.zone.ZoneOffsetTransition;
import java.util.List;

public class RandomHour extends BaseN {
    public RandomHour(ThreadData data, int n, int offset, int increment, String base_string, boolean contains_zero) {
        super(data, n, offset, increment, base_string, contains_zero);
    }
    private UpdateOrCountValueWithComment prev = null;
    private int daily_attempts = 0;
//    private long next_bar = 0L;
    private ZonedDateTime next_hour = ZonedDateTime.of(2023,1,1,0,0,0,0, ZoneId.of(timezone));
    private long next_day = 0L;

    public double countSuccessChance(UpdateOrCountValueWithComment current) {
        if(prev == null) {
            Location loc = getLocationOfFirstCountOrUpdateAfterTimestamp(new UUID(current.getTimestamp()-1, 0), CountDataType.COUNT, false);
            UpdateOrCountValue prev_count = getUpdateOrCount(loc.counts_by_file()+loc.loc_in_file(), CountDataType.COUNT);
            if(prev_count.getTimestamp() < current.getTimestamp()) {
                UpdateBody body = getUpdateOrCount(loc.counts_by_file() + loc.loc_in_file(), CountDataType.COUNT_BODY);
                prev = new UpdateOrCountValueWithComment(prev_count, body);
                UUID start_of_day = Util.getUUIDFromDate(new TimeUnit(current.getUUID(), timezone, ParticipationDuration.DAY), timezone);
                UpdateIteratorWithComment updates = new UpdateIteratorWithComment(List.of(this), start_of_day, current.getUUID(), false).init();
                while(updates.hasNext()) {
                    UpdateOrCountValueWithComment update = updates.next();
                    if(update.getUpdateId() == current.getUpdateId()) break;
                    countSuccessChance(update);
                }
            }
        }
        if(current.getTimestamp() > next_day) {
            next_day = Util.getNextDayTimestamp(current.getTimestamp(), timezone, ParticipationDuration.DAY);
            daily_attempts = 0;
        }
        double output;
        //Already found the correct hour
        if(current.isCount() && current.getTimestamp()<toHourTimestamp(next_hour)) output = 1.0;
        //The correct hour was found and has passed
        else if(prev!=null && (new TimeUnit(current.getUUID(), timezone, ParticipationDuration.DAY).equals(new TimeUnit(prev.getUUID(), timezone, ParticipationDuration.DAY)))) output = 0.0;
        //Not a count, or a second attempt in the same hour
        else if(current.getRawCount()==null || current.getTimestamp() < toHourTimestamp(next_hour)) output = 0.0;
        //Double count attempt
        else if(prev!=null && prev.getAuthorId() == current.getAuthorId() && !thread.allowDoublePosts) output = 0.0;
        else {
            int previous_num = (prev == null) ? 0 : parse(prev.getRawCount());
            int current_num = parse(current.getRawCount());
            if (current_num != previous_num + 1) output = 0.0;
            else {
                daily_attempts++;
                next_hour = nextNewHour(Instant.ofEpochSecond(current.getTimestamp()/1000000000L), ZoneId.of(timezone));
                output = 1.0 / (25-daily_attempts);
            }
        }
        if(current.isCount()) prev = current;
        return output;
    }
    public void clearCountSuccessChance() {
        prev = null;
        daily_attempts = 0;
        next_hour = ZonedDateTime.of(0,1,1,0,0,0,0, ZoneId.of(timezone));
        next_day = 0L;
    }
    private long toHourTimestamp(ZonedDateTime hour) {
        return hour.toInstant().getEpochSecond()*1000000000L;
    }
    private ZonedDateTime nextNewHour(Instant instant, ZoneId zone) {
        ZonedDateTime current = instant.atZone(zone);
        LocalDateTime candidate = current.toLocalDateTime()
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .plusHours(1);
        ZoneOffsetTransition transition =
                zone.getRules().getTransition(candidate);
        if (transition != null) {
            if (transition.isGap()) {
                // Spring-forward:
                // e.g. 01:xx -> candidate 02:00, but 02:00 doesn't exist.
                // Jump to 03:00.
                candidate = transition.getDateTimeAfter();
            } else if (transition.isOverlap()) {
                // Fall-back:
                // e.g. candidate 01:00 occurs twice.
                // Skip the repeated 01:00 hour and go to 02:00.
                candidate = transition.getDateTimeAfter().plusHours(1);
            }
        }
        return ZonedDateTime.ofLocal(candidate, zone, null);
    }
}
