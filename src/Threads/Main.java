package Threads;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        Statable test = new OneInX();
        //CombinedStats.updateAllDatabases();
       // test.serializeCountNumFile(test.readCountNumFile(0, 0),0,0);
       // System.out.println(Arrays.toString(test.readCountNumFileV3(0,0)));
//        for(Statable thread:CombinedStats.all_threads) {
//            int count_files = (thread.getLast_number()-1)/CountingThread.MSGS_PER_FILE;
//            for(int i=0;i<thread.totalUpdateFiles();i++) {
//                thread.serializeUpdateFile(thread.readUpdateFile(0, i),0,i);
//            }
//            if(thread.getLast_number()==0) continue;
//            for(int i=0;i<=count_files;i++) {
//                thread.serializeCountTimeFile(thread.readCountTimeFile_OLD(0,i),0,i);
//            }
//            for(int i=0;i<=count_files;i++) {
//                thread.serializeCountNumFile(thread.readCountNumFile_OLD(0,i),0,i);
//            }
//        }
//        for(int i=0;i<=66;i++) {
//            test.serializeCountTimeFile(test.readCountTimeFile_OLD(0,i),0,i);
//        }
//        for(int i=0;i<=66;i++) {
//            test.serializeCountNumFile(test.readCountNumFile_OLD(0, i),0,i);
//        }
//        for(int i=0;i<test.totalUpdateFiles();i++) {
//            test.serializeUpdateFile(test.readUpdateFile(0, i),0,i);
//        }
//        for(int i=0;i<67;i++) {
//            test.serializeCountNumFileV2(test.readCountNumFile_OLD(0,i),0,i);
//        }
      //  test.serializeCountNumFileV2(test.getCountsNumByFile(0),0,0);
//        test.serializeCountTimeFile(test.readCountTimeFile_OLD(0,0),0,0);
     //   ArrayList<CountValue> test2 = test.readCountTimeFile(0,0);
       //   System.out.println(test2);
//        for(CountValue test3:test2) {
//            if(test3==null) {
//                System.out.println("Unacceptable");
//            }
//        }
       // System.out.println(test.readCountTimeFileV2(0,66,8));
     //  CombinedStats.combinedGraphHocOverTimePeriodWithFilter(CombinedStats.all_threads, Util.getUUIDFromDate("2024-3-7","America/Montreal"),Util.getUUIDFromDate("2024-3-8","America/Montreal"),k -> true,"no",false,false,1000);

     //  test.updateDatabase();
     //CombinedStats.updateAllDatabases();
     //System.out.println(Util.sumIntMapValues(CombinedStats.combinedHoCTime(CombinedStats.all_threads,"00000000-0000-4000-a000-000000000000","99999999-9999-4999-a999-999999999999",false)));
//       test.updateDatabase();
//
        UUID start = new UUID("00000000-0000-4000-a000-000000000000");
        UUID start2 = new UUID("16808017-5217-4916-8598-219424916598"); //557575 in main, index 557574
        UUID end = new UUID("99999999-9999-4999-a999-999999999999");
        UUID end2 = new UUID("17010657-3598-4041-9103-640336041103"); //5575757 in main, index 5575756
        UUID start3 = new UUID("16776305-4666-4490-8075-931801490075"); //10 in main
        UUID end3 = new UUID("16776305-4711-4701-9630-645456701630"); //25 in main
        List<Map<String, Integer>> attempts = ((OneInX)test).badAttempts(start, end);
        for(int i=0;i<attempts.size();i++) {
            int attempt_counter = 0;
            for(String user:attempts.get(i).keySet()) {
                attempt_counter+=attempts.get(i).get(user);
            }
            System.out.println((i+1)+"\t"+attempt_counter);
        }



       // Map<String, CountValue> testhoc2 = CombinedStats.overallFirstCounts(CombinedStats.all_sides, "00000000-0000-1000-0000-000000000000");
       // Map<String, ? extends Comparable> testhoc2 = CombinedStats.overall_no_mistake_streak(CombinedStats.all_threads,"00000000-0000-4000-a000-000000000000","99999999-9999-4999-a999-999999999999");
   //   Map<String, ? extends Comparable> testhoc2 = test.medianSelfReplyTime("00000000-0000-4000-a000-000000000000","99999999-9999-4999-a999-999999999999",1000);
      //  Map<String, ? extends Comparable> testhoc2 = test.fastestSplitsByUser(0,test.getLast_number(),0.98f);
     // Map<String, ? extends Comparable> testhoc2 = CombinedStats.combined_days_over_n_counts(CombinedStats.all_threads,"2022-2-22","2030-1-1",200000,ParticipationDuration.DAY,false,false);

        //     List<Integer> gets = test.getGetValues();
        String start_date = "2023-2-22";
        String end_date = "2025-7-19";
        long t1 = System.currentTimeMillis();
        //List<?> testhoc2 = ((NoMistakesOrBan)test).getBanList();
       // List<StatLoader.K_HOC> testhoc2 = CombinedStats.all_k_hocs(List.of(test), 0,999999,100).get(test);
      //List<StatLoader.DayHOC> testhoc2 = CombinedStats.combined_all_day_hocs(List.of(test),start_date, end_date, ParticipationDuration.DAY);
       // Map<String, Map<String, Integer>> testhoc2 = test.all_counting_pairs(start, end);
        //Map<String, ? extends  Comparable> testhoc2 = CombinedStats.fastestKsByUser(CombinedStats.all_threads,1,0,99999,0.49f);
     //Map<String, ? extends  Comparable> testhoc2 = CombinedStats.thread_fastest_n_counts_no_user(CombinedStats.all_threads, 1, start, end,true);
        //System.out.println( test.all_counting_pairs(start,end));
        //Map<String, ? extends Comparable> testhoc2 = CombinedStats.hoc_for_individual_threads(CombinedStats.all_threads,start,end);
        Map<String, ? extends Comparable> testhoc2 = CombinedStats.hocOverTimePeriod(List.of(test),start, end);
       //GenericStatOutput testhoc2 = CombinedStats.overall_perfect_streak_v2(CombinedStats.all_threads,start, end,2, true);
        //Map<String, ? extends  Comparable> testhoc2 = test.k_streak(0,99999,1,false);
      //List<StatLoader.SplitsOutput> testhoc2 = test.getSplits(0,15);
       //StatLoader.SplitsOutput testhoc2 = test.getSplits(1,0.49f, false);
       //List<? extends NonUnique> testhoc2 =  CombinedStats.overall_non_unique_perfect_streak(CombinedStats.all_threads,start, end, 2);
       //List<? extends NonUnique> testhoc2 = test.k_streak_non_unique(0,99999,1);
       //Collections.sort(testhoc2);
       // List<? extends NonUnique> testhoc2 = test.fastestSplitsHistory(0,test.getLast_number(),0.48f);
        long t2 = System.currentTimeMillis();
       //Map<String, ? extends  Comparable> testhoc2 = test.getKHoc(43,100,false);
       //Map<String, ? extends Comparable> testhoc2 = CombinedStats.overall_no_mistake_streak(CombinedStats.all_threads,"00000000-0000-1000-0000-000000000000","99999999-9999-4999-a999-999999999999");
    //Map<String, ? extends Comparable> testhoc2 =  test.fastestSplitsByUser(0,test.getLast_number(),0.49f);
     //  Map<String, ? extends Comparable> testhoc3 =  test.fastestKsByUser(1,0,999999,0.98f);
       //Map<String, ? extends Comparable> testhoc2 =  CombinedStats.CombinedMsBetweenUpdates(CombinedStats.all_threads,"17041154-1292-4960-a216-887940960216","99999999-9999-4999-a999-999999999999",100000);
     //List<? extends NonUnique> testhoc2 =  ((NoMistakesThread)test).peak_no_mistakes_counts("00000000-0000-4000-a000-000000000000","99999999-9999-4999-a999-999999999999");
                // Collections.sort(testhoc2);
        //   Map<String,CountValue> testhoc2 = test.getFirstCounts("e83c30c4-1662-11e9-8509-0e3abe410518");
       // List<? extends NonUnique> testhoc2 = ((OneInX)test).record_streaks_without_valid_count();
        //Map<String, Integer> testhoc2 = CombinedStats.combinedKParts(CombinedStats.all_threads,0,99999999,1, false);
      //System.out.println(TableConstructor.constructRedditTable(testhoc, new String[]{"Counts"},false));
        //System.out.println("Total Counts: "+Util.sumIntMapValues(testhoc2));
      System.out.println(TableConstructor.constructRedditTable(testhoc2, new String[]{"Username","Counts"},false,false));
        //System.out.println(TableConstructor.constructRedditTable(testhoc3, new String[]{"Username","Counts"},true,false));
       // System.out.println(test.fastest_n_counts_no_user(100000,"00000000-0000-4000-a000-000000000000","99999999-9999-4999-a999-999999999999", true));
        //System.out.println(CombinedStats.overall_nth_count_no_user(CombinedStats.all_threads,36000000));
        //System.out.println(testhoc2);
       // System.out.println(CombinedStats.overall_day_record(CombinedStats.all_threads, "hmmmmlmao", start_date, end_date, ParticipationDuration.DAY, true, false));
        System.out.println(t2-t1);
    }
}

