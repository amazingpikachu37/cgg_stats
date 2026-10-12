package Stats;

import Threads.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static Stats.CountingThread.*;

public class AllThreads {
    public static List<CountingThread> all_threads = new ArrayList<>();
    public static List<CountingThread> traditional = new ArrayList<>();
    public static List<CountingThread> double_counting = new ArrayList<>();
    public static List<CountingThread> no_mistakes = new ArrayList<>();
    public static List<CountingThread> miscellaneous = new ArrayList<>();
    public static List<CountingThread> all_threads_with_combined;
    public static final CountingThread combined_thread;
    static {
        List<ThreadData> threads_temp = StatLoader.loadFromBytes("SidethreadData/Threads");
        if(threads_temp == null) {
            updateThreadList();
        } else {
            all_threads = new ArrayList<>();
            for(ThreadData thread: threads_temp) {
                CountingThread t = createCountingThread(thread);
                all_threads.add(t);
                switch(thread.category) {
                    case "Traditional" -> traditional.add(t);
                    case "Double Counting" -> double_counting.add(t);
                    case "No Mistakes" -> no_mistakes.add(t);
                    case "Miscellaneous" -> miscellaneous.add(t);
                    default -> System.out.println("Thread Category Not Found: "+thread.category);
                }
            }
        }
        combined_thread = new CountingThread(new ThreadData(0,"all_threads","All Threads"));
        all_threads_with_combined = new ArrayList<>();
        all_threads_with_combined.add(combined_thread);
        all_threads_with_combined.addAll(all_threads);
    }

    public static void updateFullDB() throws IOException, URISyntaxException {
        if(full_db_total_updates!=Util.sumIntList(full_db_updates_per_file)) {
            throw new RuntimeException("Error: filedata desync in full db");
        }
        for(CountingThread thread : all_threads_with_combined) {
            if(!thread.validateFileDataIntegrity()) throw new RuntimeException("Error: filedata desync in thread "+thread.thread.name);
        }
        if(first_and_last_full_db_updates.isEmpty()) {
            handleThreadID();
        } else {
            List<FullUpdateValue> updates = combined_thread.readUpdateOrCountFile(full_db_updates_per_file.size()-1, CountDataType.FULL);
            String last_update = updates.get(updates.size()-1).uuid().toString();
            handleThreadID(Util.getUUIDFromCurrentTime(), new String[]{"uuid="+last_update,"commentsOnly=false","validCountsOnly=false"});
        }
        syncFullDB();
    }

    private static void handleThreadID() throws IOException, URISyntaxException {
        handleThreadID(Util.getUUIDFromCurrentTime(), new String[]{"commentsOnly=false","validCountsOnly=false"});
    }
    /*
       On download of updates, when sending counts to get updated, leave off all counts in the last second and sort first.
       If this is a 100,000 update break, then keep the last update and all within one second for the next round.
       If this is the final update, then do not send any updates within one second of the stop point.
       */
    private static void handleThreadID(UUID stop_point, String[] parameters) throws IOException, URISyntaxException {
        StringBuilder query = new StringBuilder("limit=1000");
        for(String parameter:parameters) {
            query.append("&&").append(parameter);
        }
        List<FullUpdateValue> updates = new ArrayList<>();
        long stop_time = stop_point.getTime();

        while(true) {
            URL u = new URI("https", "api.counting.gg", "/api/thread/loadNewerCounts", query.toString(), null).toURL();
            FullUpdateValue[] messages;
            boolean isNewest;
            List<CounterData> counters;
            try {
                JsonNode root = mapper.readTree(Util.apiGet(u));
                messages = mapper.convertValue(root.get("counts"), new TypeReference<>() {});
                isNewest = mapper.convertValue(root.get("loadedNewest"), new TypeReference<>() {});
                counters = mapper.convertValue(root.get("counters"), new TypeReference<>() {});
            } catch(JsonMappingException e) {
                throw new RuntimeException(e);
            }
            for(CounterData counter: counters) {
                if(!Counters.reverse_lookup.containsKey(counter.id())) {
                    Counters.addCounter(counter);
                }
            }
            if(messages.length == 0) {
                Collections.sort(updates);
                combined_thread.updateThread(updates, CountDataType.FULL);
                return;
            }
            for(FullUpdateValue message:messages) {
                if(message.uuid().getTime() > stop_time) {
                    System.out.println("Received "+updates.size()+" messages");
                    Collections.sort(updates);
                    combined_thread.updateThread(updates, CountDataType.FULL);
                    return;
                } else if(message.uuid().getTime() > stop_time - 1000000000) {
                    System.out.println("Skipped the following update: "+message);
                    continue;
                }
                updates.add(message);
            }
            System.out.println("Received "+updates.size()+" messages");
            if(isNewest) {
                Collections.sort(updates);
                combined_thread.updateThread(updates, CountDataType.FULL);
                return;
            }
            if(updates.size() >= FULL_DB_MSGS_PER_FILE) {
                Collections.sort(updates);
                long stop_timestamp = updates.get(updates.size()-1).getTimestamp();
                int stop = 1;
                while(messages[messages.length-stop].getTimestamp() >= stop_timestamp-1000000000) {
                    stop++;
                }
                combined_thread.updateThread(updates.subList(0,updates.size()-stop), CountDataType.FULL);
                updates = new ArrayList<>(updates.subList(updates.size()-stop,updates.size()));
            }
            query = new StringBuilder("uuid=").append(messages[messages.length-1].getUUID());
            query.append("&&limit=1000&&commentsOnly=false&&validCountsOnly=false");
        }
    }
    public static void syncFullDB() {
        AllThreads.updateThreadList();
        AllUpdateTypeIterator<FullUpdateValue> updates = new AllUpdateTypeIterator<>(all_threads_with_combined.get(0), full_db_last_update_synced, full_db_total_updates, CountDataType.FULL);
        updates.init();
        List<List<UpdateValue>> updates_by_thread = new ArrayList<>();
        List<List<UpdateBody>> update_body_by_thread = new ArrayList<>();
        List<List<CountValue>> counts_by_thread = new ArrayList<>();
        List<List<UpdateBody>> count_body_by_thread = new ArrayList<>();
        List<Integer> current_update = new ArrayList<>();
        List<Integer> true_count = new ArrayList<>();
        for (CountingThread countingThread : AllThreads.all_threads_with_combined) {
            current_update.add(countingThread.getTotalUpdates());
            int total_counts = countingThread.getTotalCounts();
            if (total_counts == 0) true_count.add(0);
            else {
                true_count.add(((CountValue) countingThread.getUpdateOrCount(total_counts - 1, CountDataType.COUNT)).true_number());
            }
        }
        while(updates.hasNext()) {
            FullUpdateValue full_update = updates.next();
            int modified_author_id = Counters.reverse_lookup.get(full_update.authorId());
            while(updates_by_thread.size() < full_update.threadId() +1) updates_by_thread.add(new ArrayList<>());
            while(update_body_by_thread.size() < full_update.threadId() +1) update_body_by_thread.add(new ArrayList<>());
            while(counts_by_thread.size() < full_update.threadId() +1) counts_by_thread.add(new ArrayList<>());
            while(count_body_by_thread.size() < full_update.threadId() +1) count_body_by_thread.add(new ArrayList<>());
            while(current_update.size() < full_update.threadId() +1) current_update.add(0);
            while(true_count.size() < full_update.threadId() +1) true_count.add(0);
            UpdateValue update = new UpdateValue(full_update.uuid(), full_update.isValidCount(), full_update.stricken(), full_update.hasComment(), full_update.validCountNumber(), modified_author_id, full_update.threadId(), current_update.get(full_update.threadId()));
            UpdateBody comment = new UpdateBody(full_update.rawCount(), full_update.comment());
            CountValue count = null;
            if(update.isCount()) {
                true_count.set(update.thread_id(), true_count.get(update.thread_id())+1);
                count = new CountValue(full_update.uuid().getTime(), full_update.validCountNumber(), true_count.get(update.thread_id()), modified_author_id, full_update.threadId(), current_update.get(update.thread_id()));
            } else if(update.stricken() && AllThreads.all_threads_with_combined.get(update.thread_id()).thread.resetOnMistakes) {
                true_count.set(update.thread_id(), 0);
            }
            current_update.set(update.thread_id(), current_update.get(update.thread_id())+1);
            updates_by_thread.get(0).add(update);
            updates_by_thread.get(full_update.threadId()).add(update);
            update_body_by_thread.get(0).add(comment);
            update_body_by_thread.get(full_update.threadId()).add(comment);
            if(count!=null) {
                counts_by_thread.get(0).add(count);
                counts_by_thread.get(full_update.threadId()).add(count);
                count_body_by_thread.get(0).add(comment);
                count_body_by_thread.get(full_update.threadId()).add(comment);
            }
            if(updates_by_thread.get(0).size() < MSGS_PER_FILE) continue;
            for(int i=0;i<updates_by_thread.size();i++) {
                if(updates_by_thread.get(i).size() < MSGS_PER_FILE/2) continue;
                if(all_threads_with_combined.size() < i) {
                    updateThreadList();
                }
                CountingThread thread = all_threads_with_combined.get(i);
                if(thread==null) continue;
                thread.updateThread(updates_by_thread.get(i), CountDataType.UPDATE);
                thread.updateThread(update_body_by_thread.get(i), CountDataType.UPDATE_BODY);
                thread.updateThread(counts_by_thread.get(i), CountDataType.COUNT);
                thread.updateThread(count_body_by_thread.get(i), CountDataType.COUNT_BODY);
                updates_by_thread.set(i ,new ArrayList<>());
                counts_by_thread.set(i ,new ArrayList<>());
                update_body_by_thread.set(i, new ArrayList<>());
                count_body_by_thread.set(i, new ArrayList<>());
            }
        }
        for(int i=0;i<updates_by_thread.size();i++) {
            if(all_threads_with_combined.size() < i) {
                updateThreadList();
            }
            CountingThread thread = all_threads_with_combined.get(i);
            if(thread==null) continue;
            thread.updateThread(updates_by_thread.get(i), CountDataType.UPDATE);
            thread.updateThread(update_body_by_thread.get(i), CountDataType.UPDATE_BODY);
            thread.updateThread(counts_by_thread.get(i), CountDataType.COUNT);
            thread.updateThread(count_body_by_thread.get(i), CountDataType.COUNT_BODY);
        }
        full_db_last_update_synced = full_db_total_updates;
        updateFullDBFileData();
    }
    public static void updateThreadList() {
        try {
            all_threads = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            URL u = new URL("https://api.counting.gg/api/thread/all");
            JsonNode root = mapper.readTree(Util.apiGet(u));
            List<ThreadData> thread_list = mapper.convertValue(root, new TypeReference<>() {
            });
            StatLoader.saveToBytes(thread_list, "SidethreadData/Threads");
            for (ThreadData thread : thread_list) {
                CountingThread t = createCountingThread(thread);
                all_threads.add(t);
                switch(thread.category) {
                    case "Traditional" -> traditional.add(t);
                    case "Double Counting" -> double_counting.add(t);
                    case "No Mistakes" -> no_mistakes.add(t);
                    case "Miscellaneous" -> miscellaneous.add(t);
                    default -> System.out.println("Thread Category Not Found: "+thread.category);
                }
            }
            all_threads_with_combined = new ArrayList<>();
            all_threads_with_combined.add(combined_thread);
            all_threads_with_combined.addAll(all_threads);
        } catch (JsonProcessingException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Bot.UpdateParser.generateThreadAliases();
    }
    public static void deleteAllStatFiles() {
        for(CountingThread thread: all_threads_with_combined) {
            thread.deleteStatFiles();
        }
    }
    private static CountingThread createCountingThread(ThreadData t) {
        if(t.name.equals("test")) return new Test(t); //These two threads have validation type "main", but must be handled separately
        if(t.name.equals("test2")) return new Test2(t);
        if(t.validationType==null) return new CountingThread(t);
        return switch(t.validationType) {
            case "binary" -> new BaseN(t, 2, 0, 1, "01", true);
            case "ternary" -> new BaseN(t, 3, 0, 1, "012", true);
            case "quaternary" -> new BaseN(t, 4, 0, 1, "0123", true);
            case "quinary" -> new BaseN(t, 5, 0, 1, "01234", true);
            case "senary" -> new BaseN(t, 6, 0, 1, "012345", true);
            case "septenary" -> new BaseN(t, 7, 0, 1, "0123456", true);
            case "octonary" -> new BaseN(t, 8, 0, 1, "01234567", true);
            case "nonary" -> new BaseN(t, 9, 0, 1, "012345678", true);
            case "main", "slow", "bars", "randomminute", "yoco", "roulette", "tslc", "fast_tslc", "waitx" -> new BaseN(t, 10, 0, 1, "0123456789", true);
            case "hexadecimal" -> new BaseN(t, 16, 0, 1, "0123456789ABCDEF", true);
            case "letters" -> new BaseN(t, 26, 0, 1, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", false);
            case "alphanumerics" -> new BaseN(t, 36, 0, 1, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ", true);
            case "base62" -> new BaseN(t, 62, 0, 1, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz", true);
            case "base64" -> new BaseN(t, 64, 0, 1, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", true);
            case "parity" -> new BaseN(t, 10, -1*t.splitOffset, 2, "0123456789", true); //Technically works, but should probably be based off of parsing first count
            case "by3" -> new BaseN(t, 10, 0, 3, "0123456789", true);
            case "countdown" -> new BaseN(t, 10, 1000001, -1, "0123456789", true);
            case "1inx" -> new OneInX(t, 10, 0, 1, "0123456789", true);
            case "randomhour" -> new RandomHour(t, 10, 0, 1, "0123456789", true);
            case "wait_2" -> new Wait2(t, 10, 0, 1, "0123456789", true);
            case "factoradic" -> new Factoradic(t);
            case "wave" -> new Wave(t);
            case "rationals" -> new Rationals(t);
            case "2048" -> new _2048(t);
            case "roman" -> new RomanNumerals(t);
            case "mayan" -> new Mayan(t);
            case "words" -> new Words(t);
            case "ordinals" -> new Ordinals(t);
            case "no_repdigits" -> new NoRepeatingDigits(t);
            case "permutations" -> new Permutations(t);
            case "repeating_digits" -> new RepeatingDigits(t);
            case "mostly_repeating_digits" -> new MostlyRepeatingDigits(t);
            case "dates" -> new Dates(t);
            case "time" -> new Time(t);
            case "military" -> new MilitaryTime(t);
            case "incremental_odds" -> new IncrementalOdds(t);
            //Unsupported validation types: "tugofwar", "" (no counting), "username", "coordinates"
            default ->  new CountingThread(t);
        };
    }

    public static ThreadData lookupFromName(String name) {
        for(CountingThread thread: all_threads) {
            if(thread.thread.name.equals(name)) {
                return thread.thread;
            }
        }
        return null;
    }

}
