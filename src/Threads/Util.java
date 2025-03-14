package Threads;

import java.io.File;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Util {
    public static Logger setupLogger(Logger LOGGER, String filename, String name){
        LOGGER.setLevel(Level.ALL);
        try {
            FileHandler fhandler = new FileHandler(filename, true);
            MyFormatter mformatter = new MyFormatter(name);
            fhandler.setFormatter(mformatter);
            LOGGER.addHandler(fhandler);
            LOGGER.setUseParentHandlers(false);

        } catch (IOException | SecurityException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return LOGGER;
    }
    public static void createFileStructure(String threadID) {
        String[] subfiles = {"updates", "count_num", "count_time", "stats_data", "stats_output"};
        String path = "SidethreadData\\" + threadID;
        if(new File(path).mkdir())
            System.out.println(path + " was created");
        for(String directory: subfiles) {
            if(new File(path + "\\"+ directory).mkdir()) {
                System.out.println(path + "\\"+ directory + " was created");
            }
        }
    }
    public static long getTime(String uuid) {
        String parsedUUID = uuid.replaceAll("-","");
        if(parsedUUID.length()!=32) throw new IllegalArgumentException();
        return Long.parseLong(parsedUUID.substring(0,12)+parsedUUID.substring(13,16)+parsedUUID.substring(17,19));
    }
    public static String escapeJava(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if(c >= 32 && c < 34 || c >= 35 && c < 92 || c >= 93 && c < 127) {
                sb.append(c);
            } else {
                switch (c) {
                    case '\n' -> sb.append("\\n");
                    case '"' -> sb.append("\\\"");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    case '\\' -> sb.append("\\\\");
                    case '\f' -> sb.append("\\f");
                    case '\b' -> sb.append("\\b");
                    case '\u0000' -> sb.append("\\u0000");
                    case '\u0001' -> sb.append("\\u0001");
                    case '\u0002' -> sb.append("\\u0002");
                    case '\u0003' -> sb.append("\\u0003");
                    case '\u0004' -> sb.append("\\u0004");
                    case '\u0005' -> sb.append("\\u0005");
                    case '\u0006' -> sb.append("\\u0006");
                    case '\u0007' -> sb.append("\\u0007");
                    case '\u000b' -> sb.append("\\u000b");
                    case '\u000e' -> sb.append("\\u000e");
                    case '\u000f' -> sb.append("\\u000f");
                    case '\u0010' -> sb.append("\\u0010");
                    case '\u0011' -> sb.append("\\u0011");
                    case '\u0012' -> sb.append("\\u0012");
                    case '\u0013' -> sb.append("\\u0013");
                    case '\u0014' -> sb.append("\\u0014");
                    case '\u0015' -> sb.append("\\u0015");
                    case '\u0016' -> sb.append("\\u0016");
                    case '\u0017' -> sb.append("\\u0017");
                    case '\u0018' -> sb.append("\\u0018");
                    case '\u0019' -> sb.append("\\u0019");
                    case '\u001a' -> sb.append("\\u001a");
                    case '\u001b' -> sb.append("\\u001b");
                    case '\u001c' -> sb.append("\\u001c");
                    case '\u001d' -> sb.append("\\u001d");
                    case '\u001e' -> sb.append("\\u001e");
                    case '\u001f' -> sb.append("\\u001f");
                    default -> sb.append(c);
                }
            }
        }
        return sb.toString();
    }
    public static UUID getUUIDFromDate(String date, String timezone) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-M-d");
        LocalDateTime date2 = LocalDate.parse(date, dtf).atStartOfDay();
        ZoneOffset offset = ZoneId.of(timezone).getRules().getOffset(date2);
        Instant time = date2.toInstant(offset);
        long timestamp = time.getEpochSecond();
        String format = String.format("%08d",timestamp);
        return new UUID(format.substring(0,8)+"-0000-4000-a000-000000000000");
    }
    public static UUID getUUIDFromDate(TimeUnit date, String timezone) {
        LocalDateTime date2 = date.atDay(1).atStartOfDay();
        ZoneOffset offset = ZoneId.of(timezone).getRules().getOffset(date2);
        Instant time = date2.toInstant(offset);
        long timestamp = time.getEpochSecond();
        String format = String.format("%08d",timestamp);
        return new UUID(format.substring(0,8)+"-0000-4000-a000-000000000000");
    }
    public static UUID roundUUIDToLowerHour(UUID uuid) {
        long timestamp = uuid.getTime();
        timestamp = timestamp - timestamp%36000000000L;
        String format = String.format("%08d",timestamp);
        return new UUID(format.substring(0,8)+"-0000-4000-a000-000000000000");
    }
    public static UUID roundUUIDToLowerMinute(UUID uuid) {
        long timestamp = uuid.getTime();
        timestamp = timestamp - timestamp%600000000L;
        String format = String.format("%010d",timestamp);
        return new UUID(format.substring(0,8)+"-"+format.substring(8,10)+"00-4000-a000-000000000000");
    }
    public static int sumIntMapValues(Map<?,Integer> integerMap) {
        int sum=0;
        for(int num:integerMap.values()) {
            sum+=num;
        }
        return sum;
    }
    private static int partition(List<Long> arr, int l, int r) {
        long lst = arr.get(r);
        int i = l, j = l;
        while (j < r) {
            if (arr.get(j) - lst < 0) {
                long temp = arr.get(j);
                arr.set(j, arr.get(i));
                arr.set(i,temp);
                i++;
            }
            j++;
        }
        long temp = arr.get(i);
        arr.set(i, arr.get(r));
        arr.set(r,temp);
        return i;
    }
    private static int randomPartition(List<Long> arr, int l, int r) {
        int n = r - l + 1;
        int pivot = (int) (Math.random() % n);
        long temp = arr.get(l + pivot);
        arr.set(l + pivot, arr.get(r));
        arr.set(r,temp);
        return Util.partition(arr, l, r);
    }
    private static int medianUtil(List<Long> arr, int l, int r, int k, long[] a_b) {
        if (l <= r) {
            int partitionIndex = randomPartition(arr, l, r);
            if (partitionIndex == k) {
                a_b[1] = arr.get(partitionIndex);
                if (a_b[0] != -1)
                    return Integer.MIN_VALUE;
            } else if (partitionIndex == k - 1) {
                a_b[0] = arr.get(partitionIndex);
                if (a_b[1] != -1)
                    return Integer.MIN_VALUE;
            }
            if (partitionIndex >= k)
                return medianUtil(arr, l, partitionIndex - 1, k, a_b);
            else
                return medianUtil(arr, partitionIndex + 1, r, k, a_b);
        }
        return Integer.MIN_VALUE;
    }
    public static long findMedian(List<Long> arr) {
        long ans;
        long[] a_b = new long[]{-1,-1};
        if(arr.size()==1) return arr.get(0);
        if (arr.size() % 2 == 1) {
            Util.medianUtil(arr, 0, arr.size() - 1, arr.size() / 2, a_b);
            ans = a_b[1];
        } else {
            Util.medianUtil(arr, 0, arr.size() - 1, arr.size() / 2, a_b);
            ans = (a_b[0] + a_b[1]) / 2;
        }
        return ans;
    }
    public static double stdDev(List<TimeOutput> values) {
        long sum = 0;
        for(TimeOutput val:values) {
            sum+=val.time;
        }
        double mean = ((float)sum)/values.size();
        double stddev=0;
        for(TimeOutput val:values) {
            double diff = val.time-mean;
            stddev+=diff*diff;
        }
        stddev=Math.sqrt(stddev/values.size());
        return stddev;
    }

    public static Map<String, StatLoader.Participation> addParticipationMaps(List<Map<String, StatLoader.Participation>> maps) {
        Map<String, StatLoader.Participation> result = new HashMap<>();
        for(Map<String, StatLoader.Participation> map:maps) {
            for(String author: map.keySet()) {
                if(result.containsKey(author)) {
                    result.put(author, map.get(author).addParticipation(result.get(author)));
                } else {
                    result.put(author, map.get(author));
                }
            }
        }
        for(String s:result.keySet()) {
            StatLoader.Participation p = result.get(s);
            result.put(s, new StatLoader.Participation(p.hoc(), p.gets(), p.assists(), p.k_parts(), p.day_parts()));
        }
        return result;
    }
    public static Map<String, StatLoader.Medal> addMedalMaps(List<Map<String, StatLoader.Medal>> maps) {
        Map<String, StatLoader.Medal> result = new HashMap<>();
        for(Map<String, StatLoader.Medal> map:maps) {
            for(String author: map.keySet()) {
                if(result.containsKey(author)) {
                    result.put(author, result.get(author).add(map.get(author)));
                } else {
                    result.put(author, map.get(author));
                }
            }
        }
        return result;
    }
    public static <E extends Comparable> Map<String, E> optimizeMaps(List<Map<String, E>> maps, boolean first) {
        Map<String, E> result = new HashMap<>();
        for(Map<String, E> map:maps) {
            for(String author: map.keySet()) {
                if(result.containsKey(author)) {
                    E best = result.get(author);
                    E current = map.get(author);
                    if(best.compareTo(current)<0 && first || best.compareTo(current)>0 && !first) {
                        result.put(author, current);
                    }
                } else {
                    result.put(author, map.get(author));
                }
            }
        }
        return result;
    }
    public static List<StatOutput> optimizeCollection(List<CollectionContainer<String, StatOutput>> maps, Comparator<StatOutput> comparator, boolean first) {
        if(maps.size()==0) return new ArrayList<>();
        CollectionContainer<String, StatOutput> result = CollectionFactory.createCollection(!maps.get(0).isList());
        for(CollectionContainer<String, StatOutput> map:maps) {
            if(map.isList()) {
                result.addAll(map.toList());
            } else {
                for (String author : map.keySet()) {
                    if (result.containsKey(author)) {
                        StatOutput best = result.get(author);
                        StatOutput current = map.get(author);
                        if (first && comparator.compare(current, best) < 0 || !first && comparator.compare(current, best) > 0) {
                            result.add(author, current);
                        }
                    } else {
                        result.add(author, map.get(author));
                    }
                }
            }
        }
        return result.toList();
    }
    public static Map<String, TimeOutput> addTimeOutputMaps(List<Map<String, TimeOutput>> maps) {
        Map<String, TimeOutput> result = new HashMap<>();
        for(Map<String, TimeOutput> map:maps) {
            for(String author: map.keySet()) {
                if(result.containsKey(author)) {
                    result.put(author, new TimeOutput(result.get(author).time+map.get(author).time));
                } else {
                    result.put(author, map.get(author));
                }
            }
        }
        return result;
    }
    public static Map<String, Integer> addIntMaps(List<Map<String, Integer>> maps) {
        Map<String, Integer> result = new HashMap<>();
        for(Map<String, Integer> map:maps) {
            for(String author: map.keySet()) {
                if(result.containsKey(author)) {
                    result.put(author, result.get(author)+map.get(author));
                } else {
                    result.put(author, map.get(author));
                }
            }
        }
        return result;
    }
    public static Map<String, Long> addLongMaps(List<Map<String, Long>> maps) {
        Map<String, Long> result = new HashMap<>();
        for(Map<String, Long> map:maps) {
            for(String author: map.keySet()) {
                if(result.containsKey(author)) {
                    result.put(author, result.get(author)+map.get(author));
                } else {
                    result.put(author, map.get(author));
                }
            }
        }
        return result;
    }
    public static Map<String, Double> addDoubleMaps(List<Map<String, Double>> maps) {
        Map<String, Double> result = new HashMap<>();
        for(Map<String, Double> map:maps) {
            for(String author: map.keySet()) {
                if(result.containsKey(author)) {
                    result.put(author, result.get(author)+map.get(author));
                } else {
                    result.put(author, map.get(author));
                }
            }
        }
        return result;
    }
    private static void bronKerbosch2(Map<String, Set<String>> graph, Set<String> R, Set<String> P, Set<String> X, Map<String, Set<Set<String>>> result, int depth) {
        if (P.isEmpty() && X.isEmpty()) {
            int size = R.size();
            for (String vertex : R) {
                Set<Set<String>> current_result = result.getOrDefault(vertex,new HashSet<>());
                int best_size = 0;
                if(!current_result.isEmpty()) {
                    best_size = current_result.iterator().next().size();
                }
                if(size > best_size) {
                    current_result = new HashSet<>();
                    current_result.add(R);
                    result.put(vertex, current_result);
                } else if(size == best_size) {
                    current_result.add(R);
                }
            }
            return;
        }
        String pivot = P.isEmpty() ? X.iterator().next() : P.iterator().next();
        Set<String> adjacent = graph.get(pivot);
        Set<String> vertices = new HashSet<>(P);
        for (String vertex : vertices) {
            if (adjacent.contains(vertex)) {
                continue;
            }
            Set<String> newR = new HashSet<>(R);
            newR.add(vertex);
            Set<String> newP = new HashSet<>(P);
            newP.retainAll(graph.get(vertex));
            Set<String> newX = new HashSet<>(X);
            newX.retainAll(graph.get(vertex));
            bronKerbosch2(graph, newR, newP, newX, result, depth+1);
            P.remove(vertex);
            X.add(vertex);
        }
    }
    public static Map<String, Set<Set<String>>> bronKerbosch(Map<String, Set<String>> graph) {
        Set<String> P = new HashSet<>(graph.keySet());
        Set<String> X = new HashSet<>();
        Map<String, Set<Set<String>>> result = new HashMap<>();
        List<String> degeneracy = getDegeneracyOrdering(graph);
        degeneracy.sort(Comparator.reverseOrder());
        for(int i=degeneracy.size()-1;i>=0;i--) {
            String vertex = degeneracy.get(i);
            Set<String> newR = new HashSet<>();
            newR.add(vertex);
            Set<String> newP = new HashSet<>(P);
            newP.retainAll(graph.get(vertex));
            Set<String> newX = new HashSet<>(X);
            newX.retainAll(graph.get(vertex));
            bronKerbosch2(graph, newR, newP, newX, result, 1);
            P.remove(vertex);
            X.add(vertex);
        }
        return result;
    }
    private static List<String> getDegeneracyOrdering(Map<String, Set<String>> graph) {
        List<String> ordering = new ArrayList<>();
        int n = graph.size();
        int[] dv = new int[n];
        Map<String, Integer> vertexIndex = new HashMap<>();
        int i = 0;
        for (String vertex : graph.keySet()) {
            dv[i] = graph.get(vertex).size();
            vertexIndex.put(vertex, i);
            i++;
        }
        List<Integer>[] D = new ArrayList[n];
        for (int j = 0; j < n; j++) {
            D[j] = new ArrayList<>();
        }
        for (i = 0; i < n; i++) {
            D[dv[i]].add(i);
        }
        int k = 0;
        for (i = 0; i < n; i++) {
            while (D[i].size() > 0) {
                k = Math.max(k, i);
                int vIndex = D[i].get(0);
                D[i].remove(0);
                String v = "";
                for (String vertex : vertexIndex.keySet()) {
                    if (vertexIndex.get(vertex) == vIndex) {
                        v = vertex;
                        break;
                    }
                }
                ordering.add(0, v);
                for (String neighbor : graph.get(v)) {
                    int neighborIndex = vertexIndex.get(neighbor);
                    if (!ordering.contains(neighbor)) {
                        D[dv[neighborIndex]].remove(Integer.valueOf(neighborIndex));
                        dv[neighborIndex]--;
                        D[dv[neighborIndex]].add(neighborIndex);
                    }
                }
            }
        }
        return ordering;
    }


}
