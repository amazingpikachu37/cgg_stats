package Stats;


import javax.annotation.Nonnull;

public record TimeOutput(long time) implements Comparable<TimeOutput> {
    @Override
    public int compareTo(TimeOutput o) {
        long diff = this.time - o.time;
        if (diff > 0) return 1;
        if (diff < 0) return -1;
        return 0;
    }

    @Override
    @Nonnull
    public String toString() {
        long days = time / (86400L * 1000000000);
        String hours = Long.toString((time % (86400L * 1000000000)) / (3600L * 1000000000));
        String minutes = Long.toString((time % (3600L * 1000000000)) / (60L * 1000000000));
        String seconds = Long.toString((time % (60L * 1000000000)) / (1000000000));
        StringBuilder nanoseconds = new StringBuilder(Long.toString((time % (1000000000))));
        while (nanoseconds.length() < 9) nanoseconds.insert(0, "0");
        if (minutes.equals("0") && hours.equals("0") && days == 0) return seconds + "." + nanoseconds;
        if (seconds.length() == 1) seconds = "0" + seconds;
        if (hours.equals("0") && days == 0) return minutes + ":" + seconds + "." + nanoseconds;
        if (minutes.length() == 1) minutes = "0" + minutes;
        if (days == 0) return hours + ":" + minutes + ":" + seconds + "." + nanoseconds;
        if (hours.length() == 1) hours = "0" + hours;
        return days + "d " + hours + ":" + minutes + ":" + seconds + "." + nanoseconds;
    }
}
