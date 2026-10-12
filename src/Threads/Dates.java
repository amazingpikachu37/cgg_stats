package Threads;

import Stats.CountingThread;
import Stats.ThreadData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Dates extends CountingThread {
    public Dates(ThreadData t) {
        super(t);
    }

    @Override
    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM d, y");
        LocalDate date1 = LocalDate.parse("0000-12-31");
        return date1.plusDays(num).format(dtf);
    }
}
