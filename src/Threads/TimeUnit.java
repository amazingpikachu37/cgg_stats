package Threads;

import javax.annotation.Nonnull;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

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
        Instant time = Instant.ofEpochMilli(uuid.getTime()/10000);
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
        return this.atDay(1).atStartOfDay(ZoneId.of(timezone)).toEpochSecond()*10000000;
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
}
