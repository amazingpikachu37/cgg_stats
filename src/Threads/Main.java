package Threads;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        Statable test = new OneInX();
        //CombinedStats.updateAllDatabases();
        UUID start = new UUID("00000000-0000-4000-a000-000000000000");
        UUID start2 = new UUID("16808017-5217-4916-8598-219424916598"); //557575 in main, index 557574
        UUID end = new UUID("99999999-9999-4999-a999-999999999999");
        UUID end2 = new UUID("17010657-3598-4041-9103-640336041103"); //5575757 in main, index 5575756
        UUID start3 = new UUID("16776305-4666-4490-8075-931801490075"); //10 in main
        UUID end3 = new UUID("16776305-4711-4701-9630-645456701630"); //25 in main
        String start_date = "2023-2-22";
        String end_date = "2025-7-19";
        long t1 = System.currentTimeMillis();
        Map<String, ? extends Comparable> testhoc2 = CombinedStats.hocOverTimePeriod(List.of(test),start, end);
        long t2 = System.currentTimeMillis();
        System.out.println(TableConstructor.constructRedditTable(testhoc2, new String[]{"Username","Counts"},false,false));
    }
}

