package Stats;

import Stats.StatContainer.StatSaveMode;
import Stats.StatLoader.DayHOC;
import Stats.StatLoader.SplitHOC;
import Stats.StatLoader.SplitWithHOC;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CombinedStats {
    private static Map<CounterData, Map<String, List<StatLoader.TransitionMatrixEntry>>> transition_matrices = new HashMap<>();
    public static Map<CounterData, Set<String>> first_words = new HashMap<>();
    static {
        generateTransitionMatrices();
    }
    public static String lcbotQuote(String user, int length) {
        if(!Counters.username_lookup.containsKey(user)) throw new IllegalArgumentException("User does not exist");
        CounterData counter = Counters.counters.get(Counters.username_lookup.get(user));
        Map<String, List<StatLoader.TransitionMatrixEntry>> user_transition_matrix = transition_matrices.getOrDefault(counter, null);
        Set<String> user_first_words = first_words.getOrDefault(counter, null);
        if(user_transition_matrix==null) throw new IllegalArgumentException("User has not made any comments");
        String best = "";
        int longest = 0;
        for(int j=0;j<10;j++) {
            int current_length;
            StringBuilder quote = new StringBuilder();
            Random random = new Random();
            String current = user_first_words
                    .stream()
                    .skip(random.nextInt(user_first_words.size()))
                    .findFirst()
                    .orElseThrow();
            quote.append(current).append(" ");
            for (current_length = 1; current_length < length || (current.length()<=3 && current_length < length+5); current_length++) {
                List<StatLoader.TransitionMatrixEntry> next_word_freq = user_transition_matrix.get(current);
                if (next_word_freq == null) {
                    if(current_length>longest) {
                        longest = current_length;
                        best = quote.toString();
                    }
                    break;
                }
                double rand = Math.random();
                int current_loc = 0;
                for (StatLoader.TransitionMatrixEntry next : next_word_freq) {
                    current_loc++;
                    if (next.chance() > rand) break;
                }
                current = next_word_freq.get(current_loc - 1).output();
                quote.append(current).append(" ");
                current = Util.toLowercaseAlphanumeric(current);
            }
            if(current_length>longest) {
                longest = current_length;
                best = quote.toString();
            }
            if(longest >= length) break;
        }
        return best+" - "+user;
    }
    public static void generateTransitionMatrices() {
        String filename = "SidethreadData/all_threads/stats_data/transition_matrices";
        String filename2 = "SidethreadData/all_threads/stats_data/first_words";
        transition_matrices = new HashMap<>();
        first_words = new HashMap<>();
        Map<CounterData, Map<String, List<StatLoader.TransitionMatrixEntry>>> data = StatLoader.loadFromBytes(filename);
        Map<CounterData, Set<String>> data2 = StatLoader.loadFromBytes(filename2);
        if (data != null) {
            transition_matrices = data;
            first_words = data2;
            return;
        }
        Map<CounterData, Map<String, Map<String, Integer>>> comment_freq = generateCommentFrequency();
        for(CounterData user: comment_freq.keySet()) {
            Map<String, List<StatLoader.TransitionMatrixEntry>> user_transition_matrix = new HashMap<>();
            transition_matrices.put(user, user_transition_matrix);
            Map<String, Map<String, Integer>> user_comment_freq = comment_freq.get(user);
            for(String word: user_comment_freq.keySet()) {
                List<StatLoader.TransitionMatrixEntry> entry = new ArrayList<>();
                user_transition_matrix.put(word, entry);
                Map<String, Integer> next_word_freq = user_comment_freq.get(word);
                int total = 0;
                for(int k: next_word_freq.values()) total+=k;
                float running_total = 0f;
                for(String next: next_word_freq.keySet()) {
                    float prob = next_word_freq.get(next) / (float) total;
                    running_total+=prob;
                    entry.add(new StatLoader.TransitionMatrixEntry(running_total, next));
                }
            }
        }
        StatLoader.saveToBytes(transition_matrices, filename);
        StatLoader.saveToBytes(first_words, filename2);
    }
    public static Map<CounterData, Map<String, Map<String, Integer>>> generateCommentFrequency() {
        Map<CounterData, Map<String, Map<String, Integer>>> data = new HashMap<>();
        UpdateIteratorWithComment updates = new UpdateIteratorWithComment(List.of(AllThreads.combined_thread),UUID.minUUID(),UUID.maxUUID(), false).init();
        while(updates.hasNext()) {
            UpdateOrCountValueWithComment current = updates.next();
            if(current.getComment()==null) continue;
            CounterData author = current.getAuthor();
            Map<String, Map<String, Integer>> current_map = data.computeIfAbsent(author, k->new HashMap<>());
            Set<String> current_first_words = first_words.computeIfAbsent(author, k->new HashSet<>());
            String[] comment = current.getComment().split("[ \n\r\t]");
            for(int i=0;i<comment.length-1;i++) {
                String current_word = Util.toLowercaseAlphanumeric(comment[i]);
                if(!current_word.isEmpty() && (current_word.charAt(0) < '0' || current_word.charAt(0) > '9')) {
                    current_first_words.add(comment[i]);
                }
                Map<String, Integer> current_map_2 = current_map.computeIfAbsent(current_word, k->new HashMap<>());
                int freq = current_map_2.getOrDefault(comment[i+1],0)+1;
                current_map_2.put(comment[i+1],freq);
            }
        }
        return data;
    }

    public static StatContainer hoc(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        return hocWithFilter("HoC",use_counts?"Counts":"Updates",k->true,threads, uuid1, uuid2, unique, use_counters, use_counts, thread_counts_combined, false);
    }
    private static StatContainer hocWithFilter(String name, String counts_name, Predicate<UpdateOrCountValue> filter, List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined, boolean include_comment) {
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", counts_name};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{0,null};
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, o-> Arrays.copyOf(o,o.length-1), default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, include_comment, false, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            if(filter.test(count)) current[0] = (int)current[0]+1;
        }
        return hoc.finalizeStat();
    }
    public static StatContainer hoc(List<UpdateOrCountValue> counts, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = "HoC from list";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", use_counts?"Counts":"Updates"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{0,null};
        ListStatGenerator<UpdateOrCountValue> hoc = new ListStatGenerator<>(counts, o-> Arrays.copyOf(o,o.length-1), default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(use_counters ? count.getAuthorId() : count.getThreadId(), count.getThreadId());
            current[0] = (int)current[0]+1;
        }
        return hoc.finalizeStat();
    }
    public static StatContainer countsWithString(List<CountingThread> threads, UUID uuid1, UUID uuid2, String search, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = (use_counts?"Counts With String \"":"Updates With String \"")+search+"\"";
        Predicate<UpdateOrCountValue> filter = k -> {
            UpdateOrCountValueWithComment count = (UpdateOrCountValueWithComment) k;
            return count.comment().comment()!=null && count.comment().comment().contains(search);
        };
        return hocWithFilter(name, use_counts?"Counts":"Updates", filter, threads, uuid1, uuid2, unique, use_counters, use_counts, thread_counts_combined, true);
    }
    public static StatContainer strikeHoc(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        return hocWithFilter("Strike HoC", "Strikes", UpdateOrCountValue::stricken, threads, uuid1, uuid2, unique, use_counters, false, thread_counts_combined, false);
    }
    public static StatContainer participation(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean unique, boolean use_counters, boolean thread_counts_combined, boolean add_thread_stats) {
        String name = "Participation";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Score", "Counts", "Gets", "Assists", "K Parts", "Day Parts"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER};
        int[] sort_order = new int[]{3,4};
        Object[] default_object = new Object[]{0,0,0,0, null, 0, 0L, null}; //Counts, Gets, Assists, K Parts, List of Ks Participated in, Day Parts, Last Day Part (timestamp)
        Function<Object[], Object[]> finalize = k -> new Object[]{(int)k[0]*2+(int)k[1]*500+(int)k[2]*500+(int)k[3]*250+(int)k[5]*250, k[0], k[1], k[2], k[3], k[5]};
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, true, thread_counts_combined, false, add_thread_stats, StatSaveMode.NO_CONTINUE);
        long current_day_timestamp = 0L;
        while(hoc.hasNext()) {
            CountValue count = (CountValue)hoc.next();
            Object[] current = hoc.getTempObject(count);
            current[0] = (int)current[0]+1;
            if(count.isGet(0,1)) current[1] = (int)current[1]+1;
            if(count.isGet(1,1)) current[2] = (int)current[2]+1;
            if(current[4]==null) current[4] = new ArrayList<>();
            List<Boolean> k_parts = (List<Boolean>)current[4];
            int k = count.getK(1);
            while(k_parts.size()<=k) k_parts.add(false);
            if(!k_parts.get(k)) {
                k_parts.set(k, true);
                current[3] = (int)current[3]+1;
            }
            if(hoc.switched()) current_day_timestamp = 0L;
            if(count.getTimestamp() > current_day_timestamp) {
                current_day_timestamp = Util.getNextDayTimestamp(count.getTimestamp(), CountingThread.timezone, ParticipationDuration.DAY);
            }
            if(current_day_timestamp!=(long)current[6]) {
                current[5] = (int)current[5]+1;
                current[6] = current_day_timestamp;
            }
        }
        return hoc.finalizeStat();
    }
    public static StatContainer firstCounts(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean first_counts, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = (first_counts?"First ":"Last ")+(use_counts?"Counts":"Updates");
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", use_counts?"Count":"Update"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.COUNT_OR_UPDATE};
        int[] sort_order = new int[]{first_counts?-3:3};
        Object[] default_object = new Object[]{first_counts ? UpdateOrCountValue.getDefaultObjectWithMaxTimestamp(use_counts) : UpdateOrCountValue.getDefaultObject(use_counts),null};
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, o-> Arrays.copyOf(o,o.length-1), default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false, false, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            if(first_counts && current[0].equals(default_object[0])) current[0] = count;
            else if(!first_counts && ((UpdateOrCountValue)current[0]).compareTo(count) < 0) current[0] = count;
        }
        return hoc.finalizeStat();
    }
    public static StatContainer palindromes(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        Predicate<UpdateOrCountValue> filter = k -> {
            UpdateOrCountValueWithComment count = (UpdateOrCountValueWithComment) k;
            String raw_count = count.comment().rawCount();
            int i = 0;
            int j = raw_count.length() - 1;
            while (i < j) {
                if (raw_count.charAt(i) != raw_count.charAt(j)) return false;
                i++;
                j--;
            }
            return true;
        };
        return hocWithFilter("Palindromes", "Palindromes", filter, threads, uuid1, uuid2, unique, use_counters, true, thread_counts_combined, true);
    }
    public static StatContainer repdigits(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        Predicate<UpdateOrCountValue> filter = k -> {
            UpdateOrCountValueWithComment count = (UpdateOrCountValueWithComment) k;
            String raw_count = count.comment().rawCount();
            for(int i=raw_count.length()/2;i>=1;i--) {
                boolean finished = true;
                if(raw_count.length()%i!=0) continue;
                for(int j=i;j<raw_count.length();j+=i) {
                    if (!raw_count.substring(0, i).equals(raw_count.substring(j, j+i))) {
                        finished = false;
                        break;
                    }
                }
                if(finished) return true;
            }
            return false;
        };
        return hocWithFilter("Rep Digits", "Rep Digits", filter, threads, uuid1, uuid2, unique, use_counters, true, thread_counts_combined, true);
    }
    public static StatContainer nRep(List<CountingThread> threads, UUID uuid1, UUID uuid2, int n, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        Predicate<UpdateOrCountValue> filter = k -> {
            UpdateOrCountValueWithComment count = (UpdateOrCountValueWithComment) k;
            String raw_count = count.comment().rawCount();
            if(raw_count.length()<n) return false;
            char rep_number = raw_count.charAt(raw_count.length()-1);
            for(int i = raw_count.length()-2;i>=raw_count.length()-n;i--) {
                if(raw_count.charAt(i)!=rep_number) return false;
            }
            return true;
        };
        return hocWithFilter(n+"-Reps", n+"-Reps", filter, threads, uuid1, uuid2, unique, use_counters, true, thread_counts_combined, true);
    }
    public static StatContainer baseN(List<CountingThread> threads, UUID uuid1, UUID uuid2, int n, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        Predicate<UpdateOrCountValue> filter = k -> {
            if(n<0 || n > 64) return false;
            UpdateOrCountValueWithComment count = (UpdateOrCountValueWithComment) k;
            String raw_count = count.comment().rawCount();
            if(n<=36) raw_count = raw_count.toUpperCase();
            //ASCII 48-57, 65-90, 97-122, 43, 47
            for(char x:raw_count.toCharArray()) {
                if(48 <= x && x <= 57) x-=48;
                else if(65 <= x && x <= 90) x-=55;
                else if(97 <= x && x <= 122) x-=61;
                else if(x==43) x=62;
                else if(x==47) x=63;
                else return false;
                if(x>=n) return false;
            }
            return true;
        };
        return hocWithFilter("Base "+n+" Counts", "Base "+n+" Counts", filter, threads, uuid1, uuid2, unique, use_counters, true, thread_counts_combined, true);
    }
    public static StatContainer sumOfCounts(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        String name = "Sum of Counts";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Sum"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{0L, null}; //Sum
        Function<Object[], Object[]> finalize = k -> new Object[]{k[0]};
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, true, thread_counts_combined, false, false, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            current[0] = (long)current[0] + count.validCountNumber();
        }
        return hoc.finalizeStat();
    }
    public static StatContainer sumOfReciprocals(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        String name = "Sum of Reciprocals";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Sum"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{0.0d, null}; //Sum
        Function<Object[], Object[]> finalize = k -> new Object[]{k[0]};
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, true, thread_counts_combined, false, false, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            current[0] = (double)current[0] + 1.0d/count.validCountNumber();
        }
        return hoc.finalizeStat();
    }
    public static StatContainer nthCount(List<CountingThread> threads, UUID uuid1, UUID uuid2, int n, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = (use_counts?"Count ":"Update ")+n;
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", use_counts?"Count":"Update"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.COUNT_OR_UPDATE};
        int[] sort_order = new int[]{-3};
        Object[] default_object = new Object[]{0, null, null}; //Counts, Count
        Function<Object[], Object[]> finalize = k -> {
            if((int)k[0] < n) return new Object[]{UpdateOrCountValue.getDefaultObjectWithMaxTimestamp(use_counts)};
            return new Object[]{k[1]};
        };
        if(!use_counters && (!thread_counts_combined || threads.size()==1 || threads.size() == AllThreads.all_threads.size())) {
            return nthCountFast(threads, name, headers, types, sort_order, uuid1, uuid2, n, use_counts, thread_counts_combined);
        }
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false, false, StatSaveMode.CONTINUE);
        if(n<1) return hoc.finalizeStat();
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            if((int)current[0] < n) {
                current[0] = (int)current[0] + 1;
                current[1] = count;
            }
        }
        return hoc.finalizeStat();
    }
    //This directly accesses the nth count instead of iterating through the entire db. However, it can only be used with use_counters=false and either 1 thread, all threads, or thread_counts_combined=false
    private static StatContainer nthCountFast(List<CountingThread> threads, String name, String[] headers, StatDataType[] types, int[] sort_order, UUID uuid1, UUID uuid2, int n, boolean use_counts, boolean thread_counts_combined) {
        if(thread_counts_combined && !(threads.size()==1 || threads.size()==AllThreads.all_threads.size())) throw new IllegalArgumentException("Must use all threads, 1 thread, or not combine threads");
        StatContainer nth_count = new StatContainer(new Object[]{0,0,UpdateOrCountValue.getDefaultObjectWithMaxTimestamp(use_counts)}, name, headers, types, sort_order, false, false, use_counts, thread_counts_combined, false, StatSaveMode.NO_SAVE);
        CountDataType type = use_counts ? CountDataType.COUNT : CountDataType.UPDATE;
        if(threads.size()==AllThreads.all_threads.size() && thread_counts_combined) {
            long first_count_timestamp = AllThreads.combined_thread.getFirstUpdateTimestamp(type);
            int offset = -1;
            if(uuid1.getTime() > first_count_timestamp) {
                Location loc = AllThreads.combined_thread.getLocationOfFirstCountOrUpdateAfterTimestamp(uuid1, type, false);
                offset = loc.counts_by_file()+loc.loc_in_file();
            }
            UpdateOrCountValue count = AllThreads.combined_thread.getUpdateOrCount(n+offset, type);
            if(count==null || count.getTimestamp() >= uuid2.getTime()) return nth_count;
            nth_count.addTempObjectDirect(0, 0, 0, count);
            nth_count.finalizeObjectDirect(0, k->k);
        } else for(CountingThread thread:threads) {
            long first_count_timestamp = thread.getFirstUpdateTimestamp(type);
            int offset = -1;
            if(uuid1.getTime() > first_count_timestamp) {
                Location loc = thread.getLocationOfFirstCountOrUpdateAfterTimestamp(uuid1, type, false);
                if(loc==null) continue;
                offset = loc.counts_by_file()+loc.loc_in_file();
            }
            UpdateOrCountValue count = thread.getUpdateOrCount(n+offset, type);
            if(count==null || count.getTimestamp() >= uuid2.getTime()) continue;
            nth_count.addTempObjectDirect(count.getThreadId(), count.getThreadId(), count.getThreadId(), count);
            nth_count.finalizeObjectDirect(count.getThreadId(), k->k);
        }
        return nth_count;
    }
    public static StatContainer threadParts(List<CountingThread> threads, UUID uuid1, UUID uuid2, int min_counts, boolean use_counts) {
        String name = "Thread Parts"+(min_counts>1?" (Minimum "+min_counts+(use_counts?" Counts":" Updates"):"");
        String[] headers = new String[]{"User", "Thread", "Amount"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{0, null, null}; //Amount, Array of Threads Participated In
        Function<Object[], Object[]> finalize = k -> new Object[]{k[0]};
        List<Object[]> hoc = hoc(threads, uuid1, uuid2, false, true, use_counts, false).getStat();
        ListStatGenerator<Object[]> thread_parts = new ListStatGenerator<>(hoc, finalize, default_object, name, headers, types, sort_order, true, true, use_counts, true, false);
        while(thread_parts.hasNext()) {
            Object[] hoc_entry = thread_parts.next();
            if((int)hoc_entry[2]<min_counts) continue;
            Object[] current = thread_parts.getTempObject((int) hoc_entry[0], (int) hoc_entry[1]);
            if(current[1]==null) current[1] = new boolean[AllThreads.all_threads.size()];
            boolean[] thread_arr = (boolean[])current[1];
            int thread_index = ((int)hoc_entry[2])-1;
            if(thread_arr[thread_index]) continue;
            thread_arr[thread_index] = true;
            current[0] = (int) current[0] + 1;
        }
        return thread_parts.finalizeStat();
    }
    /**
     * @param sort_mode 0: Sort by # of attempts (Descending)
     *                  1: Sort by # of attempts (Ascending)
     *                  2: Sort by % of getting the count by the successful attempt (Descending)
     *                  3: Sort by # of getting the count by the successful attempt (Ascending)
     */
    public static StatContainer attemptsPerCount(List<CountingThread> threads, UUID uuid1, UUID uuid2, int sort_mode, boolean include_current, boolean exclude_100, boolean unique, boolean use_counters) {
        String name = "Attempts Per Count ";
        switch(sort_mode) {
            case 1: name+="Sort by Attempts (Ascending)";
            case 2: name+="Sorted by Probability (Descending)";
            case 3: name+="Sorted by Probability (Ascending)";
            default: name+="Sorted By Attempts (Descending)";
        }
        if(exclude_100) name+=" (100% Scores Excluded)";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Attempts", "Probability", "Count"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.PERCENT, StatDataType.COUNT};
        int[] sort_order = switch (sort_mode) {
            case 1 -> new int[]{-3,-4,-5};
            case 2 -> new int[]{4,3,-5};
            case 3 -> new int[]{-4,-3,-5};
            default -> new int[]{3,4,-5};
        };
        Object[] default_object = new Object[]{0, 1.0, null, null}; //Attempts, Probability, Count
        Function<Object[], Object[]> finalize = k -> {
            if((int)k[0] == 0 || (k[2]==null&&!include_current)) {
                return switch(sort_mode) {
                    case 1 -> new Object[]{Integer.MAX_VALUE, 0.0f, k[2]};
                    case 2 -> new Object[]{0, -1.0f, k[2]};
                    case 3 -> new Object[]{0, 2.0f, k[2]};
                    default -> new Object[]{0, 0.0f, k[2]};
                };
            }
            return new Object[]{k[0], (float)(1-(double)k[1]), k[2]};
        };
        for(CountingThread thread:threads) thread.clearCountSuccessChance();
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, false, false, true, false, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            UpdateOrCountValueWithComment count = (UpdateOrCountValueWithComment) hoc.next();
            Object[] current = hoc.getTempObject(count);
            double probability = count.getThread().countSuccessChance(count);
            if(probability!=0.0) {
                current[1] = (double) current[1] * (1-probability);
                current[0] = (int) current[0] + 1;
            }
            if(count.isCount()) {
                current[2] = count.update();
                if(!exclude_100 || (double)current[1]!=0.0) hoc.finalizeObject(count);
                current[0] = 0;
                current[1] = 1.0;
                current[2] = null;
            }
        }
        return hoc.finalizeStat();
    }

    public static StatContainer fastestSelfReplies(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean fast, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = fast?"Fastest Self Replies":"Slowest Self Replies";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Time", "Start Count", "End Count"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.TIME, StatDataType.COUNT_OR_UPDATE, StatDataType.COUNT_OR_UPDATE};
        int[] sort_order = new int[]{fast?-3:3, -4};
        Object[] default_object = new Object[]{fast?Long.MAX_VALUE:0L,UpdateOrCountValue.getDefaultObject(use_counts),null, null}; //Time, First Count, Last Count
        Function<Object[], Object[]> finalize = k -> new Object[]{k[0],k[1],k[2]};
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false, false, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            if(((UpdateOrCountValue)current[1]).getTimestamp()!=0L) {
                current[0] = count.getTimestamp() - ((UpdateOrCountValue) current[1]).getTimestamp();
                current[2] = count;
                hoc.finalizeObject(count);
                current[0] = fast ? Long.MAX_VALUE : 0L; //Reinitialize to avoid duplication
            }
            current[1] = count;
        }
        return hoc.finalizeStat();
    }
    public static StatContainer timeSpentHavingLastCount(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined, boolean add_thread_stats) {
        String name = "Time Spent Having Last Count";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Time"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.TIME};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{0L,null,null}; //Time, Most Recent Count
        UpdateOrCountValue[] last_count = new UpdateOrCountValue[1];
        Function<Object[], Object[]> finalize = k -> {
            if(k[1]!=null && k[1] == last_count[0]) {
                long last_update_timestamp = AllThreads.combined_thread.getLastUpdateTimestamp(CountDataType.UPDATE);
                if(last_update_timestamp > uuid2.getTime()) {
                    last_update_timestamp = uuid2.getTime();
                }
                k[0] = (long)k[0] + (last_update_timestamp - last_count[0].getTimestamp());
            }
            return new Object[]{k[0]};
        };
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false, add_thread_stats, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            current[1] = count;
            if(hoc.switched()) last_count[0] = null;
            if(last_count[0]!=null) {
                current = hoc.getTempObject(last_count[0]);
                current[0] = (long)current[0] + (count.getTimestamp() - last_count[0].getTimestamp());

            }
            last_count[0] = count;
        }
        return hoc.finalizeStat();
    }
    public static StatContainer timeBetweenHoCAmounts(List<CountingThread> threads, UUID uuid1, UUID uuid2, int n1, int n2, boolean fastest, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = (fastest?"Fastest ":"Slowest ")+"Time Between "+n1+(use_counts?" Counts":" Updates")+" and "+n2+(use_counts?" Counts":" Updates");
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Speed", "Start Count", "End Count"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.TIME, StatDataType.COUNT_OR_UPDATE, StatDataType.COUNT_OR_UPDATE};
        int[] sort_order = new int[]{fastest?-3:3};
        Object[] default_object = new Object[]{0, null, null, null}; //Current Counts, Start Count, End Count
        Function<Object[], Object[]> finalize = k -> {
            if(k[2]==null) return new Object[]{fastest?Long.MAX_VALUE:0L, null, null};
            return new Object[]{((UpdateOrCountValue)k[2]).getTimestamp()-((UpdateOrCountValue)k[1]).getTimestamp(), k[1], k[2]};
        };
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false, false, StatSaveMode.CONTINUE);
        if(n1<0 || n2<=0 || n2<=n1) return hoc.finalizeStat();
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            if((int)current[0] > n2) continue;
            current[0] = (int) current[0] + 1;
            if((int)current[0] == n2) current[2] = count;
            if((int)current[0] == n1) current[1] = count;
        }
        return hoc.finalizeStat();
    }

    /**HoC must be of the form FOCUS, THREAD_INSERT, NUMBER*/
    private static List<List<Object[]>> getMedalists(StatContainer hoc, int max_position) {
        List<List<Object[]>> medalists = new ArrayList<>();
        while(medalists.size()<max_position) medalists.add(new ArrayList<>());
        Map<Integer, List<Object[]>> sorted_hoc_by_thread = new HashMap<>();
        for(Object[] o: hoc.getStat()) {
            int thread = hoc.useThreadInsert() ? (int) o[1] : 0;
            List<Object[]> sorted_hoc = sorted_hoc_by_thread.computeIfAbsent(thread, k -> new ArrayList<>());
            sorted_hoc.add(o);
        }
        for(int thread: sorted_hoc_by_thread.keySet()) {
            int last_amount = 0;
            int last_position = 0;
            List<Object[]> sorted_hoc = sorted_hoc_by_thread.get(thread);
            for(int i=0;i<sorted_hoc.size();i++) {
                Object[] current = sorted_hoc.get(i);
                int current_amount = (int)current[2];
                if(current_amount!=last_amount) {
                    last_position = i;
                }
                if(last_position >= max_position) break;
                medalists.get(last_position).add(current);
                last_amount = current_amount;
            }
        }
        return medalists;
    }
    public static List<DayHOC> dayHocs(List<CountingThread> threads, TimeUnit start_date, TimeUnit end_date, ParticipationDuration mode, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = mode.toCapitalizedString()+" HoC";
        start_date = TimeUnit.getMinTimeUnitOfThreads(threads, start_date, mode);
        TimeUnit cgg_start_date = TimeUnit.getCGGStartDate(mode);
        end_date = TimeUnit.getMaxTimeUnitOfThreads(threads, end_date, mode);
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", use_counts?"Counts":"Updates"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{0, null};
        MultiStatGenerator hoc = new MultiStatGenerator(threads, cgg_start_date, TimeUnit.today(mode), mode, o-> Arrays.copyOf(o,o.length-1), default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, StatSaveMode.CONTINUE);
        List<DayHOC> day_hocs = new ArrayList<>();
        long current_day_timestamp = 0L;
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            if(hoc.switched() || count.getTimestamp() > current_day_timestamp) {
                hoc.setIndex((int)TimeUnit.timeBetween(cgg_start_date, new TimeUnit(count.getTimestamp(), CountingThread.timezone, mode), mode));
                current_day_timestamp = Util.getNextDayTimestamp(count.getTimestamp(), CountingThread.timezone, mode);
            }
            Object[] current = hoc.getTempObject(count);
            current[0] = (int)current[0]+1;
        }
        List<StatContainer> hocs_without_date = hoc.finalizeStat();
        TimeUnit current_date = start_date;
        int start = (int)TimeUnit.timeBetween(cgg_start_date, start_date, mode);
        for(int i=start;i<start+TimeUnit.timeBetween(start_date, end_date, mode);i++) {
            if(i>=hocs_without_date.size()) break;
            day_hocs.add(new DayHOC(current_date, hocs_without_date.get(i)));
            current_date = current_date.addTime(1);
        }
        return day_hocs;
    }
    public static StatContainer dayHoc(List<CountingThread> threads, TimeUnit start_date, ParticipationDuration mode, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        List<DayHOC> day_hoc = dayHocs(threads, start_date, start_date.addTime(1), mode, unique, use_counters, use_counts, thread_counts_combined);
        if(day_hoc.isEmpty()) {
            return new StatContainer(null, mode.toCapitalizedString()+" HoC", new String[0], new StatDataType[0], new int[0], unique, use_counters, use_counts, thread_counts_combined, false, StatSaveMode.NO_SAVE);
        } else return day_hoc.get(0).hoc();
    }
    public static StatContainer dayParts(List<CountingThread> threads, TimeUnit start_date, TimeUnit end_date, ParticipationDuration mode, int min_counts, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined, boolean add_thread_stats) {
        String name = mode.toCapitalizedString()+" Parts";
        if(min_counts>1) name+=" with "+min_counts+(use_counts?" Counts":" Updates");
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", mode.toCapitalizedString()+" Parts"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_temp_object = new Object[]{0, null}; //Day Parts
        Function<Object[], Object[]> finalize = o->new Object[]{o[0]};
        List<DayHOC> day_hocs = dayHocs(threads, start_date, end_date, mode, false, use_counters, use_counts, thread_counts_combined);
        ListStatGenerator<DayHOC> day_parts = new ListStatGenerator<>(day_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, add_thread_stats);
        while(day_parts.hasNext()) {
            DayHOC day = day_parts.next();
            List<Object[]> hoc = day.hoc().getStat();
            for(Object[] user: hoc) {
                Object[] current = day_parts.getTempObject((int) user[0], (int) user[1]);
                if((int)user[2] >= min_counts) {
                    current[0] = (int) current[0] + 1;
                }
            }
        }
        return day_parts.finalizeStat();
    }
    public static StatContainer dayStreak(List<CountingThread> threads, TimeUnit start_date, TimeUnit end_date, ParticipationDuration mode, int min_counts, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = mode.toCapitalizedString()+" Streak";
        if(min_counts>1) name+=" of "+min_counts+(use_counts?" Counts":" Updates");
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", mode.toCapitalizedString()+" Streak", "Start Date", "End Date"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.TIMEUNIT, StatDataType.TIMEUNIT};
        int[] sort_order = new int[]{3,-4};
        Object[] default_temp_object = new Object[]{new TimeUnit(0,CountingThread.timezone,mode), 0, null}; //Last Date, Streak
        Function<Object[], Object[]> finalize = o->new Object[]{o[1],((TimeUnit)o[0]).subtractTime((int)o[1]-1),o[0]};
        List<DayHOC> day_hocs = dayHocs(threads, start_date, end_date, mode, false, use_counters, use_counts, thread_counts_combined);
        ListStatGenerator<DayHOC> day_parts = new ListStatGenerator<>(day_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false);
        while(day_parts.hasNext()) {
            DayHOC day = day_parts.next();
            List<Object[]> hoc = day.hoc().getStat();
            for(Object[] user: hoc) {
                Object[] current = day_parts.getTempObject((int) user[0], (int) user[1]);
                if((int)user[2] >= min_counts) {
                    if(TimeUnit.timeBetween((TimeUnit)current[0], day.date(), mode)==1) {
                        current[1] = (int) current[1] + 1;
                    } else {
                        day_parts.finalizeObject((int) user[0], (int) user[1]);
                        current[1] = 1;
                    }
                    current[0] = day.date();
                }
            }
        }
        return day_parts.finalizeStat();
    }
    public static StatContainer dayRecord(List<CountingThread> threads, TimeUnit start_date, TimeUnit end_date, ParticipationDuration mode, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = mode.toCapitalizedString()+" Record";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", mode.toCapitalizedString()+" Record", "Date"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.TIMEUNIT};
        int[] sort_order = new int[]{3};
        Object[] default_temp_object = new Object[]{new TimeUnit(0,CountingThread.timezone,mode), 0, null}; //Day, Counts
        Function<Object[], Object[]> finalize = o->new Object[]{o[1],o[0]};
        List<DayHOC> day_hocs = dayHocs(threads, start_date, end_date, mode, false, use_counters, use_counts, thread_counts_combined);
        ListStatGenerator<DayHOC> day_parts = new ListStatGenerator<>(day_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false);
        while(day_parts.hasNext()) {
            DayHOC day = day_parts.next();
            List<Object[]> hoc = day.hoc().getStat();
            for(Object[] user: hoc) {
                Object[] current = day_parts.getTempObject((int) user[0], (int) user[1]);
                current[0] = day.date();
                current[1] = user[2];
                day_parts.finalizeObject((int) user[0], (int) user[1]);
            }
        }
        return day_parts.finalizeStat();
    }
    public static StatContainer dayMedals(List<CountingThread> threads, TimeUnit start_date, TimeUnit end_date, ParticipationDuration mode, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined, boolean add_thread_stats) {
        String name = mode.toCapitalizedString()+" Medals";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Gold", "Silver", "Bronze", "Total"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER};
        int[] sort_order = new int[]{3,4,5};
        Object[] default_temp_object = new Object[]{0, 0, 0, 0, null}; //Gold, Silver, Bronze, Total
        Function<Object[], Object[]> finalize = o->new Object[]{o[0],o[1],o[2],o[3]};
        List<DayHOC> day_hocs = dayHocs(threads, start_date, end_date, mode, false, use_counters, use_counts, thread_counts_combined);
        ListStatGenerator<DayHOC> day_parts = new ListStatGenerator<>(day_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, add_thread_stats);
        while(day_parts.hasNext()) {
            DayHOC day = day_parts.next();
            List<List<Object[]>> hoc = getMedalists(day.hoc(), 3);
            for(int i=0;i<hoc.size();i++) {
                for(Object[] user: hoc.get(i)) {
                    Object[] current = day_parts.getTempObject((int) user[0], (int) user[1]);
                    current[i] = (int) current[i] + 1;
                    current[3] = (int) current[3] + 1;
                }
            }
        }
        return day_parts.finalizeStat();
    }
    public static StatContainer nthPlaceDayStreak(List<CountingThread> threads, TimeUnit start_date, TimeUnit end_date, ParticipationDuration mode, int n, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = mode.toCapitalizedString()+" Streak at Position "+n;
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Streak", "Start Date", "End Date"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.TIMEUNIT, StatDataType.TIMEUNIT};
        int[] sort_order = new int[]{3,-4};
        Object[] default_temp_object = new Object[]{new TimeUnit(0,CountingThread.timezone,mode), 0, null}; //Last Date, Streak
        Function<Object[], Object[]> finalize = o->new Object[]{o[1],((TimeUnit)o[0]).subtractTime((int)o[1]-1),o[0]};
        List<DayHOC> day_hocs = dayHocs(threads, start_date, end_date, mode, false, use_counters, use_counts, thread_counts_combined);
        ListStatGenerator<DayHOC> day_parts = new ListStatGenerator<>(day_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false);
        while(day_parts.hasNext()) {
            DayHOC day = day_parts.next();
            List<List<Object[]>> medals = getMedalists(day.hoc(), n);
            if (medals.size() < n) continue;
            for (Object[] user : medals.get(n - 1)) {
                Object[] current = day_parts.getTempObject((int) user[0], (int) user[1]);
                if (TimeUnit.timeBetween((TimeUnit) current[0], day.date(), mode) == 1) {
                    current[1] = (int) current[1] + 1;
                } else {
                    day_parts.finalizeObject((int) user[0], (int) user[1]);
                    current[1] = 1;
                }
                current[0] = day.date();
            }
        }
        return day_parts.finalizeStat();
    }
    public static StatContainer countsPerDay(List<CountingThread> threads, TimeUnit start_date, TimeUnit end_date, ParticipationDuration mode, int min_counts, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = (use_counts?"Counts Per ":"Updates Per ")+mode.toCapitalizedString();
        if(min_counts>1) name+=" with "+min_counts+" "+mode.toCapitalizedString()+" Parts";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", (use_counts?"Counts Per ":"Updates Per ")+mode.toCapitalizedString()};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_temp_object = new Object[]{0, 0, null}; //Counts, Day Parts
        Function<Object[], Object[]> finalize = o->{
            if((int)o[1]==0 || (int)o[1]<min_counts) return new Object[]{0};
            return new Object[]{(int)o[0]/(float)(int)o[1]};
        };
        List<DayHOC> day_hocs = dayHocs(threads, start_date, end_date, mode, false, use_counters, use_counts, thread_counts_combined);
        ListStatGenerator<DayHOC> counts_per_day = new ListStatGenerator<>(day_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false);
        while(counts_per_day.hasNext()) {
            DayHOC day = counts_per_day.next();
            List<Object[]> hoc = day.hoc().getStat();
            for(Object[] user: hoc) {
                Object[] current = counts_per_day.getTempObject((int) user[0], (int) user[1]);
                current[0] = (int) current[0] + (int) user[2];
                current[1] = (int) current[1] + 1;
            }
        }
        return counts_per_day.finalizeStat();
    }
    public static StatContainer peakCountsPerDay(List<CountingThread> threads, TimeUnit start_date, TimeUnit end_date, ParticipationDuration mode, int min_counts, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = "Peak "+(use_counts?"Counts Per ":"Updates Per ")+mode.toCapitalizedString();
        if(min_counts>1) name+=" with "+min_counts+" "+mode.toCapitalizedString()+" Parts";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", (use_counts?"Counts Per ":"Updates Per ")+mode.toCapitalizedString(), "Date"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.TIMEUNIT};
        int[] sort_order = new int[]{3};
        Object[] default_temp_object = new Object[]{0, 0, null, null}; //Counts, Day Parts, Day
        Function<Object[], Object[]> finalize = o->{
            if((int)o[1]==0 || (int)o[1]<min_counts) return new Object[]{0, null};
            return new Object[]{(int)o[0]/(float)(int)o[1], o[2]};
        };
        List<DayHOC> day_hocs = dayHocs(threads, start_date, end_date, mode, false, use_counters, use_counts, thread_counts_combined);
        ListStatGenerator<DayHOC> counts_per_day = new ListStatGenerator<>(day_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false);
        while(counts_per_day.hasNext()) {
            DayHOC day = counts_per_day.next();
            List<Object[]> hoc = day.hoc().getStat();
            for(Object[] user: hoc) {
                Object[] current = counts_per_day.getTempObject((int) user[0], (int) user[1]);
                current[0] = (int) current[0] + (int) user[2];
                current[1] = (int) current[1] + 1;
                current[2] = day.date();
                counts_per_day.finalizeObject((int) user[0], (int) user[1]);
            }
        }
        return counts_per_day.finalizeStat();
    }
    public static StatContainer nthPlaceDailyPercentageOfCounts(List<CountingThread> threads, TimeUnit start_date, TimeUnit end_date, ParticipationDuration mode, int n, int min_counts, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = mode.toCapitalizedString()+" % of Counts"+(n>=1?" at "+mode.toCapitalizedString()+" HoC Rank "+n:"");
        if(min_counts>1) name+=" (Min "+min_counts+(use_counts?" Counts":" Updates");
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "% of "+(use_counts?"Counts":"Updates"), "User "+(use_counts?"Counts":"Updates"), mode.toCapitalizedString()+(use_counts?" Counts":" Updates"), mode.toCapitalizedString()};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.PERCENT, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.TIMEUNIT};
        int[] sort_order = new int[]{3,-6};
        Object[] default_temp_object = new Object[]{0, new TimeUnit(0,CountingThread.timezone,mode), 0, 0, null}; //% of counts, Date, User Counts, Day Counts
        Function<Object[], Object[]> finalize = o->new Object[]{o[0], o[2], o[3], o[1]};
        List<DayHOC> day_hocs = dayHocs(threads, start_date, end_date, mode, false, use_counters, use_counts, thread_counts_combined);
        ListStatGenerator<DayHOC> day_parts = new ListStatGenerator<>(day_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false);
        while(day_parts.hasNext()) {
            DayHOC day = day_parts.next();
            List<Object[]> hoc = day.hoc().getStat();
            int sum_of_counts = 0;
            for(Object[] user: hoc) sum_of_counts+=(int)user[2];
            if(n>=1) {
                List<List<Object[]>> medalists = getMedalists(day.hoc(), n);
                if(medalists.size()<n) hoc = new ArrayList<>();
                else hoc = medalists.get(n-1);
            }
            for(Object[] user: hoc) {
                if((int)user[2] >= min_counts) {
                    Object[] current = day_parts.getTempObject((int) user[0], (int) user[1]);
                    current[0] = (float)(int)user[2]/sum_of_counts;
                    current[1] = day.date();
                    current[2] = user[2];
                    current[3] = sum_of_counts;
                    day_parts.finalizeObject((int) user[0], (int) user[1]);
                    current[0] = 0;
                    current[1] = new TimeUnit(0,CountingThread.timezone,mode);
                }
            }
        }
        return day_parts.finalizeStat();
    }
    public static StatContainer photoFinishes(List<CountingThread> threads, TimeUnit start_date, TimeUnit end_date, ParticipationDuration mode, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = mode.toOtherCapitalizedString()+" Photo Finishes";
        String[] headers = new String[]{use_counters?"User 1":"Thread 1", use_counters?"User 1 ":"Thread 1 "+(use_counts?"Counts":"Updates"), use_counters?"User 2":"Thread 2", use_counters?"User 2 ":"Thread 2 "+(use_counts?"Counts":"Updates"), "Thread", "% Difference", mode.toCapitalizedString()};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.NUMBER, use_counters?StatDataType.COUNTER:StatDataType.THREAD, StatDataType.NUMBER, StatDataType.THREAD_INSERT, StatDataType.PERCENT, StatDataType.TIMEUNIT};
        int[] sort_order = new int[]{-6,-7};
        Object[] default_temp_object = new Object[]{1, new TimeUnit(0,CountingThread.timezone,mode), 0, 0, null, null}; //%, Date, User 1 Counts, User 2 Counts, User 2
        Function<Object[], Object[]> finalize = o->new Object[]{o[2], o[4], o[3], o[0], o[1]};
        List<DayHOC> day_hocs = dayHocs(threads, start_date, end_date, mode, false, use_counters, use_counts, thread_counts_combined);
        ListStatGenerator<DayHOC> day_parts = new ListStatGenerator<>(day_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false);
        while(day_parts.hasNext()) {
            DayHOC day = day_parts.next();
            List<List<Object[]>> medalists = getMedalists(day.hoc(), day.hoc().getStat().size());
            for(int i=0;i<Math.min(3, medalists.size());i++) {
                if(medalists.get(i).isEmpty()) continue;
                for(int j=i+1;j<medalists.size();j++) {
                    if(medalists.get(j).isEmpty()) continue;
                    float percent = 1 - ((float)(int)medalists.get(j).get(0)[2])/(int)medalists.get(i).get(0)[2];
                    for(Object[] user1:medalists.get(i)) {
                        for(Object[] user2:medalists.get(j)) {
                            Object[] current = day_parts.getTempObject((int) user1[0], (int) user1[1]);
                            current[0] = percent;
                            current[1] = day.date();
                            current[2] = user1[2];
                            current[3] = user2[2];
                            current[4] = use_counters ? ((UpdateOrCountValue) user2[0]).getAuthorId() : AllThreads.all_threads_with_combined.get(((UpdateOrCountValue) user2[0]).getThreadId()).thread;
                            day_parts.finalizeObject((int) user1[0], (int) user1[1]);
                            current[0] = 1;
                            current[1] = new TimeUnit(0,CountingThread.timezone,mode);
                        }
                    }
                }
            }
        }
        return day_parts.finalizeStat();
    }
    public static StatContainer percentageThreadPartsByDay(List<CountingThread> threads, TimeUnit start_date, TimeUnit end_date, ParticipationDuration mode, int min_counts, boolean unique, boolean use_counters, boolean use_counts) {
        String name = "Thread Parts % by "+mode.toCapitalizedString()+(min_counts>1?" (Minimum "+min_counts+(use_counts?" Counts":" Updates"):"");
        String[] headers = new String[]{"User", "Thread", mode.toCapitalizedString(), "%", "Thread Parts", "Total Threads"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.TIMEUNIT, StatDataType.PERCENT, StatDataType.NUMBER, StatDataType.NUMBER};
        int[] sort_order = new int[]{4, 5, -3};
        Object[] default_object = new Object[]{0, 0, 0, new TimeUnit(0,CountingThread.timezone,mode), null, null}; //%, Amount, Total, Date, Array of Threads Participated In
        Function<Object[], Object[]> finalize = k -> new Object[]{k[3],k[0],k[1],k[2]};
        List<DayHOC> hoc = dayHocs(threads, start_date, end_date, mode, false, use_counters, use_counts, false);
        ListStatGenerator<DayHOC> thread_parts = new ListStatGenerator<>(hoc, finalize, default_object, name, headers, types, sort_order, unique, use_counters, use_counts, true, false);
        while(thread_parts.hasNext()) {
            DayHOC day_hoc = thread_parts.next();
            int threads_by_date = 0;
            for(CountingThread thread:threads) {
                if(!thread.first_day(mode, CountingThread.timezone).isAfter(day_hoc.date())) {
                    threads_by_date++;
                }
            }
            for(Object[] user: day_hoc.hoc().getStat()) {
                if((int)user[2]<min_counts) continue;
                Object[] current = thread_parts.getTempObject((int) user[0], (int) user[1]);
                if(current[4]==null) current[4] = new boolean[AllThreads.all_threads.size()];
                boolean[] thread_arr = (boolean[])current[4];
                int thread_index = ((UpdateOrCountValue)user[0]).getThreadId()-1;
                if(thread_arr[thread_index]) continue;
                thread_arr[thread_index] = true;
                current[1] = (int) current[1] + 1;
                current[0] = (float)(int)current[1]/threads_by_date;
                current[2] = threads_by_date;
                current[3] = day_hoc.date();
            }
            thread_parts.finalizeStat();
        }
        return thread_parts.finalizeStat();
    }

    public static StatContainer gets(List<CountingThread> threads, UUID uuid1, UUID uuid2, int offset, int k_size, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        String name = k_size+"K "+(offset==1?"Assists":"Gets With Offset "+offset);
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", offset==1?"Assists":"Gets"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{0,null};
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, o-> Arrays.copyOf(o,o.length-1), default_object, name, headers, types, sort_order, unique, use_counters, true, thread_counts_combined, false, false, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            CountValue count = (CountValue)hoc.next();
            if(count.isGet(offset, k_size)) {
                Object[] current = hoc.getTempObject(count);
                current[0] = (int) current[0] + 1;
            }
        }
        return hoc.finalizeStat();
    }
    public static List<SplitHOC> splitHocs(List<CountingThread> threads, int start_k, int end_k, int k_size, boolean k_hoc, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        start_k = Math.max(start_k, 0);
        String name = k_hoc ? k_size+"K HoC" : "Split HoC";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Counts"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{0, null}; //Count
        MultiStatGenerator hoc = new MultiStatGenerator(threads, 0, Integer.MAX_VALUE, k_size, o-> Arrays.copyOf(o,o.length-1), default_object, name, headers, types, sort_order, unique, use_counters, true, thread_counts_combined);
        int max_splits_per_get = 1;
        if(!k_hoc) {
            for (CountingThread thread : threads) {
                if (thread.thread.splitsPerGet > max_splits_per_get) max_splits_per_get = thread.thread.splitsPerGet;
            }
            max_splits_per_get *= k_size;
        }
        while(hoc.hasNext()) {
            CountValue count = (CountValue)hoc.next();
            if(k_hoc) {
                int index = count.getK(k_size);
                hoc.setIndex(index);
            } else {
                int[] index = count.getSplit(k_size);
                hoc.setIndex(index[0]*max_splits_per_get+index[1]);
            }

            Object[] current = hoc.getTempObject(count);
            current[0] = (int)current[0]+1;
        }
        List<SplitHOC> k_hocs = new ArrayList<>();
        List<StatContainer> ks_without_number = hoc.finalizeStat();
        int current_k = start_k;
        int current_split = 0;
        for(int i=start_k*max_splits_per_get;i<Math.min((long)end_k*max_splits_per_get, ks_without_number.size());i++) {
            k_hocs.add(new SplitHOC(current_k, current_split, ks_without_number.get(i)));
            current_split++;
            if(current_split == max_splits_per_get) {
                current_k++;
                current_split = 0;
            }
        }
        return k_hocs;
    }
    public static StatContainer kHoc(List<CountingThread> threads, int start_k, int k_size, int split, boolean use_ks, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        List<SplitHOC> k_hoc = splitHocs(threads, start_k, start_k+1, k_size, use_ks, unique, use_counters, thread_counts_combined);
        if(split<0 || k_hoc.size()<=split) {
            return new StatContainer(null, use_ks ? k_size+"K HoC" : "Split HoC", new String[0], new StatDataType[0], new int[0], unique, use_counters, true, thread_counts_combined, false, StatSaveMode.NO_SAVE);
        } else return k_hoc.get(split).hoc();
    }
    public static StatContainer kParts(List<CountingThread> threads, int start_k, int end_k, int k_size, boolean use_ks, int min_counts, boolean unique, boolean use_counters, boolean thread_counts_combined, boolean add_thread_stats) {
        String name = use_ks ? k_size+"k Parts" : "Split Parts";
        if(min_counts>1) name+=" with "+min_counts+" Counts";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", name};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_temp_object = new Object[]{0, null}; //K Parts
        Function<Object[], Object[]> finalize = o->new Object[]{o[0]};
        List<SplitHOC> k_hocs = splitHocs(threads, start_k, end_k, k_size, use_ks, false, use_counters, thread_counts_combined);
        ListStatGenerator<SplitHOC> k_parts = new ListStatGenerator<>(k_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, true, thread_counts_combined, add_thread_stats);
        while(k_parts.hasNext()) {
            SplitHOC k = k_parts.next();
            List<Object[]> hoc = k.hoc().getStat();
            for(Object[] user: hoc) {
                Object[] current = k_parts.getTempObject((int) user[0], (int) user[1]);
                if((int)user[2] >= min_counts) {
                    current[0] = (int) current[0] + 1;
                }
            }
        }
        return k_parts.finalizeStat();
    }
    public static StatContainer kStreak(List<CountingThread> threads, int start_k, int end_k, int k_size, boolean use_ks, int min_counts, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        String name = use_ks ? k_size+"k Streak" : "Split Streak";
        if(min_counts>1) name+=" of "+min_counts+" Counts";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", name, "Start K", "End K"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER};
        int[] sort_order = new int[]{3,-4};
        Object[] default_temp_object = new Object[]{0, 0, null}; //Last K, Streak
        Function<Object[], Object[]> finalize = o->new Object[]{o[1],((int)o[0])-((int)o[1]-1),o[0]};
        List<SplitHOC> k_hocs = splitHocs(threads, start_k, end_k, k_size, use_ks, false, use_counters, thread_counts_combined);
        ListStatGenerator<SplitHOC> k_parts = new ListStatGenerator<>(k_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, true, thread_counts_combined, false);
        while(k_parts.hasNext()) {
            SplitHOC k = k_parts.next();
            List<Object[]> hoc = k.hoc().getStat();
            for(Object[] user: hoc) {
                Object[] current = k_parts.getTempObject((int) user[0], (int) user[1]);
                if((int)user[2] >= min_counts) {
                    if(k.k()-(int)current[0]==1) {
                        current[1] = (int) current[1] + 1;
                    } else {
                        k_parts.finalizeObject((int) user[0], (int) user[1]);
                        current[1] = 1;
                    }
                    current[0] = k.k();
                }
            }
        }
        return k_parts.finalizeStat();
    }
    public static StatContainer fastestKs(List<CountingThread> threads, int start_k, int end_k, int k_size, boolean use_ks, float required_percentage, boolean fastest, boolean unique, boolean use_counters) {
        String name = (fastest?"Fastest ":"Slowest ")+(use_ks ? k_size+"ks" : "Splits") +" (Min "+(required_percentage*100)+"% of Counts)";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Time", "Start Count", "End Count", "Users"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.TIME, StatDataType.COUNT, StatDataType.COUNT, StatDataType.LIST_COUNTER};
        int[] sort_order = new int[]{fastest?-3:3,4};
        Object[] default_temp_object = new Object[]{CountValue.getDefaultObject(), CountValue.getDefaultObject(), null, null}; //Start Count, End Count, Users
        Function<Object[], Object[]> finalize = o->{
            if(((CountValue)o[0]).timestamp()==((CountValue)o[1]).timestamp()) {
                if(fastest) return new Object[]{Long.MAX_VALUE, o[0], o[1], new ArrayList<>()};
                else return new Object[]{0, o[1], o[1], new ArrayList<>()};
            }
            return new Object[]{((CountValue)o[1]).timestamp()-((CountValue)o[0]).timestamp(), o[0], o[1], o[2]};
        };
        List<SplitWithHOC> k_hocs = new ArrayList<>();
        for(CountingThread thread: threads) {
            k_hocs.addAll(thread.completeDistinctSplitHocs(start_k, end_k, k_size, use_ks, true));
        }
        ListStatGenerator<SplitWithHOC> fastest_ks = new ListStatGenerator<>(k_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, true, false, false);
        while(fastest_ks.hasNext()) {
            SplitWithHOC k = fastest_ks.next();
            int counts_in_k = k.end_count().true_number() - k.start_count().true_number();
            if(k.start_count().true_number()==1) counts_in_k++;
            int thread_id = (int) k.hoc().getStat().get(0)[1];
            List<Integer> qualified_users = new ArrayList<>();
            for(Object[] entry: k.hoc().getStat()) {
                if((int)entry[2] >= counts_in_k*required_percentage) {
                    qualified_users.add((int)entry[0]);
                }
            }
            if(use_counters) {
                for(int user:qualified_users) {
                    Object[] current = fastest_ks.getTempObject(user, thread_id);
                    current[0] = k.start_count();
                    current[1] = k.end_count();
                    current[2] = qualified_users;
                    fastest_ks.finalizeObject(user, thread_id);
                    current[1] = current[0];
                }
            } else {
                Object[] current = fastest_ks.getTempObject(thread_id, thread_id);
                current[0] = k.start_count();
                current[1] = k.end_count();
                current[2] = qualified_users;
                fastest_ks.finalizeObject(thread_id, thread_id);
                current[1] = current[0];
            }
        }
        return fastest_ks.finalizeStat();
    }
    public static StatContainer perfectKs(List<CountingThread> threads, int start_k, int end_k, int k_size, boolean use_ks, boolean unique, boolean add_thread_parts) {
        String name = "Perfect "+k_size+"ks";
        String[] headers = new String[]{"User", "Thread", "Perfect "+k_size+"ks"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_temp_object = new Object[]{0, null}; //Perfect ks
        Function<Object[], Object[]> finalize = o->new Object[]{o[0]};
        List<SplitWithHOC> k_hocs = new ArrayList<>();
        for(CountingThread thread: threads) {
            k_hocs.addAll(thread.completeDistinctSplitHocs(start_k, end_k, k_size, use_ks, true));
        }
        ListStatGenerator<SplitWithHOC> perfect_ks = new ListStatGenerator<>(k_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, true, true, false, add_thread_parts);
        while(perfect_ks.hasNext()) {
            SplitWithHOC k = perfect_ks.next();
            CountingThread thread = k.start_count().getThread();
            int counts_in_k = k.end_count().true_number() - k.start_count().true_number();
            int start_number = k.start_count().validCountNumber();
            if(k.start_count().true_number()==1) {
                counts_in_k++;
                start_number--;
            }
            int min_counts = counts_in_k/thread.doubleCountInterval();
            int remainder = counts_in_k%thread.doubleCountInterval();
            for(Object[] entry: k.hoc().getStat()) {
                Object[] current = perfect_ks.getTempObject((int)entry[0], (int)entry[1]);
                if((int)entry[2] < min_counts) continue;
                if((int)entry[2] > min_counts || remainder==0) {
                    current[0] = (int) current[0] + 1;
                    continue;
                }
                List<CountValue> start_counts = thread.getUpdatesOrCounts(start_number, start_number+thread.doubleCountInterval()-1, CountDataType.COUNT);
                boolean broken = false;
                for(CountValue count:start_counts) {
                    if(count.getAuthorId() == (int)entry[0]) {
                        broken = true;
                        break;
                    }
                }
                if(broken) continue;
                current[0] = (int) current[0] + 1;
            }
        }
        return perfect_ks.finalizeStat();
    }
    public static StatContainer kMedals(List<CountingThread> threads, int start_k, int end_k, int k_size, boolean use_ks, boolean unique, boolean use_counters, boolean thread_counts_combined, boolean add_thread_stats) {
        String name = use_ks ? k_size+"k Medals" : "Split Medals";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Gold", "Silver", "Bronze", "Total"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER};
        int[] sort_order = new int[]{3,4,5};
        Object[] default_temp_object = new Object[]{0, 0, 0, 0, null}; //Gold, Silver, Bronze, Total
        Function<Object[], Object[]> finalize = o->new Object[]{o[0],o[1],o[2],o[3]};
        List<SplitHOC> k_hocs = splitHocs(threads, start_k, end_k, k_size, use_ks, false, use_counters, thread_counts_combined);
        ListStatGenerator<SplitHOC> k_parts = new ListStatGenerator<>(k_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, true, thread_counts_combined, add_thread_stats);
        while(k_parts.hasNext()) {
            SplitHOC k = k_parts.next();
            List<List<Object[]>> hoc = getMedalists(k.hoc(), 3);
            for(int i=0;i<hoc.size();i++) {
                for(Object[] user: hoc.get(i)) {
                    Object[] current = k_parts.getTempObject((int) user[0], (int) user[1]);
                    current[i] = (int) current[i] + 1;
                    current[3] = (int) current[3] + 1;
                }
            }
        }
        return k_parts.finalizeStat();
    }
    public static StatContainer kUnderNMsStreak(List<CountingThread> threads, int start_k, int end_k, int k_size, boolean use_ks, int n, float required_percentage, boolean unique, boolean use_counters) {
        String name = (use_ks ? k_size+"k" : "Split")+" Under "+n+"ms Streak (Min "+(required_percentage*100)+"% of Counts)";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Streak", "Start Count", "End Count"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.COUNT, StatDataType.COUNT};
        int[] sort_order = new int[]{3,-4};
        Object[] default_temp_object = new Object[]{0, CountValue.getDefaultObject(), CountValue.getDefaultObject(), null}; //Streak, Start Count, End Count
        Function<Object[], Object[]> finalize = o-> new Object[]{o[0], o[1], o[2]};
        List<SplitWithHOC> k_hocs = new ArrayList<>();
        for(CountingThread thread: threads) {
            k_hocs.addAll(thread.completeDistinctSplitHocs(start_k, end_k, k_size, use_ks, true));
        }
        ListStatGenerator<SplitWithHOC> fastest_ks = new ListStatGenerator<>(k_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, true, false, false);
        if(n<=0) return fastest_ks.finalizeStat();
        while(fastest_ks.hasNext()) {
            SplitWithHOC k = fastest_ks.next();
            int counts_in_k = k.end_count().true_number() - k.start_count().true_number();
            if(k.start_count().true_number()==1) counts_in_k++;
            if(use_counters) {
                for(Object[] entry: k.hoc().getStat()) {
                    if((int)entry[2] >= counts_in_k*required_percentage) {
                        UpdateOrCountValue u = UpdateOrCountValue.getDummyObject(true, (int)entry[0], (int)entry[1]);
                        kUnderNMsStreakHelper(fastest_ks, k, n, u.getAuthorId(), u.getThreadId());
                    }
                }
            } else {
                int thread_id = (int) k.hoc().getStat().get(0)[1];
                kUnderNMsStreakHelper(fastest_ks, k, n, thread_id, thread_id);
            }
        }
        return fastest_ks.finalizeStat();
    }
    private static void kUnderNMsStreakHelper(ListStatGenerator<SplitWithHOC> fastest_ks, SplitWithHOC k, int n, int author_id, int thread_id) {
        Object[] current = fastest_ks.getTempObject(author_id, thread_id);
        if((k.end_count().timestamp() - k.start_count().timestamp())/1000000 < n) {
            if (Objects.equals(current[2], k.start_count())) {
                current[0] = (int) current[0] + 1;
            } else {
                fastest_ks.finalizeObject(author_id, thread_id);
                current[0] = 1;
                current[1] = k.start_count();
            }
            current[2] = k.end_count();
        } else if((int)current[0] > 0) {
            fastest_ks.finalizeObject(author_id, thread_id);
            current[0] = 0;
        }
    }
    public static StatContainer pairPBs(List<CountingThread> threads, int start_k, int end_k, int k_size, boolean use_ks, float required_percentage, boolean fastest, boolean unique) {
        String name = (fastest?"Fastest ":"Slowest ")+"Pair "+(use_ks ? k_size+"ks" : "Splits") +" (Min "+(required_percentage*100)+"% of Counts)";
        String[] headers = new String[]{"User 1", "User 2", "Thread", "Time", "Start Count", "End Count"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.TIME, StatDataType.COUNT, StatDataType.COUNT};
        int[] sort_order = new int[]{fastest?-4:4,5};
        Object[] default_temp_object = new Object[]{CountValue.getDefaultObject(), CountValue.getDefaultObject(), null, null}; //Start Count, End Count
        Function<Object[], Object[]> finalize = o->{
            if(((CountValue)o[0]).timestamp()==((CountValue)o[1]).timestamp()) {
                if(fastest) return new Object[]{Long.MAX_VALUE, o[0], o[1]};
                else return new Object[]{0, o[0], o[1]};
            }
            return new Object[]{((CountValue)o[1]).timestamp()-((CountValue)o[0]).timestamp(), o[0], o[1]};
        };
        List<SplitWithHOC> k_hocs = new ArrayList<>();
        for(CountingThread thread: threads) {
            k_hocs.addAll(thread.completeDistinctSplitHocs(start_k, end_k, k_size, use_ks, true));
        }
        ListStatGenerator<SplitWithHOC> fastest_ks = new ListStatGenerator<>(k_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, true, true, false, false);
        while(fastest_ks.hasNext()) {
            SplitWithHOC k = fastest_ks.next();
            int counts_in_k = k.end_count().true_number() - k.start_count().true_number();
            if(k.start_count().true_number()==1) counts_in_k++;
            List<UpdateOrCountValue> qualified_users = new ArrayList<>();
            for(Object[] entry: k.hoc().getStat()) {
                if((int)entry[2] >= counts_in_k*required_percentage) {
                    UpdateOrCountValue u = UpdateOrCountValue.getDummyObject(true, (int)entry[0], (int)entry[1]);
                    qualified_users.add(u);
                }
            }
            for(int i=0;i<qualified_users.size();i++) {
                for(int j=i+1;j<qualified_users.size();j++) {
                    UpdateOrCountValue u1 = qualified_users.get(i).getAuthorId() < qualified_users.get(j).getAuthorId() ? qualified_users.get(i) : qualified_users.get(j);
                    UpdateOrCountValue u2 = qualified_users.get(i).getAuthorId() < qualified_users.get(j).getAuthorId() ? qualified_users.get(j) : qualified_users.get(i);
                    Object[] current = fastest_ks.getTempObject(u1.getAuthorId(), u2.getAuthorId(), u1.getThreadId());
                    current[0] = k.start_count();
                    current[1] = k.end_count();
                    fastest_ks.finalizeObject(u1.getAuthorId(), u2.getAuthorId(), u1.getThreadId());
                    current[1] = current[0];
                }
            }
        }
        return fastest_ks.finalizeStat();
    }
    public static StatContainer kMinSplitStdDev(List<CountingThread> threads, int start_k, int end_k, int k_size, float required_percentage, boolean minimum, boolean unique, boolean use_counters) {
        String name = (!minimum?"Maximum ":"Minimum ")+k_size+"K Split Standard Deviation" +(use_counters?" (Min "+(required_percentage*100)+"% of Counts)":"");
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Std Dev", k_size+"K Speed", "Start Count", "End Count", "Users"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.TIME, StatDataType.COUNT, StatDataType.COUNT, StatDataType.LIST_COUNTER};
        int[] sort_order = new int[]{minimum?-3:3,4};
        Object[] default_temp_object = new Object[]{CountValue.getDefaultObject(), CountValue.getDefaultObject(), null, 0, null}; //Start Count, End Count, Users, Std Dev
        Function<Object[], Object[]> finalize = o->{
            if(((CountValue)o[0]).timestamp()==((CountValue)o[1]).timestamp()) {
                if(minimum) return new Object[]{Float.MAX_VALUE, Long.MAX_VALUE, o[0], o[1], new ArrayList<>()};
                else return new Object[]{-1.0, -1.0, o[1], o[1], new ArrayList<>()};
            }
            return new Object[]{o[3], ((CountValue)o[1]).timestamp()-((CountValue)o[0]).timestamp(), o[0], o[1], o[2]};
        };
        List<List<SplitWithHOC>> k_hocs = new ArrayList<>();
        for(CountingThread thread: threads) {
            k_hocs.addAll(thread.completeDistinctSplitHocsByK(start_k, end_k, k_size, true));
        }
        ListStatGenerator<List<SplitWithHOC>> fastest_ks = new ListStatGenerator<>(k_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, true, false, false);
        while(fastest_ks.hasNext()) {
            List<SplitWithHOC> k = fastest_ks.next();
            List<StatContainer> split_hocs = new ArrayList<>();
            List<Long> split_times = new ArrayList<>();
            for(SplitWithHOC split: k) {
                split_hocs.add(split.hoc());
                split_times.add(split.end_count().getTimestamp()-split.start_count().getTimestamp());
            }
            StatContainer k_hoc = StatContainer.add(split_hocs);
            double stddev = Util.stdDev(split_times)/1000000000;
            int counts_in_k = k.get(k.size()-1).end_count().true_number() - k.get(0).start_count().true_number();
            if(k.get(0).start_count().true_number()==1) counts_in_k++;
            int thread_id = (int) k.get(0).hoc().getStat().get(0)[1];
            List<Integer> qualified_users = new ArrayList<>();
            for(Object[] entry: k_hoc.getStat()) {
                if((int)entry[2] >= counts_in_k*required_percentage) {
                    qualified_users.add((int)entry[0]);
                }
            }
            if(use_counters) {
                for(int user:qualified_users) {
                    Object[] current = fastest_ks.getTempObject(user, thread_id);
                    current[0] = k.get(0).start_count();
                    current[1] = k.get(k.size()-1).end_count();
                    current[2] = qualified_users;
                    current[3] = stddev;
                    fastest_ks.finalizeObject(user, thread_id);
                    current[1] = current[0];
                }
            } else {
                Object[] current = fastest_ks.getTempObject(thread_id, thread_id);
                current[0] = k.get(0).start_count();
                current[1] = k.get(k.size()-1).end_count();
                current[2] = qualified_users;
                current[3] = stddev;
                fastest_ks.finalizeObject(thread_id, thread_id);
                current[1] = current[0];
            }
        }
        return fastest_ks.finalizeStat();
    }
    public static StatContainer sumOfBest(List<CountingThread> threads, int start_k, int end_k, int k_size, float required_percentage, String username, boolean best, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        String name = k_size+"k Sum Of "+(best?"Best":"Worst")+" (Min "+(required_percentage*100)+"% of Counts)";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Time"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.TIME};
        int[] sort_order = new int[]{best?-3:3};
        Object[] default_temp_object = new Object[]{0, null, null}; //Complete Splits, List of Splits (Time)
        int max_splits = k_size * threads.stream().max(Comparator.comparingInt(k -> k.thread.splitsPerGet)).orElseThrow().thread.splitsPerGet;
        Function<Object[], Object[]> finalize = o->{
            if(o[2]==null) return new Object[]{best?Long.MAX_VALUE : 0L};
            int split_amount = max_splits;
            if(!thread_counts_combined) split_amount = k_size*((UpdateOrCountValue)o[2]).getThread().thread.splitsPerGet;
            if((int)o[0]!=split_amount) return new Object[]{best?Long.MAX_VALUE : 0L};
            long sum_time = ((List<SplitWithHOC>)o[1]).stream().mapToLong(SplitWithHOC::time).reduce(Long::sum).orElseThrow();
            return new Object[]{sum_time};
        };
        Map<Integer, List<SplitWithHOC>> split_temp_data = new HashMap<>();
        List<SplitWithHOC> k_hocs = new ArrayList<>();
        for(CountingThread thread: threads) {
            k_hocs.addAll(thread.completeDistinctSplitHocs(start_k, end_k, k_size, false, use_counters));
        }
        ListStatGenerator<SplitWithHOC> fastest_ks = new ListStatGenerator<>(k_hocs, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, true, thread_counts_combined, false);
        while(fastest_ks.hasNext()) {
            SplitWithHOC k = fastest_ks.next();
            int counts_in_k = k.end_count().true_number() - k.start_count().true_number();
            if(k.start_count().true_number()==1) counts_in_k++;
            List<UpdateOrCountValue> qualified_users = new ArrayList<>();
            for(Object[] entry: k.hoc().getStat()) {
                if((int)entry[2] >= counts_in_k*required_percentage) {
                    UpdateOrCountValue u = UpdateOrCountValue.getDummyObject(true, (int)entry[0], (int)entry[1]);
                    qualified_users.add(u);
                }
            }
            for(UpdateOrCountValue user:qualified_users) {
                Object[] current = fastest_ks.getTempObject(user.getAuthorId(), user.getThreadId());
                if(current[1]==null) current[1] = new ArrayList<>();
                List<SplitWithHOC> split_times = (List<SplitWithHOC>) current[1];
                while(split_times.size() <= k.split()) split_times.add(null);
                if(split_times.get(k.split())==null) {
                    split_times.set(k.split(), k);
                    current[0] = (int) current[0] + 1;
                }
                if(best && k.time() < split_times.get(k.split()).time() || !best && k.time() > split_times.get(k.split()).time()) {
                    split_times.set(k.split(), k);
                }
                split_temp_data.put((thread_counts_combined && !use_counters) ? 0 : user.getAuthorId(), split_times);
            }
        }
        if(username!=null) return userSumOfBest(split_temp_data, username, max_splits, name, use_counters, (threads.size() == 1 && use_counters) || thread_counts_combined);
        return fastest_ks.finalizeStat();
    }
    private static StatContainer userSumOfBest(Map<Integer, List<SplitWithHOC>> splits, String username, int max_splits, String stat_name, boolean use_counters, boolean thread_counts_combined) {
        if(use_counters && !thread_counts_combined) throw new IllegalArgumentException();
        int counter_id = Counters.username_lookup.getOrDefault(username, -1);
        ThreadData thread = AllThreads.lookupFromName(username);
        if(counter_id==-1 && use_counters || thread==null && !use_counters && !thread_counts_combined) throw new IllegalArgumentException();
        int focus_id = use_counters ? counter_id : (thread_counts_combined ? 0 : thread.id);
        if(!(thread_counts_combined && !use_counters)) stat_name = stat_name+" For "+(use_counters?username:thread.title);
        String[] headers = new String[]{"Thread", "Time", "Start Count", "End Count"};
        StatDataType[] types = new StatDataType[]{StatDataType.THREAD_INSERT, StatDataType.TIME, StatDataType.COUNT, StatDataType.COUNT};
        int[] sort_order = new int[0]; //Sort by insertion order
        Object[] default_final_temp_object = new Object[]{focus_id, null, null, null, null}; //Focus, Thread Insert, Time, Start Count, End Count
        StatContainer sob = new StatContainer(default_final_temp_object, stat_name, headers, types, sort_order, false, true, true, !use_counters && !thread_counts_combined, false, StatSaveMode.NO_SAVE);
        List<SplitWithHOC> sob_splits = splits.getOrDefault(focus_id, new ArrayList<>());
        while(sob_splits.size() < max_splits) sob_splits.add(null);
        for(SplitWithHOC split: sob_splits) {
            if(split==null) sob.addTempObjectDirect(focus_id, null, null, null, null);
            else sob.addTempObjectDirect(focus_id, split.hoc().getStat().get(0)[1], split.time(), split.start_count(), split.end_count());
            sob.finalizeObjectDirect(focus_id, k->k);
        }
        return sob;
    }

    public static StatContainer hourParts(List<CountingThread> threads, UUID uuid1, UUID uuid2, int duration_seconds, int min_counts, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined, boolean add_thread_stats) {
        if(duration_seconds<=0 || 3600%duration_seconds!=0) duration_seconds = 3600;
        String name = duration_seconds+"s Parts";
        if(min_counts > 1) name+=" (Min "+min_counts+(use_counts?" Counts":" Updates");
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", duration_seconds+"s Parts"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{0, 0, 0L,null}; //Hour Parts, Counts This Hour, Timestamp of Last Count
        Function<Object[], Object[]> finalize = k -> {
            if((int)k[1]>=min_counts) k[0] = (int) k[0] + 1;
            return new Object[]{k[0]};
        };
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false, add_thread_stats, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            if(count.getTimestamp()-(long)current[2] >= duration_seconds*1000000000L) {
                if((int)current[1]>=min_counts) current[0] = (int) current[0] + 1;
                current[1] = 0;
                current[2] = count.getTimestamp()-count.getTimestamp()%(duration_seconds*1000000000L);
            }
            current[1] = (int)current[1]+1;
        }
        return hoc.finalizeStat();
    }
    public static StatContainer hourRecord(List<CountingThread> threads, UUID uuid1, UUID uuid2, int duration_seconds, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        if(duration_seconds<=0 || 3600%duration_seconds!=0) duration_seconds = 3600;
        String name = duration_seconds+"s Record ";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", name, "Hour"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.HOUR};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{0,0L,null}; //Hour Parts, Current "Hour"
        Function<Object[], Object[]> finalize = k -> new Object[]{k[0], k[1]};
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false, false, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            if(count.getTimestamp()-(long)current[1] >= duration_seconds*1000000000L) {
                hoc.finalizeObject(count);
                current[0] = 0;
                current[1] = count.getTimestamp()-count.getTimestamp()%(duration_seconds*1000000000L);
            }
            current[0] = (int) current[0] + 1;
        }
        return hoc.finalizeStat();
    }
    public static StatContainer hourRecordOffset(List<CountingThread> threads, UUID uuid1, UUID uuid2, int duration_seconds, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = duration_seconds+"s Record ";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", name};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{0,null,null}; //Hour Parts, Current "Hour" Counts (Linked List)
        Function<Object[], Object[]> finalize = k -> new Object[]{k[0]};
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false, false, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            if(current[1]==null) current[1] = new LinkedList<>();
            LinkedList<UpdateOrCountValue> counts = (LinkedList<UpdateOrCountValue>)current[1];
            counts.add(count);
            while(counts.getFirst().getTimestamp() < count.getTimestamp()-duration_seconds*1000000000L) {
                counts.removeFirst();
            }
            if(counts.size() > (int)current[0]) current[0] = counts.size();
        }
        return hoc.finalizeStat();
    }
    public static StatContainer hourRecordNoMistakes(List<CountingThread> threads, UUID uuid1, UUID uuid2, int duration_seconds, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        if(duration_seconds<=0 || 3600%duration_seconds!=0) duration_seconds = 3600;
        String name = duration_seconds+"s Record (No Mistakes)";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", name, "Hour"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.HOUR};
        int[] sort_order = new int[]{3,-4};
        Object[] default_object = new Object[]{0,0L,true,null}; //Hour Parts, Current "Hour", Mistakeless
        Function<Object[], Object[]> finalize = k -> {
            if(!(boolean)k[2]) return new Object[]{0,0L};
            return new Object[]{k[0], k[1]};
        };
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, false, thread_counts_combined, false, false, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            if(count.getTimestamp()-(long)current[1] >= duration_seconds*1000000000L) {
                hoc.finalizeObject(count);
                current[0] = 0;
                current[2] = true;
                current[1] = count.getTimestamp()-count.getTimestamp()%(duration_seconds*1000000000L);
            }
            if(count.isCount()) current[0] = (int) current[0] + 1;
            else if(count.stricken()) current[2] = false;
        }
        return hoc.finalizeStat();
    }
    public static StatContainer fastestBars(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean fastest, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = (fastest?"Fastest ":"Slowest ")+"Bars";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Speed", "Count"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.TIME, StatDataType.COUNT_OR_UPDATE};
        int[] sort_order = new int[]{fastest?-3:3};
        Object[] default_object = new Object[]{fastest?Long.MAX_VALUE:0L, null, null}; //Time, Count
        Function<Object[], Object[]> finalize = k -> new Object[]{k[0],k[1]};
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false, false, StatSaveMode.NO_CONTINUE);
        long next_bar = 0;
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            if(hoc.switched()) next_bar = 0L;
            if(count.getTimestamp() > next_bar) {
                next_bar = count.getTimestamp()+3600000000000L-count.getTimestamp()%3600000000000L;
                Object[] current = hoc.getTempObject(count);
                current[0] = count.getTimestamp()-(next_bar-3600000000000L);
                current[1] = count;
                hoc.finalizeObject(count);
                current[0] = default_object[0];
            }
        }
        return hoc.finalizeStat();
    }

    public static StatContainer fastestNCounts(List<CountingThread> threads, UUID uuid1, UUID uuid2, int n, boolean fastest, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = fastest?"Fastest ":"Slowest "+n+"Counts";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Time", "Start "+(use_counts?"Count":"Update"), "End "+(use_counts?"Count":"Update")};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.TIME, StatDataType.COUNT_OR_UPDATE, StatDataType.COUNT_OR_UPDATE};
        int[] sort_order = new int[]{fastest?-3:3,4};
        Object[] default_temp_object = new Object[]{-1, null, fastest?Long.MAX_VALUE:-1, UpdateOrCountValue.getDefaultObject(use_counts), null, null}; //Index, Current Data (array of n Counts), record time, first count, last count
        Function<Object[], Object[]> finalize = o-> new Object[]{o[2],o[3],o[4]};
        StatContainer hoc = hoc(threads, uuid1, uuid2, false, use_counters, use_counts, thread_counts_combined);
        List<Integer> user_restrictions = new ArrayList<>();
        for(Object[] user: hoc.getStat()) {
            if((int)user[2] > n) user_restrictions.add((int)user[0]);
        }
        StatGenerator fastest_n_counts = new StatGenerator(threads, uuid1, uuid2, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false, false, StatSaveMode.NO_CONTINUE);
        fastest_n_counts.setAllowedCounters(user_restrictions);
        while(fastest_n_counts.hasNext()) {
            UpdateOrCountValue count = fastest_n_counts.next();
            Object[] current = fastest_n_counts.getTempObject(count);
            if(current[1]==null) current[1] = new UpdateOrCountValue[n];
            current[0] = ((int)current[0]+1)%n;
            if(((UpdateOrCountValue[])current[1])[(int)current[0]]!=null) {
                UpdateOrCountValue prev = ((UpdateOrCountValue[])current[1])[(int)current[0]];
                long record = (long)current[2];
                long current_speed = count.getTimestamp() - prev.getTimestamp();
                if(fastest && current_speed < record || !fastest && current_speed > record) {
                    current[2] = current_speed;
                    current[3] = prev;
                    current[4] = count;
                }
            }
            ((UpdateOrCountValue[])current[1])[(int)current[0]]=count;
        }
        return fastest_n_counts.finalizeStat();
    }
    public static StatContainer dominantNCounts(List<CountingThread> threads, UUID uuid1, UUID uuid2, int n, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = "Most Dominant "+n+"Counts";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Counts", "Start "+(use_counts?"Count":"Update"), "End "+(use_counts?"Count":"Update")};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.COUNT_OR_UPDATE, StatDataType.COUNT_OR_UPDATE};
        int[] sort_order = new int[]{3};
        Object[] default_temp_object = new Object[]{0, 0, UpdateOrCountValue.getDefaultObject(use_counts), null, null}; //Record # of Counts, Current # of Counts, Start Count, End Count
        UpdateOrCountValue[] counts = new UpdateOrCountValue[n];
        Function<Object[], Object[]> finalize = o->new Object[]{o[0], o[2], o[3]};
        int index=0;
        StatGenerator fastest_n_counts = new StatGenerator(threads, uuid1, uuid2, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false, false, StatSaveMode.NO_CONTINUE);
        while(fastest_n_counts.hasNext()) {
            UpdateOrCountValue count = fastest_n_counts.next();
            if(fastest_n_counts.switched()) {
                counts = new UpdateOrCountValue[n];
                index = 0;
            }
            if(counts[index]!=null) {
                Object[] previous = fastest_n_counts.getTempObject(counts[index]);
                previous[1] = (int)previous[1] - 1;
            }
            counts[index] = count;
            index = (index+1)%n;
            Object[] current = fastest_n_counts.getTempObject(count);
            current[1] = (int)current[1] + 1;
            if((int)current[1] > (int)current[0]) {
                current[0] = current[1];
                current[3] = count;
                current[2] = counts[index];
                if(current[2]==null) current[2] = counts[0];
            }
        }
        return fastest_n_counts.finalizeStat();
    }
    public static StatContainer fewestStrikesInNCounts(List<CountingThread> threads, UUID uuid1, UUID uuid2, int n, boolean fewest, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        String name = fewest?"Fewest ":"Most "+"Strikes In "+n+" Counts";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Strikes", "Start Count", "End Count"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.COUNT, StatDataType.COUNT};
        int[] sort_order = new int[]{fewest?-3:3,4};
        Object[] default_temp_object = new Object[]{-1, null, null, 0, fewest?Integer.MAX_VALUE:-1, UpdateValue.getDefaultObject(), null, null}; //Index, Current Data (array of n Counts), Current Data (total stricken at each count), current stricken, record amount, first count, last count
        Function<Object[], Object[]> finalize = o-> new Object[]{o[4],o[5],o[6]};
        StatContainer hoc = hoc(threads, uuid1, uuid2, false, use_counters, true, thread_counts_combined);
        List<Integer> user_restrictions = new ArrayList<>();
        for(Object[] user: hoc.getStat()) {
            if((int)user[2] > n) user_restrictions.add((int)user[0]);
        }
        StatGenerator fastest_n_counts = new StatGenerator(threads, uuid1, uuid2, finalize, default_temp_object, name, headers, types, sort_order, unique, use_counters, false, thread_counts_combined, false, false, StatSaveMode.NO_CONTINUE);
        fastest_n_counts.setAllowedCounters(user_restrictions);
        while(fastest_n_counts.hasNext()) {
            UpdateOrCountValue count = fastest_n_counts.next();
            Object[] current = fastest_n_counts.getTempObject(count);
            if(current[1]==null) current[1] = new UpdateOrCountValue[n];
            if(current[2]==null) current[2] = new int[n];
            int index = (int)current[0];
            if(count.stricken()) {
                if(index==-1) continue;
                ((int[])current[2])[index]++;
                current[3] = (int)current[3] + 1;
            } else if(count.isCount()) {
                index = (index+1)%n;
                current[0] = index;
                if(((UpdateOrCountValue[])current[1])[index]!=null) {
                    current[3] = (int)current[3] - ((int[])current[2])[index];
                    UpdateOrCountValue prev = ((UpdateOrCountValue[])current[1])[(int)current[0]];
                    int record = (int)current[4];
                    int current_strikes = (int)current[3];
                    if(fewest && current_strikes < record || !fewest && current_strikes > record) {
                        current[4] = current_strikes;
                        current[5] = prev;
                        current[6] = count;
                    }
                }
                ((UpdateOrCountValue[])current[1])[index]=count;
                ((int[])current[2])[index]=0;
            }
        }
        return fastest_n_counts.finalizeStat();
    }
    public static StatContainer uniqueTimeParts(List<CountingThread> threads, UUID uuid1, UUID uuid2, int increment_milliseconds, boolean week, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        if(86400000%increment_milliseconds!=0) increment_milliseconds = 3600000;
        int total_parts = (86400000*(week?7:1))/increment_milliseconds;
        String name = "Unique "+increment_milliseconds+"ms Parts in "+(week?"Week":"Day");
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Amount"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{null,null}; //Array of counts in each "part"
        Function<Object[], Object[]> finalize = k -> {
            if(k[0]==null) return new Object[]{0};
            int parts = 0;
            for(int counts:(int[])k[0]) {
                if(counts>0) parts++;
            }
            return new Object[]{parts};
        };
        StatContainer hoc = hoc(threads, uuid1, uuid2, false, use_counters, use_counts, thread_counts_combined);
        List<Integer> user_restrictions = new ArrayList<>();
        for(Object[] user: hoc.getStat()) {
            if((int)user[2] > total_parts/2) user_restrictions.add((int)user[0]);
        }
        StatGenerator time_parts = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false, false, StatSaveMode.NO_CONTINUE);
        if(!(thread_counts_combined && !use_counters)) time_parts.setAllowedCounters(user_restrictions);
        while(time_parts.hasNext()) {
            UpdateOrCountValue count = time_parts.next();
            Object[] current = time_parts.getTempObject(count);
            if(current[0]==null) current[0] = new int[total_parts];
            ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(count.getTimestamp() / 1000000), ZoneId.of(CountingThread.timezone));
            int loc = 60 * 60 * 1000 * date.getHour() + 60 * 1000 * date.getMinute() + 1000 * date.getSecond() + date.getNano() / 1000000;
            if (week) loc += 60 * 60 * 24 * 1000 * (date.getDayOfWeek().getValue() % 7);
            loc /= increment_milliseconds;
            ((int[])current[0])[loc]++;
        }
        return time_parts.finalizeStat();
    }
    public static StatContainer uniqueCountsModN(List<CountingThread> threads, UUID uuid1, UUID uuid2, int n, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        String name = "Unique Counts Mod "+n;
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Amount"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{null,null}; //Array of counts in each "part"
        Function<Object[], Object[]> finalize = k -> {
            if(k[0]==null) return new Object[]{0};
            int parts = 0;
            for(int counts:(int[])k[0]) {
                if(counts>0) parts++;
            }
            return new Object[]{parts};
        };
        StatContainer hoc = hoc(threads, uuid1, uuid2, false, use_counters, true, thread_counts_combined);
        List<Integer> user_restrictions = new ArrayList<>();
        for(Object[] user: hoc.getStat()) {
            if((int)user[2] > n/2) user_restrictions.add((int)user[0]);
        }
        StatGenerator time_parts = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, true, thread_counts_combined, false, false, StatSaveMode.NO_CONTINUE);
        if(!(thread_counts_combined && !use_counters)) time_parts.setAllowedCounters(user_restrictions);
        while(time_parts.hasNext()) {
            UpdateOrCountValue count = time_parts.next();
            Object[] current = time_parts.getTempObject(count);
            if(current[0]==null) current[0] = new int[n];
            ((int[])current[0])[count.validCountNumber()%n]++;
        }
        return time_parts.finalizeStat();
    }

    public static StatContainer countsPerMistake(List<CountingThread> threads, UUID uuid1, UUID uuid2, int min_counts, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        String name = "Counts Per Mistake";
        if(min_counts>0) name+="(Min "+min_counts+" Counts)";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Counts Per Mistake"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{0,0,null}; //Counts, Strikes
        Function<Object[], Object[]> finalize = o->{
            if((int)o[0]==0 && (int)o[1]==0) return new Object[]{-1};
            if((int)o[0]<min_counts) return new Object[]{-1};
            return new Object[]{((int)o[0]/(float)(int)o[1])};
        };
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, false, thread_counts_combined, false, false, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            if(count.isCount()) current[0] = (int)current[0]+1;
            else if(count.stricken()) current[1] = (int)current[1]+1;
        }
        return hoc.finalizeStat();
    }
    public static StatContainer noMistakeStreak(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean unique, boolean use_counters, boolean thread_counts_combined) {
        String name = "No Mistake Streak";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Counts", "Start Count", "End Count"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.COUNT, StatDataType.COUNT};
        int[] sort_order = new int[]{3, -4};
        Object[] default_object = new Object[]{0, UpdateValue.getDefaultObject(), null, null};
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, o-> Arrays.copyOf(o,o.length-1), default_object, name, headers, types, sort_order, unique, use_counters, false, thread_counts_combined, false, false, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            if(count.stricken()) {
                hoc.finalizeObject(count);
                current[0] = 0;
                current[1] = UpdateValue.getDefaultObject();
                current[2] = null;
            } else if(count.isCount()) {
                current[0] = (int) current[0] + 1;
                if(((UpdateValue)current[1]).getTimestamp()==0L) current[1] = count;
                current[2] = count;
            }
        }
        return hoc.finalizeStat();
    }
    /**The user must have NONE of the counts in between the two replies (for cbr > 1)*/
    public static StatContainer perfectStreak(List<CountingThread> threads, UUID uuid1, UUID uuid2, int counts_between_replies, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = "Perfect "+counts_between_replies+" Streaks";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Streak", "Start Count", "End Count"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.COUNT_OR_UPDATE, StatDataType.COUNT_OR_UPDATE};
        int[] sort_order = new int[]{3, -4};
        Object[] default_object = new Object[]{0,UpdateOrCountValue.getDefaultObjectWithMaxTimestamp(use_counts), null, null}; //Streak, Start Count, End Count
        Function<Object[], Object[]> finalize = k -> new Object[]{k[0], k[1], k[2]};
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false, false, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            UpdateOrCountValue prev = (UpdateOrCountValue) current[2];
            if(prev!=null && count.validCountNumber() - prev.validCountNumber() == counts_between_replies) {
                current[0] = (int)current[0]+1;
            } else {
                hoc.finalizeObject(count);
                current[0] = 1;
                current[1] = count;
            }
            current[2] = count;
        }
        return hoc.finalizeStat();
    }
    public static StatContainer rotationStreak(List<CountingThread> threads, UUID uuid1, UUID uuid2, int counts_between_replies, boolean duplicate_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = "Perfect "+counts_between_replies+" Rotation Streaks"+(duplicate_counters?" With Duplicates":"");
        String[] headers = new String[]{"Users", "Thread", "Streak", "Start Count", "End Count"};
        StatDataType[] types = new StatDataType[]{StatDataType.LIST_COUNTER, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.COUNT_OR_UPDATE, StatDataType.COUNT_OR_UPDATE};
        int[] sort_order = new int[]{3, -4};
        Object[] default_object = new Object[]{0,UpdateOrCountValue.getDefaultObject(use_counts), null, null, false, null}; //Streak, Start Count, End Count, List of Users, Contains Duplicate
        Function<Object[], Object[]> finalize = k -> {
            if((boolean)k[4]) return new Object[]{k[3], 0, k[1], k[2]};
            return new Object[]{k[3], k[0], k[1], k[2]};
        };
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, false, true, use_counts, thread_counts_combined, false, false, StatSaveMode.NO_CONTINUE);
        ArrayList<UpdateOrCountValue> current_users = new ArrayList<>();
        UpdateOrCountValue streak_start = null;
        int current_streak = 0;
        int current_index = 0;
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            if(current_users.size() < counts_between_replies) {
                if(streak_start==null) streak_start = count;
                current_users.add(count);
                current_streak++;
                current_index = (current_index+1)%counts_between_replies;
                continue;
            }
            UpdateOrCountValue prev = current_users.get(current_index);
            if(duplicate_counters && prev.getAuthorId()==count.getAuthorId()
                    || !duplicate_counters && matchOnlyIndex(current_index, current_users, count)) current_streak++;
            else {
                Object[] current = hoc.getTempObject(count);
                current[0] = current_streak;
                current[1] = streak_start;
                current[2] = current_users.get((current_index+counts_between_replies-1)%counts_between_replies);
                current[3] = current_users.stream().map(UpdateOrCountValue::getAuthorId).collect(Collectors.toList());
                if(current_users.stream().map(UpdateOrCountValue::getAuthorId).collect(Collectors.toSet()).size() != current_users.size()) {
                    current[4] = true;
                } else {
                    current[4] = false;
                    hoc.finalizeObject(count);
                }
                current[0] = 0; //To avoid duplication on finalization
                current_streak = counts_between_replies;
                streak_start = current_users.get((current_index+1)%counts_between_replies);
            }
            current_users.set(current_index, count);
            current_index = (current_index+1)%counts_between_replies;
        }
        return hoc.finalizeStat();
    }
    private static boolean matchOnlyIndex(int index, List<UpdateOrCountValue> counts, UpdateOrCountValue current) {
        for(int i=0;i<counts.size();i++) {
            if((i==index) != (counts.get(i).getAuthorId()==current.getAuthorId())) {
                return false;
            }
        }
        return true;
    }
    public static StatContainer nMsReplies(List<CountingThread> threads, UUID uuid1, UUID uuid2, int n, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined, boolean add_thread_stats) {
        String name = n+"ms Replies";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Amount"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_object = new Object[]{0,null}; //Amount
        Function<Object[], Object[]> finalize = k -> new Object[]{k[0]};
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false, add_thread_stats, StatSaveMode.NO_CONTINUE);
        UpdateOrCountValue last_count = null;
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            if(hoc.switched()) last_count = null;
            if(last_count!=null && (count.getTimestamp()-last_count.getTimestamp())/1000000 == n) {
                Object[] current = hoc.getTempObject(count);
                current[0] = (int)current[0] + 1;
            }
            last_count = count;
        }
        return hoc.finalizeStat();
    }
    public static StatContainer countsUnderNMsStreak(List<CountingThread> threads, UUID uuid1, UUID uuid2, int n, boolean unique, boolean use_counters, boolean use_counts, boolean thread_counts_combined) {
        String name = (use_counts?"Counts":"Updates")+" Under "+n+"ms Streaks";
        String[] headers = new String[]{use_counters?"User":"Thread", "Thread", "Streak", "Start Count", "End Count"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.COUNT_OR_UPDATE, StatDataType.COUNT_OR_UPDATE};
        int[] sort_order = new int[]{3, -4};
        Object[] default_object = new Object[]{0,UpdateOrCountValue.getDefaultObjectWithMaxTimestamp(use_counts), null, null}; //Streak, Start Count, End Count
        Function<Object[], Object[]> finalize = k -> new Object[]{k[0], k[1], k[2]};
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, use_counters, use_counts, thread_counts_combined, false, false, StatSaveMode.CONTINUE);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            Object[] current = hoc.getTempObject(count);
            UpdateOrCountValue prev = (UpdateOrCountValue) current[2];
            if(prev!=null && (count.getTimestamp() - prev.getTimestamp())/1000000 < n) {
                current[0] = (int)current[0]+1;
            } else {
                hoc.finalizeObject(count);
                current[0] = 0;
                current[1] = count;
            }
            current[2] = count;
        }
        return hoc.finalizeStat();
    }
    public static StatContainer oddEvenRatio(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean odd, int min_counts, boolean unique, boolean thread_counts_combined) {
        String name = odd?"Odd":"Even"+" Ratio"+(min_counts>1?"":" (Minimum "+min_counts+" Counts)");
        String[] headers = new String[]{"User", "Thread", "Percentage"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_temp_object = new Object[]{-1, 0, null}; //Ratio, Total Counts
        Function<Object[], Object[]> finalize = o->new Object[]{o[0]};
        List<Object[]> odd_hoc = hocWithFilter(odd?"Odd":"Even", "Counts",k->k.validCountNumber()%2==(odd?1:0), threads, uuid1, uuid2, unique, true, true, thread_counts_combined, false).getStat();
        List<Object[]> hoc = hoc(threads, uuid1, uuid2, unique, true, true, thread_counts_combined).getStat();
        Map<ThreadData, Map<CounterData, Integer>> hoc_map = new HashMap<>();
        for(Object[] user: odd_hoc) {
            ThreadData thread = thread_counts_combined ? AllThreads.combined_thread.thread : AllThreads.all_threads_with_combined.get((int)user[0]).thread;
            Map<CounterData, Integer> thread_hoc = hoc_map.computeIfAbsent(thread, k->new HashMap<>());
            CounterData counter = Counters.counters.get((int)user[0]);
            thread_hoc.put(counter, (int)user[2]);
        }
        ListStatGenerator<Object[]> odd_ratio = new ListStatGenerator<>(hoc, finalize, default_temp_object, name, headers, types, sort_order, unique, true, true, thread_counts_combined, false);
        while(odd_ratio.hasNext()) {
            Object[] user = odd_ratio.next();
            if((int)user[2] < min_counts) continue;
            Object[] current = odd_ratio.getTempObject((int) user[0], (int) user[1]);
            ThreadData thread = thread_counts_combined ? AllThreads.combined_thread.thread : ((UpdateOrCountValue)user[0]).getThread().thread;
            CounterData counter = ((UpdateOrCountValue)user[0]).getAuthor();
            int counts = hoc_map.get(thread).getOrDefault(counter,0);
            current[0] = (float)counts/(int)user[2];
            odd_ratio.finalizeObject((int) user[0], (int) user[1]);
            current[0] = -1;
        }
        return odd_ratio.finalizeStat();
    }

    /**
     * @return 1 for Chalupa Exacta, 2 for Poonxacta, 3 for trejexacta, 4 for trifecta, 0 otherwise
     */
    private static int checkTrifecta(UpdateOrCountValue[] bars) {
        if(bars.length==3) {
            if(bars[0]!=null && bars[1]!=null && bars[0].getAuthorId()==bars[1].getAuthorId() && (bars[2]==null || bars[0].getAuthorId()!=bars[2].getAuthorId())) {
                return 2;
            } else if(bars[0]!=null && bars[2]!=null && bars[0].getAuthorId()==bars[2].getAuthorId() && (bars[1]==null || bars[0].getAuthorId()!=bars[1].getAuthorId())) {
                return 3;
            } else if(bars[1]!=null && bars[2]!=null && bars[1].getAuthorId()==bars[2].getAuthorId() && (bars[0]==null || bars[0].getAuthorId()!=bars[1].getAuthorId())) {
                return 1;
            }
        }
        int trifecta_user = -1;
        for(UpdateOrCountValue bar: bars) {
            if(bar==null || (trifecta_user!=-1 && bar.getAuthorId() != trifecta_user)) {
                trifecta_user = -2;
                break;
            }
            trifecta_user = bar.getAuthorId();
        }
        if(trifecta_user>=0) return 4;
        return 0;
    }
    /**Exactas only work with exactly 3 threads. Chalupa Exacta is threads 2/3, trejexacta is threads 1/3, poonxacta is threads 1/2, so main/bars/slow in that order is required for the standard exacta to be applied*/
    public static StatContainer trifecta(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean anti, boolean use_counts) {
        String name = anti?"Antifectas":"Trifectas";
        String[] headers = threads.size()==3 ? new String[]{"User", "Thread", name, anti?"Antiexactas":"Exactas", (anti?"Anti ":"")+"Chalupa Exactas", anti?"Antipoonxactas":"Poonxactas", anti?"Antitrejexactas":"trejexactas"} : new String[]{"User", "Thread", name};
        StatDataType[] types = threads.size()==3 ? new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER} : new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = threads.size()==3 ? new int[]{3, 4, 5, 6, 7} : new int[]{3};
        Object[] default_object = new Object[]{0,0,0,0,0,null}; //Trifecta, Chalupa, Poon, treje, Exactas
        Function<Object[], Object[]> finalize = k -> {
            if(threads.size()==3) return new Object[]{k[0], k[4], k[1], k[2], k[3]};
            return new Object[]{k[0]};
        };
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, true, true, use_counts, true, false, false, StatSaveMode.NO_SAVE);
        long next_bar = 0L;
        UpdateOrCountValue[] bars = new UpdateOrCountValue[threads.size()];
        Map<String, Integer> thread_map = new HashMap<>();
        for(int i=0;i<threads.size();i++) thread_map.put(threads.get(i).thread.name, i);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            if(count.getTimestamp()>=next_bar) {
                int result = checkTrifecta(bars);
                switch(result) {
                    case 1 -> {
                        Object[] current = hoc.getTempObject(bars[1]);
                        current[1] = (int) current[1] + 1;
                        current[4] = (int) current[4] + 1;
                    }
                    case 2 -> {
                        Object[] current = hoc.getTempObject(bars[0]);
                        current[2] = (int) current[2] + 1;
                        current[4] = (int) current[4] + 1;
                    }
                    case 3 -> {
                        Object[] current = hoc.getTempObject(bars[0]);
                        current[3] = (int) current[3] + 1;
                        current[4] = (int) current[4] + 1;
                    }
                    case 4 -> {
                        Object[] current = hoc.getTempObject(bars[0]);
                        current[0] = (int) current[0] + 1;
                    }
                }
                next_bar = count.getTimestamp()+3600000000000L-count.getTimestamp()%(3600000000000L);
                bars = new UpdateOrCountValue[threads.size()];
            }
            int index = thread_map.get(count.getThreadName());
            if(anti || bars[index] == null) bars[index] = count;
        }
        return hoc.finalizeStat();
    }
    public static List<DayHOC> trifectaDayHocs(List<CountingThread> threads, TimeUnit start_date, TimeUnit end_date, ParticipationDuration mode, boolean anti, boolean use_counts) {
        String name = (anti ? "Antifecta " : "Trifecta ") + mode.toCapitalizedString() + " HoC";
//        if(threads.size()==3) for(CountingThread thread:threads) name.append(" ").append(thread.thread.id);
        start_date = TimeUnit.getMinTimeUnitOfThreads(threads, start_date, mode);
        TimeUnit cgg_start_date = TimeUnit.getCGGStartDate(mode);
        end_date = TimeUnit.getMaxTimeUnitOfThreads(threads, end_date, mode);
        String[] headers = threads.size()==3 ? new String[]{"User", "Thread", (anti?"Antifectas":"Trifectas"), anti?"Antiexactas":"Exactas", (anti?"Anti ":"")+"Chalupa Exactas", anti?"Antipoonxactas":"Poonxactas", anti?"Antitrejexactas":"trejexactas"} : new String[]{"User", "Thread", anti?"Antifecta ":"Trifecta "+mode.toCapitalizedString()+" HoC"};
        StatDataType[] types = threads.size()==3 ? new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER} : new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = threads.size()==3 ? new int[]{3, 4, 5, 6, 7} : new int[]{3};
        Object[] default_object = new Object[]{0,0,0,0,0,null}; //Trifecta, Chalupa, Poon, treje, Exactas
        Function<Object[], Object[]> finalize = k -> {
            if(threads.size()==3) return new Object[]{k[0], k[4], k[1], k[2], k[3]};
            return new Object[]{k[0]};
        };
        MultiStatGenerator hoc = new MultiStatGenerator(threads, cgg_start_date, TimeUnit.today(mode), mode, finalize, default_object, name, headers, types, sort_order, true, true, use_counts, true, StatSaveMode.NO_SAVE);
        List<DayHOC> day_hocs = new ArrayList<>();
        long next_day_timestamp = 0L;
        long next_bar = 0L;
        UpdateOrCountValue[] bars = new UpdateOrCountValue[threads.size()];
        Map<String, Integer> thread_map = new HashMap<>();
        for(int i=0;i<threads.size();i++) thread_map.put(threads.get(i).thread.name, i);
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            if(count.getTimestamp()>=next_bar) {
                int result = checkTrifecta(bars);
                switch(result) {
                    case 1 -> {
                        Object[] current = hoc.getTempObject(bars[1]);
                        current[1] = (int) current[1] + 1;
                        current[4] = (int) current[4] + 1;
                    }
                    case 2 -> {
                        Object[] current = hoc.getTempObject(bars[0]);
                        current[2] = (int) current[2] + 1;
                        current[4] = (int) current[4] + 1;
                    }
                    case 3 -> {
                        Object[] current = hoc.getTempObject(bars[0]);
                        current[3] = (int) current[3] + 1;
                        current[4] = (int) current[4] + 1;
                    }
                    case 4 -> {
                        Object[] current = hoc.getTempObject(bars[0]);
                        current[0] = (int) current[0] + 1;
                    }
                }
                next_bar = count.getTimestamp()+3600000000000L-count.getTimestamp()%(3600000000000L);
                bars = new UpdateOrCountValue[threads.size()];
            }
            if(hoc.switched() || count.getTimestamp() > next_day_timestamp) {
                hoc.setIndex((int)TimeUnit.timeBetween(cgg_start_date, new TimeUnit(count.getTimestamp(), CountingThread.timezone, mode), mode));
                next_day_timestamp = Util.getNextDayTimestamp(count.getTimestamp(), CountingThread.timezone, mode);
            }
            int index = thread_map.get(count.getThreadName());
            if(anti || bars[index] == null) bars[index] = count;
        }
        List<StatContainer> hocs_without_date = hoc.finalizeStat();
        TimeUnit current_date = start_date;
        int start = (int)TimeUnit.timeBetween(cgg_start_date, start_date, mode);
        for(int i=start;i<start+TimeUnit.timeBetween(start_date, end_date, mode);i++) {
            day_hocs.add(new DayHOC(current_date, hocs_without_date.get(i)));
            current_date = current_date.addTime(1);
        }
        return day_hocs;
    }
    public static StatContainer trifectaDayHoc(List<CountingThread> threads, TimeUnit start_date, ParticipationDuration mode, boolean anti, boolean use_counts) {
        List<DayHOC> day_hoc = trifectaDayHocs(threads, start_date, start_date.addTime(1), mode, anti, use_counts);
        if(day_hoc.isEmpty()) {
            return new StatContainer(null, mode.toCapitalizedString()+" HoC", new String[0], new StatDataType[0], new int[0], true, true, use_counts, true, false, StatSaveMode.NO_SAVE);
        } else return day_hoc.get(0).hoc();
    }
    public static StatContainer trifectaDayParts(List<CountingThread> threads, TimeUnit start_date, TimeUnit end_date, ParticipationDuration mode, boolean anti, boolean use_counts) {
        String name = anti?"Antifecta ":"Trifecta "+mode.toCapitalizedString()+" Parts";
        String[] headers = threads.size()==3 ? new String[]{"User", "Thread", name, (anti?"Antiexacta ":"Exacta ")+mode.toCapitalizedString()+" Parts", (anti?"Anti ":"")+"Chalupa Exacta "+mode.toCapitalizedString()+" Parts", anti?"Antipoonxacta ":"Poonxacta "+mode.toCapitalizedString()+" Parts", (anti?"Antitrejexacta ":"trejexacta ")+mode.toCapitalizedString()+" Parts"} : new String[]{"User", "Thread", name};
        StatDataType[] types = threads.size()==3 ? new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER, StatDataType.NUMBER} : new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = threads.size()==3 ? new int[]{3, 4, 5, 6, 7} : new int[]{3};
        Object[] default_object = new Object[]{0,0,0,0,0,null}; //Trifecta, Exactas, Chalupa, Poon, treje
        Function<Object[], Object[]> finalize = k -> {
            if(threads.size()==3) return new Object[]{k[0], k[1], k[2], k[3], k[4]};
            return new Object[]{k[0]};
        };
        List<DayHOC> day_hocs = trifectaDayHocs(threads, start_date, end_date, mode, anti, use_counts);
        ListStatGenerator<DayHOC> day_parts = new ListStatGenerator<>(day_hocs, finalize, default_object, name, headers, types, sort_order, true, true, use_counts, true, false);
        while(day_parts.hasNext()) {
            DayHOC day = day_parts.next();
            List<Object[]> hoc = day.hoc().getStat();
            for(Object[] user: hoc) {
                Object[] current = day_parts.getTempObject((int) user[0], (int) user[1]);
                if((int) user[2] > 0) current[0] = (int) current[0] + 1;
                if(threads.size()==3) {
                    if ((int) user[3] > 0) current[1] = (int) current[1] + 1;
                    if ((int) user[4] > 0) current[2] = (int) current[2] + 1;
                    if ((int) user[5] > 0) current[3] = (int) current[3] + 1;
                    if ((int) user[6] > 0) current[4] = (int) current[4] + 1;
                }
            }
        }
        return day_parts.finalizeStat();
    }
    /**
     * @param type 0 is trifecta
     *             1 is combined exactas
     *             2 is chalupa exacta,
     *             3 is poonxacta,
     *             4 is trejexacta,
     *             anything else is combined exactas
     * */
    public static StatContainer trifectaDayStreak(List<CountingThread> threads, TimeUnit start_date, TimeUnit end_date, ParticipationDuration mode, int type, boolean anti, boolean unique, boolean use_counts) {
        String name = switch(type) {
            case 0 -> anti?"Antifecta ":"Trifecta "+mode.toCapitalizedString()+" Streak";
            case 1 -> anti?"Antiexacta ":"Exacta "+mode.toCapitalizedString()+" Streak";
            case 2 -> anti?"Anti Chalupa Exacta ":"Chalupa Exacta "+mode.toCapitalizedString()+" Streak";
            case 3 -> anti?"Antipoonxacta ":"Poonxacta "+mode.toCapitalizedString()+" Streak";
            default -> anti?"Antitrejexacta ":"trejexacta "+mode.toCapitalizedString()+" Streak"; // case 4
        };
        if(type<0||type>4) type = 0;
        String[] headers = new String[]{"User", "Thread", "Streak", "Start Date", "End Date"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.TIMEUNIT, StatDataType.TIMEUNIT};
        int[] sort_order = new int[]{3, -4};
        Object[] default_object = new Object[]{new TimeUnit(0,CountingThread.timezone,mode), 0,null}; //Last Date, Streak
        Function<Object[], Object[]> finalize = o->new Object[]{o[1],((TimeUnit)o[0]).subtractTime((int)o[1]-1),o[0]};
        List<DayHOC> day_hocs = trifectaDayHocs(threads, start_date, end_date, mode, anti, use_counts);
        ListStatGenerator<DayHOC> day_parts = new ListStatGenerator<>(day_hocs, finalize, default_object, name, headers, types, sort_order, unique, true, use_counts, true, false);
        if(threads.size()!=3 && type!=0) return day_parts.finalizeStat();
        while(day_parts.hasNext()) {
            DayHOC day = day_parts.next();
            List<Object[]> hoc = day.hoc().getStat();
            for(Object[] user: hoc) {
                Object[] current = day_parts.getTempObject((int) user[0], (int) user[1]);
                if((int)user[type+2] >= 1) {
                    if(TimeUnit.timeBetween((TimeUnit)current[0], day.date(), mode)==1) {
                        current[1] = (int) current[1] + 1;
                    } else {
                        day_parts.finalizeObject((int) user[0], (int) user[1]);
                        current[1] = 1;
                    }
                    current[0] = day.date();
                }
            }
        }
        return day_parts.finalizeStat();
    }

    public static StatContainer countingPairs(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean hour, int duration_seconds, boolean duplicate_counters, boolean unique, boolean use_counts, boolean thread_counts_combined, boolean add_thread_stats) {
        if(duration_seconds<=0 || 3600%duration_seconds!=0) duration_seconds = 3600;
        String name = (hour?duration_seconds+"s ":"")+"Counting Pairs"+(duplicate_counters?"":" (No Double Counts)");
        String[] headers = hour ? new String[]{"User 1", "User 2", "Thread", "Counts", "Hour"} : new String[]{"User 1", "User 2", "Thread", "Counts"};
        StatDataType[] types = hour ? new StatDataType[]{StatDataType.FOCUS, StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER, StatDataType.HOUR} : new StatDataType[]{StatDataType.FOCUS, StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = hour ? new int[]{4,5} : new int[]{4};
        Object[] default_object = new Object[]{0, 0L, null, null}; //Counts, Hour
        Function<Object[], Object[]> finalize = k -> {
            if(hour) return new Object[]{k[0], k[1]};
            return new Object[]{k[0]};
        };
        StatGenerator hoc = new StatGenerator(threads, uuid1, uuid2, finalize, default_object, name, headers, types, sort_order, unique, true, use_counts, thread_counts_combined, false, add_thread_stats, StatSaveMode.NO_CONTINUE);
        UpdateOrCountValue prev_prev = null;
        UpdateOrCountValue prev = null;
        while(hoc.hasNext()) {
            UpdateOrCountValue count = hoc.next();
            if(hoc.switched()) prev = null;
            if(prev!=null && (duplicate_counters || prev.getAuthorId()!=count.getAuthorId())) {
                Object[] current = hoc.getTempObject(count, prev);
                if(hour && count.getTimestamp() >= (long)current[1]) {
                    hoc.finalizeObject(count, prev);
                    current[0] = 0;
                    current[1] = count.getTimestamp()+duration_seconds*1000000000L-count.getTimestamp()%(duration_seconds*1000000000L);
                }
                current[0] = (int)current[0]+1;

                if (prev_prev==null || prev_prev.getAuthorId() != count.getAuthorId()) {
                    current = hoc.getTempObject(prev, count);
                    if(hour && count.getTimestamp() >= (long)current[1]) {
                        hoc.finalizeObject(count, prev);
                        current[0] = 0;
                        current[1] = count.getTimestamp()+duration_seconds*1000000000L-count.getTimestamp()%(duration_seconds*1000000000L);
                    }
                    current[0] = (int)current[0]+1;
                }
            }
            prev_prev = prev;
            prev = count;
        }
        return hoc.finalizeStat();
    }
    public static StatContainer favoriteCounters(List<CountingThread> threads, UUID uuid1, UUID uuid2, boolean duplicate_counters, boolean unique, boolean use_counts, boolean thread_counts_combined, boolean add_thread_stats) {
        String name = "Favorite Counters";
        String[] headers = new String[]{"User 1", "User 2", "Thread", "Counts"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.COUNTER, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{4};
        Object[] default_temp_object = new Object[]{0, null, null}; //Counts, Counter
        Function<Object[], Object[]> finalize = o->new Object[]{o[1], o[0]};
        List<Object[]> counting_pairs = countingPairs(threads, uuid1, uuid2, false, -1, duplicate_counters,unique, use_counts, thread_counts_combined, add_thread_stats).getStat();
        ListStatGenerator<Object[]> counting_pairs_specific_user = new ListStatGenerator<>(counting_pairs, finalize, default_temp_object, name, headers, types, sort_order, unique, true, use_counts, thread_counts_combined, add_thread_stats);
        while(counting_pairs_specific_user.hasNext()) {
            Object[] pair = counting_pairs_specific_user.next();
            Object[] current = counting_pairs_specific_user.getTempObject((int) pair[0], (int) pair[2]);
            if((int)pair[3] > (int)current[0]) {
                current[0] = pair[3];
                current[1] = pair[1];
            }
        }
        return counting_pairs_specific_user.finalizeStat();
    }
    public static StatContainer countingPairsSpecificUser(List<CountingThread> threads, UUID uuid1, UUID uuid2, String username, boolean duplicate_counters, boolean unique, boolean use_counts, boolean thread_counts_combined, boolean add_thread_stats) {
        String name = "Counts with "+username+(duplicate_counters?"":" (No Duplicate Counters)");
        String[] headers = new String[]{"User", "Thread", "Counts"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_temp_object = new Object[]{0, null}; //Counts
        Function<Object[], Object[]> finalize = o->new Object[]{o[0]};
        List<Object[]> counting_pairs = countingPairs(threads, uuid1, uuid2, false, -1, duplicate_counters,unique, use_counts, thread_counts_combined, add_thread_stats).getStat();
        ListStatGenerator<Object[]> counting_pairs_specific_user = new ListStatGenerator<>(counting_pairs, finalize, default_temp_object, name, headers, types, sort_order, unique, true, use_counts, thread_counts_combined, add_thread_stats);
        while(counting_pairs_specific_user.hasNext()) {
            Object[] pair = counting_pairs_specific_user.next();
            if(Objects.equals(Counters.counters.get((int)pair[0]).username(), username)) {
                Object[] current = counting_pairs_specific_user.getTempObject((int) pair[1], (int) pair[2]);
                current[0] = pair[3];
                counting_pairs_specific_user.finalizeObject((int) pair[1], (int) pair[2]);
                current[0] = 0;
            }
        }
        return counting_pairs_specific_user.finalizeStat();
    }
    public static StatContainer percentageOfCountsWithUser(List<CountingThread> threads, UUID uuid1, UUID uuid2, String username, int min_counts, boolean unique, boolean use_counts, boolean thread_counts_combined, boolean add_thread_stats) {
        String name = "Percentage of User's Counts Performed With "+username+(min_counts>1?"":" (Minimum "+min_counts+" Counts)");
        String[] headers = new String[]{"User", "Thread", "Percentage"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.PERCENT};
        int[] sort_order = new int[]{3};
        Object[] default_temp_object = new Object[]{0, null}; //Counts
        Function<Object[], Object[]> finalize = o->new Object[]{o[0]};
        List<Object[]> counting_pairs = countingPairs(threads, uuid1, uuid2, false, -1, true, unique, use_counts, thread_counts_combined, add_thread_stats).getStat();
        List<Object[]> hoc = hoc(threads, uuid1, uuid2, unique, true, use_counts, thread_counts_combined).getStat();
        Map<ThreadData, Map<CounterData, Integer>> hoc_map = new HashMap<>();
        for(Object[] user: hoc) {
            ThreadData thread = thread_counts_combined ? AllThreads.combined_thread.thread : AllThreads.all_threads_with_combined.get((int)user[2]).thread;
            Map<CounterData, Integer> thread_hoc = hoc_map.computeIfAbsent(thread, k->new HashMap<>());
            CounterData counter = Counters.counters.get((int)user[0]);
            thread_hoc.put(counter, (int)user[2]);
        }
        ListStatGenerator<Object[]> counting_pairs_specific_user = new ListStatGenerator<>(counting_pairs, finalize, default_temp_object, name, headers, types, sort_order, unique, true, use_counts, thread_counts_combined, add_thread_stats);
        while(counting_pairs_specific_user.hasNext()) {
            Object[] pair = counting_pairs_specific_user.next();
            if(Objects.equals(Counters.counters.get((int)pair[0]).username(), username)) {
                ThreadData thread = thread_counts_combined ? AllThreads.combined_thread.thread : AllThreads.all_threads_with_combined.get((int)pair[2]).thread;
                CounterData counter =  Counters.counters.get((int)pair[1]);
                int counts = hoc_map.get(thread).get(counter);
                if(counts < min_counts) continue;
                Object[] current = counting_pairs_specific_user.getTempObject((int) pair[1], (int) pair[2]);
                current[0] = (float)(int)pair[3]/counts;
                counting_pairs_specific_user.finalizeObject((int) pair[1], (int) pair[2]);
                current[0] = 0;
            }
        }
        return counting_pairs_specific_user.finalizeStat();
    }
    public static StatContainer usersNotCountedWith(List<CountingThread> threads, UUID uuid1, UUID uuid2, String username, boolean unique, boolean use_counts, boolean thread_counts_combined, boolean add_thread_stats) {
        String name = "Users With the Most "+(use_counts?"Counts ":"Updates ")+username+" Has Not Counted With";
        String[] headers = new String[]{"User", "Thread", "Counts"};
        StatDataType[] types = new StatDataType[]{StatDataType.FOCUS, StatDataType.THREAD_INSERT, StatDataType.NUMBER};
        int[] sort_order = new int[]{3};
        Object[] default_temp_object = new Object[]{0, null}; //Counts
        Function<Object[], Object[]> finalize = o->new Object[]{o[0]};
        List<Object[]> counting_pairs = countingPairs(threads, uuid1, uuid2, false, -1, true, unique, use_counts, thread_counts_combined, add_thread_stats).getStat();
        List<Object[]> hoc = hoc(threads, uuid1, uuid2, unique, true, use_counts, thread_counts_combined).getStat();
        Map<ThreadData, Set<CounterData>> counting_pairs_map = new HashMap<>();
        for(Object[] user: counting_pairs) {
            if(!Objects.equals(Counters.counters.get((int)user[0]).username(), username)) continue;
            ThreadData thread = thread_counts_combined ? AllThreads.combined_thread.thread : AllThreads.all_threads_with_combined.get((int)user[2]).thread;
            Set<CounterData> thread_hoc = counting_pairs_map.computeIfAbsent(thread, k->new HashSet<>());
            CounterData counter = Counters.counters.get((int)user[1]);
            thread_hoc.add(counter);
        }
        ListStatGenerator<Object[]> users_not_counted_with = new ListStatGenerator<>(hoc, finalize, default_temp_object, name, headers, types, sort_order, unique, true, use_counts, thread_counts_combined, add_thread_stats);
        while(users_not_counted_with.hasNext()) {
            Object[] user = users_not_counted_with.next();
            ThreadData thread = thread_counts_combined ? AllThreads.combined_thread.thread : AllThreads.all_threads_with_combined.get((int)user[1]).thread;
            CounterData counter =  Counters.counters.get((int)user[0]);
            if(counting_pairs_map.containsKey(thread) && counting_pairs_map.get(thread).contains(counter)) continue;
            Object[] current = users_not_counted_with.getTempObject((int) user[0], (int) user[1]);
            current[0] = user[2];
            users_not_counted_with.finalizeObject((int) user[0], (int) user[1]);
            current[0] = 0;
        }
        return users_not_counted_with.finalizeStat();
    }
}
