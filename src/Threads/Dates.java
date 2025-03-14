package Threads;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Dates extends Statable {
    public Dates() {
        super("dates", List.of("dates"),"America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(1461, 0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM d, y");
        LocalDate date1 = LocalDate.parse("0000-12-31");
        return date1.plusDays(num).format(dtf);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1461+1)*1461)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/dates"};
    }

    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(487, 1461, 0);
    }
}
