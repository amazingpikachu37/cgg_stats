package Stats;

import javax.annotation.Nonnull;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Objects;

public class TimeUnit implements Comparable<TimeUnit> {
    Temporal time;
    ParticipationDuration mode;
    public TimeUnit(String date, ParticipationDuration mode) {
        time = switch(mode) {
            case DAY -> LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-M-d"));
            case MONTH -> YearMonth.parse(date, DateTimeFormatter.ofPattern("yyyy-M"));
            case YEAR -> Year.parse(date, DateTimeFormatter.ofPattern("yyyy"));
        };
        this.mode = mode;
    }
    public TimeUnit(Temporal date, ParticipationDuration mode) {
        time = date;
        this.mode = mode;
    }
    public TimeUnit(UUID uuid, String timezone, ParticipationDuration mode) {
        Instant time = Instant.ofEpochMilli(uuid.getTime()/1000000);
        LocalDate date = LocalDate.ofInstant(time, ZoneId.of(timezone));
        this.time = switch(mode) {
            case DAY -> date;
            case MONTH -> YearMonth.of(date.getYear(), date.getMonth());
            case YEAR -> Year.of(date.getYear());
        };
        this.mode = mode;
    }
    public TimeUnit(long timestamp, String timezone, ParticipationDuration mode) {
        Instant time = Instant.ofEpochMilli(timestamp/1000000);
        LocalDate date = LocalDate.ofInstant(time, ZoneId.of(timezone));
        this.time = switch(mode) {
            case DAY -> date;
            case MONTH -> YearMonth.of(date.getYear(), date.getMonth());
            case YEAR -> Year.of(date.getYear());
        };
        this.mode = mode;
    }
    public Temporal getTemporal() {
        return time;
    }
    public static String getProperDateFormatting(String date, ParticipationDuration mode) {
        return switch(mode) {
            case DAY -> LocalDate.parse(date,DateTimeFormatter.ofPattern("yyyy-M-d")).toString();
            case MONTH -> YearMonth.parse(date,DateTimeFormatter.ofPattern("yyyy-M")).toString();
            case YEAR -> Year.parse(date,DateTimeFormatter.ofPattern("yyyy")).toString();
        };
    }
    public TimeUnit subtractTime(long timeToSubtract) {
        return switch (mode) {
            case DAY -> new TimeUnit(((LocalDate) time).minusDays(timeToSubtract),mode);
            case MONTH -> new TimeUnit(((YearMonth) time).minusMonths(timeToSubtract), mode);
            case YEAR -> new TimeUnit(((Year) time).minusYears(timeToSubtract),mode);
        };
    }
    public TimeUnit addTime(long timeToAdd) {
        return switch (mode) {
            case DAY -> new TimeUnit(((LocalDate) time).plusDays(timeToAdd),mode);
            case MONTH -> new TimeUnit(((YearMonth) time).plusMonths(timeToAdd),mode);
            case YEAR -> new TimeUnit(((Year) time).plusYears(timeToAdd),mode);
        };
    }
    public static int compareDateStrings(String date1, String date2, ParticipationDuration mode) {
        LocalDate start_date = null;
        LocalDate end_date = null;
        switch(mode) {
            case DAY -> {
                start_date = LocalDate.parse(date1, DateTimeFormatter.ofPattern("yyyy-M-d"));
                end_date = LocalDate.parse(date2, DateTimeFormatter.ofPattern("yyyy-M-d"));
            }
            case MONTH -> {
                start_date = YearMonth.parse(date1, DateTimeFormatter.ofPattern("yyyy-M")).atDay(1);
                end_date = YearMonth.parse(date2, DateTimeFormatter.ofPattern("yyyy-M")).atDay(1);
            }
            case YEAR -> {
                start_date = Year.parse(date1, DateTimeFormatter.ofPattern("yyyy")).atDay(1);
                end_date = Year.parse(date2, DateTimeFormatter.ofPattern("yyyy")).atDay(1);
            }
        };
        return start_date.compareTo(end_date);
    }
    public LocalDate atDay(int day) {
        return switch(mode) {
            case DAY -> (LocalDate) time;
            case MONTH -> ((YearMonth)time).atDay(day);
            case YEAR -> ((Year)time).atDay(day);
        };
    }
    public String toString() {
        return switch (mode) {
            case DAY -> time.toString();
            case MONTH -> time.toString().substring(0, 7);
            case YEAR -> time.toString().substring(0, 4);
        };
    }
    public static long timeBetween(TimeUnit start, TimeUnit end, ParticipationDuration mode) {
        return switch(mode) {
            case DAY -> ChronoUnit.DAYS.between(start.time, end.time);
            case MONTH -> ChronoUnit.MONTHS.between(start.time, end.time);
            case YEAR -> ChronoUnit.YEARS.between(start.time, end.time);
        };
    }
    public static long timeSinceCGGStart(TimeUnit end, ParticipationDuration mode) {
        return switch(mode) {
            case DAY -> ChronoUnit.DAYS.between(new TimeUnit("2023-2-22", ParticipationDuration.DAY).time, end.time);
            case MONTH -> ChronoUnit.MONTHS.between(new TimeUnit("2023-2", ParticipationDuration.MONTH).time, end.time);
            case YEAR -> ChronoUnit.YEARS.between(new TimeUnit("2023", ParticipationDuration.YEAR).time, end.time);
        };
    }

    public static long timeBetween(Temporal start, Temporal end, ParticipationDuration mode) {
        return switch(mode) {
            case DAY -> ChronoUnit.DAYS.between(start, end);
            case MONTH -> ChronoUnit.MONTHS.between(start, end);
            case YEAR -> ChronoUnit.YEARS.between(start, end);
        };
    }
    public boolean isAfter(TimeUnit other) {
        return switch(mode) {
            case DAY -> ((LocalDate)this.time).isAfter((LocalDate)other.time);
            case MONTH -> ((YearMonth)this.time).isAfter((YearMonth)other.time);
            case YEAR -> ((Year)this.time).isAfter((Year)other.time);
        };
    }
    public boolean isBefore(TimeUnit other) {
        return switch(mode) {
            case DAY -> ((LocalDate)this.time).isBefore((LocalDate)other.time);
            case MONTH -> ((YearMonth)this.time).isBefore((YearMonth)other.time);
            case YEAR -> ((Year)this.time).isBefore((Year)other.time);
        };
    }
    public long timestamp(String timezone) {
        return this.atDay(1).atStartOfDay(ZoneId.of(timezone)).toEpochSecond()*1000000000;
    }
    public static TimeUnit getCGGStartDate(ParticipationDuration mode) {
        return switch(mode) {
            case DAY -> new TimeUnit("2023-2-22", mode);
            case MONTH -> new TimeUnit("2023-2", mode);
            case YEAR -> new TimeUnit("2023", mode);
        };
    }
    public static TimeUnit today(ParticipationDuration mode) {
        return switch(mode) {
            case DAY -> new TimeUnit(LocalDate.now(ZoneId.of(CountingThread.timezone)), mode);
            case MONTH -> new TimeUnit(YearMonth.now(ZoneId.of(CountingThread.timezone)), mode);
            case YEAR -> new TimeUnit(Year.now(ZoneId.of(CountingThread.timezone)), mode);
        };
    }

    public static TimeUnit getMinTimeUnitOfThreads(List<CountingThread> threads, TimeUnit min_time, ParticipationDuration mode) {
        TimeUnit first_day = threads.get(0).first_day(mode, CountingThread.timezone);
        for(int i=1;i<threads.size();i++) {
            TimeUnit start = threads.get(i).first_day(mode, CountingThread.timezone);
            if(start.isBefore(first_day)) first_day = start;
        }
        if(first_day.isBefore(min_time)) first_day = min_time;
        return first_day;
    }
    public static TimeUnit getMaxTimeUnitOfThreads(List<CountingThread> threads, TimeUnit max_time, ParticipationDuration mode) {
        TimeUnit last_day = threads.get(0).last_day(mode, CountingThread.timezone);
        for(int i=1;i<threads.size();i++) {
            TimeUnit end = threads.get(i).last_day(mode, CountingThread.timezone);
            if(end.isAfter(last_day)) last_day = end;
        }
        if(last_day.isAfter(max_time)) last_day = max_time;
        return last_day;
    }


    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public int compareTo(@Nonnull TimeUnit o) {
        if(this.isAfter(o)) return 1;
        if(this.isBefore(o)) return -1;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TimeUnit timeUnit = (TimeUnit) o;
        return Objects.equals(time, timeUnit.time) && mode == timeUnit.mode;
    }
}
