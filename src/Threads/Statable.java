package Threads;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Statable extends CountingThread {
    protected final String file_dir;
    protected final StatLoader loader;
    public Statable(String name, List<String> live_threads) {
        super(name, live_threads, "UTC");
        this.file_dir = "SidethreadData\\" + live_threads.get(live_threads.size()-1);
        this.loader = new StatLoader(this, file_dir);
    }
    public Statable(String name, List<String> live_threads, String timezone) {
        super(name, live_threads, timezone);
        this.file_dir = "SidethreadData\\" + live_threads.get(live_threads.size()-1);
        this.loader = new StatLoader(this, file_dir);
    }
    public List<CountValue> getSimpleGetsWithOffset(int diff, int offset) {
        List<Integer> gets = this.constructSimpleGetListWithOffset(diff, offset);
        return this.getCountsOffsetFromList(gets, 0);
    }
    public List<Integer> constructSimpleGetListWithOffset(int diff, int offset) {
        if(this.getLast_number() < diff-offset || diff-offset <= 0) return new ArrayList<>();
        List<Integer> gets = new ArrayList<>();
        for(int i=diff-offset;i<=this.getLast_number();i+=diff) {
            gets.add(i);
        }
        return gets;
    }
    /**
     * Note: split_diff must divide get_diff, and the offset needs to be the same as used to construct the get list.
     * */
    public List<List<Integer>> constructSimpleSplitsListWithInitialOffset(int split_diff, int get_diff, int offset) {
        if(this.getLast_number() < split_diff-offset || split_diff-offset <= 0 || get_diff%split_diff!=0) return new ArrayList<>();
        List<List<Integer>> gets = new ArrayList<>();
        for(int i=split_diff-offset;i<=this.getLast_number();i+=get_diff) {
            List<Integer> splits = new ArrayList<>();
            for(int j=i; j<Math.min(i+get_diff, this.getLast_number()+1);j+=split_diff) {
                splits.add(j);
            }
            gets.add(splits);
        }
        return gets;
    }
    public List<CountValue> getCountsOffsetFromList(List<Integer> counts, int offset) {
        Collections.sort(counts);
        if(counts.size() == 0 || this.getLast_number() < counts.get(counts.size()-1)-offset || counts.get(0)-offset <= 0) return new ArrayList<>();
        counts = counts.stream().map(count -> count-offset).collect(Collectors.toList());
        return this.getCountsFromList(counts);
    }
    public int getCurrentK(int gets_per_k) {
        int current_k = this.getGetValues().size();
        return 1+current_k/gets_per_k;
    }

    public List<StatLoader.K_HOC> generateAllKHocs(int start_k, int end_k, int gets_per_k) {
        if(gets_per_k<=0) throw new IllegalArgumentException();
        int current_k = this.getCurrentK(gets_per_k);
        if(start_k > end_k || start_k > current_k) return new ArrayList<>();
        if(start_k<0) start_k = 0;
        if(end_k<0 || end_k > current_k) end_k = current_k;
        String filename = "all_"+gets_per_k+"k_hocs";
        List<StatLoader.K_HOC> data = loader.loadStatFromBytes(filename);
        if(data != null) return data.subList(start_k, end_k);
        List<Integer> get_locs = this.getGetValues();
        List<Integer> true_get_locs = new ArrayList<>(get_locs.size()+1);
        for(int i=gets_per_k-1;i<get_locs.size();i+=gets_per_k) {
            true_get_locs.add(get_locs.get(i));
        }
        true_get_locs.add(this.getLast_number());
        int current_get = 0;
        List<StatLoader.K_HOC> all_k_hocs = new ArrayList<>();
        CountTimeIterator counts = new CountTimeIterator(this, 1, this.getLast_number()).init();
        long last_get_time = 0;
        while(current_get < current_k) {
            Map<String, Integer> hoc = new HashMap<>();
            long get_time = -1;
            while(counts.hasNext()) {
                CountValue count = counts.next();
                String author = count.getAuthor();
                int current = hoc.getOrDefault(author, 0);
                hoc.put(author, current+1);
                if(last_get_time==0) last_get_time = count.getUUID().getTime(); //So that the first k has a valid time
                if(count.getValidCountNumber() == true_get_locs.get(current_get)) {
                    long timestamp = count.getUUID().getTime();
                    get_time = timestamp - last_get_time;
                    last_get_time = timestamp;

                    break;
                }
            }
            current_get+=1;
            all_k_hocs.add(new StatLoader.K_HOC(current_get, true_get_locs.get(current_get-1), get_time, hoc));
        }
        loader.saveStatToBytes(all_k_hocs, filename);
        return new ArrayList<>(all_k_hocs.subList(start_k, end_k));
    }
    public StatLoader.K_HOC KHoc(int k, int gets_per_k) {
        List<StatLoader.K_HOC> k_hoc = generateAllKHocs(k, k+1, gets_per_k);
        if(k_hoc.size()==0) return null;
        return k_hoc.get(0);
    }
    protected boolean isPalindrome(CountValue num) {
        String number = num.getRawCount();
        int i = 0;
        int j = number.length() - 1;
        while (i < j) {
            if (number.charAt(i) != number.charAt(j))
                return false;
            i++;
            j--;
        }
        return true;
    }
    protected boolean isRepDigit(CountValue number) {
        String num = number.getRawCount();
        for(int i=num.length()/2;i>=1;i--) {
            boolean finished = true;
            if(num.length()%i!=0) continue;
            for(int j=i;j<num.length();j+=i) {
                if (!num.substring(0, i).equals(num.substring(j, j+i))) {
                    finished = false;
                    break;
                }
            }
            if(finished) return true;
        }
        return false;
    }
    protected boolean isBaseN(CountValue number, int base) {
        String num = number.getRawCount();
        if(base<0 || base > 64) return false;
        if(base<=36) num = num.toUpperCase();
        //ASCII 48-57, 65-90, 97-122, 43, 47
        for(char x:num.toCharArray()) {
            if(48 <= x && x <= 57) x-=48;
            else if(65 <= x && x <= 90) x-=55;
            else if(97 <= x && x <= 122) x-=61;
            else if(x==43) x=62;
            else if(x==47) x=63;
            else return false;
            if(x>=base) return false;
        }
        return true;
    }
    protected boolean isNRep(CountValue number, int n) {
        String num = number.getRawCount();
        if(num.length()<n) return false;
        char rep_number = num.charAt(num.length()-1);
        for(int i = num.length()-2;i>=num.length()-n;i--) {
            if(num.charAt(i)!=rep_number) return false;
        }
        return true;
    }
    public List<StatLoader.SplitsOutput> getSplits(int start_k, int end_k) {
        List<List<Integer>> split_locs = this.getSplitValues();
        int current_k = split_locs.size();
        if(start_k > end_k || start_k > current_k) return new ArrayList<>();
        if(start_k<0) start_k = 0;
        if(end_k<0 || end_k > current_k) end_k = current_k;
        String filename = "all_splits";
        List<StatLoader.SplitsOutput> data = loader.loadStatFromBytes(filename);
        if(data != null) return data.subList(start_k, end_k);
        List<StatLoader.SplitsOutput> all_splits = new ArrayList<>();
        CountTimeIterator counts = new CountTimeIterator(this, 1, this.getLast_number()).init();
        long last_split_time = 0;
        int last_get_value = 0;
        for(int current_get=0;current_get<current_k;current_get++) {
            List<TimeOutput> split_times = new ArrayList<>();
            List<Integer> end_counts = new ArrayList<>();
            end_counts.add(last_get_value);
            List<Map<String, Integer>> hocs = new ArrayList<>();
            for(int split_loc:split_locs.get(current_get)) {
                Map<String, Integer> hoc = new HashMap<>();
                while(counts.hasNext()) {
                    CountValue count = counts.next();
                    String author = count.getAuthor();
                    if (author == null) author = "[deleted]";
                    int current = hoc.getOrDefault(author, 0);
                    hoc.put(author, current + 1);
                    if (last_split_time == 0) { //So that the first split is valid
                        last_split_time = count.getUUID().getTime();
                    }
                    if(count.getValidCountNumber() == split_loc) {
                        long timestamp = count.getUUID().getTime();
                        split_times.add(new TimeOutput(timestamp - last_split_time));
                        last_split_time = timestamp;
                        end_counts.add(count.getValidCountNumber());
                        hocs.add(hoc);
                        last_get_value = count.getValidCountNumber();
                        break;
                    }
                }
            }
            all_splits.add(new StatLoader.SplitsOutput(current_get+1, split_times, end_counts, hocs));
        }
        loader.saveStatToBytes(all_splits, filename);
        return new ArrayList<>(all_splits.subList(start_k, end_k));
    }
    public Map<String, StatLoader.SumOfBestOutput> sumOfBest(int start_k, int end_k, float required_percentage) {
        if(required_percentage<0||required_percentage>1) throw new IllegalArgumentException();
        String filename = "sum_of_best_"+required_percentage+"_"+start_k+"_"+end_k;
        Map<String, StatLoader.SumOfBestOutput> data = loader.loadStatFromBytes(filename);
        if(data != null) return data;
        List<StatLoader.SplitsOutput> splits = this.getSplits(start_k, end_k);
        Map<String, long[]> current_best = new HashMap<>();
        for(StatLoader.SplitsOutput k:splits) {
            for(int i=0;i<k.splits().size();i++) {
                Map<String, Integer> hoc = k.hocs().get(i);
                int total_counts = k.end_counts().get(i+1)-k.end_counts().get(i);
                long time = k.splits().get(i).time;
                for(String user:hoc.keySet()) {
                    if(((float)hoc.get(user))/total_counts < required_percentage) continue;
                    long[] best_times = current_best.computeIfAbsent(user, key -> new long[k.splits().size()]);
                    if ((best_times[i] == 0 || best_times[i] > time) && time > 0) {
                        best_times[i] = time;
                    }
                }
            }
        }
        Map<String, StatLoader.SumOfBestOutput> sob = new HashMap<>();
        for(String user:current_best.keySet()) {
            long sum = 0;
            List<TimeOutput> best = new ArrayList<>();
            long[] best_splits = current_best.get(user);
            boolean broken = false;
            for(long split:best_splits) {
                if(split==0) {
                    broken = true;
                    break;
                }
                sum+=split;
                best.add(new TimeOutput(split));
            }
            if(broken) continue;
            sob.put(user, new StatLoader.SumOfBestOutput(best, new TimeOutput(sum)));
        }
        loader.saveStatToBytes(sob,filename);
        return sob;
    }
    public Map<String, List<StatLoader.NonUniqueFastestSplitsOutput>> fastestSplitsByUserWithHistory(int start_k, int end_k, float required_percentage) {
        if(required_percentage<0||required_percentage>1) throw new IllegalArgumentException();
        List<StatLoader.SplitsOutput> splits = this.getSplits(start_k, end_k);
        Map<String, List<StatLoader.NonUniqueFastestSplitsOutput>> fastestSplits = new HashMap<>();
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
                    List<StatLoader.NonUniqueFastestSplitsOutput> history = fastestSplits.computeIfAbsent(user, key -> new ArrayList<>());
                    if(history.size()==0 || time.time < history.get(history.size()-1).time().time) {
                        List<String> other_users = new ArrayList<>(qualified_users);
                        other_users.remove(user);
                        history.add(new StatLoader.NonUniqueFastestSplitsOutput(time, Math.max(k.end_counts().get(i),1), k.end_counts().get(i+1), other_users));
                    }
                }
            }
        }
        return fastestSplits;
    }
    public List<StatLoader.NonUniqueFastestSplitsOutput> fastestSplitsHistory(int start_k, int end_k, float required_percentage) {
        if(required_percentage<0||required_percentage>1) throw new IllegalArgumentException();
        List<StatLoader.SplitsOutput> splits = this.getSplits(start_k, end_k);
        List<StatLoader.NonUniqueFastestSplitsOutput> history = new ArrayList<>();
        for(StatLoader.SplitsOutput k:splits) {
            for(int i=0;i<k.splits().size();i++) {
                Map<String, Integer> hoc = k.hocs().get(i);
                int total_counts = k.end_counts().get(i + 1) - k.end_counts().get(i);
                TimeOutput time = k.splits().get(i);
                List<String> qualified_users = new ArrayList<>();
                for (String user : hoc.keySet()) {
                    if (((float) hoc.get(user)) / total_counts < required_percentage) continue;
                    qualified_users.add(user);
                }
                if (history.size() == 0 || time.time < history.get(history.size() - 1).time().time) {
                    history.add(new StatLoader.NonUniqueFastestSplitsOutput(time, Math.max(k.end_counts().get(i),1), k.end_counts().get(i + 1), qualified_users));
                }
            }
        }
        return history;
    }
    public int doubleCountInterval() {
        return 2;
    }
    public abstract List<Integer> getGetValues();
    public abstract List<List<Integer>> getSplitValues();
}
