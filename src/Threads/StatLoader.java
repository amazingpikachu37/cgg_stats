package Threads;

import org.apache.fury.Fury;
import org.apache.fury.config.Language;
import org.apache.fury.logging.LoggerFactory;
import org.apache.fury.memory.MemoryBuffer;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class StatLoader {
    private final String file_dir;
    private final Statable thread;
    private final static Fury fury;
    static {
        LoggerFactory.disableLogging();
        fury = Fury.builder()
                .withLanguage(Language.JAVA)
                .requireClassRegistration(true)
                .build();
        fury.register(PeakCountsPerDayOutput.class);
        fury.register(Medal.class);
        fury.register(TimeStreakOutput.class);
        fury.register(NonUniqueTimeStreak.class);
        fury.register(CountValue.class);
        fury.register(TimeOutput.class);
        fury.register(NonUniqueTopCountsOutput.class);
        fury.register(UniqueTopCountsOutput.class);
        fury.register(NonUniqueDailyPercentOfCountsOutput.class);
        fury.register(NonUniquePhotoFinish.class);
        fury.register(PerfectStreak.class);
        fury.register(OverallPerfectStreak.class);
        fury.register(NonUniquePerfectStreak.class);
        fury.register(SpeedTag.class);
        fury.register(OverallSpeedTag.class);
        fury.register(SplitsOutput.class);
        fury.register(SumOfBestOutput.class);
        fury.register(TimeUnit.class);
        fury.register(Participation.class);
        fury.register(ParticipationDuration.class);
        fury.register(DayHOC.class);
        fury.register(K_HOC.class);
        fury.register(ExactaOutput.class);
        fury.register(CountValue.class);
        fury.register(UpdateValue.class);
        fury.register(UUID.class);
        fury.register(StatOutput.class);
        fury.register(GenericStatOutput.class);
        fury.register(CollectionContainer.class);
        fury.register(MapContainer.class);
        fury.register(ListContainer.class);
    }
    public StatLoader(Statable thread, String file_dir) {
        this.file_dir = file_dir;
        this.thread = thread;
    }
    public <T> void saveStatToBytes(T stat_data, String filename) {
        File f = new File(file_dir +"\\stats_data\\"+filename);
        try(FileOutputStream stream = new FileOutputStream(f)) {
            ByteBuffer data = ByteBuffer.allocate(12);
            data.putInt(thread.getTotal_updates());
            data.putInt(thread.getTotal_counts());
            data.putInt(thread.getLast_number());
            stream.write(data.array());
            stream.write(fury.serialize(stat_data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("unchecked")
    public <T> T loadStatFromBytes(String filename) {
        File f = new File(file_dir +"\\stats_data\\"+filename);
        try {
            byte[] byte_data = Files.readAllBytes(f.toPath());
            ByteBuffer wrapper = ByteBuffer.wrap(byte_data);
            int t_updates = wrapper.getInt();
            int t_counts = wrapper.getInt();
            int l_count = wrapper.getInt();
            if(!(t_updates==thread.getTotal_updates()&&t_counts==thread.getTotal_counts()&&l_count==thread.getLast_number())) {
                return null;
            }
            return (T) fury.deserialize(MemoryBuffer.fromByteBuffer(wrapper));
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
    public record PeakCountsPerDayOutput(float value, String date, int day_parts, int total_counts) implements Comparable<PeakCountsPerDayOutput> {
        @Override
        public int compareTo(PeakCountsPerDayOutput o) {
            if(this.value-o.value > 0) return 1;
            if(this.value-o.value < 0) return -1;
            return o.date.compareTo(this.date);
        }
        public String toString() {
            return value+"|"+date+"|"+day_parts+"|"+total_counts;
        }
    }
    public record Medal(int gold, int silver, int bronze) implements Comparable<Medal> {
        @Override
        public String toString() {
            return String.format("%d|%d|%d|%d", gold, silver, bronze, gold+silver+bronze);
        }

        @Override
        public int compareTo(Medal o) {
            int compare = this.gold-o.gold;
            if(compare != 0) return compare;
            compare = this.silver-o.silver;
            if(compare != 0) return compare;
            return this.bronze - o.bronze;
        }
        public Medal add(Medal o) {
            return new Medal(this.gold+o.gold, this.silver+o.silver, this.bronze+o.bronze);
        }
    }
    public record DayHOC(TimeUnit date, Map<String, Integer> hoc) {
        public String toString() {
            return date+"="+hoc.toString();
        }
    }
    public record ExactaDayHOC(TimeUnit date, Map<String, ExactaOutput> hoc) {
        public String toString() {
            return hoc.toString();
        }
    }
    public record K_HOC(int k, int end_count, long time, Map<String, Integer> hoc) {
        public String toString() {
            return "{"+k+","+end_count+","+time+" "+hoc.toString()+"}";
        }
    }
    public record NonUniqueTimeStreak(String user, String start, String end, int size) implements NonUnique {
        public String toString() {
            return user+"|"+size+"|"+start+"|"+end;
        }

        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof NonUniqueTimeStreak p)) throw new IllegalArgumentException();
            int compare = p.size-this.size;
            if(compare!=0) return compare;
            compare =  this.start.compareTo(p.start);
            if(compare != 0) return compare;
            return p.user.compareTo(this.user);
        }
    }
    public record NonUniqueCountStreakWithThread(String user, int start, int end, int size, String thread) implements NonUnique {
        public String toString() {
            return user+"|"+size+"|"+start+"|"+end+"|"+thread;
        }

        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof NonUniqueCountStreakWithThread p)) throw new IllegalArgumentException();
            int compare = p.size-this.size;
            if(compare!=0) return compare;
            compare = this.start-p.start;
            if(compare != 0) return compare;
            return p.user.compareTo(this.user);
        }
    }
    public record Participation(int hoc, int gets, int assists, int k_parts, int day_parts) implements Comparable<Participation> {
        public int getScore() {
            return 2 * this.hoc + 500 * (this.gets + this.assists) + 250 * (this.k_parts + this.day_parts);
        }
        public int getCombinedGetsAssists() {
            return gets + assists;
        }
        @Override
        public int compareTo(Participation o) {
            int compare = this.getScore() - o.getScore();
            if (compare != 0) return compare;
            compare = this.hoc - o.hoc;
            if (compare != 0) return compare;
            compare = this.gets - o.gets;
            if (compare != 0) return compare;
            compare = this.assists - o.assists;
            if (compare != 0) return compare;
            compare = this.k_parts - o.k_parts;
            if (compare != 0) return compare;
            compare = this.day_parts - o.day_parts;
            return compare;
        }
        @Override
        public String toString() {
            return String.format("%d|%d|%d|%d|%d|%d", this.getScore(), this.hoc, this.gets, this.assists, this.k_parts, this.day_parts);
        }
        public Participation addParticipation(Participation p1) {
            if (p1 == null) return null;
            return new Participation(p1.hoc() + this.hoc(), p1.gets() + this.gets(), p1.assists() + this.assists(), p1.k_parts() + this.k_parts(), p1.day_parts() + this.day_parts());
        }
    }
    public record StringIntPair(String user, int value) implements Comparable<StringIntPair> {
        @Override
        public int compareTo(StringIntPair o) {
            int compare = this.value-o.value;
            if(compare != 0) return compare;
            return this.user.compareTo(o.user);
        }
        public String toString() {
            return user+"|"+value;
        }
    }
    public record NonUniqueTopCountsOutput(String user, Integer value, String date) implements NonUnique {
        public String toString() {
            return user+"|"+value+"|"+date;
        }

        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof NonUniqueTopCountsOutput p)) throw new IllegalArgumentException();
            int compare = p.value-this.value;
            if(compare != 0) return compare;
            compare =  this.date.compareTo(p.date);
            if(compare != 0) return compare;
            return this.user.compareTo(p.user);
        }
    }
    public record UniqueTopCountsOutput(int value, String date) implements Comparable<UniqueTopCountsOutput> {
        @Override
        public int compareTo(UniqueTopCountsOutput o) {
            int compare = this.value-o.value;
            if(compare != 0) return compare;
            return o.date.compareTo(this.date);
        }
        public String toString() {
            return value+"|"+date;
        }
    }

    public record FastestNCountsNoUserOutput(int start_count, int end_count, long time) implements Comparable<FastestNCountsNoUserOutput> {
        public String toString() {
            return (new TimeOutput(time))+"|"+start_count+"|"+end_count;
        }
        @Override
        public int compareTo(FastestNCountsNoUserOutput o) {
            int compare = Long.signum(o.time-this.time);
            if(compare != 0) return compare;
            return o.end_count-this.end_count;
        }
    }
    public record NonUniqueDailyPercentOfCountsOutput(String user, float percentage, int user_counts, int daily_counts, String date) implements NonUnique {
        public String toString() {
            return date+"|"+percentage*100+"%|"+user+"|"+user_counts+"|"+daily_counts;
        }

        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof NonUniqueDailyPercentOfCountsOutput p)) throw new IllegalArgumentException();
            if(p.percentage-this.percentage>0) return 1;
            if(p.percentage-this.percentage<0) return -1;
            int compare = p.user_counts-this.user_counts;
            if(compare != 0) return compare;
            compare = this.date.compareTo(p.date);
            if(compare != 0) return compare;
            return p.user.compareTo(this.user);
        }
    }
    public record NonUniquePhotoFinish(String user, String user2, int user_counts, int user2_counts, float percentage, String date) implements NonUnique {

        public String toString() {
            return user+"|"+user_counts+"|"+user2+"|"+user2_counts+"|"+percentage*100+"%|"+date;
        }

        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof NonUniquePhotoFinish p)) throw new IllegalArgumentException();
            if(p.percentage-this.percentage>0) return -1;
            if(p.percentage-this.percentage<0) return 1;
            int compare = this.user_counts-p.user_counts;
            if(compare != 0) return compare;
            compare = p.date.compareTo(this.date);
            if(compare != 0) return compare;
            return p.user.compareTo(this.user);
        }
    }
    public record HighestUserNotCountingWithOutput(String[] users, int user_counts) implements Comparable<HighestUserNotCountingWithOutput> {
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(String user:users) {
                sb.append(user);
                sb.append("|");
            }
            sb.deleteCharAt(sb.length()-1);
            return sb.toString();
        }

        @Override
        public int compareTo(HighestUserNotCountingWithOutput o) {
            return this.user_counts - o.user_counts;
        }
    }
    public record PerfectStreak(int streak, int start, int end) implements Comparable<PerfectStreak> {
        public String toString() {
            return streak + "|" + start + "|" + end;
        }
        @Override
        public int compareTo(PerfectStreak o) {
            int compare = this.streak - o.streak;
            if (compare != 0) return compare;
            return o.start - this.start;
        }
    }
    public record NonUniquePerfectStreak(String[] users, int streak, int start, int end) implements NonUnique {

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(String user:users) {
                sb.append(user).append("|");
            }
            return sb.append(streak).append("|").append(start).append("|").append(end).toString();
        }
        public String user() {
            return users[0];
        }

        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof NonUniquePerfectStreak p)) throw new IllegalArgumentException();
            int compare = p.streak-this.streak;
            if(compare != 0) return compare;
            compare = this.start-p.start;
            if(compare != 0) return compare;
            return p.user().compareTo(this.user());
        }
    }
    public record SpeedTag(int thread, String[] users, TimeOutput speed, int counts_users, int counts_in_k, boolean artbn) implements NonUnique {
        public String toString() {
            StringBuilder sb = new StringBuilder().append(thread).append("|");
            for(String user:users) {
                sb.append(user).append("|");
            }
            return sb.append(speed).append("|").append(counts_users).append("/").append(counts_in_k).append("|").append(artbn?"Yes":"No").toString();
        }
        public String user() {
            return users[0];
        }

        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof SpeedTag p)) throw new IllegalArgumentException();
            int compare = Long.signum(this.speed.time-p.speed.time);
            if(compare != 0) return compare;
            return this.thread-p.thread;
        }
    }
    public record OverallSpeedTag(int thread_num, String[] users, TimeOutput speed, int counts_users, int counts_in_k, boolean artbn, String thread_name) implements NonUnique {
        public String toString() {
            StringBuilder sb = new StringBuilder().append(thread_num).append("|");
            for(String user:users) {
                sb.append(user).append("|");
            }
            return sb.append(speed).append("|").append(thread_name).append("|").append(counts_users).append("/").append(counts_in_k).append("|").append(artbn?"Yes":"No").toString();
        }
        public String user() {
            return users[0];
        }

        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof OverallSpeedTag p)) throw new IllegalArgumentException();
            int compare = Long.signum(this.speed.time-p.speed.time);
            if(compare != 0) return compare;
            return this.thread_num-p.thread_num;
        }
    }
    public record SplitsOutput(int k, List<TimeOutput> splits, List<Integer> end_counts, List<Map<String, Integer>> hocs) {
    }
    public record SumOfBestOutput(List<TimeOutput> splits, TimeOutput sum) implements Comparable<SumOfBestOutput> {
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(TimeOutput split:splits) {
                sb.append(split).append("|");
            }
            return sb.append(sum).toString();
        }

        @Override
        public int compareTo(SumOfBestOutput o) {
            return Long.signum(o.sum.time-this.sum.time);
        }
    }
    public record NonUniqueFastestSplitsOutput(TimeOutput time, int start_num, int end_num, List<String> qualified_users) implements NonUnique {
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(time).append("|");
            sb.append(start_num).append("|");
            sb.append(end_num).append("|");
            for(int i=0;i<qualified_users.size();i++) {
                sb.append(qualified_users.get(i));
                if(i+1!=qualified_users.size()) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }

        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof NonUniqueFastestSplitsOutput p)) throw new IllegalArgumentException();
            return Long.signum(this.time.time-p.time.time);
        }

        @Override
        public String user() {
            return String.valueOf(start_num);
        }
    }
    public record OverallNonUniqueFastestSplitsOutput(TimeOutput time, int start_num, int end_num, List<String> qualified_users, String thread) implements NonUnique {
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(time).append("|");
            sb.append(start_num).append("|");
            sb.append(end_num).append("|");
            sb.append(thread).append("|");
            for(int i=0;i<qualified_users.size();i++) {
                sb.append(qualified_users.get(i));
                if(i+1!=qualified_users.size()) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }

        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof OverallNonUniqueFastestSplitsOutput p)) throw new IllegalArgumentException();
            return Long.signum(this.time.time-p.time.time);
        }

        @Override
        public String user() {
            return String.valueOf(start_num);
        }
    }
    public record OverallPairPB(int thread, TimeOutput speed, int counts_users, int counts_in_k, String thread_name) implements Comparable<OverallPairPB> {
        public String toString() {
            return speed + "|" + thread_name+"|"+thread + "|" +counts_users + "/" + counts_in_k;
        }
        @Override
        public int compareTo(OverallPairPB o) {
            int compare = Long.signum(this.speed.time-o.speed.time);
            if(compare != 0) return compare;
            return this.thread-o.thread;
        }
    }
    public record NonUniqueFastestSelfReplies(String user, TimeOutput time, int count, String thread) implements NonUnique {
        public String toString() {
            return user+"|"+count+"|"+time+"|"+thread;
        }
        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof NonUniqueFastestSelfReplies p)) throw new IllegalArgumentException();
            return Long.signum(this.time.time-p.time.time);
        }
    }
    public record OverallMinSplitStdDev(int thread, TimeOutput speed, float stdDev, List<String> qualified_users, String thread_name) implements NonUnique {

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(String user:qualified_users) {
                sb.append(user).append(",");
            }
            if(sb.length()>0)
                sb.deleteCharAt(sb.length()-1);
            return stdDev+"|"+thread+"|"+speed.toString()+"|"+thread_name+"|"+sb;
        }
        public String user() {
            return String.valueOf(thread);
        }

        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof OverallMinSplitStdDev p)) throw new IllegalArgumentException();
            if(this.stdDev < p.stdDev) return -1;
            else if(this.stdDev > p.stdDev) return 1;
            int compare = Long.signum(this.speed.time-p.speed.time);
            if(compare != 0) return compare;
            return this.thread-p.thread;
        }
    }
    public record ExactaOutput(int trifecta, int chalupa, int lein, int treje, int total) implements Comparable<ExactaOutput> {
        public String toString() {
            return trifecta+"|"+total+"|"+chalupa+"|"+lein+"|"+treje;
        }
        @Override
        public int compareTo(ExactaOutput o) {
            int compare = this.total-o.total;
            if(compare!=0) return compare;
            compare = this.chalupa-o.chalupa;
            if(compare!=0) return compare;
            return this.lein-o.lein;
        }
        public ExactaOutput sign() {
            return new StatLoader.ExactaOutput(Math.min(1, this.trifecta()),Math.min(1, this.chalupa()),Math.min(1, this.lein()),Math.min(1, this.treje()),Math.min(1, this.total()));
        }
        public ExactaOutput add(ExactaOutput o) {
            return new ExactaOutput(this.trifecta+o.trifecta, this.chalupa+o.chalupa, this.lein+o.lein, this.treje+o.treje, this.total+o.total);
        }
        public int[] asArray() {
            return new int[]{this.trifecta, this.chalupa, this.lein, this.treje, this.total};
        }
    }
    public record IndividualThreadRecords(List<StatLoader.UniqueTopCountsOutput> records) implements Comparable<IndividualThreadRecords> {
        @Override
        public int compareTo(@Nonnull IndividualThreadRecords o) {
            int max_this = 0;
            int max_o = 0;
            for(StatLoader.UniqueTopCountsOutput record:this.records) {
                if(record.value() > max_this) {
                    max_this = record.value();
                }
            }
            for(StatLoader.UniqueTopCountsOutput record:o.records) {
                if(record.value() > max_o) {
                    max_o = record.value();
                }
            }
            return max_this - max_o;
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(StatLoader.UniqueTopCountsOutput record:this.records) {
                sb.append(record.value()).append("|");
            }
            sb.deleteCharAt(sb.length()-1);
            return sb.toString();
        }
    }
    public record OverallRecordHour(int record, String thread) implements Comparable<OverallRecordHour> {
        @Override
        public int compareTo(OverallRecordHour o) {
            return this.record - o.record;
        }
        public String toString() {
            return record+"|"+thread;
        }
    }
    public record OverallThreadRecordHour(String hour, int record, String thread) implements NonUnique {
        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(o instanceof OverallThreadRecordHour p) {
                int compare = p.record - this.record;
                if(compare!=0) return compare;
                return this.hour.compareTo(p.hour);
            }
            throw new IllegalArgumentException();
        }
        public String toString() {
            return hour+"|"+record+"|"+thread;
        }

        @Override
        public String user() {
            return hour;
        }
    }
    public record OverallFastestNCountsOutput(int start_hoc, int end_hoc, int start_count, int end_count, long time, String thread) implements Comparable<OverallFastestNCountsOutput> {
        @Override
        public int compareTo(OverallFastestNCountsOutput o) {
            int compare = Long.signum(o.time-this.time);
            if(compare != 0) return compare;
            return o.end_count-this.end_count;
        }
        public String toString() {
            return (new TimeOutput(time))+"|"+thread+"|"+start_hoc+"|"+end_hoc+"|"+start_count+"|"+end_count;
        }
    }
    public record OverallMostDominantNCountsOutput(int start_hoc, int end_hoc, int start_count, int end_count, int user_counts, String thread) implements Comparable<OverallMostDominantNCountsOutput> {
        @Override
        public int compareTo(OverallMostDominantNCountsOutput o) {
            int compare = Long.signum(this.user_counts-o.user_counts);
            if(compare != 0) return compare;
            return o.end_count-this.end_count;
        }
        public String toString() {
            return user_counts+"|"+thread+"|"+start_hoc+"|"+end_hoc+"|"+start_count+"|"+end_count;
        }
    }
    public record OverallPerfectStreak(int streak, int start, int end, String thread) implements Comparable<OverallPerfectStreak> {
        @Override
        public int compareTo(OverallPerfectStreak o) {
            int compare = this.streak - o.streak;
            if (compare != 0) return compare;
            return o.start - this.start;
        }
        public String toString() {
            return streak+"|"+thread + "|" + start + "|" + end;
        }
    }
    public record OverallNonUniquePerfectStreak(String[] users, int streak, int start, int end, String thread) implements NonUnique {

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(String user:users) {
                sb.append(user).append("|");
            }
            return sb.append(streak).append("|").append(thread).append("|").append(start).append("|").append(end).toString();
        }
        public String user() {
            return users[0];
        }

        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof OverallNonUniquePerfectStreak p)) throw new IllegalArgumentException();
            int compare = p.streak-this.streak;
            if(compare != 0) return compare;
            compare = this.start-p.start;
            if(compare != 0) return compare;
            return p.user().compareTo(this.user());
        }
    }
    public record ThreadDayRecord(int counts, String date) implements Comparable<ThreadDayRecord> {
        @Override
        public int compareTo(ThreadDayRecord o) {
            int compare = this.counts-o.counts;
            if(compare!=0) return compare;
            return o.date.compareTo(this.date);
        }
        @Override
        public String toString() {
            return counts+"|"+date;
        }
    }
    public record SitewideFastestNCountsNoUserOutput(int start_count, String start_thread, int end_count, String end_thread, long time) implements Comparable<SitewideFastestNCountsNoUserOutput> {
        public String toString() {
            return (new TimeOutput(time))+"|"+start_count+"|"+start_thread+"|"+end_count+"|"+end_thread;
        }
        @Override
        public int compareTo(SitewideFastestNCountsNoUserOutput o) {
            int compare = Long.signum(o.time-this.time);
            if(compare != 0) return compare;
            return o.end_count-this.end_count;
        }
    }
    public record SitewideFastestNCountsOutput(int start_hoc, int end_hoc, int start_count, String start_thread, int end_count, String end_thread, long time) implements Comparable<SitewideFastestNCountsOutput> {
        public String toString() {
            return (new TimeOutput(time))+"|"+start_hoc+"|"+end_hoc+"|"+start_count+"|"+start_thread+"|"+end_count+"|"+end_thread;
        }
        @Override
        public int compareTo(SitewideFastestNCountsOutput o) {
            int compare = Long.signum(o.time-this.time);
            if(compare != 0) return compare;
            return o.end_count-this.end_count;
        }
    }
    public record CountStreakOutputWithThread(int start, int end, int size, String thread) implements Comparable<CountStreakOutputWithThread> {
        @Override
        public int compareTo(CountStreakOutputWithThread o) {
            int compare = this.size-o.size;
            if(compare!=0) return compare;
            return o.start-this.start;
        }
        public String toString() {
            return size+"|"+thread+"|"+start+"|"+end;
        }
    }
    public record NonUniqueCountStreakOutputWithThread(int start, int end, int size, String thread) implements NonUnique {
        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof NonUniqueCountStreakOutputWithThread p)) throw new IllegalArgumentException();
            int compare = p.size-this.size;
            if(compare!=0) return compare;
            return this.start-p.start;
        }
        public String toString() {
            return size+"|"+thread+"|"+start+"|"+end;
        }
        public String user() {
            return String.valueOf(size);
        }
    }

    public record OverallFastestBarsOutput(String user, TimeOutput time, String count, String thread) implements NonUnique {
        public String toString() {
            return user+"|"+time+"|"+count+"|"+thread;
        }
        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof OverallFastestBarsOutput p)) throw new IllegalArgumentException();
            int compare = Long.signum(this.time.time - p.time.time);
            if(compare != 0) return compare;
            return p.user.compareTo(this.user);
        }
    }
    public record FirstCountThread(CountValue count,Statable thread) implements Comparable<FirstCountThread> {
        @Override
        public String toString() {
            return thread.getName()+"|"+count.getRawText();
        }
        @Override
        public int compareTo(FirstCountThread o) {
            return Long.compare(this.count.getUUID().getTime(),o.count.getUUID().getTime());
        }
    }
    public record TimeStreakOutput(String start, String end, int size) implements Comparable<TimeStreakOutput> {

        @Override
        public int compareTo(TimeStreakOutput o) {
            int compare = this.size-o.size;
            if(compare!=0) return compare;
            return o.start.compareTo(this.start);
        }
        public String toString() {
            return size+"|"+start+"|"+end;
        }
    }
    public record TimeStreakOutputWithThread(String start, String end, int size, String thread) implements Comparable<TimeStreakOutputWithThread> {

        @Override
        public int compareTo(TimeStreakOutputWithThread o) {
            int compare = this.size-o.size;
            if(compare!=0) return compare;
            return o.start.compareTo(this.start);
        }
        public String toString() {
            return size+"|"+thread+"|"+start+"|"+end;
        }
    }
    public record NonUniqueTimeStreakWithThread(String user, String start, String end, int size, String thread) implements NonUnique {
        public String toString() {
            return user+"|"+size+"|"+thread+"|"+start+"|"+end;
        }

        @Override
        public int compareTo(@Nonnull NonUnique o) {
            if(!(o instanceof NonUniqueTimeStreakWithThread p)) throw new IllegalArgumentException();
            int compare = p.size-this.size;
            if(compare!=0) return compare;
            compare =  this.start.compareTo(p.start);
            if(compare != 0) return compare;
            return p.user.compareTo(this.user);
        }
    }
}
