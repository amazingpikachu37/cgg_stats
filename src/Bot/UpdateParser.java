package Bot;

import Threads.UUID;
import Threads.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class UpdateParser {
    private static boolean running = false;
    private static final Map<String, Statable> all_threads = CombinedStats.all_threads_map;
    private static final Map<String, String> aliases = new HashMap<>();
    private static final String[] admins = new String[]{"amazingpikachu_37"};
    private static final LinkedList<String> recent_uuids = new LinkedList<>();
    private static final Logger botLogger = Util.setupLogger(Logger.getLogger("BOT"),  "SidethreadData\\bot_history.log", "bot");
    private static final LinkedBlockingDeque<FuncWithArgs> queue = new LinkedBlockingDeque<>();
    private record FuncWithArgs(String func_name, String[] args, Flags flags, List<Statable> counting_threads) {}
    static {
        File f = new File("SidethreadData//SidethreadAliases.txt");
        try(Scanner in = new Scanner(f)) {
            while(in.hasNextLine()) {
                String[] map = in.nextLine().split(" ");
                aliases.put(map[0],map[1]);
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }
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
        Set<Statable> counting_threads_set = new HashSet<>();
        int i;
        for(i=0;i<args.length;i++) {
            boolean to_remove = args[i].charAt(0) == '-';
            String arg = args[i].replaceAll("-|_","");
            List<Statable> threads_to_add = switch(arg) {
                case "favorites","favourites" -> CombinedStats.favorites;
                case "traditional" -> CombinedStats.traditional;
                case "double", "doublecounting" -> CombinedStats.double_counting;
                case "nomistakesthread" -> CombinedStats.no_mistakes;
                case "other","miscellaneous" -> CombinedStats.miscellaneous;
                case "allthreads" -> CombinedStats.all_threads;
                default -> new ArrayList<>();
            };
            if(threads_to_add.size()==0) {
                String thread_name = aliases.get(arg);
                if(thread_name!=null) {
                    threads_to_add.add(all_threads.get(thread_name));
                }
            }
            if(threads_to_add.size()!=0) {
                if(to_remove) {
                    threads_to_add.forEach(counting_threads_set::remove);
                } else {
                    counting_threads_set.addAll(threads_to_add);
                }
            } else break;
        }
        args = Arrays.copyOfRange(args,i,args.length);
        List<Statable> counting_threads = counting_threads_set.stream().toList();
        botLogger.getHandlers()[0].setFormatter(new MyFormatter(user));
        botLogger.info(body);
        String func_name = args.length > 0 ? args[0].toLowerCase().replaceAll("_","") : "about";
        Flags flags = new Flags(user, new UUID(uuid));
        try {
            for (int j = 1; j < args.length; j++) {
                if (args[j].charAt(0) != '-') continue;
                switch (args[j].toLowerCase().replaceAll("_", "")) {
                    case "-u", "-username","-t","-thread" -> {flags.username = args[j + 1];flags.username_modified=true;}
                    case "-p", "-position" -> flags.position = Integer.parseInt(args[j + 1]);
                    case "-c", "-context" -> flags.context = Integer.parseInt(args[j + 1]);
                    case "-su", "-startuuid" -> flags.start_uuid = new UUID(args[j + 1]);
                    case "-eu", "-enduuid" -> flags.end_uuid = new UUID(args[j + 1]);
                    case "-sd", "-startdate" -> flags.start_date = args[j + 1];
                    case "-ed", "-enddate" -> flags.end_date = args[j + 1];
                    case "-current" -> flags.is_current = true;
                    case "-mode" -> flags.mode = Integer.parseInt(args[j + 1]);
                    case "-ks", "-ksize" -> flags.gets_per_k = Integer.parseInt(args[j + 1]);
                    case "-sk", "-startk" -> flags.start_k = Integer.parseInt(args[j + 1]);
                    case "-ek", "-endk" -> flags.end_k = Integer.parseInt(args[j + 1]);
                    case "-mp", "-minparts" -> flags.min_parts = Integer.parseInt(args[j + 1]);
                    case "-d", "-duration" -> flags.duration_seconds = Integer.parseInt(args[j + 1]);
                    case "-combined" -> flags.thread_stats_combined = true;
                    case "-%", "-percentage" -> flags.percentage = Float.parseFloat(args[j + 1]);
                    case "-sc", "-startcount" -> flags.start_count = Integer.parseInt(args[j + 1]);
                    case "-ec", "-endcount" -> flags.end_count = Integer.parseInt(args[j + 1]);
                    case "-cbr","-countsbetweenreplies" -> flags.counts_between_replies = Integer.parseInt(args[j + 1]);
                    case "-cap" -> flags.cap = Integer.parseInt(args[j + 1]);
                    case "-offset","-o" -> {flags.offset = Integer.parseInt(args[j + 1]);flags.offset_modified=true;}
                    case "-exact","-e" -> flags.is_exact=true;
                }
            }
        } catch(NumberFormatException e) {
            return "Illegal arguments to "+func_name;
        }
        queue.add(new FuncWithArgs(func_name,args,flags,counting_threads));
        return null;
    }
    private static String getOrdinal(int num) {
        String ordinal = "th";
        if(num%10==3&&num%100!=13) ordinal = "rd";
        else if(num%10==2&&num%100!=12) ordinal = "nd";
        else if(num%10==1&&num%100!=11) ordinal = "st";
        return num+ordinal;
    }

    public static void runQueue() throws InterruptedException, IOException, URISyntaxException {
        if(running) return;
        running = true;
        while(true) {
            FuncWithArgs data = queue.poll(Long.MAX_VALUE, TimeUnit.DAYS);
            String output = "If you see this, something went wrong.";
            String header = "";
            String[] table_headers = null;
            Map<String, ? extends Comparable> countData1 = null;
            List<? extends NonUnique> countData2 = null;
            boolean reversed = false;
            assert data != null;
            if(data.counting_threads.size()==0) {
                switch(data.func_name) {
                    case "help" -> output = "View the documentation [here](https://counting.gg/blog/17193695-6525-4954-9194-463781954194)";
                    case "10milliondollars" -> {
                        Long result = Counters.addMoney(data.flags.username, 10000000L);
                        if(result == null) output = data.flags.username+" does not exist.";
                        else output = data.flags.username + " now has " + result + " dollars";
                    }
                    case "threadlist" -> {
                        if(!data.flags.username_modified || !aliases.containsKey(data.flags.username.replaceAll("-|_",""))) {
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
                            String arg = aliases.get(data.flags.username.replaceAll("-|_",""));
                            for (String key : aliases.keySet()) {
                                if(aliases.get(key).equals(arg)) {
                                    amount++;
                                    sb2.append("  \n").append(key);
                                }
                            }
                            output = sb1+amount+sb2;
                        }
                    }
                    case "updateall" ->    {
                        if(Arrays.binarySearch(admins, data.flags.username) < 0) output = "User does not have privileges to run this command";
                        else {
                            CombinedStats.updateAllDatabases();
                            output = "Successfully updated all threads";
                        }
                    }
                    case "exacta","trifecta" -> {
                        header = header + "Exactas for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                        countData1 = CombinedStats.trifecta(data.flags.start_uuid, data.flags.end_uuid);
                        table_headers = new String[]{"Username", "Trifectas", "Exactas", "Chalupa Exactas", "Poonxactas", "trejexactas"};
                    }
                    case "exactadayparts", "trifectadayparts", "exactamonthparts", "trifectamonthparts", "exactayearparts", "trifectayearparts" -> {
                        ParticipationDuration mode = switch (data.func_name) {
                            case "exactadayparts","trifectadayparts" -> ParticipationDuration.DAY;
                            case "exactamonthparts","trifectamonthparts" -> ParticipationDuration.MONTH;
                            case "exactayearparts","trifectayearparts" -> ParticipationDuration.YEAR;
                            default -> null;
                        };
                        header = header + "Exactas "+mode.toString().toLowerCase()+" parts for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                        countData1 = CombinedStats.exacta_day_parts(data.flags.getStartDate(mode), data.flags.getEndDate(mode), mode);
                        System.out.println(countData1);
                        table_headers = new String[]{"Username", "Trifectas", "Exactas", "Chalupa Exactas", "Poonxactas", "trejexactas"};
                    }
                    case "exactadaystreak", "trifectadaystreak", "exactamonthstreak", "trifectamonthstreak", "exactayearstreak", "trifectayearstreak" -> {
                        ParticipationDuration mode = switch (data.func_name) {
                            case "exactadaystreak","trifectadaystreak" -> ParticipationDuration.DAY;
                            case "exactamonthstreak","trifectamonthstreak" -> ParticipationDuration.MONTH;
                            case "exactayearstreak","trifectayearstreak" -> ParticipationDuration.YEAR;
                            default -> null;
                        };
                        String mode_str = switch(data.flags.mode) {
                            case 0 -> " trifecta ";
                            case 1 -> " chalupa exacta ";
                            case 2 -> " poonexacta ";
                            case 3 -> " trejexacta ";
                            default -> " exacta ";
                        };
                        header = header + (data.flags.is_current?"Current":"Record") + mode_str+mode.toString().toLowerCase() + " streak for "+data.flags.username+" between "+ data.flags.getStartDate(mode)+" and "+data.flags.getEndDate(mode);
                        countData1 = CombinedStats.exacta_day_streak(data.flags.mode, data.flags.getStartDate(mode), data.flags.getEndDate(mode),data.flags.is_current, mode);
                        table_headers = new String[]{"Username", "Streak","Start","End"};
                    }
                    case "nonuniqueexactadaystreak", "nonuniquetrifectadaystreak", "nonuniqueexactamonthstreak", "nonuniquetrifectamonthstreak", "nonuniqueexactayearstreak", "nonuniquetrifectayearstreak" -> {
                        ParticipationDuration mode = switch (data.func_name) {
                            case "nonuniqueexactadaystreak","nonuniquetrifectadaystreak" -> ParticipationDuration.DAY;
                            case "nonuniqueexactamonthstreak","nonuniquetrifectamonthstreak" -> ParticipationDuration.MONTH;
                            case "nonuniqueexactayearstreak","nonuniquetrifectayearstreak" -> ParticipationDuration.YEAR;
                            default -> null;
                        };
                        String mode_str = switch(data.flags.mode) {
                            case 0 -> "Trifecta ";
                            case 1 -> "Chalupa exacta ";
                            case 2 -> "Poonexacta ";
                            case 3 -> "Trejexacta ";
                            default -> "Exacta ";
                        };
                        header = header + mode_str+mode.toString().toLowerCase() + " streaks at position "+data.flags.position+" between "+ data.flags.getStartDate(mode)+" and "+data.flags.getEndDate(mode);
                        countData2 = CombinedStats.non_unique_exacta_day_streak(data.flags.mode, data.flags.getStartDate(mode), data.flags.getEndDate(mode),mode);
                        table_headers = new String[]{"Username", "Streak","Start","End"};
                    }
                    default -> output = "You are trying to run this command without any threads";
                }
            } else {
                try {
                    StringBuilder sb = new StringBuilder("*Thread");
                    if (data.counting_threads.size() == 1) {
                        sb.append(": ").append(data.counting_threads.get(0).getName());
                    } else {
                        sb.append("s: ").append(data.counting_threads.get(0).getName());
                        for (int i=1;i<data.counting_threads.size();i++) {
                            sb.append(", ").append(data.counting_threads.get(i).getName());
                        }
                    }
                    sb.append("*\n\n");
                    header = sb.toString();
                    switch (data.func_name) {
                        case "hoc" -> {
                            header = header + "HoC for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.cappedHoCTime(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.cap);
                            table_headers = new String[]{"Username", "Counts"};
                        }
                        case "kparts" -> {
                            header = header + "Hall of " + data.flags.gets_per_k + "K parts for " + data.flags.username + " between ks " + data.flags.start_k + " and " + data.flags.end_k;
                            countData1 = CombinedStats.combinedKParts(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.gets_per_k);
                            table_headers = new String[]{"Username", "K Parts"};
                        }
                        case "dayparts", "monthparts", "yearparts" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "dayparts" -> ParticipationDuration.DAY;
                                case "monthparts" -> ParticipationDuration.MONTH;
                                case "yearparts" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            header = header + "Hall of " + mode.toString().toLowerCase() + " parts for " + data.flags.username + " between " + data.flags.getStartDate(mode) + " and " + data.flags.getEndDate(mode);
                            countData1 = CombinedStats.combinedDayParts(data.counting_threads, data.flags.getStartDate(mode), data.flags.getEndDate(mode), mode, data.flags.thread_stats_combined);
                            table_headers = new String[]{"Username", mode.toCapitalizedString()+ " Parts"};
                        }
                        case "countsperday", "countspermonth", "countsperyear" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "countsperday" -> ParticipationDuration.DAY;
                                case "countspermonth" -> ParticipationDuration.MONTH;
                                case "countsperyear" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            header = header + "Counts per " + mode.toString().toLowerCase() + " for " + data.flags.username + " between " + data.flags.getStartDate(mode) + " and " + data.flags.getEndDate(mode);
                            countData1 = CombinedStats.combinedCountsPerDay(data.counting_threads, data.flags.getStartDate(mode), data.flags.getEndDate(mode), mode);
                            table_headers = new String[]{"Username", "Counts per "+mode.toString().toLowerCase()};
                        }
                        case "peakcountsperday", "peakcountspermonth", "peakcountsperyear" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "peakcountsperday" -> ParticipationDuration.DAY;
                                case "peakcountspermonth" -> ParticipationDuration.MONTH;
                                case "peakcountsperyear" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            header = header + "Peak counts per " + mode.toString().toLowerCase() + " for " + data.flags.username + " between days " + data.flags.getStartDate(mode) + " and " + data.flags.getEndDate(mode);
                            countData1 = CombinedStats.combinedPeakCountsPerDay(data.counting_threads, data.flags.getStartDate(mode), data.flags.getEndDate(mode), mode, data.flags.min_parts);
                            table_headers = new String[]{"Username", "Counts per "+mode.toString().toLowerCase()};
                        }
                        case "hourparts" -> {
                            header = header + "Hall of " + data.flags.duration_seconds + " second parts for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.hourParts(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.duration_seconds, data.flags.thread_stats_combined);
                            table_headers = new String[]{"Username", "Hour Parts"};
                        }
                        case "hop","participation" -> {
                            header = header + "Hall of participation for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.combinedParticipation(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"Username", "Score","Counts", "Gets","Assists","K Parts","Day Parts"};
                        }
                        case "gets","assists" -> {
                            if(data.func_name().equals("assists") && !data.flags.offset_modified) data.flags.offset = 1;
                            String header_center = switch(data.flags.offset) {
                                case 0 -> "gets";
                                case 1 -> "assists";
                                default -> "gets with offset "+data.flags.offset;
                            };
                            header = header + "Hall of "+header_center+" for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.combinedGets(data.counting_threads, data.flags.start_k, data.flags.end_k,data.flags.gets_per_k,data.flags.offset);
                            table_headers = new String[]{"Username", "Gets"};
                        }
                        case "day", "month", "year" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "day" -> ParticipationDuration.DAY;
                                case "month" -> ParticipationDuration.MONTH;
                                case "year" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            header = header + "That " + mode.toString().toLowerCase() + " hoc looked like this:";
                            data.flags.start_date = data.args[1];
                            countData1 = CombinedStats.combinedDayHoc(data.counting_threads, data.flags.getStartDate(mode), mode);
                            table_headers = new String[]{"Username", "Counts"};
                        }
                        case "first_count" -> {
                            header = header + "First count for " + data.flags.username + " after " + data.flags.start_uuid;
                            countData1 = CombinedStats.overallFirstCounts(data.counting_threads, data.flags.start_uuid);
                            table_headers = new String[]{"Username", "First Count"};
                        }
                        case "last_count" -> {
                            header = header + "Last count for " + data.flags.username + " before " + data.flags.start_uuid;
                            countData1 = CombinedStats.overallLastCounts(data.counting_threads, data.flags.start_uuid);
                            table_headers = new String[]{"Username", "Last Count"};
                        }
                        case "daymedals","monthmedals","yearmedals","cotd","cotm","coty" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "daymedals", "cotd" -> ParticipationDuration.DAY;
                                case "monthmedals", "cotm" -> ParticipationDuration.MONTH;
                                case "yearmedals", "coty" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            header = header + mode.toCapitalizedString()+" medals for " + data.flags.username + " between " + data.flags.getStartDate(mode) + " and " + data.flags.getEndDate(mode);
                            countData1 = CombinedStats.combinedMedals(data.counting_threads, data.flags.getStartDate(mode),data.flags.getEndDate(mode),mode,data.flags.thread_stats_combined);
                            table_headers = new String[]{"Username","Gold","Silver","Bronze","Total"};
                        }
                        case "kmedals"-> {
                            header = header + data.flags.gets_per_k+"k medals for " + data.flags.username + " between ks " + data.flags.start_k + " and " + data.flags.end_k;
                            countData1 = CombinedStats.combinedKMedals(data.counting_threads, data.flags.start_k,data.flags.end_k,data.flags.gets_per_k);
                            table_headers = new String[]{"Username","Gold","Silver","Bronze","Total"};
                        }
//                        case "daily_counts", "monthly_counts", "yearly_counts" -> {
//                            ParticipationDuration mode = switch (data.func_name) {
//                                case "daily_counts" -> ParticipationDuration.DAY;
//                                case "monthly_counts" -> ParticipationDuration.MONTH;
//                                case "yearly_counts" -> ParticipationDuration.YEAR;
//                                default -> null;
//                            };
//                            header = header + "That " + mode.toString().toLowerCase() + " hoc looked like this:";
//                            countData1 = CombinedStats.combined_total_count_day_hocs(data.counting_threads, data.flags.getStartDate(mode), mode);
//                            table_headers = new String[]{"Username", "Counts"};
//                        }
                        case "daystreak", "monthstreak", "yearstreak" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "daystreak" -> ParticipationDuration.DAY;
                                case "monthstreak" -> ParticipationDuration.MONTH;
                                case "yearstreak" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            header = header + (data.flags.is_current?"Current ":"Record ") + mode.toString().toLowerCase() + " streak for "+data.flags.username+" between "+ data.flags.getStartDate(mode)+" and "+data.flags.getEndDate(mode);
                            countData1 = CombinedStats.combined_day_streak(data.counting_threads, data.flags.getStartDate(mode), data.flags.getEndDate(mode),data.flags.min_parts,mode,data.flags.is_current, data.flags.is_exact);
                            table_headers = new String[]{"Username", "Streak","Start","End"};
                        }
                        case "nonuniquedaystreak", "nonuniquemonthstreak", "nonuniqueyearstreak" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "nonuniquedaystreak" -> ParticipationDuration.DAY;
                                case "nonuniquemonthstreak" -> ParticipationDuration.MONTH;
                                case "nonuniqueyearstreak" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            header = header + mode.toCapitalizedString() + " streaks at position "+data.flags.position +" between "+ data.flags.getStartDate(mode)+" and "+data.flags.getEndDate(mode);
                            countData2 = CombinedStats.combined_day_streak_non_unique(data.counting_threads, data.flags.getStartDate(mode), data.flags.getEndDate(mode),data.flags.min_parts,mode);
                            table_headers = new String[]{"Username", "Streak","Start","End"};
                        }
                        case "palindromes","dromes" -> {
                            header = header + "Palindromes for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.combined_palindromes(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"Username", "Dromes"};
                        }
                        case "repdigits" -> {
                            header = header + "Repdigits for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.combined_repdigits(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"Username", "Repdigits"};
                        }
                        case "nrep" -> {
                            header = header + data.args[1]+"-reps for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.combined_n_rep(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid,Integer.parseInt(data.args[1]));
                            table_headers = new String[]{"Username", data.args[1]+"-reps"};
                        }
                        case "basen" -> {
                            header = header + "base "+data.args[1]+" counts for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.combined_base_n(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid,Integer.parseInt(data.args[1]));
                            table_headers = new String[]{"Username", "base "+data.args[1]+" counts"};
                        }
                        case "daysoverncounts", "monthsoverncounts", "yearsoverncounts" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "daysoverncounts" -> ParticipationDuration.DAY;
                                case "monthsoverncounts" -> ParticipationDuration.MONTH;
                                case "yearsoverncounts" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            header = header + mode.toCapitalizedString() + "s over "+data.args[1]+" counts for "+data.flags.username+" between "+ data.flags.getStartDate(mode)+" and "+data.flags.getEndDate(mode);
                            countData1 = CombinedStats.combined_days_over_n_counts(data.counting_threads, data.flags.getStartDate(mode), data.flags.getEndDate(mode),Integer.parseInt(data.args[1]),mode,data.flags.thread_stats_combined);
                            table_headers = new String[]{"Username", mode.toCapitalizedString() + "s"};
                        }
                        case "threaddaysoverncounts", "threadmonthsoverncounts", "threadyearsoverncounts" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "threaddaysoverncounts" -> ParticipationDuration.DAY;
                                case "threadmonthsoverncounts" -> ParticipationDuration.MONTH;
                                case "threadyearsoverncounts" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            if(!data.flags.username_modified) data.flags.username = "main";
                            header = header + mode.toCapitalizedString() + "s over "+data.args[1]+" counts for "+data.flags.username+" between "+ data.flags.getStartDate(mode)+" and "+data.flags.getEndDate(mode);
                            countData1 = CombinedStats.thread_days_over_n_counts(data.counting_threads, data.flags.getStartDate(mode), data.flags.getEndDate(mode),Integer.parseInt(data.args[1]),mode);
                            table_headers = new String[]{"Thread", mode.toCapitalizedString() + "s"};
                        }
                        case "dayrecord", "monthrecord", "yearrecord" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "dayrecord" -> ParticipationDuration.DAY;
                                case "monthrecord" -> ParticipationDuration.MONTH;
                                case "yearrecord" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            header = header + mode.toCapitalizedString() + " record for "+data.flags.username+" between "+ data.flags.getStartDate(mode)+" and "+data.flags.getEndDate(mode);
                            countData1 = CombinedStats.combined_unique_top_counts_per_day(data.counting_threads, data.flags.getStartDate(mode), data.flags.getEndDate(mode),mode,data.flags.thread_stats_combined);
                            table_headers = new String[]{"Username", mode.toCapitalizedString() + " record", "Date"};
                        }
                        case "topcountsperday", "topcountspermonth", "topcountsperyear" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "topcountsperday" -> ParticipationDuration.DAY;
                                case "topcountspermonth" -> ParticipationDuration.MONTH;
                                case "topcountsperyear" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            header = header + "Top counts per "+mode.toString().toLowerCase()+" around position "+data.flags.position+" between "+ data.flags.getStartDate(mode)+" and "+data.flags.getEndDate(mode);
                            countData2 = CombinedStats.combined_non_unique_top_counts_per_day(data.counting_threads, data.flags.getStartDate(mode), data.flags.getEndDate(mode),mode,data.flags.thread_stats_combined);
                            table_headers = new String[]{"Username", "Counts", "Date"};
                        }
                        case "threaddayrecord", "threadmonthrecord", "threadyearrecord" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "threaddayrecord" -> ParticipationDuration.DAY;
                                case "threadmonthrecord" -> ParticipationDuration.MONTH;
                                case "threadyearrecord" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            header = header + mode.toCapitalizedString() + " record for "+data.flags.username+" between "+ data.flags.getStartDate(mode)+" and "+data.flags.getEndDate(mode);
                            countData1 = CombinedStats.thread_day_records(data.counting_threads, data.flags.getStartDate(mode), data.flags.getEndDate(mode),mode);
                            table_headers = new String[]{"Thread", mode.toCapitalizedString() + " record","Date"};
                        }
                        case "recordhour","recordhouroffset" -> {
                            boolean offset = data.func_name.equals("recordhouroffset");
                            header = header + "Record " + data.flags.duration_seconds + "s period for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            if(data.flags.thread_stats_combined) {
                                countData1 = CombinedStats.combined_record_hour(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.duration_seconds, offset);
                                table_headers = new String[]{"Username", "Record Hour"};
                            } else {
                                countData1 = CombinedStats.overall_record_hour(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.duration_seconds, offset);
                                table_headers = new String[]{"Username", "Record Hour","Thread"};
                            }
                        }
                        case "threadrecordhour" -> {
                            header = header + "Record " + data.flags.duration_seconds + "s period at position " + data.flags.position + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData2 = CombinedStats.overall_thread_record_hour(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.duration_seconds);
                            table_headers = new String[]{"Hour", "Counts","Thread"};
                        }
                        case "dominantncounts" -> {
                            header = header + "Most dominant " + data.args[1] + " count period for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.overall_most_dominant_n_counts(data.counting_threads, Integer.parseInt(data.args[1]), data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"Username", "Counts", "Thread", "Start HoC", "End HoC", "Start Count", "End Count"};
                        }
                        case "perfectstreak" -> {
                            header = header + "Record perfect streak for "+ data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.overall_perfect_streak(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.counts_between_replies);
                            table_headers = new String[]{"Username", "Streak", "Thread", "Start Count", "End Count"};
                        }
                        case "nonuniqueperfectstreak" -> {
                            header = header + "Perfect streaks at position "+ data.flags.position + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData2 = CombinedStats.overall_non_unique_perfect_streak(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.counts_between_replies);
                            table_headers = new String[]{"Username", "Streak", "Thread", "Start Count", "End Count"};
                        }
                        case "rotationstreak","doubleperfectstreak" -> {
                            header = header + "Rotation streaks at position "+ data.flags.position + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData2 = CombinedStats.overall_non_unique_rotation_streak(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.counts_between_replies);
                            table_headers = new String[data.flags.counts_between_replies+4];
                            for(int i=0;i<data.flags.counts_between_replies;i++) {
                                table_headers[i] = "User " + (i+1);
                            }
                            table_headers[table_headers.length-4] = "Streak";
                            table_headers[table_headers.length-3] = "Thread";
                            table_headers[table_headers.length-2] = "Start Count";
                            table_headers[table_headers.length-1] = "End Count";
                        }
                        case "fastestselfreplies" -> {
                            header = header + "Fastest self replies for "+ data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData2 = CombinedStats.overall_fastest_self_replies(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, 1000);
                            table_headers = new String[]{"Username", "Time", "Count", "Thread"};
                        }
                        case "medianselfreply" -> {
                            header = header + "Median reply times for "+ data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.medianSelfReplyTime(data.counting_threads, data.flags.min_parts, data.flags.start_uuid, data.flags.end_uuid);
                            reversed = true;
                            table_headers = new String[]{"Username", "Time"};
                        }
                        case "countingpairs" -> {
                            header = header + "Counts between "+ data.args[1]+" and "+data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            if(data.flags.username.compareTo(data.args[1])<0) {
                                data.flags.username += "|"+data.args[1];
                            } else {
                                data.flags.username = data.args[1] +"|"+ data.flags.username;
                            }
                            countData1 = CombinedStats.combined_counting_pairs(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"User 1", "User 2", "Counts"};
                        }
                        case "hourcountingpairs" -> {
                            header = header + "Record hourly counts between "+ data.args[1]+" and "+data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            if(data.flags.username.compareTo(data.args[1])<0) {
                                data.flags.username += "|"+data.args[1];
                            } else {
                                data.flags.username = data.args[1] +"|"+ data.flags.username;
                            }
                            countData1 = CombinedStats.record_counting_pairs_by_hour(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.duration_seconds);
                            table_headers = new String[]{"User 1", "User 2", "Counts","Thread"};
                        }
                        case "usercountingpairs" -> {
                            header = header + "Counts between "+ data.args[1]+" and "+data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.combined_counting_pair_specific_user(data.counting_threads, data.args[1], data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"Username", "Counts"};
                        }
                        case "percentofcountswithuser" -> {
                            header = header + "Percentage of counts "+ data.args[1]+" has with "+data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.percentage_of_combined_counts_users_have_with_specific_user(data.counting_threads, data.args[1], data.flags.min_parts, data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"Username", "Percentage"};
                        }
                        case "favoritecounter","favouritecounter" -> {
                            header = header + "Favorite counter for "+data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.overall_favorite_counter(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"User 1", "User 2", "Counts"};
                        }
                        case "timespenthavinglastcount" -> {
                            header = header + "Time "+data.flags.username + "has held the last count between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.combined_time_spent_having_last_count(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.thread_stats_combined);
                            table_headers = new String[]{"Username", "Time"};
                        }
                        case "nmsreplies" -> {
                            header = header + "Most "+data.args[1]+"ms replies for "+ data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.combinedMsBetweenUpdates(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, Integer.parseInt(data.args[1]));
                            table_headers = new String[]{"Username", "Amount"};
                        }
                        case "subnmsselfreplystreak" -> {
                            header = header + "Longest sub "+data.args[1]+"ms self reply streak for "+ data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.overall_counts_under_n_ms_streak(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, Integer.parseInt(data.args[1]));
                            table_headers = new String[]{"Username", "Streak", "Thread", "Start Count", "End Count"};
                        }
                        case "oddratio","evenratio" -> {
                            boolean is_odd = data.func_name.equals("oddratio");
                            header = header + (is_odd?"Ddd":"Even")+" ratio for "+ data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.combined_odd_even_ratio(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.min_parts, is_odd);
                            table_headers = new String[]{"Username", "Ratio"};
                        }
                        case "uniquehourpartsinday","uniquehourpartsinweek" -> {
                            boolean is_day = data.func_name.equals("uniquehourpartsinday");
                            header = header + "Unique "+(is_day?"day":"week")+" "+data.flags.duration_seconds+"s parts for "+ data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.combined_unique_time_parts(data.counting_threads, data.flags.duration_seconds, is_day, data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"Username", data.flags.duration_seconds+"s parts"};
                        }
                        case "dailypercentofcounts","monthlypercentofcounts","yearlypercentofcounts" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "dailypercentofcounts" -> ParticipationDuration.DAY;
                                case "monthlypercentofcounts" -> ParticipationDuration.MONTH;
                                case "yearlypercentofcounts" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            header = header + mode.toOtherCapitalizedString()+" percentage of counts at position "+ data.flags.position + " between " + data.flags.getStartDate(mode) + " and " + data.flags.getEndDate(mode);
                            countData2 = CombinedStats.daily_percent_of_combined_counts(data.counting_threads, data.flags.getStartDate(mode), data.flags.getEndDate(mode), mode);
                            table_headers = new String[]{mode.toCapitalizedString(), "%", "Username", "User Counts", "Day Counts"};
                        }
                        case "nthplacedailypercentofcounts","nthplacemonthlypercentofcounts","nthplaceyearlypercentofcounts" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "nthplacedailypercentofcounts" -> ParticipationDuration.DAY;
                                case "nthplacemonthlypercentofcounts" -> ParticipationDuration.MONTH;
                                case "nthplaceyearlypercentofcounts" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            header = header + mode.toOtherCapitalizedString()+" percentage of counts in "+getOrdinal(Integer.parseInt(data.args[1]))+" place at position "+ data.flags.position + " between " + data.flags.getStartDate(mode) + " and " + data.flags.getEndDate(mode);
                            countData2 = CombinedStats.nth_place_daily_percent_of_combined_counts(data.counting_threads, data.flags.getStartDate(mode), data.flags.getEndDate(mode), Integer.parseInt(data.args[1]), mode);
                            table_headers = new String[]{mode.toCapitalizedString(), "%", "Username", "User Counts", "Day Counts"};
                        }
                        case "dailyphotofinish","monthlyphotofinish","yearlyphotofinish" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "dailyphotofinish" -> ParticipationDuration.DAY;
                                case "monthlyphotofinish" -> ParticipationDuration.MONTH;
                                case "yearlyphotofinish" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            header = header + mode.toOtherCapitalizedString()+" photo finishes at position "+ data.flags.position + " between " + data.flags.getStartDate(mode) + " and " + data.flags.getEndDate(mode);
                            countData2 = CombinedStats.combined_photo_finishes(data.counting_threads, data.flags.getStartDate(mode), data.flags.getEndDate(mode), mode, data.flags.percentage, data.flags.min_parts);
                            table_headers = new String[]{"User 1", "User 1 Counts", "User 2", "User 2 Counts", "%", mode.toCapitalizedString()};
                        }
                        case "largestcliques" -> {
                            header = header + "Largest clique "+ data.flags.username + " is contained in between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.largestCliques(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid, data.flags.min_parts);
                            table_headers = new String[]{"Username", "Size"};
                        }
                        case "highestusernotcountedwith" -> {
                            header = header + "Counters with the most counts that "+ data.flags.username + " has not counted with between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.combined_highest_user_not_counted_with(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"Username", "User 1","User 2","User 3","User 4","User 5"};
                        }
                        case "perfectks","p500" -> {
                            header = header + "Total perfect "+data.flags.gets_per_k+"ks for "+ data.flags.username + " between ks " + data.flags.start_k + " and " + data.flags.end_k;
                            countData1 = CombinedStats.combined_perfect_ks(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.gets_per_k);
                            table_headers = new String[]{"Username", "Perfect "+data.flags.gets_per_k+"ks"};
                        }
                        case "nthcount" -> {
                            header = header + getOrdinal(Integer.parseInt(data.args[1]))+" count for "+data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.overall_nth_count(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid,Integer.parseInt(data.args[1]));
                            table_headers = new String[]{"Username", "Count"};
                        }
                        case "nthcountnouser" -> {
                            header = header + getOrdinal(Integer.parseInt(data.args[1]))+" count for counting.gg between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            CountValue count = CombinedStats.overall_nth_count_no_user(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid,Integer.parseInt(data.args[1]));
                            output = header + "\n\n"+count;
                        }
                        case "updatehoc" -> {
                            header = header + "Update HoC for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.combinedUpdatesHoc(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"Username", "Updates"};
                        }
                        case "updateswithstring" -> {
                            header = header + "Update containing "+data.args[1]+" for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.totalUpdatesWithString(data.counting_threads, data.args[1], data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"Username", "Updates"};
                        }
                        case "fastestncounts","slowestncounts","threadfastestncounts","threadslowestncounts" -> {
                            boolean fast = data.func_name.equals("fastestncounts")||data.func_name.equals("threadfastestncounts");
                            boolean thread = data.func_name.equals("threadslowestncounts")||data.func_name.equals("threadfastestncounts");
                            if(!data.flags.username_modified) {
                                header = header + (fast?"Fastest ":"Slowest ")+data.args[1]+" counts between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            } else {
                                header = header + (fast ? "Fastest " : "Slowest ") + data.args[1] + " counts for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            }if(thread) {
                                countData1 = CombinedStats.thread_fastest_n_counts_no_user(data.counting_threads, Integer.parseInt(data.args[1]), data.flags.start_uuid, data.flags.end_uuid,fast);
                                table_headers = new String[]{"Thread", "Time", "Start Count", "End Count"};
                            } else if(!data.flags.thread_stats_combined) {
                                countData1 = CombinedStats.sitewide_fastest_n_counts(data.counting_threads, Integer.parseInt(data.args[1]), data.flags.start_uuid, data.flags.end_uuid,fast);
                                table_headers = new String[]{"Username", "Time", "Start HoC", "End HoC", "Start Count", "Start Thread", "End Count", "End Thread"};
                            } else {
                                countData1 = CombinedStats.overall_fastest_n_counts(data.counting_threads, Integer.parseInt(data.args[1]), data.flags.start_uuid, data.flags.end_uuid,fast);
                                table_headers = new String[]{"Username", "Time", "Thread", "Start HoC", "End HoC", "Start Count", "End Count"};
                            }
                        }
                        case "sitefastestncounts","siteslowestncounts" -> {
                            boolean fast = data.func_name.equals("sitefastestncounts");
                            header = header + (fast?"Fastest ":"Slowest ")+data.args[1]+" counts for counting.gg between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            StatLoader.SitewideFastestNCountsNoUserOutput speed = CombinedStats.sitewide_fastest_n_counts_no_user(data.counting_threads, Integer.parseInt(data.args[1]), data.flags.start_uuid, data.flags.end_uuid,fast);
                            countData1 = Map.of("cgg",speed);
                            table_headers = new String[]{"Website", "Time", "Start Count", "Start Thread", "End Count", "Start Thread"};
                        }
                        case "nomistakestreak" -> {
                            header = header + "No mistake streak for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.overall_no_mistake_streak(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"Username", "Streak", "Thread", "Start Count", "End Count"};
                        }
                        case "threadparts" -> {
                            header = header + "Unique threads  " + data.flags.username + " has participated in between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.user_thread_parts(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"Username", "Threads   "};
                        }
//                        case "dailythreadpartspercentage","monthlythreadpartspercentage","yearlythreadpartspercentage" -> {
//                            ParticipationDuration mode = switch (data.func_name) {
//                                case "dailythreadpartspercentage" -> ParticipationDuration.DAY;
//                                case "monthlythreadpartspercentage" -> ParticipationDuration.MONTH;
//                                case "yearlythreadpartspercentage" -> ParticipationDuration.YEAR;
//                                default -> null;
//                            };
//                            header = header + mode.toOtherCapitalizedString()+" percentage of threads participated in at position "+ data.flags.position + " between " + data.flags.getStartDate(mode) + " and " + data.flags.getEndDate(mode);
//                            countData1 = CombinedStats.days_with_greatest_percentage_thread_parts(data.counting_threads, data.flags.getStartDate(mode), data.flags.getEndDate(mode), mode);
//                            table_headers = new String[]{"Date", "%", mode.toCapitalizedString()};
//                        }
                        case "fastestbars","slowestbars" -> {
                            boolean fast = data.func_name.equals("fastestbars");
                            header = header + (fast?"Fastest ":"Slowest ")+" bars at position "+data.flags.position+" between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData2 = CombinedStats.overall_fastest_bars(data.counting_threads, 3600000, data.flags.start_uuid, data.flags.end_uuid);
                            if(!fast) reversed = true;
                            table_headers = new String[]{"Username", "Time", "Count", "Thread"};
                        }
                        case "timebetweenhocamounts" -> {
                            header = header + "Time between " + data.args[1]+" and "+data.args[2]+" for "+data.flags.username+" between "+ data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.timeBetweenTwoHoCAmounts(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid,Integer.parseInt(data.args[1]), Integer.parseInt(data.args[2]));
                            table_headers = new String[]{"Username", "Time"};
                        }
                        case "sumofcounts" -> {
                            header = header + "Sum of counts for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.getSumOfCounts(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"Username", "Sum"};
                        }
                        case "sumofreciprocals" -> {
                            header = header + "Sum of reciprocals for " + data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.getSumOfReciprocals(data.counting_threads, data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"Username", "Sum"};
                        }
                        case "uniquecountsmodn" -> {
                            header = header + "Unique counts mod " +data.args[1]+" for "+ data.flags.username + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                            countData1 = CombinedStats.unique_counts_mod_n(data.counting_threads, Integer.parseInt(data.args[1]), data.flags.start_uuid, data.flags.end_uuid);
                            table_headers = new String[]{"Username", "Unique Counts"};
                        }
                        case "speedtag" -> {
                            header = header + "Speed tags between " +data.args[1]+" users at position "+ data.flags.position + " between ks " + data.flags.start_k + " and " + data.flags.end_k;
                            countData2 = CombinedStats.overall_speed_tags(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.gets_per_k, data.flags.percentage, Integer.parseInt(data.args[1]));
                            table_headers = new String[Integer.parseInt(data.args[1])+5];
                            table_headers[0] = "k";
                            for(int i=1;i<=Integer.parseInt(data.args[1]);i++) {
                                table_headers[i] = "User " + i;
                            }
                            table_headers[table_headers.length-4] = "Time";
                            table_headers[table_headers.length-3] = "Thread";
                            table_headers[table_headers.length-2] = "Counts";
                            table_headers[table_headers.length-1] = "artbn?";
                        }
                        case "fastestsplits" -> {
                            header = header + "Fastest splits at position "+ data.flags.position + " between ks " + data.flags.start_k + " and " + data.flags.end_k;
                            countData2 = CombinedStats.fastestSplits(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.percentage);
                            table_headers = new String[]{"Time", "Start Count", "End Count", "Thread", "Users"};
                        }
                        case "fastestsplitsbyuser" -> {
                            header = header + "Fastest split for user "+ data.flags.username + " between ks " + data.flags.start_k + " and " + data.flags.end_k;
                            reversed = true;
                            countData1 = CombinedStats.fastestSplitsByUser(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.percentage);
                            table_headers = new String[]{"Username", "Time", "Start Count", "End Count", "Thread", "Other Users"};
                        }
                        case "fastestks" -> {
                            header = header + "Fastest "+data.flags.gets_per_k+"ks at position "+ data.flags.position + " between ks " + data.flags.start_k + " and " + data.flags.end_k;
                            countData2 = CombinedStats.fastestKs(data.counting_threads, data.flags.gets_per_k, data.flags.start_k, data.flags.end_k, data.flags.percentage);
                            table_headers = new String[]{"Time", "Start Count", "End Count", "Thread", "Users"};
                        }
                        case "fastestksbyuser" -> {
                            header = header + "Fastest "+data.flags.gets_per_k+"k for user "+ data.flags.username + " between ks " + data.flags.start_k + " and " + data.flags.end_k;
                            reversed = true;
                            countData1 = CombinedStats.fastestKsByUser(data.counting_threads, data.flags.gets_per_k, data.flags.start_k, data.flags.end_k, data.flags.percentage);
                            table_headers = new String[]{"Username", "Time", "Start Count", "End Count", "Thread", "Other Users"};
                        }
                        case "pairpb" -> {
                            header = header + "Fastest k for users "+ data.args[1]+" and " +data.flags.username + " between ks " + data.flags.start_k + " and " + data.flags.end_k;
                            if(data.flags.username.compareTo(data.args[1])>0) {
                                data.flags.username += "|"+data.args[1];
                            } else {
                                data.flags.username = data.args[1] +"|"+ data.flags.username;
                            }
                            reversed = true;
                            countData1 = CombinedStats.pair_pbs(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.percentage);
                            table_headers = new String[]{"User1", "User 2", "Time", "Thread", "k", "Counts"};
                        }
                        case "splitstddev" -> {
                            header = header + "Split standard deviation at position "+ data.flags.position + " between ks " + data.flags.start_k + " and " + data.flags.end_k;
                            countData2 = CombinedStats.speedMinSplitStdDev(data.counting_threads, data.flags.start_k, data.flags.end_k, Float.POSITIVE_INFINITY, data.flags.percentage);
                            table_headers = new String[]{"Std Dev", "Split #", "Time", "Thread", "Users"};
                        }
                        case "kstreak" -> {
                            header = header + (data.flags.is_current?"Current ":"Record ")+data.flags.gets_per_k+"k streak for "+ data.flags.username + " between ks " + data.flags.start_k + " and " + data.flags.end_k;
                            countData1 = CombinedStats.overall_k_streak(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.gets_per_k, data.flags.is_current);
                            table_headers = new String[]{"Username", "Streak", "Thread", "Start "+data.flags.gets_per_k+"k", "End "+data.flags.gets_per_k+"k"};
                        }
                        case "nonuniquekstreak" -> {
                            header = header +data.flags.gets_per_k+"k streaks at position "+ data.flags.position + " between ks " + data.flags.start_k + " and " + data.flags.end_k;
                            countData2 = CombinedStats.overall_non_unique_k_streak(data.counting_threads, data.flags.start_k, data.flags.end_k, data.flags.gets_per_k);
                            table_headers = new String[]{"Username", "Streak", "Thread", "Start "+data.flags.gets_per_k+"k", "End "+data.flags.gets_per_k+"k"};
                        }
                        case "nonuniquedailynthplacestreak","nonuniquemonthlynthplacestreak","nonuniqueyearlynthplacestreak" -> {
                            ParticipationDuration mode = switch (data.func_name) {
                                case "nonuniquedailynthplacestreak" -> ParticipationDuration.DAY;
                                case "nonuniquemonthlynthplacestreak" -> ParticipationDuration.MONTH;
                                case "nonuniqueyearlynthplacestreak" -> ParticipationDuration.YEAR;
                                default -> null;
                            };
                            header = header + mode.toOtherCapitalizedString()+" "+getOrdinal(Integer.parseInt(data.args[1]))+" place streak at position "+ data.flags.position + " between " + data.flags.getStartDate(mode) + " and " + data.flags.getEndDate(mode);
                            countData2 = CombinedStats.overall_non_unique_nth_place_streak(data.counting_threads, Integer.parseInt(data.args[1]), data.flags.getStartDate(mode), data.flags.getEndDate(mode), mode);
                            table_headers = new String[]{"Username", "Streak", "Thread", "Start Date", "End Date"};
                        }
                        case "about" -> {
                            if(data.counting_threads.size() != 1) {
                                output = "This command only works when one thread is selected";
                            } else {
                                output = "idk what to put here";
                            }
                        }
                        case "k" -> {
                            if(data.counting_threads.size() != 1) {
                                output = "This command only works when one thread is selected";
                            } else {
                                header = header + data.flags.gets_per_k+"k HoC for "+ data.flags.username + " at k " + data.args[1];
                                if(Integer.parseInt(data.args[1]) < 0 || Integer.parseInt(data.args[1]) > data.counting_threads.get(0).getCurrentK(data.flags.gets_per_k)) {
                                    output = "k must be greater than 0 and <= "+data.counting_threads.get(0).getCurrentK(data.flags.gets_per_k);
                                } else {
                                    StatLoader.K_HOC val = data.counting_threads.get(0).KHoc(Integer.parseInt(data.args[1]) - 1, data.flags.gets_per_k);
                                    countData1 = val.hoc();
                                }
                                table_headers = new String[]{"Username", "Counts"};
                            }
                        }
                        case "sumofbest","sob" -> {
                            if(data.counting_threads.size() != 1) {
                                output = "This command only works when one thread is selected";
                            } else {
                                header = header + "Sum of Best for "+ data.flags.username + " between ks " + data.flags.start_k+" and "+data.flags.end_k;
                                Map<String, StatLoader.SumOfBestOutput> sob = data.counting_threads.get(0).sumOfBest(data.flags.start_k, data.flags.end_k, data.flags.percentage);
                                StatLoader.SumOfBestOutput sob_user;
                                if((sob_user = sob.get(data.flags.username)) == null) {
                                    output = "User currently has no sum of best";
                                } else {
                                    output = header+"\n\n"+TableConstructor.constructSplitsTable(new StatLoader.SplitsOutput(0, sob_user.splits(),null,null));
                                }
                            }
                        }
                        case "splits" -> {
                            if(data.counting_threads.size() != 1) {
                                output = "This command only works when one thread is selected";
                            } else {
                                header = header + "Splits for k " + data.args[1];
                                if(Integer.parseInt(data.args[1]) <= 0 || Integer.parseInt(data.args[1]) > data.counting_threads.get(0).getCurrentK(1)) {
                                    output = "k must be greater than 0 and <= "+data.counting_threads.get(0).getCurrentK(1);
                                } else {
                                    data.flags.start_k = Integer.parseInt(data.args[1])-1;
                                    List<StatLoader.SplitsOutput> sob = data.counting_threads.get(0).getSplits(data.flags.start_k, data.flags.start_k+1);
                                    output = header+"\n\n"+TableConstructor.constructSplitsTable(sob.get(0));
                                }
                            }
                        }
                        case "speed" -> {
                            if(data.counting_threads.size() != 1) {
                                output = "This command only works when one thread is selected";
                            } else {
                                header = header + "The speed for "+data.flags.gets_per_k+"k " + data.args[1]+" was ";
                                if(Integer.parseInt(data.args[1]) <= 0 || Integer.parseInt(data.args[1]) > data.counting_threads.get(0).getCurrentK(data.flags.gets_per_k)) {
                                    output = "k must be greater than 0 and <= "+data.counting_threads.get(0).getCurrentK(data.flags.gets_per_k);
                                } else {
                                    StatLoader.K_HOC val = data.counting_threads.get(0).KHoc(Integer.parseInt(data.args[1]) - 1, data.flags.gets_per_k);
                                    output = header + " "+new TimeOutput(val.time());
                                }
                            }
                        }
                        case "streakswithoutvalidcount" -> {
                            if(data.counting_threads.size() != 1 || !data.counting_threads.get(0).getName().equals("1inx")) {
                                output = "This command only works when 1/x is selected";
                            } else {
                                header = header + "Streak without a valid counts at position " + data.flags.position + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                                countData2 = ((OneInX) data.counting_threads.get(0)).record_streaks_without_valid_count(data.flags.start_uuid, data.flags.end_uuid);
                                table_headers = new String[]{"Username", "Streak", "Start Count", "End Count"};
                            }
                        }
                        case "peakcount" -> {
                            if(data.counting_threads.size() != 1 || !(data.counting_threads.get(0) instanceof NoMistakesThread)) {
                                output = "This command only works when a no mistakes thread is selected";
                            } else {
                                header = header + "Peak count reached at position " + data.flags.position + " between " + data.flags.start_uuid + " and " + data.flags.end_uuid;
                                countData2 = ((NoMistakesThread) data.counting_threads.get(0)).peak_no_mistakes_counts(data.flags.start_uuid, data.flags.end_uuid);
                                table_headers = new String[]{"Count Reached", "Count Number"};
                            }
                        }
                        case "update" -> {
                            if (data.counting_threads.size() != 1) {
                                output = "This command only works when one thread is selected";
                            } else if(Integer.parseInt(data.args[1]) < 0 || Integer.parseInt(data.args[1]) >= data.counting_threads.get(0).getTotal_updates()) {
                                output = "Update must be >= 0 and < "+data.counting_threads.get(0).getTotal_updates();
                            } else {
                                output = header + "Update " + data.args[1] + ":\n\n";
                                List<String> urls = new LinkedList<>();
                                UpdateValue update = data.counting_threads.get(0).getUpdates(Integer.parseInt(data.args[1]));
                                urls.add(data.counting_threads.get(0).getURLFromUpdateValue(update));
                                List<String> update_data = new LinkedList<>();
                                update_data.add(update.getAuthor() + "|" +update.getRawText().replace("\n", " "));
                                output += TableConstructor.constructRedditTableWithURLsFromList(update_data, new String[]{"Username", "Body"}, urls);
                            }
                        }
                        case "count","number" -> {
                            if (data.counting_threads.size() != 1) {
                                output = "This command only works when one thread is selected";
                            } else if(Integer.parseInt(data.args[1]) <= 0 || Integer.parseInt(data.args[1]) > data.counting_threads.get(0).getTotal_counts()) {
                                output = "Count must be > 0 and <= "+data.counting_threads.get(0).getTotal_counts();
                            } else {
                                output = header + "Count " + data.args[1] + ":\n\n";
                                List<String> urls = new LinkedList<>();
                                CountValue update = data.counting_threads.get(0).getCountsTimeSort(Integer.parseInt(data.args[1])-1);
                                urls.add(data.counting_threads.get(0).getURLFromCountValue(update));
                                List<String> update_data = new LinkedList<>();
                                update_data.add(update.getAuthor() + "|" +update.getRawText().replace("\n", " "));
                                output += TableConstructor.constructRedditTableWithURLsFromList(update_data, new String[]{"Username", "Body"}, urls);
                            }
                        }
                        case "updatedb" -> {
                            if(Arrays.binarySearch(admins, data.flags.username) < 0) output = "User does not have privileges to run this command";
                            else {
                                for (Statable thread : data.counting_threads) {
                                    thread.updateDatabase();
                                }
                                output = header + "\n\nAll above threads successfully updated";
                            }
                        }
                        default -> {
                            output = "That command does not exist";
                        }
                    }
                } catch(NumberFormatException | IndexOutOfBoundsException | DateTimeParseException e) {
                    output = "Illegal arguments to "+data.func_name;
                } catch(UnsupportedOperationException e) {
                    output = "Method unsupported for the current thread";
                } catch(IllegalArgumentException e) {
                    output = e.getMessage();
                }
            }
            if(countData1!=null) {
                String table = TableConstructor.constructRedditTable(countData1, table_headers, reversed, data.flags.username, data.flags.context);
                if (table == null) {
                    output = "No counts were found for the given command";
                } else {
                    output = header + "\n\n" + table;
                }
            } else if(countData2!=null) {
                String table = TableConstructor.constructRedditTable(countData2, table_headers, reversed, data.flags.position, data.flags.context);
                if (table == null) {
                    output = "No counts were found for the given command";
                } else {
                    output = header + "\n\n" + table;
                }
            }
            CGGAuthenticator.postToAPI(output);
            //Bot.RedditAuthenticator.postToAPI("/api/live/"+ Bot.RedditAuthenticator.live_thread+"/update", "body="+output, 0);
        }
    }

}
