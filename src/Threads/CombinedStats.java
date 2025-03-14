package Threads;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class CombinedStats {
    public static final Map<String, Statable> all_threads_map = new LinkedHashMap<>();
    private static final Map<String, Statable> favorites_map = new LinkedHashMap<>();
    private static final Map<String, Statable> traditional_map = new LinkedHashMap<>();
    private static final Map<String, Statable> double_counting_map = new LinkedHashMap<>();
    private static final Map<String, Statable> no_mistakes_map = new LinkedHashMap<>();
    private static final Map<String, Statable> miscellaneous_map = new LinkedHashMap<>();
    public static final List<Statable> all_threads;
    public static final List<Statable> favorites;
    public static final List<Statable> traditional;
    public static final List<Statable> double_counting;
    public static final List<Statable> no_mistakes;
    public static final List<Statable> miscellaneous;
    static {
        favorites_map.put("main", new Decimal());
        favorites_map.put("slow", new Slow());
        favorites_map.put("bars", new Bars());
        favorites_map.put("double_counting", new DoubleCounting());
        favorites_map.put("no_mistakes", new NoMistakes());
        traditional_map.put("letters", new Letters());
        traditional_map.put("odds", new Odds());
        traditional_map.put("evens", new Evens());
        traditional_map.put("tug_of_war", new TugOfWar());
        traditional_map.put("binary", new Binary());
        traditional_map.put("hexadecimal", new Hexadecimal());
        traditional_map.put("roman", new RomanNumerals());
        traditional_map.put("ternary", new Ternary());
        traditional_map.put("quaternary", new Quaternary());
        traditional_map.put("alphanumerics", new Alphanumerics());
        traditional_map.put("quinary", new Quinary());
        traditional_map.put("senary", new Senary());
        traditional_map.put("septenary", new Septenary());
        traditional_map.put("octonary", new Octal());
        traditional_map.put("nonary", new Nonary());
        traditional_map.put("words", new Words());
        traditional_map.put("no_repeating_digits", new NoRepeatingDigits());
        traditional_map.put("factoradic", new Factoradic());
        traditional_map.put("wave", new Wave());
        traditional_map.put("base_62", new Base62());
        traditional_map.put("mayan", new Mayan());
        traditional_map.put("permutations", new Permutations());
        traditional_map.put("dates", new Dates());
        traditional_map.put("time", new Time());
        traditional_map.put("military_time", new MilitaryTime());
        traditional_map.put("wait_2", new Wait2());
        traditional_map.put("base_64", new Base64());
        traditional_map.put("coordinates", new Coordinates());
        double_counting_map.put("odds_double", new OddsDouble());
        double_counting_map.put("evens_double", new EvensDouble());
        double_counting_map.put("letters_double", new LettersDouble());
        double_counting_map.put("binary_double", new BinaryDouble());
        double_counting_map.put("hex_double", new HexDouble());
        double_counting_map.put("roman_double", new RomanDouble());
        double_counting_map.put("ternary_double", new TernaryDouble());
        double_counting_map.put("quaternary_double", new QuaternaryDouble());
        double_counting_map.put("alphanumerics_double", new AlphanumericsDouble());
        double_counting_map.put("quinary_double", new QuinaryDouble());
        double_counting_map.put("senary_double", new SenaryDouble());
        double_counting_map.put("septenary_double", new SeptenaryDouble());
        double_counting_map.put("octonary_double", new OctalDouble());
        double_counting_map.put("nonary_double", new NonaryDouble());
        double_counting_map.put("words_double", new WordsDouble());
        double_counting_map.put("no_repeating_digits_double", new NoRepeatingDigitsDouble());
        double_counting_map.put("tug_of_war_double", new TugOfWarDouble());
        double_counting_map.put("factoradic_double", new FactoradicDouble());
        double_counting_map.put("wave_double", new WaveDouble());
        double_counting_map.put("base_62_double", new Base62Double());
        double_counting_map.put("mayan_double", new MayanDouble());
        double_counting_map.put("permutations_double", new PermutationsDouble());
        double_counting_map.put("dates_double", new DatesDouble());
        double_counting_map.put("time_double", new TimeDouble());
        double_counting_map.put("military_time_double", new MilitaryTimeDouble());
        double_counting_map.put("base_64_double", new Base64Double());
        double_counting_map.put("coordinates_double", new CoordinatesDouble());
        no_mistakes_map.put("no_mistakes_double", new NoMistakesDouble());
        no_mistakes_map.put("no_mistakes_or_ban", new NoMistakesOrBan());
        no_mistakes_map.put("no_mistakes_or_ban_double", new NoMistakesOrBanDouble());
        no_mistakes_map.put("no_mistakes_tslc", new NoMistakesTSLC());
        no_mistakes_map.put("fast_tslc", new FastTSLC());
        miscellaneous_map.put("test", new Test());
        miscellaneous_map.put("test2", new Test2());
        miscellaneous_map.put("random_hour", new RandomHour());
        miscellaneous_map.put("tug_of_war_avoid_0", new TugOfWarAvoid0());
        miscellaneous_map.put("countdown", new Countdown());
        miscellaneous_map.put("yoco", new YOCO());
        miscellaneous_map.put("russian_roulette", new RussianRoulette());
        miscellaneous_map.put("1inx", new OneInX());
        miscellaneous_map.put("slow_tslc", new SlowTSLC());
        miscellaneous_map.put("random_minute", new RandomMinute());
        miscellaneous_map.put("wait_x", new WaitX());
        miscellaneous_map.put("no_counting", new NoCounting());
        miscellaneous_map.put("username", new UsernameLength());
        miscellaneous_map.put("incremental_odds", new IncrementalOdds());
        all_threads_map.putAll(favorites_map);
        all_threads_map.putAll(traditional_map);
        all_threads_map.putAll(double_counting_map);
        all_threads_map.putAll(no_mistakes_map);
        all_threads_map.putAll(miscellaneous_map);
        all_threads = all_threads_map.values().stream().toList();
        favorites = favorites_map.values().stream().toList();
        traditional = traditional_map.values().stream().toList();
        double_counting = double_counting_map.values().stream().toList();
        no_mistakes = no_mistakes_map.values().stream().toList();
        miscellaneous = miscellaneous_map.values().stream().toList();
    }
    public static Integer updateAllDatabases() throws IOException, URISyntaxException, InterruptedException {
        for(CountingThread thread:all_threads) {
            thread.updateDatabase();
        }
        return 0;
    }
    public static Map<String, Integer> getHocOverTimePeriodWithFilter(List<Statable> threads, UUID uuid1, UUID uuid2, Function<Statable,Predicate<CountValue>> filter, String filter_name) {
        List<Map<String, Integer>> combined_data = new ArrayList<>();
        for(Statable thread:threads) {
            String filename = "hoc_time_" + filter_name + "_" + uuid1 + "_" + uuid2;
            Map<String, Integer> data = thread.loader.loadStatFromBytes(filename);
            if (data != null) {
                combined_data.add(data);
                continue;
            }
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            Map<String, Integer> hoc = new HashMap<>(2 * Counters.getCounters().size());
            while (counts.hasNext()) {
                CountValue count = counts.next();
                if (!filter.apply(thread).test(count)) continue;
                String author = count.getAuthor();
                hoc.compute(author, (k, v) -> v == null ? 1 : v + 1);
            }
            thread.loader.saveStatToBytes(hoc, filename);
            combined_data.add(hoc);
        }
        return Util.addIntMaps(combined_data);
    }
    public static Map<String, Integer> hocOverTimePeriod(List<Statable> threads, UUID uuid1, UUID uuid2) {
        return getHocOverTimePeriodWithFilter(threads, uuid1, uuid2, l->(k->true), "standard");
    }
    public static Map<String, Integer> cappedHoCTime(List<Statable> threads, UUID uuid1, UUID uuid2, int cap) {
        List<Map<String, Integer>> data = new ArrayList<>();
        if(cap<=0) return hocOverTimePeriod(threads, uuid1, uuid2);
        for(Statable thread:threads) {
            Map<String, Integer> hoc = hocOverTimePeriod(List.of(thread), uuid1, uuid2);
            hoc.forEach((k,v)->{
                if(v>cap) hoc.put(k,cap);
            });
            data.add(hoc);
        }
        return Util.addIntMaps(data);
    }
    public static Map<String, Integer> combinedKParts(List<Statable> threads, int start_k, int end_k, int gets_per_k) {
        List<Map<String, Integer>> data = new ArrayList<>();
        for(Statable thread:threads) {
            List<StatLoader.K_HOC> all_k_hocs = thread.generateAllKHocs(start_k, end_k, gets_per_k);
            Map<String, Integer> k_parts = new HashMap<>();
            for(StatLoader.K_HOC k_hoc:all_k_hocs) {
                for(String user:k_hoc.hoc().keySet()) {
                    k_parts.put(user, k_parts.getOrDefault(user,0)+1);
                }
            }
            data.add(k_parts);
        }
        return Util.addIntMaps(data);
    }
    public static Map<String, Integer> combinedDayParts(List<Statable> threads, String start_date, String end_date, ParticipationDuration mode, boolean day_counts_combined) {
        List<List<StatLoader.DayHOC>> all_all_day_hocs = day_counts_combined
                ? List.of(combined_all_day_hocs(threads, start_date, end_date, mode))
                : threads.stream().map(thread -> combined_all_day_hocs(List.of(thread),start_date,end_date,mode)).toList();
        List<Map<String, Integer>> combined_day_parts = new ArrayList<>();
        for(List<StatLoader.DayHOC> all_day_hocs:all_all_day_hocs) {
            Map<String, Integer> day_parts = new HashMap<>();
            for(StatLoader.DayHOC day_hoc:all_day_hocs) {
                for(String user:day_hoc.hoc().keySet()) {
                    day_parts.put(user, day_parts.getOrDefault(user, 0)+1);
                }
            }
            combined_day_parts.add(day_parts);
        }
        return Util.addIntMaps(combined_day_parts);
    }
    public static Map<String, Float> combinedCountsPerDay(List<Statable> threads, String start_date, String end_date, ParticipationDuration mode) {
        List<StatLoader.DayHOC> all_day_hocs = combined_all_day_hocs(threads, start_date, end_date, mode);
        Map<String, Integer> day_parts = new HashMap<>();
        Map<String, Integer> hoc = new HashMap<>();
        for(StatLoader.DayHOC day_hoc:all_day_hocs) {
            for(String user:day_hoc.hoc().keySet()) {
                int current_day_parts = day_parts.getOrDefault(user,0)+1;
                int current_hoc = hoc.getOrDefault(user, 0)+day_hoc.hoc().get(user);
                day_parts.put(user, current_day_parts);
                hoc.put(user, current_hoc);
            }
        }
        Map<String, Float> counts_per_day = new HashMap<>();
        for(String user:hoc.keySet()) {
            counts_per_day.put(user,hoc.get(user)/((float)day_parts.get(user)));
        }
        return counts_per_day;
    }
    public static Map<String, StatLoader.PeakCountsPerDayOutput> combinedPeakCountsPerDay(List<Statable> threads, String start_date, String end_date, ParticipationDuration mode, int min_day_parts) {
        List<StatLoader.DayHOC> all_day_hocs = combined_all_day_hocs(threads,start_date,end_date,mode);
        Map<String, StatLoader.PeakCountsPerDayOutput> peak_counts_per_day = new HashMap<>();
        Map<String, Integer> day_parts = new HashMap<>();
        Map<String, Integer> hoc = new HashMap<>();
        for(StatLoader.DayHOC day_hoc:all_day_hocs) {
            for(String user:day_hoc.hoc().keySet()) {
                int current_day_parts = day_parts.getOrDefault(user,0)+1;
                int current_hoc = hoc.getOrDefault(user, 0)+day_hoc.hoc().get(user);
                day_parts.put(user, current_day_parts);
                hoc.put(user, current_hoc);
                if(current_day_parts>=min_day_parts) {
                    StatLoader.PeakCountsPerDayOutput peak = peak_counts_per_day.getOrDefault(user, new StatLoader.PeakCountsPerDayOutput(0, null, 0, 0));
                    if (((float) current_hoc) / current_day_parts > peak.value()) {
                        peak_counts_per_day.put(user, new StatLoader.PeakCountsPerDayOutput(((float) current_hoc) / current_day_parts, day_hoc.date().toString(), current_day_parts, current_hoc));
                    }
                }
            }
        }
        return peak_counts_per_day;
    }
    private static Map<String, Integer> getHourParts(CountIterator counts, int duration_seconds, String filename) {
        if(duration_seconds<=0 || 3600%duration_seconds!=0) duration_seconds = 3600;
        Map<String, Integer> data = counts.getDefaultThread().loader.loadStatFromBytes(filename);
        if(data != null) return data;
        Instant moment = Instant.ofEpochMilli(0);
        Map<String, Integer> hoc = new HashMap<>();
        Set<String> current_day_parts = new HashSet<>();
        counts.init();
        while(counts.hasNext()) {
            CountValue count = counts.next();
            Instant new_moment = Instant.ofEpochMilli(count.getUUID().getTime()/10000);
            if (new_moment.toEpochMilli()-duration_seconds*1000L > moment.toEpochMilli()) {
                current_day_parts = new HashSet<>();
                moment = new_moment.minusMillis(new_moment.toEpochMilli()%(duration_seconds*1000L));
            }
            String author = count.getAuthor();
            if (current_day_parts.contains(author)) continue;
            current_day_parts.add(author);
            int current = hoc.getOrDefault(author, 0);
            hoc.put(author, current+1);
        }
        counts.getDefaultThread().loader.saveStatToBytes(hoc, filename);
        return hoc;
    }
    public static Map<String, Integer> hourParts(List<Statable> threads, UUID uuid1, UUID uuid2, int duration_seconds, boolean thread_stats_combined) {
        String filename = "hourparts_"+uuid1+"_"+uuid2+"_"+duration_seconds+"_"+thread_stats_combined+"_"+threads.hashCode();
        if(thread_stats_combined) {
            CombinedCountTimeIterator counts = new CombinedCountTimeIterator(threads, uuid1, uuid2);
            return getHourParts(counts, duration_seconds, filename);
        }
        List<Map<String, Integer>> data = new ArrayList<>();
        for(Statable thread:threads) {
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2);
            data.add(getHourParts(counts, duration_seconds, filename));
        }
        return Util.addIntMaps(data);
    }
    public static Map<String, StatLoader.Participation> combinedParticipation(List<Statable> threads, UUID uuid1, UUID uuid2) {
        List<Map<String, StatLoader.Participation>> data = new ArrayList<>();
        for(Statable thread:threads) {
            String filename = "participation_"+uuid1+"_"+uuid2;
            Map<String, StatLoader.Participation> data2 = thread.loader.loadStatFromBytes(filename);
            if(data2 != null) {
                data.add(data2);
                continue;
            }
            Map<String, Integer> hoc = new HashMap<>();
            Set<String> current_k_parts = new HashSet<>();
            Set<String> current_day_parts = new HashSet<>();
            Map<String, Integer> day_parts = new HashMap<>();
            Map<String, Integer> k_parts = new HashMap<>();
            Map<String, Integer> gets = new HashMap<>();
            Map<String, Integer> assists = new HashMap<>();
            List<Integer> get_values = thread.getGetValues();
            get_values.add(Integer.MAX_VALUE);
            int current_get = 0;
            long current_day_timestamp = 0;
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            while(counts.hasNext()) {
                CountValue count = counts.next();
                String author = count.getAuthor();
                long timestamp = count.getUUID().getTime();
                hoc.compute(author, (k,v) -> v==null?1:v+1);
                while(count.getValidCountNumber() > get_values.get(current_get)) {
                    current_get++;
                    current_k_parts = new HashSet<>();
                }
                if(timestamp > current_day_timestamp) {
                    TimeUnit current_day = new TimeUnit(count.getUUID(), thread.timezone, ParticipationDuration.DAY).addTime(1);
                    current_day_timestamp = current_day.timestamp(thread.timezone);
                    current_day_parts = new HashSet<>();
                }
                if(count.getValidCountNumber() == get_values.get(current_get)) {
                    gets.compute(author, (k,v) -> v==null?1:v+1);
                }
                if(count.getValidCountNumber() == get_values.get(current_get)-1) {
                    assists.compute(author, (k,v) -> v==null?1:v+1);
                }
                if(!current_k_parts.contains(author)) {
                    current_k_parts.add(author);
                    k_parts.compute(author, (k,v) -> v==null?1:v+1);
                }
                if(!current_day_parts.contains(author)) {
                    current_day_parts.add(author);
                    day_parts.compute(author, (k,v) -> v==null?1:v+1);
                }
            }
            Map<String, StatLoader.Participation> participation = new HashMap<>();
            for(String user:hoc.keySet()) {
                int user_hoc = hoc.get(user);
                int user_gets = gets.getOrDefault(user, 0);
                int user_assists = assists.getOrDefault(user, 0);
                int user_k_parts = k_parts.get(user);
                int user_day_parts = day_parts.get(user);
                participation.put(user, new StatLoader.Participation(user_hoc, user_gets, user_assists, user_k_parts, user_day_parts));
            }
            thread.loader.saveStatToBytes(participation, filename);
            data.add(participation);
        }
        return Util.addParticipationMaps(data);
    }
    public static Map<String, Integer> combinedGets(List<Statable> threads, int start, int end, int gets_per_k, int offset) {
        if(start > end) return new HashMap<>();
        if(start < 1) start = 1;
        List<Map<String, Integer>> get_data = new ArrayList<>();
        for(Statable thread:threads) {
            String filename = "get_hoc_"+start+"_"+end+"_"+gets_per_k+"_"+offset;
            Map<String, Integer> data = thread.loader.loadStatFromBytes(filename);
            if(data != null) {
                get_data.add(data);
                continue;
            }
            List<CountValue> gets = thread.getCountsOffsetFromList(thread.getGetValues(), offset);
            Map<String, Integer> get_hoc = new HashMap<>();
            for(int i=start*gets_per_k-1;i<Math.min(gets.size(),end*gets_per_k-1);i+=gets_per_k) {
                String author = gets.get(i).getAuthor();
                int current = get_hoc.getOrDefault(author, 0);
                get_hoc.put(author, current+1);
            }
            thread.loader.saveStatToBytes(get_hoc, filename);
            get_data.add(get_hoc);
        }
        return Util.addIntMaps(get_data);
    }
    public static Map<String, Integer> combinedDayHoc(List<Statable> threads, String date, ParticipationDuration mode) {
        TimeUnit start = new TimeUnit(date, mode);
        List<StatLoader.DayHOC> day_hoc = combined_all_day_hocs(threads, start.toString(), start.addTime(1).toString(), mode);
        if(day_hoc.size()==0) return new HashMap<>();
        return day_hoc.get(0).hoc();
    }
    public static Map<String, StatLoader.FirstCountThread> overallFirstCounts(List<Statable> threads, UUID uuid1) {
        Map<String, StatLoader.FirstCountThread> first_counts = new HashMap<>();
        for(Statable thread: threads) {
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, new UUID("99999999-9999-4999-a999-999999999999")).init();
            Map<String, CountValue> first_counts_in_thread = new HashMap<>();
            while (counts.hasNext()) {
                CountValue count = counts.next();
                String author = count.getAuthor();
                first_counts_in_thread.putIfAbsent(author, count);
            }
            for(String user: first_counts_in_thread.keySet()) {
                if(!first_counts.containsKey(user) || first_counts.get(user).count().getUUID().getTime() > first_counts_in_thread.get(user).getUUID().getTime()) {
                    first_counts.put(user, new StatLoader.FirstCountThread(first_counts_in_thread.get(user), thread));
                }
            }
        }
        return first_counts;
    }
    public static Map<String, StatLoader.FirstCountThread> overallLastCounts(List<Statable> threads, UUID uuid1) {
        Map<String, StatLoader.FirstCountThread> first_counts = new HashMap<>();
        for(Statable thread: threads) {
            CountTimeIterator counts = new CountTimeIterator(thread, new UUID("00000000-0000-1000-0000-000000000000"), uuid1).init();
            Map<String, CountValue> first_counts_in_thread = new HashMap<>();
            while(counts.hasNext()) {
                CountValue count = counts.next();
                String author = count.getAuthor();
                first_counts_in_thread.put(author, count);
            }
            for(String user: first_counts_in_thread.keySet()) {
                if(!first_counts.containsKey(user) || first_counts.get(user).count().getUUID().getTime() < first_counts_in_thread.get(user).getUUID().getTime()) {
                    first_counts.put(user, new StatLoader.FirstCountThread(first_counts_in_thread.get(user), thread));
                }
            }
        }
        return first_counts;
    }
    public static Map<String, StatLoader.Medal> combinedMedals(List<Statable> threads, String start_date, String end_date, ParticipationDuration mode, boolean day_counts_combined) {
        List<List<StatLoader.DayHOC>> all_all_day_hocs = day_counts_combined
                ? List.of(combined_all_day_hocs(threads, start_date, end_date, mode))
                : threads.stream().map(thread -> combined_all_day_hocs(List.of(thread),start_date,end_date,mode)).toList();
        List<Map<String, StatLoader.Medal>> combined_medal_hocs = new ArrayList<>();
        for(List<StatLoader.DayHOC> all_day_hocs: all_all_day_hocs) {
            List<Map<String, Integer>> hocs_without_dates = new ArrayList<>();
            for(StatLoader.DayHOC day_hoc:all_day_hocs) {
                hocs_without_dates.add(day_hoc.hoc());
            }
            combined_medal_hocs.add(medals(hocs_without_dates));
        }
        return Util.addMedalMaps(combined_medal_hocs);
    }
    public static Map<String, StatLoader.Medal> combinedKMedals(List<Statable> threads, int start_k, int end_k, int gets_per_k) {
        List<Map<String, StatLoader.Medal>> medal_hoc = new ArrayList<>();
        for(Statable thread:threads) {
            List<StatLoader.K_HOC> all_k_hocs = thread.generateAllKHocs(start_k, end_k, gets_per_k);
            List<Map<String, Integer>> hocs_without_ks = new ArrayList<>();
            for(StatLoader.K_HOC k_hoc:all_k_hocs) {
                hocs_without_ks.add(k_hoc.hoc());
            }
            medal_hoc.add(medals(hocs_without_ks));
        }
        return Util.addMedalMaps(medal_hoc);
    }
    private static Map<String, StatLoader.Medal> medals(List<Map<String, Integer>> hocs) {
        HashMap<String, int[]> medals = new HashMap<>();
        for(Map<String, Integer> k_hoc:hocs) {
            List<List<String>> medalists = getMedalists(k_hoc, 3);
            for(int i=0;i<medalists.size();i++) {
                for(String user:medalists.get(i)) {
                    int[] current = medals.computeIfAbsent(user, k -> new int[]{0,0,0});
                    current[i]++;
                    medals.put(user, current);
                }
            }
        }
        HashMap<String, StatLoader.Medal> medal_hoc = new HashMap<>();
        for(String user:medals.keySet()) {
            int[] current = medals.get(user);
            medal_hoc.put(user, new StatLoader.Medal(current[0] ,current[1], current[2]));
        }
        return medal_hoc;
    }
    public static Map<String, StatLoader.ExactaOutput> trifecta(UUID uuid1, UUID uuid2) {
        String filename = "trifecta_"+uuid1+"_"+uuid2;
        Map<String, StatLoader.ExactaOutput> data = CombinedStats.all_threads_map.get("main").loader.loadStatFromBytes(filename);
        if(data != null) return data;
        Statable main = CombinedStats.all_threads_map.get("main");
        Statable wols = CombinedStats.all_threads_map.get("slow");
        Statable bars = CombinedStats.all_threads_map.get("bars");
        Map<String, int[]> exactas = new HashMap<>();
        TrifectaIterator count_generator = new TrifectaIterator(List.of(main, bars, wols), uuid1, uuid2).init();
        CountValue[] counts;
        while((counts = count_generator.next()) != null) {
            check_trifecta(counts, exactas);
        }
        Map<String, StatLoader.ExactaOutput> output = new HashMap<>();
        for(String user:exactas.keySet()) {
            int[] current = exactas.get(user);
            if(current[4]>0 || current[0] > 0) {
                output.put(user, new StatLoader.ExactaOutput(current[0], current[1], current[2], current[3], current[4]));
            }
        }
        CombinedStats.all_threads_map.get("main").loader.saveStatToBytes(output, filename);
        return output;
    }
    private static void check_trifecta(CountValue[] counts, Map<String, int[]> exactas) {
        Map<String, Integer> freq_table = new HashMap<>();
        for(int i=0;i<counts.length;i++) {
            if(counts[i] != null) {
                freq_table.put(counts[i].getAuthor(), (1<<i)+freq_table.getOrDefault(counts[i].getAuthor(), 0));
            }
        }
        for(String user:freq_table.keySet()) {
            //Trifecta, Chalupa Exacta, poonxacta, trejexacta, total exactas
            int[] current = exactas.computeIfAbsent(user, k->new int[5]);
            switch(freq_table.get(user)) {
                case 7 -> current[0]++;
                case 6 -> {current[1]++;current[4]++;}
                case 5 -> {current[3]++;current[4]++;}
                case 3 -> {current[2]++;current[4]++;}
            }
        }
    }
    public static List<StatLoader.ExactaDayHOC> exacta_day_hoc(String start_date, String end_date, ParticipationDuration mode) {
        TimeUnit date1 = new TimeUnit(start_date, mode);
        TimeUnit date2 = new TimeUnit(end_date, mode);
        Statable main = CombinedStats.all_threads_map.get("main");
        Statable wols = CombinedStats.all_threads_map.get("slow");
        Statable bars = CombinedStats.all_threads_map.get("bars");
        TrifectaIterator counts_generator = new TrifectaIterator(List.of(main, bars, wols), Util.getUUIDFromDate(date1, main.timezone), Util.getUUIDFromDate(date2, main.timezone)).init();
        System.out.println(Util.getUUIDFromDate(date1, main.timezone)+" "+Util.getUUIDFromDate(date2, main.timezone));
        CountValue[] counts;
        CountValue[] overflow = new CountValue[3];
        List<StatLoader.ExactaDayHOC> all_day_hocs = new ArrayList<>();
        while(date1.isBefore(date2)) {
            Map<String, int[]> hoc = new HashMap<>();
            long end_timestamp = Util.getUUIDFromDate(date1.addTime(1), main.timezone).getTime();
            check_trifecta(overflow, hoc);
            Arrays.fill(overflow, null);
            while((counts = counts_generator.next()) != null) {
                if(counts_generator.getCurrentBar() >= end_timestamp) {
                    System.arraycopy(counts, 0, overflow, 0, counts.length);
                    break;
                }
                check_trifecta(counts, hoc);
            }
            Map<String, StatLoader.ExactaOutput> exactas = new HashMap<>();
            for(String user:hoc.keySet()) {
                int[] current = hoc.get(user);
                if (current[4] > 0 || current[0] > 0) {
                    exactas.put(user, new StatLoader.ExactaOutput(current[0], current[1], current[2], current[3], current[4]));
                }
            }
            all_day_hocs.add(new StatLoader.ExactaDayHOC(date1, exactas));
            date1 = date1.addTime(1);
        }
        return all_day_hocs;
    }
    public static Map<String, StatLoader.ExactaOutput> exacta_day_parts(String start_date, String end_date, ParticipationDuration type) {
        List<StatLoader.ExactaDayHOC> all_day_hocs = exacta_day_hoc(start_date, end_date, type);
        Map<String, StatLoader.ExactaOutput> day_parts = new HashMap<>();
        for (StatLoader.ExactaDayHOC day_hoc : all_day_hocs) {
            for (String user : day_hoc.hoc().keySet()) {
                day_parts.put(user, day_parts.getOrDefault(user, new StatLoader.ExactaOutput(0,0,0,0,0)).add(day_hoc.hoc().get(user).sign()));
            }
        }
        return day_parts;
    }
    /**
     * @param type 0 is trifecta
     *             1 is chalupa exacta,
     *             2 is poonxacta,
     *             3 is trejexacta,
     *             anything else is combined exactas
     * */
    public static Map<String, StatLoader.TimeStreakOutput> exacta_day_streak(int type, String start_date, String end_date, boolean is_current, ParticipationDuration mode) {
        if(type<0 || type > 4) type = 4;
        List<StatLoader.ExactaDayHOC> all_day_hocs = exacta_day_hoc(start_date, end_date, mode);
        Map<String, Integer> current_streak = new HashMap<>();
        Map<String, StatLoader.TimeStreakOutput> best_streak = new HashMap<>();
        TimeUnit current_day = null;
        for(StatLoader.ExactaDayHOC day_hoc:all_day_hocs) {
            current_day = day_hoc.date();
            Set<String> dead_streaks = new HashSet<>(current_streak.keySet());
            for (String user:day_hoc.hoc().keySet()) {
                if(day_hoc.hoc().get(user).asArray()[type] == 0) continue;
                int current = current_streak.getOrDefault(user, 0);
                current_streak.put(user, current + 1);
                dead_streaks.remove(user);
            }
            if(!is_current) {
                for (String user : dead_streaks) {
                    StatLoader.TimeStreakOutput best = best_streak.getOrDefault(user, new StatLoader.TimeStreakOutput(null, null, 0));
                    int current = current_streak.get(user);
                    if (current > best.size()) {
                        best_streak.put(user, new StatLoader.TimeStreakOutput(current_day.subtractTime(current).toString(), current_day.subtractTime(1).toString(), current));
                    }
                    current_streak.remove(user);
                }
            } else {
                for (String user : dead_streaks) {
                    current_streak.remove(user);
                }
            }
        }
        for(String user:current_streak.keySet()) {
            int current = current_streak.get(user);
            StatLoader.TimeStreakOutput best = best_streak.getOrDefault(user, new StatLoader.TimeStreakOutput(null, null, 0));
            if(current > best.size()) {
                best_streak.put(user, new StatLoader.TimeStreakOutput(current_day.subtractTime(current-1).toString(), current_day.toString(), current));
            }
        }
        return best_streak;
    }
    /**
     * @param type 0 is trifecta
     *             1 is chalupa exacta,
     *             2 is poonxacta,
     *             3 is trejexacta,
     *             anything else is combined exactas
     * */
    public static List<StatLoader.NonUniqueTimeStreak> non_unique_exacta_day_streak(int type, String start_date, String end_date, ParticipationDuration mode) {
        if(type<0 || type > 4) type = 4;
        List<StatLoader.ExactaDayHOC> all_day_hocs = exacta_day_hoc(start_date, end_date, mode);
        Map<String, Integer> current_streak = new HashMap<>();
        List<StatLoader.NonUniqueTimeStreak> day_streaks = new ArrayList<>();
        TimeUnit current_day = null;
        for(StatLoader.ExactaDayHOC day_hoc:all_day_hocs) {
            current_day = day_hoc.date();
            Set<String> dead_streaks = new HashSet<>(current_streak.keySet());
            for (String user:day_hoc.hoc().keySet()) {
                if(day_hoc.hoc().get(user).asArray()[type] == 0) continue;
                int current = current_streak.getOrDefault(user, 0);
                current_streak.put(user, current + 1);
                dead_streaks.remove(user);
            }
            for (String user : dead_streaks) {
                int current = current_streak.get(user);
                day_streaks.add(new StatLoader.NonUniqueTimeStreak(user, current_day.subtractTime(current).toString(), current_day.subtractTime(1).toString(), current));
                current_streak.remove(user);
            }
        }
        for(String user:current_streak.keySet()) {
            int current = current_streak.get(user);
            day_streaks.add(new StatLoader.NonUniqueTimeStreak(user, current_day.subtractTime(current-1).toString(), current_day.toString(), current));
        }
        return day_streaks;
    }
    public static List<StatLoader.DayHOC> combined_all_day_hocs(List<Statable> threads, String start_date, String end_date, ParticipationDuration mode) {
        List<List<StatLoader.DayHOC>> all_all_day_hocs = new ArrayList<>();
        for(Statable thread: threads) {
            TimeUnit date1 = new TimeUnit(start_date, mode);
            TimeUnit date2 = new TimeUnit(end_date, mode);
            TimeUnit first_day = thread.first_day(mode, thread.timezone);
            TimeUnit last_day = thread.last_day(mode, thread.timezone);
            long max_index = TimeUnit.timeBetween(first_day, last_day, mode);
            int start_index = (int)Math.max(0,Math.min(max_index, TimeUnit.timeBetween(first_day, date1, mode)));
            int end_index = (int)Math.min(max_index,Math.max(0,TimeUnit.timeBetween(first_day, date2, mode)));
            if(start_index>end_index) start_index = end_index;
            date1 = first_day;
            date2 = last_day;
            String filename = "all_"+mode.toString().toLowerCase()+"_hocs";
            List<StatLoader.DayHOC> data = thread.loader.loadStatFromBytes(filename);
            if(data != null) {
                all_all_day_hocs.add(data.subList(start_index, end_index));
                continue;
            }
            List<StatLoader.DayHOC> all_day_hocs = new ArrayList<>();
            CountTimeIterator counts = new CountTimeIterator(thread, Util.getUUIDFromDate(date1, thread.timezone), Util.getUUIDFromDate(date2, thread.timezone)).init();
            String overflow = null;
            long overflow_timestamp = 0;
            while(date1.isBefore(date2)) {
                Map<String, Integer> hoc = new HashMap<>();
                long end_timestamp = Util.getUUIDFromDate(date1.addTime(1), thread.timezone).getTime();
                if(overflow != null && overflow_timestamp < end_timestamp) {
                    int current = hoc.getOrDefault(overflow, 0);
                    hoc.put(overflow, current+1);
                    overflow = null;
                }
                while(counts.hasNext() && overflow==null) {
                    CountValue count = counts.next();
                    String author = count.getAuthor();
                    if(count.getUUID().getTime() >= end_timestamp) {
                        overflow = author;
                        overflow_timestamp = count.getUUID().getTime();
                        break;
                    }
                    int current = hoc.getOrDefault(author, 0);
                    hoc.put(author, current+1);
                }
                all_day_hocs.add(new StatLoader.DayHOC(date1, hoc));
                date1 = date1.addTime(1);
            }
            thread.loader.saveStatToBytes(all_day_hocs, filename);
            all_all_day_hocs.add(all_day_hocs.subList(start_index, end_index));
        }
        Map<String, List<Map<String, Integer>>> day_hocs_by_date = new HashMap<>();
        for(List<StatLoader.DayHOC> all_day_hocs: all_all_day_hocs) {
            for(StatLoader.DayHOC day_hoc: all_day_hocs) {
                List<Map<String, Integer>> current = day_hocs_by_date.computeIfAbsent(day_hoc.date().toString(), k -> new ArrayList<>());
                current.add(day_hoc.hoc());
            }
        }
        List<StatLoader.DayHOC> combined_day_hocs = new ArrayList<>();
        for(String day:day_hocs_by_date.keySet()) {
            combined_day_hocs.add(new StatLoader.DayHOC(new TimeUnit(day, mode), Util.addIntMaps(day_hocs_by_date.get(day))));
        }
        combined_day_hocs.sort(Comparator.comparing(StatLoader.DayHOC::date));
        return combined_day_hocs;
    }
    public static Map<String, Integer> combined_total_count_day_hocs(List<Statable> threads, String start_date, String end_date, ParticipationDuration mode) {
        List<StatLoader.DayHOC> all_day_hocs = combined_all_day_hocs(threads, start_date, end_date, mode);
        Map<String, Integer> total_count_day_hocs = new HashMap<>();
        for(StatLoader.DayHOC day_hoc:all_day_hocs) {
            total_count_day_hocs.put(day_hoc.date().toString(), Util.sumIntMapValues(day_hoc.hoc()));
        }
        return total_count_day_hocs;
    }
    public static Map<String, StatLoader.TimeStreakOutput> combined_day_streak(List<Statable> threads, String start_date, String end_date, int min_counts, ParticipationDuration mode, boolean is_current, boolean exact) {
        List<StatLoader.DayHOC> all_day_hocs = combined_all_day_hocs(threads, start_date, end_date, mode);
        Map<String, Integer> current_streak = new HashMap<>();
        Map<String, StatLoader.TimeStreakOutput> best_streak = new HashMap<>();
        TimeUnit current_day = null;
        for(StatLoader.DayHOC day_hoc:all_day_hocs) {
            current_day = day_hoc.date();
            Set<String> dead_streaks = new HashSet<>(current_streak.keySet());
            for (String user:day_hoc.hoc().keySet()) {
                if(!exact && day_hoc.hoc().get(user)>=min_counts || exact && day_hoc.hoc().get(user)==min_counts) {
                    int current = current_streak.getOrDefault(user, 0);
                    current_streak.put(user, current + 1);
                    dead_streaks.remove(user);
                }
            }
            if(!is_current) {
                for (String user : dead_streaks) {
                    StatLoader.TimeStreakOutput best = best_streak.getOrDefault(user, new StatLoader.TimeStreakOutput(null, null, 0));
                    int current = current_streak.get(user);
                    if (current > best.size()) {
                        best_streak.put(user, new StatLoader.TimeStreakOutput(current_day.subtractTime(current).toString(), current_day.subtractTime(1).toString(), current));
                    }
                    current_streak.remove(user);
                }
            } else {
                for (String user : dead_streaks) {
                    current_streak.remove(user);
                }
            }
        }
        for(String user:current_streak.keySet()) {
            int current = current_streak.get(user);
            StatLoader.TimeStreakOutput best = best_streak.getOrDefault(user, new StatLoader.TimeStreakOutput(null, null, 0));
            if(current > best.size()) {
                best_streak.put(user, new StatLoader.TimeStreakOutput(current_day.subtractTime(current-1).toString(), current_day.toString(), current));
            }
        }
        return best_streak;
    }
    public static List<StatLoader.NonUniqueTimeStreak> combined_day_streak_non_unique(List<Statable> threads, String start_date, String end_date, int min_counts, ParticipationDuration mode) {
        List<StatLoader.DayHOC> all_day_hocs = combined_all_day_hocs(threads, start_date, end_date, mode);
        Map<String, Integer> current_streak = new HashMap<>();
        List<StatLoader.NonUniqueTimeStreak> day_streaks = new ArrayList<>();
        TimeUnit current_day = null;
        for(StatLoader.DayHOC day_hoc:all_day_hocs) {
            current_day = day_hoc.date();
            Set<String> dead_streaks = new HashSet<>(current_streak.keySet());
            for (String user:day_hoc.hoc().keySet()) {
                if(day_hoc.hoc().get(user)>=min_counts) {
                    int current = current_streak.getOrDefault(user, 0);
                    current_streak.put(user, current + 1);
                    dead_streaks.remove(user);
                }
            }
            for (String user : dead_streaks) {
                int current = current_streak.get(user);
                day_streaks.add(new StatLoader.NonUniqueTimeStreak(user, current_day.subtractTime(current).toString(), current_day.subtractTime(1).toString(), current));
                current_streak.remove(user);
            }
        }
        for(String user:current_streak.keySet()) {
            int current = current_streak.get(user);
            day_streaks.add(new StatLoader.NonUniqueTimeStreak(user, current_day.subtractTime(current-1).toString(), current_day.toString(), current));
        }
        return day_streaks;
    }
    public static Map<String, Integer> combined_palindromes(List<Statable> threads, UUID uuid1, UUID uuid2) {
        return getHocOverTimePeriodWithFilter(threads, uuid1, uuid2, thread -> (thread::isPalindrome),"palindrome");
    }
    public static Map<String, Integer> combined_repdigits(List<Statable> threads, UUID uuid1, UUID uuid2) {
        return getHocOverTimePeriodWithFilter(threads, uuid1, uuid2, thread -> (thread::isRepDigit),"repdigits");
    }
    public static Map<String, Integer> combined_n_rep(List<Statable> threads, UUID uuid1, UUID uuid2, int n) {
        return getHocOverTimePeriodWithFilter(threads, uuid1, uuid2, thread -> (num -> thread.isNRep(num, n)),n+"_rep");
    }
    public static Map<String, Integer> combined_base_n(List<Statable> threads, UUID uuid1, UUID uuid2, int n) {
        return getHocOverTimePeriodWithFilter(threads, uuid1, uuid2, thread -> (num -> thread.isBaseN(num, n)),"base_"+n);
    }
    public static Map<String, Integer> combined_days_over_n_counts(List<Statable> threads, String start_date, String end_date, int n, ParticipationDuration mode, boolean day_counts_combined) {
        List<List<StatLoader.DayHOC>> all_all_day_hocs = day_counts_combined
                ? List.of(combined_all_day_hocs(threads, start_date, end_date, mode))
                : threads.stream().map(thread -> combined_all_day_hocs(List.of(thread),start_date,end_date,mode)).toList();
        List<Map<String, Integer>> combined_days_over_n_counts = new ArrayList<>();
        for(List<StatLoader.DayHOC> all_day_hocs:all_all_day_hocs) {
            Map<String, Integer> days_over_n_counts = new HashMap<>();
            for(StatLoader.DayHOC day_hoc:all_day_hocs) {
                for(String user:day_hoc.hoc().keySet()) {
                    if(day_hoc.hoc().get(user)>=n) {
                        days_over_n_counts.put(user, days_over_n_counts.getOrDefault(user, 0)+1);
                    }
                }
            }
            combined_days_over_n_counts.add(days_over_n_counts);
        }
        return Util.addIntMaps(combined_days_over_n_counts);
    }

    public static Map<String, Integer> thread_days_over_n_counts(List<Statable> threads, String start_date, String end_date, int n, ParticipationDuration mode) {
        Map<String, Integer> total_days = new HashMap<>();
        for(Statable thread:threads) {
            Map<String, Integer> days_over_n_counts = combined_total_count_day_hocs(List.of(thread), start_date, end_date, mode);
            for(String date:days_over_n_counts.keySet()) {
                if(days_over_n_counts.get(date) > n) {
                    int current = total_days.getOrDefault(thread.getName(), 0);
                    total_days.put(thread.getName(),current+1);
                }
            }
        }
        return total_days;
    }
    public static Map<String, StatLoader.UniqueTopCountsOutput> combined_unique_top_counts_per_day(List<Statable> threads, String start_date, String end_date, ParticipationDuration mode, boolean day_counts_combined) {
        List<List<StatLoader.DayHOC>> all_all_day_hocs = day_counts_combined
                ? List.of(combined_all_day_hocs(threads, start_date, end_date, mode))
                : threads.stream().map(thread -> combined_all_day_hocs(List.of(thread),start_date,end_date,mode)).toList();
        Map<String, StatLoader.UniqueTopCountsOutput> day_records = new HashMap<>();
        for(List<StatLoader.DayHOC> all_day_hocs: all_all_day_hocs) {
            for (StatLoader.DayHOC day_hoc : all_day_hocs) {
                for (String user : day_hoc.hoc().keySet()) {
                    StatLoader.UniqueTopCountsOutput current = day_records.getOrDefault(user, new StatLoader.UniqueTopCountsOutput(0, null));
                    if (day_hoc.hoc().get(user) >= current.value()) {
                        day_records.put(user, new StatLoader.UniqueTopCountsOutput(day_hoc.hoc().get(user), day_hoc.date().toString()));
                    }
                }
            }
        }
        return day_records;
    }
    public static List<StatLoader.NonUniqueTopCountsOutput> combined_non_unique_top_counts_per_day(List<Statable> threads, String start_date, String end_date, ParticipationDuration mode, boolean day_counts_combined) {
        List<List<StatLoader.DayHOC>> all_all_day_hocs = day_counts_combined
                ? List.of(combined_all_day_hocs(threads, start_date, end_date, mode))
                : threads.stream().map(thread -> combined_all_day_hocs(List.of(thread),start_date,end_date,mode)).toList();
        List<StatLoader.NonUniqueTopCountsOutput> day_records = new ArrayList<>();
        for(List<StatLoader.DayHOC> all_day_hocs: all_all_day_hocs) {
            for(StatLoader.DayHOC day_hoc:all_day_hocs) {
                for(String user:day_hoc.hoc().keySet()) {
                    day_records.add(new StatLoader.NonUniqueTopCountsOutput(user, day_hoc.hoc().get(user),day_hoc.date().toString()));
                }
            }
        }
        return day_records;
    }

    public static Map<String, StatLoader.IndividualThreadRecords> day_records_for_individual_threads(List<Statable> threads, String start_date, String end_date, ParticipationDuration mode) {
        Map<String, StatLoader.IndividualThreadRecords> day_records_by_user = new HashMap<>();
        List<String> thread_order = new ArrayList<>();
        for (int i=0;i<threads.size();i++) {
            Map<String, StatLoader.UniqueTopCountsOutput> day_records_by_thread = combined_unique_top_counts_per_day(List.of(threads.get(i)), start_date, end_date, mode,false);
            thread_order.add(threads.get(i).getName());
            for (String user : day_records_by_thread.keySet()) {
                List<StatLoader.UniqueTopCountsOutput> user_day_records = day_records_by_user.computeIfAbsent(user, k -> new StatLoader.IndividualThreadRecords(new ArrayList<>())).records();
                while(user_day_records.size() < i) {
                    user_day_records.add(new StatLoader.UniqueTopCountsOutput(0,null));
                }
                user_day_records.add(day_records_by_thread.getOrDefault(user, new StatLoader.UniqueTopCountsOutput(0, null)));
            }
        }
        for(String user:day_records_by_user.keySet()) {
            List<StatLoader.UniqueTopCountsOutput> user_day_records = day_records_by_user.get(user).records();
            while(user_day_records.size() < threads.size()) {
                user_day_records.add(new StatLoader.UniqueTopCountsOutput(0,null));
            }
        }
        System.out.println(thread_order);
        return day_records_by_user;
    }
    public static Map<String, StatLoader.IndividualThreadRecords> hoc_for_individual_threads(List<Statable> threads, UUID start, UUID end) {
        Map<String, StatLoader.IndividualThreadRecords> day_records_by_user = new HashMap<>();
        List<String> thread_order = new ArrayList<>();
        for (int i=0;i<threads.size();i++) {
            Map<String, Integer> hoc = hocOverTimePeriod(threads.subList(i,i+1), start, end);
            thread_order.add(threads.get(i).getName());
            for (String user : hoc.keySet()) {
                List<StatLoader.UniqueTopCountsOutput> user_day_records = day_records_by_user.computeIfAbsent(user, k -> new StatLoader.IndividualThreadRecords(new ArrayList<>())).records();
                while(user_day_records.size() < i) {
                    user_day_records.add(new StatLoader.UniqueTopCountsOutput(0,null));
                }
                user_day_records.add(new StatLoader.UniqueTopCountsOutput(hoc.getOrDefault(user, 0), null));
            }
        }
        for(String user:day_records_by_user.keySet()) {
            List<StatLoader.UniqueTopCountsOutput> user_day_records = day_records_by_user.get(user).records();
            while(user_day_records.size() < threads.size()) {
                user_day_records.add(new StatLoader.UniqueTopCountsOutput(0,null));
            }
        }
        System.out.println(thread_order);
        return day_records_by_user;
    }
    public static StatLoader.UniqueTopCountsOutput overall_day_record(List<Statable> threads, String user, String start_date, String end_date, ParticipationDuration mode, boolean day_counts_combined) {
        return combined_unique_top_counts_per_day(threads, start_date, end_date, mode, day_counts_combined).get(user);
    }
    private static Map<String, Integer> recordHour(CountIterator counts, int duration_seconds, boolean thread, String filename) {
        if(duration_seconds<=0 || 3600%duration_seconds!=0) duration_seconds = 3600;
        Map<String, Integer> data = counts.getDefaultThread().loader.loadStatFromBytes(filename);
        if(data != null) return data;
        Instant moment = Instant.ofEpochMilli(0);
        Map<String, Integer> hoc = new HashMap<>();
        Map<String, Integer> current_day_parts = new HashMap<>();
        counts.init();
        while(counts.hasNext()) {
            CountValue count = counts.next();
            Instant new_moment = Instant.ofEpochMilli(count.getUUID().getTime()/10000);
            if (new_moment.toEpochMilli()-duration_seconds*1000L > moment.toEpochMilli()) {
                for(String user: current_day_parts.keySet()) {
                    if(current_day_parts.get(user) > hoc.getOrDefault(user, 0)) {
                        hoc.put(user, current_day_parts.get(user));
                    }
                }
                current_day_parts = new HashMap<>();
                moment = new_moment.minusMillis(new_moment.toEpochMilli()%(duration_seconds*1000L));
            }
            String author;
            if(thread) {
                author = LocalDateTime.ofInstant(moment, ZoneId.of(counts.getThread().timezone)).format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
            } else {
                author = count.getAuthor();
            }
            int counted = current_day_parts.getOrDefault(author,0);
            current_day_parts.put(author, counted+1);
        }
        for(String user: current_day_parts.keySet()) {
            if(current_day_parts.get(user) > hoc.getOrDefault(user, 0)) {
                hoc.put(user, current_day_parts.get(user));
            }
        }
        counts.getDefaultThread().loader.saveStatToBytes(hoc, filename);
        return hoc;
    }
    public static Map<String, StatLoader.OverallRecordHour> overall_record_hour(List<Statable> threads, UUID uuid1, UUID uuid2, int duration_seconds,  boolean offset) {
        String filename = "overall_record_hour_"+uuid1+"_"+uuid2+"_"+duration_seconds+"_"+offset;
        Map<String, StatLoader.OverallRecordHour> record_hour= new HashMap<>();
        for(Statable thread:threads) {
            Map<String, Integer> peak_hour = offset
                    ?recordHourOffset(new CountTimeIterator(thread, uuid1, uuid2), duration_seconds, filename)
                    :recordHour(new CountTimeIterator(thread, uuid1, uuid2), duration_seconds, false, filename);
            for(String user:peak_hour.keySet()) {
                int current_best = record_hour.getOrDefault(user, new StatLoader.OverallRecordHour(0,null)).record();
                if(peak_hour.get(user) > current_best) {
                    record_hour.put(user, new StatLoader.OverallRecordHour(peak_hour.get(user), thread.getName()));
                }
            }
        }
        return record_hour;
    }
    public static List<StatLoader.OverallThreadRecordHour> overall_thread_record_hour(List<Statable> threads, UUID uuid1, UUID uuid2, int duration_seconds) {
        String filename = "overall_thread_record_hour_"+uuid1+"_"+uuid2+"_"+duration_seconds;
        List<StatLoader.OverallThreadRecordHour> record_hour= new ArrayList<>();
        for(Statable thread:threads) {
            Map<String, Integer> peak_hour = recordHour(new CountTimeIterator(thread, uuid1, uuid2), duration_seconds, true, filename);
            for(String user:peak_hour.keySet()) {
                record_hour.add(new StatLoader.OverallThreadRecordHour(user, peak_hour.get(user), thread.getName()));
            }
        }
        return record_hour;
    }
    public static Map<String, Integer> combined_record_hour(List<Statable> threads, UUID uuid1, UUID uuid2, int duration_seconds, boolean offset) {
        String filename = "combined_record_hour_"+threads.hashCode()+"_"+uuid1+"_"+uuid2+"_"+duration_seconds+"_"+offset;
        Map<String, Integer> record_hour= new HashMap<>();
        Map<String, Integer> peak_hour = offset
                ?recordHourOffset(new CombinedCountTimeIterator(threads, uuid1, uuid2), duration_seconds, filename)
                :recordHour(new CombinedCountTimeIterator(threads, uuid1, uuid2), duration_seconds, true, filename);
        for(String user:peak_hour.keySet()) {
            int current_best = record_hour.getOrDefault(user, 0);
            if(peak_hour.get(user) > current_best) {
                record_hour.put(user, peak_hour.get(user));
            }
        }
        return record_hour;
    }
    private static Map<String, Integer> recordHourOffset(CountIterator counts, int duration_seconds, String filename) {
        if(duration_seconds<=0 || 3600%duration_seconds!=0) duration_seconds = 3600;
        Map<String, Integer> data = counts.getDefaultThread().loader.loadStatFromBytes(filename);
        if(data != null) return data;
        Map<String, Integer> record_hour = new HashMap<>();
        Map<String, LinkedList<Long>> current_hour_parts = new HashMap<>();
        counts.init();
        while(counts.hasNext()) {
            CountValue count = counts.next();
            String author = count.getAuthor();
            long timestamp = count.getUUID().getTime();
            LinkedList<Long> user_counts = current_hour_parts.computeIfAbsent(author, k->new LinkedList<>());
            user_counts.add(timestamp);
            while(user_counts.getFirst() < timestamp-duration_seconds*10000000L) {
                user_counts.removeFirst();
            }
            int current = record_hour.getOrDefault(author, 0);
            if(user_counts.size() > current) {
                record_hour.put(author, user_counts.size());
            }
        }
        counts.getDefaultThread().loader.saveStatToBytes(record_hour, filename);
        return record_hour;
    }
    public static Map<String, StatLoader.OverallMostDominantNCountsOutput> overall_most_dominant_n_counts(List<Statable> threads, int n, UUID uuid1, UUID uuid2) {
        List<Map<String, StatLoader.OverallMostDominantNCountsOutput>> fastest_n_output = new ArrayList<>();
        for(Statable thread:threads) {
            Map<String, Integer> current_hoc = new HashMap<>();
            Map<String, Integer> hoc_time = new HashMap<>();
            Map<String, int[]> current_record = new HashMap<>();
            int[] current = new int[n];
            String[] users = new String[n];
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            while(counts.hasNext()) {
                CountValue count = counts.next();
                String author = count.getAuthor();
                int number = count.getValidCountNumber();
                int index = number%n;
                int current_counts = current_hoc.getOrDefault(author, 0);
                current_hoc.put(author, current_counts+1);
                int total_counts = hoc_time.getOrDefault(author, 0);
                hoc_time.put(author, total_counts+1);
                if(number > n) {
                    int not_current_counts = current_hoc.get(users[index]);
                    current_hoc.put(users[index], not_current_counts-1);
                }
                int[] current_speed = current_record.computeIfAbsent(author, k->new int[5]);
                current_counts = current_hoc.getOrDefault(author, 0);
                if(current_counts > current_speed[0]) {
                    current_speed[0] = current_counts;
                    current_speed[1] = hoc_time.get(author) - current_counts;
                    current_speed[2] = hoc_time.get(author);
                    current_speed[3] = current[index] + 1; //Will equal 1 if number < n
                    current_speed[4] = Math.max(number, n);
                }
                current[index] = number;
                users[index] = author;
            }
            Map<String, StatLoader.OverallMostDominantNCountsOutput> fastest_n = new HashMap<>();
            for(String user:current_record.keySet()) {
                int[] record = current_record.get(user);
                fastest_n.put(user, new StatLoader.OverallMostDominantNCountsOutput(record[1], record[2], record[3], record[4], record[0],thread.getName()));
            }
            if(fastest_n.size()==0) continue;
            fastest_n_output.add(fastest_n);
        }
        return Util.optimizeMaps(fastest_n_output, true);
    }
    public static Map<String, StatLoader.OverallPerfectStreak> overall_perfect_streak(List<Statable> threads, UUID uuid1, UUID uuid2, int counts_between_replies) {
        Map<String, StatLoader.OverallPerfectStreak> streaks = new HashMap<>();
        for(Statable thread:threads) {
            Map<String, Integer> previous_counts = new HashMap<>();
            Map<String, Integer> current_streak = new HashMap<>();
            if(counts_between_replies<1) throw new IllegalArgumentException("counts_between_replies must be greater than or equal to 1");
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            while(counts.hasNext()) {
                CountValue count = counts.next();
                String author = count.getAuthor();
                int current = current_streak.getOrDefault(author, 0);
                if(count.getValidCountNumber() - previous_counts.getOrDefault(author, 0) == counts_between_replies) {
                    current_streak.put(author, current+1);
                } else {
                    int best_streak = streaks.containsKey(author) ? streaks.get(author).streak() : 0;
                    if(current > best_streak) {
                        int end = previous_counts.get(author);
                        streaks.put(author, new StatLoader.OverallPerfectStreak(current,end-(current-1)*counts_between_replies,end,thread.getName()));
                    }
                    current_streak.put(author, 1);
                }
                previous_counts.put(author, count.getValidCountNumber());
            }
            for(String user:current_streak.keySet()) {
                int best_streak = streaks.containsKey(user) ? streaks.get(user).streak() : 0;
                int current = current_streak.getOrDefault(user, 0);
                if(current > best_streak) {
                    int end = previous_counts.get(user);
                    streaks.put(user, new StatLoader.OverallPerfectStreak(current,end-(current-1)*counts_between_replies,end, thread.getName()));
                }
            }
        }
        return streaks;
    }

    public static GenericStatOutput overall_perfect_streak_v2(List<Statable> threads, UUID uuid1, UUID uuid2, int counts_between_replies, boolean unique) {
        List<CollectionContainer<String, StatOutput>> streaks = new ArrayList<>();
        int MIN_STREAK = 10;
        for(Statable thread:threads) {
            String filename = "overall_perfect_streak_"+counts_between_replies+"_"+uuid1+"_"+uuid2;
            CollectionContainer<String, StatOutput> data = thread.loader.loadStatFromBytes(filename);
            if(data != null) {
                streaks.add(data);
                continue;
            }
            CollectionContainer<String, StatOutput> thread_streaks = CollectionFactory.createCollection(unique);
            Map<String, Integer> previous_counts = new HashMap<>();
            Map<String, Integer> current_streak = new HashMap<>();
            if(counts_between_replies<1) throw new IllegalArgumentException("counts_between_replies must be greater than or equal to 1");
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            while(counts.hasNext()) {
                CountValue count = counts.next();
                String author = count.getAuthor();
                int current = current_streak.getOrDefault(author, 0);
                if(count.getValidCountNumber() - previous_counts.getOrDefault(author, 0) == counts_between_replies) {
                    current_streak.put(author, current+1);
                } else {
                    int best_streak = thread_streaks.containsKey(author) ? thread_streaks.get(author).streak : 0;
                    if((!unique && current>=MIN_STREAK) || (unique && current > best_streak)) {
                        int end = previous_counts.get(author);
                        thread_streaks.add(author, new StatOutput.Builder().setUser1(author).setStreak(current).setStart(end-(current-1)*counts_between_replies).setEnd(end).setStartThread(thread.getName()).build());
                    }
                    current_streak.put(author, 1);
                }
                previous_counts.put(author, count.getValidCountNumber());
            }
            for(String user:current_streak.keySet()) {
                int best_streak = thread_streaks.containsKey(user) ? thread_streaks.get(user).streak : 0;
                int current = current_streak.getOrDefault(user, 0);
                if((!unique && current>=MIN_STREAK) || (unique && current > best_streak)) {
                    int end = previous_counts.get(user);
                    thread_streaks.add(user, new StatOutput.Builder().setUser1(user).setStreak(current).setStart(end-(current-1)*counts_between_replies).setEnd(end).setStartThread(thread.getName()).build());
                }
            }
            thread.loader.saveStatToBytes(thread_streaks, filename);
            streaks.add(thread_streaks);
        }
        String[] table_headers = new String[]{"User", "Streak", "Thread", "Start", "End"};
        String[] printOrder = new String[]{"user1", "streak","start_thread","start","end"};
        return new GenericStatOutput(StatComparators.streak_comparator, table_headers, printOrder, Util.optimizeCollection(streaks, StatComparators.streak_comparator, true));
    }


    public static List<StatLoader.OverallNonUniquePerfectStreak> overall_non_unique_perfect_streak(List<Statable> threads, UUID uuid1, UUID uuid2, int counts_between_replies) {
        List<StatLoader.OverallNonUniquePerfectStreak> perfect_streak_output = new ArrayList<>();
        for(Statable thread:threads) {
            int MIN_STREAK = 10;
            List<StatLoader.OverallNonUniquePerfectStreak> streaks = new ArrayList<>();
            Map<String, Integer> previous_counts = new HashMap<>();
            Map<String, Integer> current_streak = new HashMap<>();
            if(counts_between_replies<1) throw new IllegalArgumentException("counts_between_replies must be greater than 1");
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            while(counts.hasNext()) {
                CountValue count = counts.next();
                String author = count.getAuthor();
                int current = current_streak.getOrDefault(author, 0);
                if(count.getValidCountNumber() - previous_counts.getOrDefault(author, 0) == counts_between_replies) {
                    current_streak.put(author, current+1);
                } else {
                    if(current >= MIN_STREAK) {
                        int end = previous_counts.get(author);
                        streaks.add(new StatLoader.OverallNonUniquePerfectStreak(new String[]{author},current,end-(current-1)*counts_between_replies,end, thread.getName()));
                    }
                    current_streak.put(author, 1);
                }
                previous_counts.put(author, count.getValidCountNumber());
            }
            for(String user:current_streak.keySet()) {
                int current = current_streak.getOrDefault(user, 0);
                if(current >= MIN_STREAK) {
                    int end = previous_counts.get(user);
                    streaks.add(new StatLoader.OverallNonUniquePerfectStreak(new String[]{user},current,end-(current-1)*counts_between_replies,end, thread.getName()));
                }
            }
            perfect_streak_output.addAll(streaks);
        }
        return perfect_streak_output;
    }
    public static List<StatLoader.OverallNonUniquePerfectStreak> overall_non_unique_rotation_streak(List<Statable> threads, UUID uuid1, UUID uuid2, int counts_between_replies) {
        List<StatLoader.OverallNonUniquePerfectStreak> perfect_streak_output = new ArrayList<>();
        for(Statable thread:threads) {
            int MIN_STREAK = 10;
            List<StatLoader.OverallNonUniquePerfectStreak> streaks = new ArrayList<>();
            Map<String, Integer> last_count = new HashMap<>();
            String[] active_streak_users = new String[counts_between_replies];
            int streak = counts_between_replies;
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            CountValue count = null;
            while(counts.hasNext()) {
                count = counts.next();
                String author = count.getAuthor();
                int index = count.getValidCountNumber()%counts_between_replies;
                if(last_count.size() < counts_between_replies) {
                    streak = counts_between_replies;
                } else if(author.equals(active_streak_users[index])) {
                    streak++;
                } else {
                    if(streak >= MIN_STREAK) {
                        String[] users = Arrays.copyOf(active_streak_users, active_streak_users.length);
                        streaks.add(new StatLoader.OverallNonUniquePerfectStreak(users, streak, count.getValidCountNumber() - streak, count.getValidCountNumber() - 1,thread.getName()));
                    }
                    streak = counts_between_replies;
                }
                if(last_count.getOrDefault(active_streak_users[index], 0) == count.getValidCountNumber() - counts_between_replies) {
                    last_count.remove(active_streak_users[index]);
                }
                last_count.put(author, count.getValidCountNumber());
                active_streak_users[index] = author;
            }
            if(streak >= MIN_STREAK && last_count.size() == counts_between_replies) {
                String[] users = Arrays.copyOf(active_streak_users, active_streak_users.length);
                streaks.add(new StatLoader.OverallNonUniquePerfectStreak(users, streak, count.getValidCountNumber() - streak, count.getValidCountNumber() - 1,thread.getName()));
            }
            perfect_streak_output.addAll(streaks);
        }
        return perfect_streak_output;
    }
    public static List<StatLoader.NonUniqueFastestSelfReplies> overall_fastest_self_replies(List<Statable> threads, UUID uuid1, UUID uuid2, int max_ms) {
        List<StatLoader.NonUniqueFastestSelfReplies> self_replies_output = new ArrayList<>();
        for(Statable thread:threads) {
            List<StatLoader.NonUniqueFastestSelfReplies> self_reply_times = new ArrayList<>();
            Map<String, Long> last_count = new HashMap<>();
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            while(counts.hasNext()) {
                CountValue count = counts.next();
                String author = count.getAuthor();
                long time = count.getUUID().getTime();
                if(last_count.containsKey(author)) {
                    long delta_time = time - last_count.get(author);
                    if(delta_time/10000 < max_ms) {
                        self_reply_times.add(new StatLoader.NonUniqueFastestSelfReplies(author, new TimeOutput(delta_time), count.getValidCountNumber(),thread.getName()));
                    }
                }
                last_count.put(author, time);
            }
            self_replies_output.addAll(self_reply_times);
        }
        return self_replies_output;
    }
    public static Map<String,Map<String,Integer>> combined_all_counting_pairs(List<Statable> threads, UUID uuid1, UUID uuid2) {
        List<Map<String,Map<String,Integer>>> all_all_counting_pairs = new ArrayList<>();
        Map<String,Map<String,Integer>> combined_counting_pairs = new HashMap<>();
        for(Statable thread:threads) {
            String filename = "all_counting_pairs_"+uuid1+"_"+uuid2;
            Map<String, Map<String, Integer>> data = thread.loader.loadStatFromBytes(filename);
            if(data != null) {
                all_all_counting_pairs.add(data);
                continue;
            };
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            Map<String,Map<String,Integer>> count_pairs = new HashMap<>();
            String author_current = null;
            String author_prev = null;
            String author_prev_prev;
            while(counts.hasNext()) {
                CountValue count = counts.next();
                author_prev_prev = author_prev;
                author_prev = author_current;
                author_current = count.getAuthor();
                if (author_prev != null) {
                    Map<String, Integer> current_map = count_pairs.computeIfAbsent(author_current, k -> new HashMap<>());
                    int current_amount = current_map.getOrDefault(author_prev, 0);
                    current_map.put(author_prev, current_amount + 1);
                    if(!author_current.equals(author_prev_prev)) {
                        current_map = count_pairs.computeIfAbsent(author_prev,k->new HashMap<>());
                        current_amount = current_map.getOrDefault(author_current,0);
                        current_map.put(author_current,current_amount+1);
                    }
                }
            }
            thread.loader.saveStatToBytes(count_pairs, filename);
            all_all_counting_pairs.add(count_pairs);
        }
        for (Map<String, Map<String, Integer>> map : all_all_counting_pairs) {
            for (Map.Entry<String, Map<String, Integer>> entry : map.entrySet()) {
                String key = entry.getKey();
                Map<String, Integer> innerMap = entry.getValue();
                if (combined_counting_pairs.containsKey(key)) {
                    combined_counting_pairs.put(key, Util.addIntMaps(List.of(innerMap,combined_counting_pairs.get(key))));
                } else {
                    combined_counting_pairs.put(key, new HashMap<>(innerMap));
                }
            }
        }
        return combined_counting_pairs;
    }
    public static Map<String, StatLoader.OverallRecordHour> record_counting_pairs_by_hour(List<Statable> threads, UUID uuid1, UUID uuid2, int duration_seconds) {
        if(duration_seconds<=0 || 3600%duration_seconds!=0) duration_seconds = 3600;
        Map<String, StatLoader.OverallRecordHour> records = new HashMap<>();
        for(Statable thread:threads) {
            Map<String, Integer> record_count_pairs = new HashMap<>();
            Map<String, Integer> current_count_pairs = new HashMap<>();
            String author_current = null;
            String author_prev = null;
            String author_prev_prev;
            Instant moment = Instant.ofEpochMilli(0);
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            while (counts.hasNext()) {
                CountValue count = counts.next();
                author_prev_prev = author_prev;
                author_prev = author_current;
                author_current = count.getAuthor();
                Instant new_moment = Instant.ofEpochMilli(count.getUUID().getTime() / 10000);
                if (new_moment.toEpochMilli() - duration_seconds * 1000L > moment.toEpochMilli()) {
                    for (String user : current_count_pairs.keySet()) {
                        if (current_count_pairs.get(user) > record_count_pairs.getOrDefault(user, 0)) {
                            record_count_pairs.put(user, current_count_pairs.get(user));
                        }
                    }
                    current_count_pairs = new HashMap<>();
                    moment = new_moment.minusMillis(new_moment.toEpochMilli() % (duration_seconds * 1000L));
                    author_prev = null;
                }
                if (author_prev != null) {
                    List<String> users = new ArrayList<>();
                    users.add(author_current);
                    users.add(author_prev);
                    Collections.sort(users);
                    String key = users.get(0) + "|" + users.get(1);
                    int current_amount = current_count_pairs.getOrDefault(key, 0);
                    current_amount++;
                    current_count_pairs.put(key, current_amount);
                    if (!author_current.equals(author_prev_prev)) {
                        current_count_pairs.put(key, current_amount + 1);
                    }
                }
            }
            for(String user:record_count_pairs.keySet()) {
                if(record_count_pairs.get(user) > records.getOrDefault(user, new StatLoader.OverallRecordHour(0,null)).record()) {
                    records.put(user, new StatLoader.OverallRecordHour(record_count_pairs.get(user), thread.getName()));
                }
            }
        }
        return records;
    }
    public static Map<String,Integer> combined_counting_pair_specific_user(List<Statable> threads, String user, UUID uuid1, UUID uuid2) {
        return combined_all_counting_pairs(threads, uuid1, uuid2).getOrDefault(user, new HashMap<>());
    }
    public static Map<String,Float> percentage_of_combined_counts_users_have_with_specific_user(List<Statable> threads, String user, int min_counts, UUID uuid1, UUID uuid2) {
        Map<String, Integer> hoc = hocOverTimePeriod(threads, uuid1, uuid2);
        Map<String,Map<String, Integer>> counting_pairs = combined_all_counting_pairs(threads, uuid1, uuid2);
        Map<String, Float> percentage = new HashMap<>();
        for(String user2:counting_pairs.keySet()) {
            if(hoc.get(user2) >= min_counts) {
                percentage.put(user2, ((float) counting_pairs.get(user2).getOrDefault(user,0)) / hoc.get(user2));
            }
        }
        return percentage;
    }
    public static Map<String,Integer> combined_counting_pairs(List<Statable> threads, UUID uuid1, UUID uuid2) {
        Map<String,Map<String, Integer>> all_counting_pairs = combined_all_counting_pairs(threads,uuid1,uuid2);
        Map<String, Integer> counting_pairs = new HashMap<>();
        for(String user1: all_counting_pairs.keySet()) {
            for(String user2: all_counting_pairs.get(user1).keySet()) {
                if(user1.compareTo(user2)<=0) {
                    int current = counting_pairs.getOrDefault(user1+"|"+user2,0);
                    counting_pairs.put(user1 + "|" + user2, current + all_counting_pairs.get(user1).get(user2));
                } else {
                    int current = counting_pairs.getOrDefault(user2+"|"+user1,0);
                    counting_pairs.put(user2 + "|" + user1, current + all_counting_pairs.get(user1).get(user2));
                }
            }
        }
        return counting_pairs;
    }
    public static Map<String, StatLoader.StringIntPair> overall_favorite_counter(List<Statable> threads, UUID uuid1, UUID uuid2) {
        Map<String,Map<String, Integer>> count_pairs = combined_all_counting_pairs(threads, uuid1, uuid2);
        Map<String, StatLoader.StringIntPair> favorites = new HashMap<>();
        for(String user: count_pairs.keySet()) {
            String favorite = null;
            int max = 0;
            for(String user2:count_pairs.get(user).keySet()) {
                if(count_pairs.get(user).get(user2) > max) {
                    favorite = user2;
                    max = count_pairs.get(user).get(user2);
                }
            }
            favorites.put(user, new StatLoader.StringIntPair(favorite, count_pairs.get(user).get(favorite)));
        }
        return favorites;
    }
    private static Map<String, TimeOutput> time_spent_having_last_count(CountIterator counts, UUID uuid2, String filename) {
        Map<String, TimeOutput> data = counts.getDefaultThread().loader.loadStatFromBytes(filename);
        if(data != null) return data;
        long last_timestamp = 0;
        String last_author = null;
        Map<String, Long> hoc = new HashMap<>();
        counts.init();
        while(counts.hasNext()) {
            CountValue count = counts.next();
            String author = count.getAuthor();
            long timestamp = count.getUUID().getTime();
            if(last_author!=null) {
                long current = hoc.getOrDefault(last_author,0L);
                hoc.put(last_author, current+(timestamp-last_timestamp));
            }
            last_author = author;
            last_timestamp = timestamp;
        }
        if(last_author!=null) {
            long current = hoc.getOrDefault(last_author,0L);
            long timestamp1 = uuid2.getTime();
            long timestamp2 = System.currentTimeMillis()*10000;
            hoc.put(last_author, current+(Math.min(timestamp2,timestamp1)-last_timestamp));
        }
        Map<String, TimeOutput> return_data = new HashMap<>();
        for(String user:hoc.keySet()) {
            return_data.put(user, new TimeOutput(hoc.get(user)));
        }
        counts.getDefaultThread().loader.saveStatToBytes(return_data, filename);
        return return_data;
    }
    public static Map<String, TimeOutput> combined_time_spent_having_last_count(List<Statable> threads, UUID uuid1, UUID uuid2, boolean thread_stats_combined) {
        if(thread_stats_combined) {
            String filename = "time_spent_having_last_count_" + uuid1 + "_" + uuid2;
            List<Map<String, TimeOutput>> data = new ArrayList<>();
            for(Statable thread : threads) {
                data.add(time_spent_having_last_count(new CountTimeIterator(thread, uuid1, uuid2), uuid2, filename));
            }
            return Util.addTimeOutputMaps(data);
        } else {
            String filename = "time_spent_having_last_count_" + uuid1 + "_" + uuid2+"_"+threads.hashCode();
            return time_spent_having_last_count(new CombinedCountTimeIterator(threads, uuid1, uuid2), uuid2, filename);
        }
    }
    public static Map<String, Integer> combinedMsBetweenUpdates(List<Statable> threads, UUID uuid1, UUID uuid2, long ms_value) {
        List<Map<String, Integer>> data = new ArrayList<>();
        for(Statable thread:threads) {
            Map<String, Integer> hoc = new HashMap<>();
            UpdateIterator updates = new UpdateIterator(thread, uuid1, uuid2).init();
            long last_timestamp = 0;
            while(updates.hasNext()) {
                UpdateValue update = updates.next();
                long timestamp = update.getUUID().getTime();
                if(Math.abs(timestamp/10000 - 1719228439356L) < 600000) {
                    System.out.println(update);
                }
                if((timestamp-last_timestamp)/10000 == ms_value) {
                    String author = update.getAuthor();
                    hoc.put(author, hoc.getOrDefault(author, 0) + 1);
                }
                last_timestamp = timestamp;
            }
            data.add(hoc);
        }
        return Util.addIntMaps(data);
    }
    public static Map<String, Float> combined_odd_even_ratio(List<Statable> threads, UUID uuid1, UUID uuid2, int min_counts, boolean odd_or_even) {
        Map<String, Integer> hoc = hocOverTimePeriod(threads, uuid1, uuid2);
        Map<String, Integer> odd_or_even_hoc;
        if(odd_or_even) {
            odd_or_even_hoc = getHocOverTimePeriodWithFilter(threads, uuid1, uuid2, thread->(i->i.getValidCountNumber()%2==1), "odd");
        } else {
            odd_or_even_hoc = getHocOverTimePeriodWithFilter(threads, uuid1, uuid2, thread->(i->i.getValidCountNumber()%2==0), "even");
        }
        Map<String, Float> ratio = new HashMap<>();
        for(String user:hoc.keySet()) {
            if(hoc.get(user)>=min_counts) {
                ratio.put(user, ((float) odd_or_even_hoc.getOrDefault(user,0)) / hoc.get(user));
            }
        }
        return ratio;
    }
    public static Map<String,Integer> combined_unique_time_parts(List<Statable> threads, int increment_seconds, boolean day_or_week, UUID uuid1, UUID uuid2) {
        if(threads.size()==0) return new HashMap<>();
        if(86400%increment_seconds!=0) increment_seconds = 3600;
        Map<String, Integer> hoc = hocOverTimePeriod(threads, uuid1, uuid2);
        Map<String, int[]> unique_time_parts = new HashMap<>();
        int total_parts = (86400*(day_or_week?1:7))/increment_seconds;
        for (String user : hoc.keySet()) {
            if (hoc.get(user) > total_parts / 2) {
                unique_time_parts.put(user, new int[total_parts + 1]);
            }
        }
        for(Statable thread:threads) {
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            while (counts.hasNext()) {
                CountValue count = counts.next();
                String author = count.getAuthor();
                long timestamp = count.getUUID().getTime();
                ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp / 10000), ZoneId.of(counts.getThread().timezone));
                int[] current = unique_time_parts.get(author);
                if (current == null) continue;
                int loc = 60 * 60 * date.getHour() + 60 * date.getMinute() + date.getSecond();
                if (!day_or_week) {
                    loc += 60 * 60 * 24 * (date.getDayOfWeek().getValue() % 7);
                }
                loc /= increment_seconds;
                if (current[loc] == 0) {
                    current[total_parts]++;
                }
                current[loc]++;
            }
        }
        hoc = new HashMap<>();
        for(String user:unique_time_parts.keySet()) {
            hoc.put(user,unique_time_parts.get(user)[total_parts]);
        }
        return hoc;
    }
    public static List<StatLoader.NonUniqueDailyPercentOfCountsOutput> daily_percent_of_combined_counts(List<Statable> threads, String start_date, String end_date, ParticipationDuration mode) {
        List<StatLoader.DayHOC> all_day_hocs = combined_all_day_hocs(threads, start_date, end_date, mode);
        List<StatLoader.NonUniqueDailyPercentOfCountsOutput> daily_percent = new ArrayList<>();
        for(StatLoader.DayHOC day_hoc:all_day_hocs) {
            int daily_counts = Util.sumIntMapValues(day_hoc.hoc());
            for(String user:day_hoc.hoc().keySet()) {
                daily_percent.add(new StatLoader.NonUniqueDailyPercentOfCountsOutput(user, ((float)day_hoc.hoc().get(user))/daily_counts, day_hoc.hoc().get(user),daily_counts, day_hoc.date().toString()));
            }
        }
        return daily_percent;
    }
    private static List<List<String>> getMedalists(Map<String, Integer> hoc, int max_position) {
        List<List<String>> medalists = new ArrayList<>();
        List<String> sorted_hoc = hoc.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .toList();
        int last_amount = 0;
        int last_position = 0;
        int current_amount;
        for(int i=0;i<sorted_hoc.size();i++) {
            String user = sorted_hoc.get(i);
            current_amount = hoc.get(user);
            if(current_amount!=last_amount) {
                last_position = i;
            }
            if(last_position >= max_position) break;
            if(i < max_position) medalists.add(new ArrayList<>());
            medalists.get(last_position).add(user);
            last_amount = current_amount;
        }
        return medalists;
    }
    public static List<StatLoader.NonUniqueDailyPercentOfCountsOutput> nth_place_daily_percent_of_combined_counts(List<Statable> threads, String start_date, String end_date, int day_hoc_position, ParticipationDuration mode) {
        List<StatLoader.DayHOC> all_day_hocs = combined_all_day_hocs(threads, start_date, end_date, mode);
        List<StatLoader.NonUniqueDailyPercentOfCountsOutput> daily_percent = new ArrayList<>();
        for(StatLoader.DayHOC day_hoc:all_day_hocs) {
            List<List<String>> medalists = getMedalists(day_hoc.hoc(), day_hoc_position);
            int daily_counts = Util.sumIntMapValues(day_hoc.hoc());
            for(String user:medalists.get(day_hoc_position-1)) {
                daily_percent.add(new StatLoader.NonUniqueDailyPercentOfCountsOutput(user, ((float)day_hoc.hoc().get(user))/daily_counts, day_hoc.hoc().get(user),daily_counts, day_hoc.date().toString()));
            }
        }
        return daily_percent;
    }
    public static List<StatLoader.NonUniquePhotoFinish> combined_photo_finishes(List<Statable> threads, String start_date, String end_date, ParticipationDuration mode, float percentage, int min_counts) {
        List<StatLoader.DayHOC> all_day_hocs = combined_all_day_hocs(threads, start_date, end_date, mode);
        List<StatLoader.NonUniquePhotoFinish> photo_finishes = new ArrayList<>();
        for(StatLoader.DayHOC day_hoc:all_day_hocs) {
            List<List<String>> medals = getMedalists(day_hoc.hoc(), day_hoc.hoc().size());
            for(int i=0;i<3;i++) {
                if(medals.get(i).size()==0) continue;
                if(day_hoc.hoc().get(medals.get(i).get(0)) < min_counts) break;
                for(int j=i+1;j<medals.size();j++) {
                    if(medals.get(j).size()==0) continue;
                    float percent = 1 - ((float)day_hoc.hoc().get(medals.get(j).get(0)))/day_hoc.hoc().get(medals.get(i).get(0));
                    if(percent < percentage) {
                        for(String user1:medals.get(i)) {
                            for(String user2:medals.get(j)) {
                                photo_finishes.add(new StatLoader.NonUniquePhotoFinish(user1, user2, day_hoc.hoc().get(user1), day_hoc.hoc().get(user2), percent, day_hoc.date().toString()));
                            }
                        }
                    }
                }
            }
        }
        return photo_finishes;
    }
    public static Map<String, Integer> largestCliques(List<Statable> threads, UUID uuid1, UUID uuid2, int weight) {
        if(threads.size()==0) return new HashMap<>();
        Map<String, Map<String, Integer>> counting_pairs = combined_all_counting_pairs(threads, uuid1, uuid2);
        Map<String, Set<String>> graph = new HashMap<>();
        for(String user:counting_pairs.keySet()) {
            Set<String> neighbors = counting_pairs.get(user).keySet();
            neighbors.remove(user);
            if(weight>0) {
                List<String> to_remove = new ArrayList<>();
                for(String user2:counting_pairs.get(user).keySet()) {
                    if(counting_pairs.get(user).get(user2)+counting_pairs.getOrDefault(user2, new HashMap<>()).getOrDefault(user, 0)<weight) {
                        to_remove.add(user2);
                    }
                }
                for(String user2:to_remove) {
                    neighbors.remove(user2);
                }
            }
            graph.put(user, neighbors);
        }
        Map<String, Set<Set<String>>> result = Util.bronKerbosch(graph);
        Map<String, Integer> to_return = new HashMap<>();
        int max_size = 0;
        for(String user:result.keySet()) {
            Set<String> next = result.get(user).iterator().next();
            if(next.size() > max_size) {
                max_size = next.size();
            }
            for(Set<String> clique:result.get(user)) {
                to_return.put(user, clique.size());
            }
        }
        return to_return;
    }
    public static Map<String, StatLoader.HighestUserNotCountingWithOutput> combined_highest_user_not_counted_with(List<Statable> threads, UUID uuid1, UUID uuid2) {
        Map<String, Map<String, Integer>> counting_pairs = combined_all_counting_pairs(threads, uuid1, uuid2);
        Map<String, Integer> hoc = hocOverTimePeriod(threads, uuid1, uuid2);
        TreeMap<String, Integer> ordered_hoc = new TreeMap<>(new ValueComparator(hoc));
        ordered_hoc.putAll(hoc);
        Map<String, StatLoader.HighestUserNotCountingWithOutput> output = new HashMap<>();
        for(String user:counting_pairs.keySet()) {
            String[] users_not_counted_with = new String[5];
            int current = 0;
            for(String user2:ordered_hoc.keySet()) {
                if(user.equals(user2)) continue;
                if(counting_pairs.get(user).get(user2)==null) {
                    users_not_counted_with[current] = user2;
                    current++;
                    if(current==5) {
                        output.put(user, new StatLoader.HighestUserNotCountingWithOutput(users_not_counted_with, hoc.get(user)));
                        break;
                    }
                }
            }
        }
        return output;
    }
    public static Map<String, Integer> combined_perfect_ks(List<Statable> threads, int start_k, int end_k, int gets_per_k) {
        List<Map<String, Integer>> data = new ArrayList<>();
        for(Statable thread:threads) {
            List<Integer> get_locs = thread.getGetValues();
            List<Integer> true_get_locs = new ArrayList<>();
            true_get_locs.add(0);
            for(int i=0;i<get_locs.size();i+=gets_per_k) {
                true_get_locs.add(get_locs.get(i));
            }
            if(get_locs.get(get_locs.size()-1)!=thread.getLast_number()) {
                true_get_locs.add(thread.getLast_number()+2); //The +2 guarantees that no perfect will be registered on an incomplete k
            }
            List<StatLoader.K_HOC> all_k_hocs = thread.generateAllKHocs(start_k, end_k, gets_per_k);
            Map<String, Integer> perfect_ks = new HashMap<>();
            int i=0;
            for(StatLoader.K_HOC k_hoc:all_k_hocs) {
                int counts_this_k = true_get_locs.get(i+1)-true_get_locs.get(i);
                int min_counts = counts_this_k/thread.doubleCountInterval();
                int remainder = counts_this_k%min_counts;
                List<CountValue> second_count = null;
                boolean broken = false;
                for(String user:k_hoc.hoc().keySet()) {
                    if(k_hoc.hoc().get(user) < min_counts) continue;
                    if(remainder==0 || k_hoc.hoc().get(user) > min_counts) {
                        perfect_ks.put(user, perfect_ks.getOrDefault(user, 0) + 1);
                        continue;
                    }
                    //Everything else is a check in case the "double count interval" doesn't cleanly divide
                    if(second_count==null) {
                        second_count = thread.getCountsTimeSort(true_get_locs.get(i) + remainder - 1, true_get_locs.get(i) + thread.doubleCountInterval() - 1);
                    }
                    for(CountValue count:second_count) {
                        String user2 = count.getAuthor();
                        if(user.equals(user2)) {
                            broken = true;
                            break;
                        }
                    }
                    if(broken) continue;
                    perfect_ks.put(user, perfect_ks.getOrDefault(user, 0) + 1);
                }
                i++;
            }
            data.add(perfect_ks);
        }
        return Util.addIntMaps(data);
    }
    public static Map<String, CountValue> overall_nth_count(List<Statable> threads, UUID uuid1, UUID uuid2, int n) {
        Map<String, Integer> current_hoc = new HashMap<>();
        Map<String, CountValue> nth_counts = new HashMap<>();
        CombinedCountTimeIterator counts = new CombinedCountTimeIterator(threads, uuid1, uuid2).init();
        while(counts.hasNext()) {
            CountValue count = counts.next();
            String author = count.getAuthor();
            int current = current_hoc.getOrDefault(author, 0);
            current_hoc.put(author, ++current);
            if(current == n) {
                nth_counts.put(author, count);
            }
        }
        return nth_counts;
    }
    public static CountValue overall_nth_count_no_user(List<Statable> threads, UUID uuid1, UUID uuid2, int n) {
        int counter = 0;
        CombinedCountTimeIterator counts = new CombinedCountTimeIterator(threads, uuid1, uuid2).init();
        while(counts.hasNext()) {
            CountValue count = counts.next();
            counter++;
            if(counter == n) {
                return count;
            }
        }
        return null;
    }
    public static Map<String, StatLoader.ThreadDayRecord> thread_day_records(List<Statable> threads, String start_date, String end_date, ParticipationDuration mode) {
        Map<String, StatLoader.ThreadDayRecord> thread_day_records = new HashMap<>();
        for(Statable thread:threads) {
            Map<String, Integer> day_totals = combined_total_count_day_hocs(List.of(thread), start_date, end_date, mode);
            String max_date = null;
            int max_counts = 0;
            for(String date:day_totals.keySet()) {
                if(day_totals.get(date)>max_counts) {
                    max_date = date;
                    max_counts = day_totals.get(date);
                }
            }
            thread_day_records.put(thread.getName(), new StatLoader.ThreadDayRecord(max_counts, max_date));
        }
        return thread_day_records;
    }

    public static Map<String, Integer> totalUpdatesWithString(List<Statable> threads, String search, UUID uuid1, UUID uuid2) {
        List<Map<String, Integer>> hocs = new ArrayList<>();
        for(Statable thread:threads) {
            Map<String, Integer> hoc = new HashMap<>();
            UpdateIterator updates = new UpdateIterator(thread, uuid1, uuid2).init();
            while(updates.hasNext()) {
                UpdateValue update = updates.next();
                if(update.getRawText().contains(search)) {
                    String author = update.getAuthor();
                    hoc.put(author, hoc.getOrDefault(author, 0) + 1);
                }
            }
            hocs.add(hoc);
        }
        return Util.addIntMaps(hocs);
    }
    public static Map<String, Integer> combinedUpdatesHoc(List<Statable> threads, UUID uuid1, UUID uuid2) {
        List<Map<String, Integer>> hocs = new ArrayList<>();
        for(Statable thread:threads) {
            Map<String, Integer> hoc = new HashMap<>();
            UpdateIterator updates = new UpdateIterator(thread, uuid1, uuid2).init();
            while(updates.hasNext()) {
                UpdateValue update = updates.next();
                String author = update.getAuthor();
                hoc.put(author, hoc.getOrDefault(author, 0) + 1);
            }
            hocs.add(hoc);
        }
        return Util.addIntMaps(hocs);
    }

    public static Map<String, StatLoader.FastestNCountsNoUserOutput> thread_fastest_n_counts_no_user(List<Statable> threads, int n, UUID uuid1, UUID uuid2, boolean fast) {
        Map<String, StatLoader.FastestNCountsNoUserOutput> fastest_n_counts = new HashMap<>();
        for(Statable thread:threads) {
            if(thread.getTotal_counts() < n) continue;
            StatLoader.SitewideFastestNCountsNoUserOutput data = fastest_n_counts_no_user(new CountTimeIterator(thread, uuid1, uuid2),n,fast);
            fastest_n_counts.put(thread.getName(), new StatLoader.FastestNCountsNoUserOutput(data.start_count(),data.end_count(),data.time()));
        }
        return fastest_n_counts;
    }
    public static StatLoader.SitewideFastestNCountsNoUserOutput sitewide_fastest_n_counts_no_user(List<Statable> threads, int n, UUID uuid1, UUID uuid2, boolean fast) {
        return fastest_n_counts_no_user(new CombinedCountTimeIterator(threads, uuid1, uuid2),n,fast);
    }
    private static StatLoader.SitewideFastestNCountsNoUserOutput fastest_n_counts_no_user(CountIterator counts, int n, boolean fast) {
        //timestamp, number
        long[][] current = new long[n][2];
        String[] thread_str = new String[n];
        //time, start count, end count
        long[] current_fastest = new long[]{fast?Long.MAX_VALUE:0,0,0};
        String[] record_thread_str = new String[2];
        int index = 0;
        counts.init();
        while(counts.hasNext()) {
            CountValue count = counts.next();
            long timestamp = count.getUUID().getTime();
            long number = count.getValidCountNumber();
            if(current[index][0]!=0) {
                long speed = timestamp - current[index][0];
                if((speed < current_fastest[0] && fast) || (speed > current_fastest[0] && !fast)) {
                    current_fastest[0] = speed;
                    current_fastest[1] = current[index][1];
                    current_fastest[2] = number;
                    record_thread_str[0] = thread_str[index];
                    record_thread_str[1] = counts.getThread().getName();
                }
            }
            current[index][0] = timestamp;
            current[index][1] = number;
            thread_str[index] = counts.getThread().getName();
            index = (index+1)%n;
        }
        return new StatLoader.SitewideFastestNCountsNoUserOutput((int)current_fastest[1], record_thread_str[0], (int)current_fastest[2], record_thread_str[1], current_fastest[0]);
    }

    public static Map<String, StatLoader.SitewideFastestNCountsOutput> sitewide_fastest_n_counts(List<Statable> threads, int n, UUID uuid1, UUID uuid2, boolean fast) {
        CombinedCountTimeIterator counts = new CombinedCountTimeIterator(threads, uuid1, uuid2);
        return get_fastest_n_counts(counts, threads, n, uuid1, uuid2, fast);
    }
    public static Map<String, StatLoader.OverallFastestNCountsOutput> overall_fastest_n_counts(List<Statable> threads, int n, UUID uuid1, UUID uuid2, boolean fast) {
        List<Map<String, StatLoader.SitewideFastestNCountsOutput>> fastest_n_output = new ArrayList<>();
        for(Statable thread:threads) {
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2);
            Map<String, StatLoader.SitewideFastestNCountsOutput> fastest_n = get_fastest_n_counts(counts, List.of(thread), n, uuid1, uuid2, fast);
            if(fastest_n.size()==0) continue;
            fastest_n_output.add(fastest_n);
        }
        Map<String, StatLoader.SitewideFastestNCountsOutput> optimized = Util.optimizeMaps(fastest_n_output, fast);
        Map<String, StatLoader.OverallFastestNCountsOutput> retyped = new HashMap<>();
        optimized.forEach((k,v)->retyped.put(k,new StatLoader.OverallFastestNCountsOutput(v.start_hoc(),v.end_hoc(),v.start_count(),v.end_count(),v.time(),v.start_thread())));
        return retyped;
    }
    private static Map<String, StatLoader.SitewideFastestNCountsOutput> get_fastest_n_counts(CountIterator counts, List<Statable> threads, int n, UUID uuid1, UUID uuid2, boolean fast) {
        String filename = "fastest_"+n+"_counts"+"_"+fast+"_"+uuid1+"_"+uuid2;
        Map<String, Integer> hoc_time = hocOverTimePeriod(threads, uuid1, uuid2);
        Map<String, long[][]> users_with_sufficient_counts = new HashMap<>();
        Map<String, long[]> current_fastest = new HashMap<>();
        Map<String, String[]> current_thread = new HashMap<>();
        Map<String, String[]> start_end_threads = new HashMap<>();
        Map<String, Integer> index_map = new HashMap<>();
        for(String user:hoc_time.keySet()) {
            if(hoc_time.get(user)>n) {
                //timestamp, hoc, number
                users_with_sufficient_counts.put(user, new long[n][3]);
                //time, start hoc, end hoc, start count, end count
                current_fastest.put(user, new long[]{fast?Long.MAX_VALUE:0,0,0,0,0});
                current_thread.put(user, new String[n]);
                start_end_threads.put(user, new String[2]);
                index_map.put(user, 0);
            }
        }
        hoc_time = new HashMap<>();
        counts.init();
        while(counts.hasNext()) {
            CountValue count = counts.next();
            String author = count.getAuthor();
            long[][] current = users_with_sufficient_counts.get(author);
            if(current!=null) {
                int index = index_map.get(author);
                long timestamp = count.getUUID().getTime();
                long number = count.getValidCountNumber();
                int current_counts = hoc_time.getOrDefault(author, 0);
                String[] user_current_thread = current_thread.get(author);
                hoc_time.put(author, current_counts+1);
                if(current[index][0]!=0) {
                    long speed = timestamp - current[index][0];
                    long[] current_speed = current_fastest.get(author);
                    if(speed < current_speed[0] && fast || speed > current_speed[0] && !fast) {
                        current_speed[0] = speed;
                        current_speed[1] = current[index][1];
                        current_speed[2] = hoc_time.get(author);
                        current_speed[3] = current[index][2];
                        current_speed[4] = number;
                        String[] user_start_end_threads = start_end_threads.get(author);
                        user_start_end_threads[0] = user_current_thread[index];
                        user_start_end_threads[1] = counts.getThread().getName();
                    }
                }
                current[index][0] = timestamp;
                current[index][1] = hoc_time.get(author);
                current[index][2] = number;
                user_current_thread[index] = counts.getThread().getName();
                index_map.put(author, (index+1)%n);
            }
        }
        Map<String, StatLoader.SitewideFastestNCountsOutput> hoc = new HashMap<>();
        for(String user:current_fastest.keySet()) {
            long[] current = current_fastest.get(user);
            String[] user_start_end_threads = start_end_threads.get(user);
            hoc.put(user, new StatLoader.SitewideFastestNCountsOutput((int)current[1], (int)current[2], (int)current[3], user_start_end_threads[0], (int)current[4], user_start_end_threads[1], current[0]));
        }
        return hoc;
    }
    public static Map<String, StatLoader.CountStreakOutputWithThread> overall_counts_under_n_ms_streak(List<Statable> threads, UUID uuid1, UUID uuid2, long max_ms) {
        Map<String, StatLoader.CountStreakOutputWithThread> record_streak = new HashMap<>();
        for(Statable thread:threads) {
            Map<String, long[]> current_streak = new HashMap<>();
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            while(counts.hasNext()) {
                CountValue count = counts.next();
                String author = count.getAuthor();
                //Streak, Last Timestamp, End Num (Current), Start Num
                long[] current = current_streak.computeIfAbsent(author, k->new long[]{0,0,0,count.getValidCountNumber()});
                current[2] = count.getValidCountNumber();
                long timestamp = count.getUUID().getTime();
                if((timestamp - current[1]) <= max_ms*10000) {
                    current[0]++;
                } else {
                    StatLoader.CountStreakOutputWithThread current_record = record_streak.getOrDefault(author, new StatLoader.CountStreakOutputWithThread(0,0,0,null));
                    if(current[0] > current_record.size()) {
                        record_streak.put(author, new StatLoader.CountStreakOutputWithThread((int)current[3], count.getValidCountNumber(), (int)current[0],thread.getName()));
                    }
                    current[0] = 0;
                    current[3] = count.getValidCountNumber();
                }
                current[1] = timestamp;
            }
            for(String author: current_streak.keySet()) {
                long[] current = current_streak.get(author);
                StatLoader.CountStreakOutputWithThread current_record = record_streak.getOrDefault(author, new StatLoader.CountStreakOutputWithThread(0,0,0,null));
                if(current[0] > current_record.size()) {
                    record_streak.put(author, new StatLoader.CountStreakOutputWithThread((int)current[3], (int)current[2], (int)current[0],thread.getName()));
                }
            }
        }
        return record_streak;
    }
    public static List<StatLoader.NonUniqueCountStreakOutputWithThread> overall_counts_under_n_ms_streak_no_user(List<Statable> threads, UUID uuid1, UUID uuid2, long max_ms) {
        List<StatLoader.NonUniqueCountStreakOutputWithThread> record_streaks = new ArrayList<>();
        int MIN_SIZE = 10;
        for(Statable thread:threads) {
            long[] current = new long[4];
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            while(counts.hasNext()) {
                CountValue count = counts.next();
                //Streak, Last Timestamp, End Num (Current), Start Num
                current[2] = count.getValidCountNumber();
                long timestamp = count.getUUID().getTime();
                if((timestamp - current[1]) <= max_ms*10000) {
                    current[0]++;
                } else {
                    if(current[2]-current[3]>MIN_SIZE)
                        record_streaks.add(new StatLoader.NonUniqueCountStreakOutputWithThread((int)current[3], count.getValidCountNumber(), (int)current[0],thread.getName()));
                    current[0] = 0;
                    current[3] = count.getValidCountNumber();
                }
                current[1] = timestamp;
            }
            if(current[2]-current[3]>MIN_SIZE)
                record_streaks.add(new StatLoader.NonUniqueCountStreakOutputWithThread((int)current[3], (int)current[2], (int)current[0],thread.getName()));
        }
        return record_streaks;
    }
    public static Map<String, StatLoader.CountStreakOutputWithThread> overall_no_mistake_streak(List<Statable> threads, UUID uuid1, UUID uuid2) {
        Map<String, StatLoader.CountStreakOutputWithThread> no_mistakes = new HashMap<>();
        for(Statable thread:threads) {
            Map<String, int[]> current_streak = new HashMap<>(); //int[] contains size, start, and end
            UpdateIterator counts = new UpdateIterator(thread, uuid1, uuid2).init();
            while(counts.hasNext()) {
                UpdateValue count = counts.next();
                String author = count.getAuthor();
                if(count.isStricken() && current_streak.containsKey(author)) {
                    int[] streak = current_streak.get(author);
                    int prev_streak = no_mistakes.getOrDefault(author, new StatLoader.CountStreakOutputWithThread(0,0,0,null)).size();
                    if(streak[0] > prev_streak) {
                        no_mistakes.put(author, new StatLoader.CountStreakOutputWithThread(streak[1], streak[2], streak[0], thread.getName()));
                    }
                    current_streak.remove(author);
                }
                else if(count.isIsValidCount()) {
                    int[] streak = current_streak.computeIfAbsent(author, k -> new int[3]);
                    if(streak[1]==0) {
                        streak[1] = count.getValidCountNumber();
                    }
                    streak[2] = count.getValidCountNumber();
                    streak[0]++;
                }
            }
            for(String user:current_streak.keySet()) {
                int[] streak = current_streak.get(user);
                int prev_streak = no_mistakes.getOrDefault(user, new StatLoader.CountStreakOutputWithThread(0,0,0,null)).size();
                if(streak[0] > prev_streak) {
                    no_mistakes.put(user, new StatLoader.CountStreakOutputWithThread(streak[1], streak[2], streak[0],thread.getName()));
                }
            }
        }
        return no_mistakes;
    }
    public static Map<String, Integer> user_thread_parts(List<Statable> threads, UUID uuid1, UUID uuid2) {
        Map<String, Integer> thread_parts = new HashMap<>();
        for(Statable thread:threads) {
            Map<String, Integer> hoc = hocOverTimePeriod(List.of(thread),uuid1, uuid2);
            for(String user:hoc.keySet()) {
                int current_parts = thread_parts.getOrDefault(user, 0);
                thread_parts.put(user, current_parts+1);
            }
        }
        return thread_parts;
    }
    public static Map<String, Float> days_with_greatest_percentage_thread_parts(List<Statable> threads, String start_date, String end_date, ParticipationDuration mode) {
        List<List<StatLoader.DayHOC>> all_all_day_hocs = new ArrayList<>();
        List<TimeUnit> first_counts = new ArrayList<>();
        Map<String, int[]> data = new HashMap<>();
        Map<String, Float> days_with_greatest_percent = new HashMap<>();
        for(Statable thread:threads) {
            all_all_day_hocs.add(combined_all_day_hocs(List.of(thread), start_date, end_date, mode));
            first_counts.add(thread.first_day(mode, thread.timezone));
        }
        Collections.sort(first_counts);
        for(List<StatLoader.DayHOC> all_day_hocs: all_all_day_hocs) {
            int total_threads_by_date = 0;
            for(StatLoader.DayHOC date:all_day_hocs) {
                while(total_threads_by_date!=first_counts.size() && !first_counts.get(total_threads_by_date).isAfter(date.date())) {
                    total_threads_by_date++;
                }
                int[] current = data.computeIfAbsent(date.date().toString(), k->new int[threads.size()]);
                current[1] = total_threads_by_date;
                if(date.hoc().size() > 0) current[0]++;
            }
        }
        for(String date:data.keySet()) {
            int[] current = data.get(date);
            days_with_greatest_percent.put(date, ((float)current[0])/current[1]);
        }
        return days_with_greatest_percent;
    }

    public static List<StatLoader.OverallFastestBarsOutput> overall_fastest_bars(List<Statable> threads, int max_ms, UUID uuid1, UUID uuid2) {
        List<StatLoader.OverallFastestBarsOutput> fast_bars = new ArrayList<>();
        for(Statable thread:threads) {
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            long last_bar = 0;
            while(counts.hasNext()) {
                CountValue count = counts.next();
                String author = count.getAuthor();
                long timestamp = count.getUUID().getTime();
                if(timestamp - last_bar < 36000000000L) {
                    continue;
                }
                last_bar = Util.roundUUIDToLowerHour(count.getUUID()).getTime();
                if(timestamp - last_bar < max_ms*10000L) {
                    fast_bars.add(new StatLoader.OverallFastestBarsOutput(author, new TimeOutput(timestamp - last_bar), count.getRawCount(),thread.getName()));
                }
            }
        }
        return fast_bars;
    }
    public static Map<String, TimeOutput> timeBetweenTwoHoCAmounts(List<Statable> threads, UUID uuid1, UUID uuid2, int start_counts, int end_counts) {
        CombinedCountTimeIterator counts = new CombinedCountTimeIterator(threads, uuid1, uuid2);
        String filename = start_counts+"_to_"+end_counts+"_"+uuid1+"_"+uuid2+"_"+threads.hashCode();
        Map<String, TimeOutput> data = counts.getDefaultThread().loader.loadStatFromBytes(filename);
        if(data != null) return data;
        if(start_counts >= end_counts || start_counts < 1) return new HashMap<>();
        Map<String, CountValue> start_count = new HashMap<>();
        Map<String, CountValue> end_count = new HashMap<>();
        Map<String, Integer> hoc = new HashMap<>();
        counts.init();
        while(counts.hasNext()) {
            CountValue count = counts.next();
            String author = count.getAuthor();
            int current = hoc.getOrDefault(author,0);
            hoc.put(author, current+1);
            if(current+1 == start_counts) {
                start_count.put(author, count);
            }
            else if(current+1 == end_counts) {
                end_count.put(author, count);
            }
        }
        Map<String, TimeOutput> time_taken = new HashMap<>();
        for(String user:end_count.keySet()) {
            time_taken.put(user, new TimeOutput(end_count.get(user).getUUID().getTime() - start_count.get(user).getUUID().getTime()));
        }
        counts.getDefaultThread().loader.saveStatToBytes(time_taken, filename);
        return time_taken;
    }
    public static Map<String, Double> getSumOfReciprocals(List<Statable> threads, UUID uuid1, UUID uuid2) {
        List<Map<String, Double>> data = new ArrayList<>();
        for(Statable thread:threads) {
            String filename = "sum_of_reciprocals_"+uuid1+"_"+uuid2;
            Map<String, Double> data2 = thread.loader.loadStatFromBytes(filename);
            if(data2 != null) {
                data.add(data2);
                continue;
            }
            Map<String, Double> hoc = new HashMap<>();
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            while(counts.hasNext()) {
                CountValue count = counts.next();
                String author = count.getAuthor();
                double current = hoc.getOrDefault(author,0.0);
                hoc.put(author, current+1.0/count.getValidCountNumber());
            }
            thread.loader.saveStatToBytes(hoc, filename);
            data.add(hoc);
        }
        return Util.addDoubleMaps(data);
    }
    public static Map<String, Long> getSumOfCounts(List<Statable> threads, UUID uuid1, UUID uuid2) {
        List<Map<String, Long>> data = new ArrayList<>();
        for(Statable thread:threads) {
            String filename = "sum_of_counts_"+uuid1+"_"+uuid2;
            Map<String, Long> data2 = thread.loader.loadStatFromBytes(filename);
            if(data2 != null) {
                data.add(data2);
                continue;
            }
            Map<String, Long> hoc = new HashMap<>();
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            while(counts.hasNext()) {
                CountValue count = counts.next();
                String author = count.getAuthor();
                long current = hoc.getOrDefault(author,0L);
                hoc.put(author, current+count.getValidCountNumber());
            }
            thread.loader.saveStatToBytes(hoc, filename);
            data.add(hoc);
        }
        return Util.addLongMaps(data);
    }
    public static Map<String,Integer> unique_counts_mod_n(List<Statable> threads, int n, UUID uuid1, UUID uuid2) {
        Map<String, Integer> hoc = hocOverTimePeriod(threads, uuid1, uuid2);
        Map<String, int[]> unique_mod_n = new HashMap<>();
        for(String user:hoc.keySet()) {
            if(hoc.get(user)>n/2) {
                unique_mod_n.put(user,new int[n+1]);
            }
        }
        for(Statable thread:threads) {
            CountTimeIterator counts = new CountTimeIterator(thread, uuid1, uuid2).init();
            while (counts.hasNext()) {
                CountValue count = counts.next();
                String author = count.getAuthor();
                int[] current = unique_mod_n.get(author);
                if (current == null) continue;
                int loc = count.getValidCountNumber() % n;
                if (current[loc] == 0) {
                    current[n]++;
                }
                current[loc]++;
            }
            hoc = new HashMap<>();
            for (String user : unique_mod_n.keySet()) {
                hoc.put(user, unique_mod_n.get(user)[n]);
            }
        }
        return hoc;
    }
    public static List<StatLoader.OverallSpeedTag> overall_speed_tags(List<Statable> threads, int k_start, int k_end, int gets_per_k, float required_percentage, int users_tagging) {
        if(required_percentage<0.5f||required_percentage>1) throw new IllegalArgumentException("Percentage must be between 0.5 and 1");
        if(users_tagging<=0) throw new IllegalArgumentException("The number of users tagging must be greater than 0");
        String filename = "overall_speed_"+users_tagging+"_tags_"+gets_per_k+"_"+required_percentage+"_"+k_start+"_"+k_end;
        List<StatLoader.OverallSpeedTag> tags = new ArrayList<>();
        for(Statable thread:threads) {
            List<StatLoader.OverallSpeedTag> data = thread.loader.loadStatFromBytes(filename);
            if (data != null) {
                tags.addAll(data);
                continue;
            }
            List<StatLoader.K_HOC> all_k_hocs = thread.generateAllKHocs(k_start, k_end, gets_per_k);
            List<StatLoader.OverallSpeedTag> speed_tags = new ArrayList<>();
            for (StatLoader.K_HOC k_hoc : all_k_hocs) {
                if (k_hoc.hoc().size() < users_tagging) continue;
                int get_size = Util.sumIntMapValues(k_hoc.hoc());
                List<String> sorted_k_hoc = k_hoc.hoc().entrySet().stream()
                        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                        .map(Map.Entry::getKey)
                        .toList();
                boolean failed = false;
                int artbn = 0;
                if (k_hoc.hoc().get(sorted_k_hoc.get(0)) >= get_size * required_percentage / 2) {
                    artbn = Math.max(users_tagging - 2, 0);
                }
                int users_counts = 0;
                for (int j = 0; j < users_tagging; j++) {
                    if (k_hoc.hoc().get(sorted_k_hoc.get(j)) < get_size * required_percentage / (users_tagging + artbn)) {
                        failed = true;
                        break;
                    } else {
                        users_counts += k_hoc.hoc().get(sorted_k_hoc.get(j));
                    }
                }
                if (failed) continue;
                if (k_hoc.time() < 0) continue;
                String[] users = sorted_k_hoc.subList(0, users_tagging).toArray(new String[0]);
                speed_tags.add(new StatLoader.OverallSpeedTag(k_hoc.k(), users, new TimeOutput(k_hoc.time()), users_counts, get_size, artbn != 0,thread.getName()));
            }
            thread.loader.saveStatToBytes(speed_tags, filename);
            tags.addAll(speed_tags);
        }
        return tags;
    }
    public static List<StatLoader.OverallNonUniqueFastestSplitsOutput> fastestSplits(List<Statable> threads, int start_k, int end_k, float required_percentage) {
        if(required_percentage<0||required_percentage>1) throw new IllegalArgumentException();
        List<StatLoader.OverallNonUniqueFastestSplitsOutput> fast_splits = new ArrayList<>();
        for(Statable thread:threads) {
            List<StatLoader.SplitsOutput> splits;
            try {
                splits = thread.getSplits(start_k, end_k);
            } catch (UnsupportedOperationException e) {
                continue;
            }
            List<StatLoader.OverallNonUniqueFastestSplitsOutput> fastestSplits = new ArrayList<>();
            for (StatLoader.SplitsOutput k : splits) {
                for (int i = 0; i < k.splits().size(); i++) {
                    Map<String, Integer> hoc = k.hocs().get(i);
                    int total_counts = k.end_counts().get(i + 1) - k.end_counts().get(i);
                    TimeOutput time = k.splits().get(i);
                    List<String> qualified_users = new ArrayList<>();
                    for (String user : hoc.keySet()) {
                        if (((float) hoc.get(user)) / total_counts < required_percentage) continue;
                        qualified_users.add(user);
                    }
                    fastestSplits.add(new StatLoader.OverallNonUniqueFastestSplitsOutput(time, Math.max(k.end_counts().get(i), 1), k.end_counts().get(i + 1), qualified_users,thread.getName()));
                }
            }
            fast_splits.addAll(fastestSplits);
        }
        return fast_splits;
    }
    public static Map<String, StatLoader.OverallNonUniqueFastestSplitsOutput> fastestSplitsByUser(List<Statable> threads, int start_k, int end_k, float required_percentage) {
        if(required_percentage<0||required_percentage>1) throw new IllegalArgumentException();
        Map<String, StatLoader.OverallNonUniqueFastestSplitsOutput> fastestSplits = new HashMap<>();
        for(Statable thread:threads) {
            List<StatLoader.SplitsOutput> splits;
            try {
                splits = thread.getSplits(start_k, end_k);
            } catch (UnsupportedOperationException e) {
                continue;
            }
            for(StatLoader.SplitsOutput k:splits) {
                for(int i=0;i<k.splits().size();i++) {
                    Map<String, Integer> hoc = k.hocs().get(i);
                    int total_counts = k.end_counts().get(i+1)-k.end_counts().get(i);
                    TimeOutput time = k.splits().get(i);
                    List<String> qualified_users = new ArrayList<>();
                    for(String user:hoc.keySet()) {
                        if(((float)hoc.get(user))/total_counts < required_percentage) continue;
                        qualified_users.add(user);
                    }
                    for(String user:qualified_users) {
                        if (time.time < fastestSplits.getOrDefault(user, new StatLoader.OverallNonUniqueFastestSplitsOutput(new TimeOutput(Long.MAX_VALUE),0,0,null,thread.getName())).time().time) {
                            List<String> other_users = new ArrayList<>(qualified_users);
                            other_users.remove(user);
                            fastestSplits.put(user, new StatLoader.OverallNonUniqueFastestSplitsOutput(time, Math.max(k.end_counts().get(i),1), k.end_counts().get(i+1), other_users,thread.getName()));
                        }
                    }
                }
            }
        }
        return fastestSplits;
    }
    public static Map<String, StatLoader.OverallNonUniqueFastestSplitsOutput> fastestKsByUser(List<Statable> threads, int gets_per_k, int start_k, int end_k, float required_percentage) {
        if(required_percentage<0||required_percentage>1) throw new IllegalArgumentException();
        Map<String, StatLoader.OverallNonUniqueFastestSplitsOutput> fastestKs = new HashMap<>();
        for(Statable thread:threads) {
            List<StatLoader.K_HOC> all_k_hocs = thread.generateAllKHocs(start_k, end_k, gets_per_k);
            for(StatLoader.K_HOC k:all_k_hocs) {
                if(k.time()<0) continue;
                List<String> qualified_users = new ArrayList<>();
                int sum = k.hoc().values().stream().mapToInt(l->l).sum();
                for(String author:k.hoc().keySet()) {
                    if(k.hoc().get(author) >= sum * required_percentage) {
                        qualified_users.add(author);
                    }
                }
                for (String user : qualified_users) {
                    if(k.time() < fastestKs.getOrDefault(user,new StatLoader.OverallNonUniqueFastestSplitsOutput(new TimeOutput(Long.MAX_VALUE),0,0,null, null)).time().time) {
                        List<String> other_qualified = new ArrayList<>(qualified_users);
                        other_qualified.remove(user);
                        fastestKs.put(user, new StatLoader.OverallNonUniqueFastestSplitsOutput(new TimeOutput(k.time()), Math.max(k.end_count()-sum, 1), k.end_count(), other_qualified, thread.getName()));
                    }
                }
            }
        }
        return fastestKs;
    }
    public static List<StatLoader.OverallNonUniqueFastestSplitsOutput> fastestKs(List<Statable> threads, int gets_per_k, int start_k, int end_k, float required_percentage) {
        if(required_percentage<0||required_percentage>1) throw new IllegalArgumentException();
        List<StatLoader.OverallNonUniqueFastestSplitsOutput> fastestKs = new ArrayList<>();
        for(Statable thread:threads) {
            List<StatLoader.K_HOC> all_k_hocs = thread.generateAllKHocs(start_k, end_k, gets_per_k);
            for(StatLoader.K_HOC k:all_k_hocs) {
                if(k.time()<0) continue;
                List<String> qualified_users = new ArrayList<>();
                int sum = k.hoc().values().stream().mapToInt(l->l).sum();
                for(String author:k.hoc().keySet()) {
                    if(k.hoc().get(author) >= sum * required_percentage) {
                        qualified_users.add(author);
                    }
                }
                fastestKs.add(new StatLoader.OverallNonUniqueFastestSplitsOutput(new TimeOutput(k.time()), Math.max(k.end_count()-sum, 1), k.end_count(), qualified_users, thread.getName()));
            }
        }
        return fastestKs;
    }
    public static Map<String, TimeOutput> medianSelfReplyTime(List<Statable> threads, int min_counts, UUID uuid1, UUID uuid2) {
        Map<String, Long> last_reply_time = new HashMap<>();
        Map<String, List<Long>> rip_my_memory = new HashMap<>();
        CombinedCountTimeIterator counts = new CombinedCountTimeIterator(threads, uuid1, uuid2).init();
        while(counts.hasNext()) {
            CountValue count = counts.next();
            String author = count.getAuthor();
            long time = count.getUUID().getTime();
            List<Long> user_reply_times = rip_my_memory.get(author);
            if(user_reply_times == null) {
                rip_my_memory.put(author, new ArrayList<>());
            } else {
                user_reply_times.add(time - last_reply_time.get(author));
            }
            last_reply_time.put(author, time);
        }
        Map<String, TimeOutput> self_reply_times = new HashMap<>();
        for(String user: rip_my_memory.keySet()) {
            if(rip_my_memory.get(user).size() < Math.max(1,min_counts-1)) continue;
            self_reply_times.put(user, new TimeOutput(Util.findMedian(rip_my_memory.get(user))));
        }
        return self_reply_times;
    }
    public static Map<String, StatLoader.OverallPairPB> pair_pbs(List<Statable> threads, int k_start, int k_end, float required_percentage) {
        List<StatLoader.OverallSpeedTag> speedTagList = overall_speed_tags(threads, k_start, k_end, 1, required_percentage, 2);
        Map<String, StatLoader.OverallPairPB> pairPBMap = new HashMap<>();
        for(StatLoader.OverallSpeedTag speedTag: speedTagList) {
            if(speedTag.users().length != 2) continue;
            String user1 = speedTag.users()[0];
            String user2 = speedTag.users()[1];
            String users = user1.compareTo(user2) >= 0 ? user1+"|"+user2 : user2+"|"+user1;
            if(speedTag.speed().compareTo(pairPBMap.getOrDefault(users, new StatLoader.OverallPairPB(0,new TimeOutput(Long.MAX_VALUE), 0, 0,null)).speed()) < 0) {
                pairPBMap.put(users, new StatLoader.OverallPairPB(speedTag.thread_num(),speedTag.speed(),speedTag.counts_users(),speedTag.counts_in_k(), speedTag.thread_name()));
            }
        }
        return pairPBMap;
    }
    public static List<StatLoader.OverallMinSplitStdDev> speedMinSplitStdDev(List<Statable> threads, int start_k, int end_k, float stdDevLimit, float required_percentage) {
        if(required_percentage<0||required_percentage>1) throw new IllegalArgumentException();
        List<StatLoader.OverallMinSplitStdDev> minSplitStdDevs = new ArrayList<>();
        for(Statable thread:threads) {
            List<StatLoader.SplitsOutput> splits;
            try {
                splits = thread.getSplits(start_k, end_k);
            } catch (UnsupportedOperationException e) {
                continue;
            }
            int current_k = thread.getCurrentK(1);
            for (StatLoader.SplitsOutput k : splits) {
                List<List<String>> qualified_users = new ArrayList<>();
                for (int i = 0; i < k.splits().size(); i++) {
                    Map<String, Integer> hoc = k.hocs().get(i);
                    int total_counts = k.end_counts().get(i + 1) - k.end_counts().get(i);
                    List<String> split_qualified_users = new ArrayList<>();
                    for (String user : hoc.keySet()) {
                        if (((float) hoc.get(user)) / total_counts < required_percentage) continue;
                        split_qualified_users.add(user);
                    }
                    qualified_users.add(split_qualified_users);
                }
                float stddev = (float) Util.stdDev(k.splits()) / 10000000;
                long speed = 0;
                if (stddev < stdDevLimit && k.k() != current_k) {
                    HashSet<String> unique_users = new HashSet<>();
                    for (int l = 0; l < k.splits().size(); l++) {
                        unique_users.addAll(qualified_users.get(l));
                        speed += k.splits().get(l).time;
                    }
                    minSplitStdDevs.add(new StatLoader.OverallMinSplitStdDev(k.k(), new TimeOutput(speed), stddev, new ArrayList<>(unique_users), thread.getName()));
                }
            }
        }
        return minSplitStdDevs;
    }
    public static Map<String, StatLoader.CountStreakOutputWithThread> overall_k_streak(List<Statable> threads, int start_k, int end_k, int gets_per_k, boolean is_current) {
        Map<String, StatLoader.CountStreakOutputWithThread> k_streak = new HashMap<>();
        for(Statable thread:threads) {
            List<StatLoader.K_HOC> all_k_hocs = thread.generateAllKHocs(start_k, end_k, gets_per_k);
            Map<String, Integer> current_k_streak = new HashMap<>();
            int current_k = 0;
            for(StatLoader.K_HOC k_hoc:all_k_hocs) {
                current_k = k_hoc.k();
                Set<String> active_streaks = new HashSet<>(current_k_streak.keySet());
                for (String user:k_hoc.hoc().keySet()) {
                    int current = current_k_streak.getOrDefault(user, 0);
                    current_k_streak.put(user, current + 1);
                    active_streaks.remove(user);
                }
                for (String user:active_streaks) {
                    if(!is_current) {
                        StatLoader.CountStreakOutputWithThread best = k_streak.getOrDefault(user, new StatLoader.CountStreakOutputWithThread(0, 0, 0, null));
                        int current = current_k_streak.get(user);
                        if (current > best.size()) {
                            k_streak.put(user, new StatLoader.CountStreakOutputWithThread(current_k - current, current_k - 1, current, thread.getName()));
                        }
                    }
                    current_k_streak.remove(user);
                }
            }
            for(String user:current_k_streak.keySet()) {
                StatLoader.CountStreakOutputWithThread best = k_streak.getOrDefault(user, new StatLoader.CountStreakOutputWithThread(0, 0, 0,null));
                int current = current_k_streak.get(user);
                if(current > best.size()) {
                    k_streak.put(user, new StatLoader.CountStreakOutputWithThread(current_k-current+1, current_k, current,thread.getName()));
                }
            }
        }
        return k_streak;
    }
    public static List<StatLoader.NonUniqueCountStreakWithThread> overall_non_unique_k_streak(List<Statable> threads, int start_k, int end_k, int gets_per_k) {
        List<StatLoader.NonUniqueCountStreakWithThread> k_streaks = new ArrayList<>();
        for(Statable thread:threads) {
            List<StatLoader.K_HOC> all_k_hocs = thread.generateAllKHocs(start_k, end_k, gets_per_k);
            Map<String, Integer> current_k_streak = new HashMap<>();
            int i=1;
            for(StatLoader.K_HOC k_hoc:all_k_hocs) {
                Set<String> active_streaks = new HashSet<>(current_k_streak.keySet());
                for (String user:k_hoc.hoc().keySet()) {
                    int current = current_k_streak.getOrDefault(user, 0);
                    current_k_streak.put(user, current + 1);
                    active_streaks.remove(user);
                }
                for (String user:active_streaks) {
                    int current = current_k_streak.get(user);
                    k_streaks.add(new StatLoader.NonUniqueCountStreakWithThread(user, i-current, i-1, current,thread.getName()));
                    current_k_streak.remove(user);
                }
                i++;
            }
            for(String user:current_k_streak.keySet()) {
                int current = current_k_streak.get(user);
                k_streaks.add(new StatLoader.NonUniqueCountStreakWithThread(user, i-current, i-1, current,thread.getName()));
            }
        }
        return k_streaks;
    }
    public static List<StatLoader.NonUniqueTimeStreakWithThread> overall_non_unique_nth_place_streak(List<Statable> threads, int n, String start_date, String end_date, ParticipationDuration mode) {
        if(n<=0) throw new IllegalArgumentException("n must be >= 1");
        List<StatLoader.NonUniqueTimeStreakWithThread> nth_place_streaks = new ArrayList<>();
        for(Statable thread: threads) {
            List<StatLoader.DayHOC> all_day_hocs = combined_all_day_hocs(List.of(thread),start_date,end_date,mode);
            Map<String, Integer> current_streak = new HashMap<>();
            TimeUnit current_day = null;
            for(StatLoader.DayHOC day_hoc:all_day_hocs) {
                current_day = day_hoc.date();
                List<List<String>> medalists = getMedalists(day_hoc.hoc(),day_hoc.hoc().size());
                Set<String> dead_streaks = new HashSet<>(current_streak.keySet());
                if(medalists.size() >= n) {
                    for (String user : medalists.get(n-1)) {
                        int current = current_streak.getOrDefault(user, 0);
                        current_streak.put(user, current + 1);
                        dead_streaks.remove(user);
                    }
                }
                for (String user : dead_streaks) {
                    int current = current_streak.get(user);
                    nth_place_streaks.add(new StatLoader.NonUniqueTimeStreakWithThread(user, current_day.subtractTime(current).toString(), current_day.subtractTime(1).toString(), current, thread.getName()));
                    current_streak.remove(user);
                }
            }
            for(String user:current_streak.keySet()) {
                int current = current_streak.get(user);
                nth_place_streaks.add(new StatLoader.NonUniqueTimeStreakWithThread(user, current_day.subtractTime(current-1).toString(), current_day.toString(), current, thread.getName()));
            }
        }
        return nth_place_streaks;
    }
    public static Map<String, Integer> recordHourNoMistakes(List<Statable> threads, UUID uuid1, UUID uuid2, int duration_seconds) {
        if(duration_seconds<=0 || 3600%duration_seconds!=0) duration_seconds = 3600;
        Instant moment = Instant.ofEpochMilli(0);
        Map<String, Integer> hoc = new HashMap<>();
        Map<String, Integer> current_day_parts = new HashMap<>();
        UpdateIterator counts = new UpdateIterator(threads.get(0), uuid1, uuid2).init();
        boolean fail = false;
        while(counts.hasNext()) {
            UpdateValue count = counts.next();
            if(!count.isIsCount()) continue;
            if(count.isStricken()) {
                fail = true;
                continue;
            }
            Instant new_moment = Instant.ofEpochMilli(count.getUUID().getTime()/10000);
            if (new_moment.toEpochMilli()-duration_seconds*1000L > moment.toEpochMilli()) {
                for(String user: current_day_parts.keySet()) {
                    if(!fail && current_day_parts.get(user) > hoc.getOrDefault(user, 0)) {
                        hoc.put(user, current_day_parts.get(user));
                    }
                }
                fail = false;
                current_day_parts = new HashMap<>();
                moment = new_moment.minusMillis(new_moment.toEpochMilli()%(duration_seconds*1000L));
            }
            String author = LocalDateTime.ofInstant(moment, ZoneId.of(counts.getThread().timezone)).format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
            int counted = current_day_parts.getOrDefault(author,0);
            current_day_parts.put(author, counted+1);
        }
        for(String user: current_day_parts.keySet()) {
            if(!fail && current_day_parts.get(user) > hoc.getOrDefault(user, 0)) {
                hoc.put(user, current_day_parts.get(user));
            }
        }
        return hoc;
    }
}
