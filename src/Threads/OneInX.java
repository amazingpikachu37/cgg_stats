package Threads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneInX extends Statable {
    public OneInX() {
        super("1inx", List.of("1inx"), "America/Montreal");
    }

    @Override
    public List<Integer> getGetValues() {
        return this.constructSimpleGetListWithOffset(1000,0);
    }

    public String getCount(int num) {
        if(num <= 0) throw new IllegalArgumentException();
        return Integer.toString(num);
    }
    public String getNextGet() {
        return "The next get is `"+ getCount((this.getLast_number()/1000+1)*1000)+"`";
    }
    public String[] link() {
        return new String[]{"https://counting.gg/thread/1inx"};
    }

    public List<Map<String, Integer>> badAttempts(UUID uuid1, UUID uuid2) {
        List<Map<String, Integer>> attempt_list = new ArrayList<>();
        attempt_list.add(new HashMap<>());
        int last_count = 0;
        String last_author = null;
        UpdateIterator updates = new UpdateIterator(this, uuid1, uuid2).init();
        while(updates.hasNext()) {
            UpdateValue update = updates.next();
            if(!update.isIsCount()) continue;
            int current_count;
            try {
                current_count = Integer.parseInt(update.getRawCount());
            } catch(NumberFormatException e) {
                continue;
            }
            if(current_count - last_count != 1) continue;
            String author = update.getAuthor();
            if(author.equals(last_author)) continue;
            int current = attempt_list.get(last_count).getOrDefault(author,0);
            attempt_list.get(last_count).put(author, current+1);
            if(update.isIsValidCount()) {
                last_count++;
                last_author = author;
                attempt_list.add(new HashMap<>());
            }
        }
        return attempt_list;
    }
    public List<StatLoader.NonUniqueCountStreakWithThread> record_streaks_without_valid_count(UUID uuid1, UUID uuid2) {
        List<Map<String, Integer>> bad_attempts = badAttempts(uuid1, uuid2);
        List<CountValue> good_attempts = this.getCountsNumSort(0,this.getLast_number());
        Map<String, int[]> current_streak = new HashMap<>();
        List<StatLoader.NonUniqueCountStreakWithThread> records = new ArrayList<>();
        for(int i=0;i<bad_attempts.size();i++) {
            for(String user:bad_attempts.get(i).keySet()) {
                int[] current = current_streak.computeIfAbsent(user, k->new int[2]);
                if(current[0]==0) {
                    current[1] = i;
                }
                current[0] += bad_attempts.get(i).get(user);
            }
            if(i==good_attempts.size()) break;
            String counter = good_attempts.get(i).getAuthor();
            int[] current = current_streak.get(counter);
            records.add(new StatLoader.NonUniqueCountStreakWithThread(counter,current[1],i+1,current[0],this.getName()));
            current[0] = 0;
        }
        for(String user: current_streak.keySet()) {
            int[] current = current_streak.get(user);
            if(current[0] > 0) {
                records.add(new StatLoader.NonUniqueCountStreakWithThread(user,current[1],-1,current[0],this.getName()));
            }
        }
        return records;
    }
    @Override
    public List<List<Integer>> getSplitValues() {
        return this.constructSimpleSplitsListWithInitialOffset(100, 1000, 0);
    }

}
