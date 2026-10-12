package Bot;

import Stats.*;
import Stats.UUID;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UpdateParser {
    private static boolean running = false;
    private static final Map<String, CountingThread> all_threads = AllThreads.all_threads.stream().collect(Collectors.toMap(c -> c.thread.name, c -> c));
    private static final Map<String, String> aliases = new HashMap<>();
    private static final String[] admins = new String[]{"amazingpikachu_37"};
    private static final LinkedList<String> recent_uuids = new LinkedList<>();
    private static final Logger botLogger = Util.setupLogger(Logger.getLogger("BOT"),  "SidethreadData/bot_history.log", "bot");
    private static final LinkedBlockingDeque<FuncWithArgs> queue = new LinkedBlockingDeque<>();
    private record FuncWithArgs(String func_name, String[] args, Flags flags, List<CountingThread> counting_threads, int count_duration_type) {}
    static {
        generateThreadAliases();
    }
    public static void generateThreadAliases() {
        File f = new File("SidethreadData/SidethreadAliases.txt");
        try(Scanner in = new Scanner(f)) {
            while(in.hasNextLine()) {
                String[] map = in.nextLine().split(" ");
                aliases.put(map[0],map[1]);
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        for(CountingThread thread:AllThreads.all_threads) {
            aliases.put(thread.thread.name.replace("_",""), thread.thread.name);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static String parse(String body, String user, String uuid) {
        String[] split = body.split("!\\{:}");
        if(split.length < 2) return null;
        body = split[1].split("\n")[0].trim();
        if(body.isEmpty()) return null;
        if(recent_uuids.contains(uuid)) return null;
        else if(recent_uuids.size() > 25) {
            recent_uuids.removeFirst();
        }
        recent_uuids.addLast(uuid);
        String[] args = body.split(" ");
        Set<CountingThread> counting_threads_set = new LinkedHashSet<>();
        int i;
        for(i=0;i<args.length;i++) {
            boolean to_remove = args[i].charAt(0) == '-';
            String arg = args[i].replaceAll("[-_]","");
            List<CountingThread> threads_to_add = switch(arg) {
                case "traditional" -> AllThreads.traditional;
                case "double", "doublecountingthread" -> AllThreads.double_counting;
                case "nomistakesthread" -> AllThreads.no_mistakes;
                case "other","miscellaneous" -> AllThreads.miscellaneous;
                case "allthreads" -> AllThreads.all_threads;
                default -> new ArrayList<>();
            };
            if(threads_to_add.isEmpty()) {
                String thread_name = aliases.get(arg);
                if(thread_name!=null) {
                    threads_to_add.add(all_threads.get(thread_name));
                }
            }
            if(!threads_to_add.isEmpty()) {
                if(to_remove) {
                    threads_to_add.forEach(counting_threads_set::remove);
                } else {
                    counting_threads_set.addAll(threads_to_add);
                }
            } else break;
        }
        args = Arrays.copyOfRange(args,i,args.length);
        List<CountingThread> counting_threads = counting_threads_set.stream().toList();
        botLogger.getHandlers()[0].setFormatter(new MyFormatter(user));
        botLogger.info(body);
        String func_name = args.length > 0 ? args[0].toLowerCase().replaceAll("_","") : "about";
        int count_duration_type = 0; // 0: uuid, 1: date, 2: k, 3: count, 4: update
        String start_date = null;
        String end_date = null;
        Flags flags = new Flags(user);
        try {
            for (int j = 1; j < args.length; j++) {
                if (args[j].isEmpty() || args[j].charAt(0) != '-') continue;
                switch (args[j].toLowerCase().replaceAll("_", "")) {
                    case "-u", "-username" -> {flags.username = args[j + 1];flags.username_modified=true;}
                    case "-p", "-position" -> flags.position = Math.max(1, Integer.parseInt(args[j + 1]));
                    case "-c", "-context" -> flags.context = Math.max(0, Integer.parseInt(args[j + 1]));
                    case "-su", "-startuuid" -> {
                        flags.start_uuid = new UUID(args[j + 1]);
                        count_duration_type = 0;
                    }
                    case "-eu", "-enduuid" -> {
                        flags.end_uuid = new UUID(args[j + 1]);
                        count_duration_type = 0;
                    }
                    case "-sd", "-startdate" -> {
                        start_date = args[j + 1];
                        count_duration_type = 1;
                    }
                    case "-ed", "-enddate" -> {
                        end_date = args[j + 1];
                        count_duration_type = 1;
                    }
                    case "-tp", "-timeperiod" ->
                        flags.time_period = switch(args[j + 1]) {
                            case "m", "month" -> ParticipationDuration.MONTH;
                            case "y", "year" -> ParticipationDuration.YEAR;
                            default -> ParticipationDuration.DAY;
                        };
                    case "-sk", "-startk" -> {
                        flags.start_k = Math.max(0, Integer.parseInt(args[j + 1]));
                        count_duration_type = 2;
                    }
                    case "-ek", "-endk" -> {
                        flags.end_k = Math.max(0, Integer.parseInt(args[j + 1]));
                        count_duration_type = 2;
                    }
                    case "-ks", "-ksize" -> flags.k_size = Math.max(1, Integer.parseInt(args[j + 1]));

                    case "-sc", "-startcount" -> {
                        flags.start_count = Math.max(1,Integer.parseInt(args[j + 1]))-1;
                        count_duration_type = 3;
                    }
                    case "-ec", "-endcount" -> {
                        flags.end_count = Math.max(1, Integer.parseInt(args[j + 1]));
                        count_duration_type = 3;
                    }
                    case "-sup", "-startupdate" -> {
                        flags.start_update = Math.max(1,Integer.parseInt(args[j + 1]))-1;
                        count_duration_type = 4;
                    }
                    case "-eup", "-endupdate" -> {
                        flags.end_update = Math.max(1,Integer.parseInt(args[j + 1]));
                        count_duration_type = 4;
                    }
                    case "-mc", "-mincounts" -> flags.min_counts = Math.max(1, Integer.parseInt(args[j + 1]));
                    case "-offset","-o" -> flags.offset = Integer.parseInt(args[j + 1]);
                    case "-%", "-percentage" -> flags.percentage = Float.parseFloat(args[j + 1]);
                    case "-mode" -> flags.mode = Integer.parseInt(args[j + 1]);
                    case "-d", "-duration" -> {
                        flags.duration_modified = true;
                        flags.duration = Math.max(1,Integer.parseInt(args[j + 1]));
                    }
                    case "-cbr","-countsbetweenreplies" -> flags.counts_between_replies = Math.max(1,Integer.parseInt(args[j + 1]));
                    case "-un", "-unique" -> flags.unique = true;
                    case "-up", "-updates" -> flags.use_counts = false;
                    case "-t", "-threads" -> flags.use_counters = false;
                    case "-com", "-combined" -> flags.thread_counts_combined = true;
                    case "-add", "-addthreadstats" -> flags.add_thread_stats = true;
                    case "-dup", "-allowduplicatecounters" -> flags.allow_duplicate_counters = true;
                    case "-s", "-splits" -> flags.use_ks = false;
                    case "-slowest", "-most", "-worst", "-antifecta", "-maximum", "-lastcounts" -> flags.first = false;
                    case "-current" -> flags.include_current = true;
                    case "-exclude" -> flags.exclude_100 = true;
                }
            }
        } catch(NumberFormatException | IndexOutOfBoundsException | DateTimeParseException e) {
            return "Illegal arguments to "+func_name;
        }
        if(flags.percentage > 1) flags.percentage = 1f;
        else if(flags.percentage < 0) flags.percentage = 0f;
        if(start_date!=null) flags.start_date = new Stats.TimeUnit(start_date, flags.time_period);
        if(end_date!=null) flags.end_date = new Stats.TimeUnit(end_date, flags.time_period);
        if(!flags.username_modified && !flags.use_counters) flags.username = "main";
        else if(!flags.use_counters) flags.username = aliases.getOrDefault(flags.username, flags.username);
        updateUUID(counting_threads, flags, count_duration_type);
        queue.add(new FuncWithArgs(func_name,args,flags,counting_threads, count_duration_type));
        return null;
    }

    private static void updateUUID(List<CountingThread> threads, Flags flags, int count_duration_type) {
        switch(count_duration_type) {
            //Date
            case 1 -> {
                flags.start_uuid = Util.getUUIDFromDate(flags.start_date, CountingThread.timezone);
                flags.end_uuid = Util.getUUIDFromDate(flags.end_date, CountingThread.timezone);
            }
            //K
            case 2 -> {
                if(threads.size()!=1) return;
                CountingThread thread = threads.get(0);
                if(thread.thread.resetOnMistakes) return;
                List<StatLoader.Split> ks = thread.splits(0, Integer.MAX_VALUE, flags.k_size, true);
                if(ks.size() > flags.start_k) flags.start_uuid = ks.get(flags.start_k).start_count().getUUID();
                if(ks.size() > flags.end_k) flags.end_uuid = ks.get(flags.end_k).start_count().getUUID();
            }
            //Count
            case 3 -> {
                long start = getNthCountTimestamp(threads, flags.start_count, true, flags.thread_counts_combined);
                long end = getNthCountTimestamp(threads, flags.end_count, true, flags.thread_counts_combined);
                if(start>=0) flags.start_uuid = new UUID(start, 0);
                if(end>=0) flags.end_uuid = new UUID(end, 0);
            }
            //Update
            case 4 -> {
                long start = getNthCountTimestamp(threads, flags.start_update, false, flags.thread_counts_combined);
                long end = getNthCountTimestamp(threads, flags.end_update, false, flags.thread_counts_combined);
                if(start>=0) flags.start_uuid = new UUID(start, 0);
                if(end>=0) flags.end_uuid = new UUID(end, 0);
            }
        }
    }
    private static long getFirstCountTimestamp(List<CountingThread> threads, boolean use_counts) {
        long first = Long.MAX_VALUE;
        for(CountingThread thread:threads) {
            long current = thread.getFirstUpdateTimestamp(use_counts ? CountDataType.COUNT : CountDataType.UPDATE);
            if(current < first) first = current;
        }
        return first;
    }
    private static long getLastCountTimestamp(List<CountingThread> threads, boolean use_counts) {
        long last = 0L;
        for(CountingThread thread:threads) {
            long current = thread.getLastUpdateTimestamp(use_counts ? CountDataType.COUNT : CountDataType.UPDATE);
            if(current > last) last = current;
        }
        return last;
    }
    private static long getNthCountTimestamp(List<CountingThread> threads, int n, boolean use_counts, boolean thread_counts_combined) {
        if(threads.size()==1) return ((UpdateOrCountValue)threads.get(0).getUpdateOrCount(n, use_counts ? CountDataType.COUNT : CountDataType.UPDATE)).getTimestamp();
        if(!thread_counts_combined) return -1L;
        if(threads.size()==AllThreads.all_threads.size()) return ((UpdateOrCountValue)AllThreads.combined_thread.getUpdateOrCount(n, use_counts ? CountDataType.COUNT : CountDataType.UPDATE)).getTimestamp();
        StatContainer nth_count = CombinedStats.nthCount(threads, UUID.minUUID(), UUID.maxUUID(), n+1, true, false, use_counts, true);
        if(nth_count.size()==0) return -1L;
        return ((UpdateOrCountValue)nth_count.getStat().get(0)[2]).getTimestamp();
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static void runQueue() throws InterruptedException, IOException, URISyntaxException {
        if(running) return;
        running = true;
        while(true) {
            FuncWithArgs data = queue.poll(Long.MAX_VALUE, TimeUnit.DAYS);
            String output = "If you see this, something went wrong.";
            StatContainer count_data = null;
            assert data != null;
            int count_duration_type = data.count_duration_type;
            if(data.counting_threads.isEmpty()) {
                switch (data.func_name) {
                    case "help" -> output = "View the documentation [here](https://counting.gg/blog/17193695-6525-4954-9194-463781954194)";
                    case "quote" -> {
                        Random random = new Random();
                        int length = random.nextInt(30)+2;
                        try {
                            output = CombinedStats.lcbotQuote(data.flags.username, length);
                        } catch (IllegalArgumentException e) {
                            output = e.getMessage();
                        }
                    }
                    case "10milliondollars" -> {
                        Long result = Counters.addMoney(data.flags.username, 10000000L);
                        if(result == null) output = data.flags.username+" does not exist.";
                        else output = data.flags.username + " now has " + result + " dollars";
                    }
                    case "threadlist" -> {
                        if(!data.flags.username_modified || !aliases.containsKey(data.flags.username.replaceAll("[-_]",""))) {
                            StringBuilder sb = new StringBuilder("There are currently ");
                            sb.append(all_threads.size()).append(" total threads.  \n");
                            for (String key : all_threads.keySet()) {
                                sb.append("  \n").append(key);
                            }
                            output = sb.toString();
                        } else {
                            String sb1 = "The thread " + data.flags.username + " has ";
                            StringBuilder sb2 = new StringBuilder(" aliases.  \n");
                            int amount = 0;
                            String arg = aliases.get(data.flags.username.replaceAll("[-_]",""));
                            for (String key : aliases.keySet()) {
                                if(aliases.get(key).equals(arg)) {
                                    amount++;
                                    sb2.append("  \n").append(key);
                                }
                            }
                            output = sb1+amount+sb2;
                        }
                    }
                    case "updateall" -> {
                        if(data.flags.username_modified) output = "Must be run without -u parameter";
                        else if(Arrays.binarySearch(admins, data.flags.username) < 0) output = "User does not have privileges to run this command";
                        else {
                            AllThreads.updateFullDB();
                            output = "Successfully updated all threads";
                        }
                    }
                    default -> output = "The command does not exist for 0 threads";
                }
            } else {
                try {
                    switch (data.func_name) {
                        case "hoc" -> count_data = CombinedStats.hoc(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "countswithstring" -> count_data = CombinedStats.countsWithString(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.args[1], data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "strikehoc" -> count_data = CombinedStats.strikeHoc(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);
                        case "participation", "hop" -> count_data = CombinedStats.participation(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined, data.flags.add_thread_stats);
                        case "firstcounts" -> count_data = CombinedStats.firstCounts(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.first, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "palindromes", "dromes" -> count_data = CombinedStats.palindromes(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);
                        case "repdigitsstat" -> count_data = CombinedStats.repdigits(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);
                        case "nrep" -> count_data = CombinedStats.nRep(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, Integer.parseInt(data.args[1]), data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);
                        case "basen" -> count_data = CombinedStats.baseN(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, Integer.parseInt(data.args[1]), data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);
                        case "sumofcounts" -> count_data = CombinedStats.sumOfCounts(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);
                        case "sumofreciprocals" -> count_data = CombinedStats.sumOfReciprocals(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);
                        case "nthcount" -> count_data = CombinedStats.nthCount(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, Integer.parseInt(data.args[1]), data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "threadparts" -> count_data = CombinedStats.threadParts(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.min_counts, data.flags.use_counts);
                        case "attemptspercount" -> count_data = CombinedStats.attemptsPerCount(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.mode, data.flags.include_current, data.flags.exclude_100, data.flags.unique, data.flags.use_counters);
                        case "fastestselfreplies" -> count_data = CombinedStats.fastestSelfReplies(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.first, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "timespenthavinglastcount" -> count_data = CombinedStats.timeSpentHavingLastCount(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined, data.flags.add_thread_stats);
                        case "timebetweenhocamounts" -> count_data = CombinedStats.timeBetweenHoCAmounts(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, Integer.parseInt(data.args[1]), Integer.parseInt(data.args[2]), data.flags.first, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "day","dayhoc" -> {count_duration_type = 5;count_data = CombinedStats.dayHoc(data.counting_threads, new Stats.TimeUnit(data.args[1], data.flags.time_period), data.flags.time_period, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);}
                        case "month","monthhoc" -> {count_duration_type = 5;count_data = CombinedStats.dayHoc(data.counting_threads, new Stats.TimeUnit(data.args[1], ParticipationDuration.MONTH), ParticipationDuration.MONTH, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);}
                        case "year","yearhoc" -> {count_duration_type = 5;count_data = CombinedStats.dayHoc(data.counting_threads, new Stats.TimeUnit(data.args[1], ParticipationDuration.YEAR), ParticipationDuration.YEAR, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);}
                        case "dayparts" -> {count_duration_type = 1;count_data = CombinedStats.dayParts(data.counting_threads, data.flags.start_date, data.flags.end_date, data.flags.time_period, data.flags.min_counts, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined, data.flags.add_thread_stats);}
                        case "daystreak" -> {count_duration_type = 1;count_data = CombinedStats.dayStreak(data.counting_threads, data.flags.start_date, data.flags.end_date, data.flags.time_period, data.flags.min_counts, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);}
                        case "dayrecord" -> {count_duration_type = 1;count_data = CombinedStats.dayRecord(data.counting_threads, data.flags.start_date, data.flags.end_date, data.flags.time_period, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);}
                        case "daymedals" -> {count_duration_type = 1;count_data = CombinedStats.dayMedals(data.counting_threads, data.flags.start_date, data.flags.end_date, data.flags.time_period, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined, data.flags.add_thread_stats);}
                        case "nthplacedaystreak" -> {count_duration_type = 1;count_data = CombinedStats.nthPlaceDayStreak(data.counting_threads, data.flags.start_date, data.flags.end_date, data.flags.time_period, Integer.parseInt(data.args[1]), data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);}
                        case "countsperday" -> {count_duration_type = 1;count_data = CombinedStats.countsPerDay(data.counting_threads, data.flags.start_date, data.flags.end_date, data.flags.time_period, data.flags.min_counts, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);}
                        case "peakcountsperday" -> {count_duration_type = 1;count_data = CombinedStats.peakCountsPerDay(data.counting_threads, data.flags.start_date, data.flags.end_date, data.flags.time_period, data.flags.min_counts, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);}
                        case "dailypercentageofcounts" -> {count_duration_type = 1;count_data = CombinedStats.nthPlaceDailyPercentageOfCounts(data.counting_threads, data.flags.start_date, data.flags.end_date, data.flags.time_period, 0, data.flags.min_counts, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);}
                        case "nthplacedailypercentageofcounts" -> {count_duration_type = 1;count_data = CombinedStats.nthPlaceDailyPercentageOfCounts(data.counting_threads, data.flags.start_date, data.flags.end_date, data.flags.time_period, Integer.parseInt(data.args[1]), data.flags.min_counts, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);}
                        case "photofinishes" -> {count_duration_type = 1;count_data = CombinedStats.photoFinishes(data.counting_threads, data.flags.start_date, data.flags.end_date, data.flags.time_period, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);}
                        case "percentagethreadpartsbyday" -> {count_duration_type = 1;count_data = CombinedStats.percentageThreadPartsByDay(data.counting_threads, data.flags.start_date, data.flags.end_date, data.flags.time_period, data.flags.min_counts, data.flags.unique, data.flags.use_counters, data.flags.use_counts);}
                        case "gets" -> count_data = CombinedStats.gets(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.offset, data.flags.k_size, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);
                        case "assists" -> count_data = CombinedStats.gets(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, 1, data.flags.k_size, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);
                        case "k","khoc" -> {count_duration_type = 6;count_data = CombinedStats.kHoc(data.counting_threads, Integer.parseInt(data.args[1]), data.flags.k_size, 0, true, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);}
                        case "split","splithoc" -> {count_duration_type = 7;count_data = CombinedStats.kHoc(data.counting_threads, Integer.parseInt(data.args[1]), data.flags.k_size, Integer.parseInt(data.args[2]), false, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);}
                        case "kparts" -> {count_duration_type = 2;count_data = CombinedStats.kParts(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.k_size, data.flags.use_ks, data.flags.min_counts, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined, data.flags.add_thread_stats);}
                        case "kstreak" -> {count_duration_type = 2;count_data = CombinedStats.kStreak(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.k_size, data.flags.use_ks, data.flags.min_counts, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);}
                        case "fastestks" -> {count_duration_type = 2;count_data = CombinedStats.fastestKs(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.k_size, data.flags.use_ks, data.flags.percentage, data.flags.first, data.flags.unique, data.flags.use_counters);}
                        case "perfectks","p500" -> {count_duration_type = 2;count_data = CombinedStats.perfectKs(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.k_size, data.flags.use_ks, data.flags.unique, data.flags.add_thread_stats);}
                        case "kmedals" -> {count_duration_type = 2;count_data = CombinedStats.kMedals(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.k_size, data.flags.use_ks, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined, data.flags.add_thread_stats);}
                        case "kundernmsstreak" -> {count_duration_type = 2;count_data = CombinedStats.kUnderNMsStreak(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.k_size, data.flags.use_ks, Integer.parseInt(data.args[1]), data.flags.percentage, data.flags.unique, data.flags.use_counters);}
                        case "pairpbs" -> {count_duration_type = 2;count_data = CombinedStats.pairPBs(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.k_size, data.flags.use_ks, data.flags.percentage, data.flags.first, data.flags.unique);}
                        case "kminsplitstddev" -> {count_duration_type = 2;count_data = CombinedStats.kMinSplitStdDev(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.k_size, data.flags.percentage, data.flags.first, data.flags.unique, data.flags.use_counters);}
                        case "sumofbest","sob" -> {count_duration_type = 2;count_data = CombinedStats.sumOfBest(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.k_size, data.flags.percentage, null, data.flags.first, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);}
                        case "usersumofbest","usersob" -> {count_duration_type = 2;count_data = CombinedStats.sumOfBest(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.k_size, data.flags.percentage, data.flags.username, data.flags.first, data.flags.unique, data.flags.use_counters, data.flags.use_counters || data.flags.thread_counts_combined);}
                        case "hourparts" -> count_data = CombinedStats.hourParts(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.duration, data.flags.min_counts, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined, data.flags.add_thread_stats);
                        case "hourrecord","recordhour" -> count_data = CombinedStats.hourRecord(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.duration, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "hourrecordoffset","recordhouroffset" -> count_data = CombinedStats.hourRecordOffset(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, Math.min(data.flags.duration, 604800), data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "hourrecordnomistakes","recordhournomistakes" -> count_data = CombinedStats.hourRecordNoMistakes(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.duration, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);
                        case "fastestbars" -> count_data = CombinedStats.fastestBars(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.first, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "fastestncounts" -> count_data = CombinedStats.fastestNCounts(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, Integer.parseInt(data.args[1]), data.flags.first, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "dominantncounts" -> count_data = CombinedStats.dominantNCounts(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, Integer.parseInt(data.args[1]), data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "feweststrikesinncounts" -> count_data = CombinedStats.fewestStrikesInNCounts(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, Integer.parseInt(data.args[1]), data.flags.first, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);
                        case "uniquetimepartsinday" -> count_data = CombinedStats.uniqueTimeParts(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.duration_modified ? data.flags.duration : 3600000, false, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "uniquetimepartsinweek" -> count_data = CombinedStats.uniqueTimeParts(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.duration_modified ? data.flags.duration : 3600000, true, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "uniquecountsmodn" -> count_data = CombinedStats.uniqueCountsModN(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, Integer.parseInt(data.args[1]), data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);
                        case "countspermistake" -> count_data = CombinedStats.countsPerMistake(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.min_counts, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);
                        case "nomistakestreak" -> count_data = CombinedStats.noMistakeStreak(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.unique, data.flags.use_counters, data.flags.thread_counts_combined);
                        case "perfectstreak" -> count_data = CombinedStats.perfectStreak(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.counts_between_replies, data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "rotationstreak" -> count_data = CombinedStats.rotationStreak(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.counts_between_replies, data.flags.allow_duplicate_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "nmsreplies" -> count_data = CombinedStats.nMsReplies(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, Integer.parseInt(data.args[1]), data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined, data.flags.add_thread_stats);
                        case "countsundernmsstreak" -> count_data = CombinedStats.countsUnderNMsStreak(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, Integer.parseInt(data.args[1]), data.flags.unique, data.flags.use_counters, data.flags.use_counts, data.flags.thread_counts_combined);
                        case "oddratio" -> count_data = CombinedStats.oddEvenRatio(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, true, data.flags.min_counts, data.flags.unique, data.flags.thread_counts_combined);
                        case "evenratio" -> count_data = CombinedStats.oddEvenRatio(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, false, data.flags.min_counts, data.flags.unique, data.flags.thread_counts_combined);
                        case "trifecta" -> count_data = CombinedStats.trifecta(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, !data.flags.first, data.flags.use_counts);
                        case "trifectadayhoc" -> {count_duration_type = 5;count_data = CombinedStats.trifectaDayHoc(data.counting_threads, new Stats.TimeUnit(data.args[1], data.flags.time_period), data.flags.time_period, !data.flags.first, data.flags.use_counts);}
                        case "trifectadayparts" -> {count_duration_type = 1;count_data = CombinedStats.trifectaDayParts(data.counting_threads, data.flags.start_date, data.flags.end_date, data.flags.time_period, !data.flags.first, data.flags.use_counts);}
                        case "trifectadaystreak" -> {count_duration_type = 1;count_data = CombinedStats.trifectaDayStreak(data.counting_threads, data.flags.start_date, data.flags.end_date, data.flags.time_period, data.flags.mode, !data.flags.first, data.flags.unique, data.flags.use_counts);}
                        case "countingpairs" -> count_data = CombinedStats.countingPairs(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, false, 0, data.flags.allow_duplicate_counters, data.flags.unique, data.flags.use_counts, data.flags.thread_counts_combined, data.flags.add_thread_stats);
                        case "hourcountingpairs" -> count_data = CombinedStats.countingPairs(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, true, data.flags.duration, data.flags.allow_duplicate_counters, data.flags.unique, data.flags.use_counts, data.flags.thread_counts_combined, data.flags.add_thread_stats);
                        case "favoritecounter","favouritecounter" -> count_data = CombinedStats.favoriteCounters(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid,  data.flags.allow_duplicate_counters, data.flags.unique, data.flags.use_counts, data.flags.thread_counts_combined, data.flags.add_thread_stats);
                        case "usercountingpairs","countingpairsspecificuser" -> count_data = CombinedStats.countingPairsSpecificUser(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid,  data.args[1], data.flags.allow_duplicate_counters, data.flags.unique, data.flags.use_counts, data.flags.thread_counts_combined, data.flags.add_thread_stats);
                        case "percentofcountswithuser" -> count_data = CombinedStats.percentageOfCountsWithUser(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.args[1], data.flags.min_counts, data.flags.unique, data.flags.use_counts, data.flags.thread_counts_combined, data.flags.add_thread_stats);
                        case "usersnotcountedwith" -> count_data = CombinedStats.usersNotCountedWith(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.args[1], data.flags.unique, data.flags.use_counts, data.flags.thread_counts_combined, data.flags.add_thread_stats);
                        default -> output = "The command does not exist for a nonzero amount of threads";
                    }
                } catch(IndexOutOfBoundsException | IllegalArgumentException | DateTimeParseException e) {
                    output = "Illegal Arguments to "+data.func_name;
                }
                if(count_data!=null) {
                    String username = data.flags.username;
                    if(!data.flags.use_counters && all_threads.containsKey(username)) username = all_threads.get(username).thread.title;
                    output = "*Threads: " + generateThreadString(data.counting_threads)+"*\n\n"+count_data.stat_name+" "+generateStatRangeString(data.counting_threads, data.args, data.flags, data.func_name, username, count_duration_type)+"\n\n";
                    if(data.flags.position>0) output+=count_data.toCompressedTable(data.flags.context, data.flags.position);
                    else output += count_data.toCompressedTable(data.flags.context, data.flags.username);
                }
                if(output.length() > 4096) output = "Output too large. Please try again with a smaller context window.";
            }
            CGGAuthenticator.postToAPI(output);
        }
    }

    private static String generateStatRangeString(List<CountingThread> threads, String[] args, Flags flags, String func_name, String username, int count_duration_type) {
        UUID min_uuid = new UUID(getFirstCountTimestamp(threads, flags.use_counts), 0);
        UUID max_uuid = new UUID(getLastCountTimestamp(threads, flags.use_counts), 0);
        String output = "";
        if(flags.position>0) output += "At Position "+flags.position+" ";
        else if(func_name.equals("rotationstreak")) output += "";
        else if(flags.use_counters) output += "For User "+username+" ";
        else if(!flags.thread_counts_combined) output += "For Thread "+username+" ";
        else output += "In CGG ";
        if(count_duration_type < 5 && flags.start_uuid.getTime() <= min_uuid.getTime() && flags.end_uuid.getTime() >= max_uuid.getTime()) return output;
        return output + switch(count_duration_type) {
            //UUID
            case 0 -> {
                if(flags.start_uuid.getTime() <= min_uuid.getTime()) yield "Up to UUID "+flags.end_uuid;
                else if(flags.end_uuid.getTime() >= max_uuid.getTime()) yield "Starting with UUID "+flags.start_uuid;
                else yield "Between UUIDs "+flags.start_uuid+" and "+flags.end_uuid;
            }
            //Date
            case 1 -> {
                if(flags.start_uuid.getTime() <= min_uuid.getTime()) yield "Up to "+flags.end_date;
                else if(flags.end_uuid.getTime() >= max_uuid.getTime()) yield "Starting From "+flags.start_date;
                else yield "Between "+flags.start_date+" and "+flags.end_date;
            }
            //K
            case 2 -> {
                if(flags.start_uuid.getTime() <= min_uuid.getTime()) yield "Up to "+flags.k_size+"K "+flags.end_k;
                else if(flags.end_uuid.getTime() >= max_uuid.getTime()) yield "Starting From "+flags.k_size+"K "+flags.start_k;
                else yield "Between "+flags.k_size+"Ks "+flags.start_k+" and "+flags.end_k;
            }
            //Count
            case 3 -> {
                if(flags.start_uuid.getTime() <= min_uuid.getTime()) yield "Up to Count "+flags.end_count;
                else if(flags.end_uuid.getTime() >= max_uuid.getTime()) yield "Starting From Count "+flags.start_count;
                else yield "Between Counts "+flags.start_count+" and "+flags.end_count;
            }
            //Update
            case 4 -> {
                if(flags.start_uuid.getTime() <= min_uuid.getTime()) yield "Up to Update "+flags.end_update;
                else if(flags.end_uuid.getTime() >= min_uuid.getTime()) yield "Starting From Update "+flags.start_update;
                else yield "Between Updates "+flags.start_update+" and "+flags.end_update;
            }
            //Day/Month/Year (Single Day)
            case 5 -> "For "+new Stats.TimeUnit(args[1], flags.time_period);
            //K (Single K)
            case 6 -> "For "+flags.k_size+"K "+Integer.parseInt(args[1]);
            //Split (Single Split)
            case 7 -> "For "+flags.k_size+"K "+Integer.parseInt(args[1])+", Split "+Integer.parseInt(args[2]);
            default -> throw new IllegalArgumentException("Illegal Count Duration Type");
        };
    }

    private static String generateThreadString(List<CountingThread> threads) {
        if(threads.size()==AllThreads.all_threads.size()) return "All Threads";
        List<CountingThread> traditional = new ArrayList<>();
        List<CountingThread> double_counting = new ArrayList<>();
        List<CountingThread> no_mistakes = new ArrayList<>();
        List<CountingThread> miscellaneous = new ArrayList<>();
        for (CountingThread thread : threads) {
            switch(thread.thread.category) {
                case "Traditional" -> traditional.add(thread);
                case "Double Counting" -> double_counting.add(thread);
                case "No Mistakes" -> no_mistakes.add(thread);
                case "Miscellaneous" -> miscellaneous.add(thread);
                default -> System.out.println("Thread Category Not Found: "+thread.thread.category);
            }
        }
        StringBuilder sb = new StringBuilder();
        if(traditional.size()==AllThreads.traditional.size()) sb.append("Traditional, ");
        if(double_counting.size()==AllThreads.double_counting.size()) sb.append("Double Counting Threads, ");
        if(no_mistakes.size()==AllThreads.no_mistakes.size()) sb.append("No Mistakes Threads, ");
        if(miscellaneous.size()==AllThreads.miscellaneous.size()) sb.append("Miscellaneous, ");
        if(traditional.size()!=AllThreads.traditional.size()) {
            for(CountingThread thread: traditional) sb.append(thread.thread.title).append(", ");
        }
        if(double_counting.size()!=AllThreads.double_counting.size()) {
            for(CountingThread thread: double_counting) sb.append(thread.thread.title).append(", ");
        }
        if(no_mistakes.size()!=AllThreads.no_mistakes.size()) {
            for(CountingThread thread: no_mistakes) sb.append(thread.thread.title).append(", ");
        }
        if(miscellaneous.size()!=AllThreads.miscellaneous.size()) {
            for(CountingThread thread: miscellaneous) sb.append(thread.thread.title).append(", ");
        }
        return sb.substring(0, sb.length()-2);
    }
}
