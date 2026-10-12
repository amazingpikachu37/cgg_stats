package Stats;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    public static String escapeJava(String str) {
        if(str==null) return "null";
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
    public static String toLowercaseAlphanumeric(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            if(c >= 'a' && c <= 'z' || c >= '0' && c <= '9') {
                sb.append(c);
            } else if(c >= 'A' && c <= 'Z') {
                sb.append((char)(c+32));
            }
        }
        return sb.toString();
    }
    public static UUID getUUIDFromDate(TimeUnit date, String timezone) {
        LocalDateTime date2 = date.atDay(1).atStartOfDay();
        ZoneOffset offset = ZoneId.of(timezone).getRules().getOffset(date2);
        Instant time = date2.toInstant(offset);
        long timestamp = time.getEpochSecond();
        String format = String.format("%08d",timestamp);
        return new UUID(format.substring(0,8)+"-0000-4000-a000-000000000000");
    }
    public static UUID getUUIDFromCurrentTime() {
        Instant time = Instant.now();
        long timestamp = time.getEpochSecond();
        int nano = time.getNano();
        String format = String.format("%010d%09d",timestamp,nano);
        return new UUID(format.substring(0,8)+"-"+format.substring(8,12)+"-4"+format.substring(12,15)+"-a"+format.substring(15,18)+"-"+format.charAt(18)+"00000000000");
    }
    public static long getNextDayTimestamp(long initial_timestamp, String timezone, ParticipationDuration mode) {
        TimeUnit time = new TimeUnit(initial_timestamp, timezone, mode);
        return time.addTime(1).timestamp(timezone);
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
    public static double stdDev(List<Long> values) {
        long sum = 0;
        for(long val:values) {
            sum += val;
        }
        double mean = ((float)sum)/values.size();
        double stddev=0;
        for(long val:values) {
            double diff = val -mean;
            stddev+=diff*diff;
        }
        stddev=Math.sqrt(stddev/values.size());
        return stddev;
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
    public static long factorial(long n) {
        long result = 1;
        while(n>0) {
            result *= n;
            n--;
        }
        return result;
    }
    public static long pow(long base, int exp) {
        if(base==-1) return (exp%2)==0 ? 1 : -1;
        if(exp==0 || base==1) return 1;
        if(base==0) return 0;
        long subtotal = 1;
        while(exp>1) {
            if(exp%2==1) {
                subtotal*=base;
                exp--;
            }
            base*=base;
            exp/=2;
        }
        return base*subtotal;
    }
    public static long nPr(int n, int r) {
        if(r>n || n<0 || r<0) return 0;
        long total=1;
        r=n-r;
        while(n>r) {
            total*=n;
            n--;
        }
        return total;
    }
    public static long nCr(int n, int r) {
        if(r>n || n<0 || r<0) return 0;
        long total=1;
        for(int i=r+1;i<=n;i++) {
            total*=i;
        }
        return total/factorial(n-r);
    }
    public static String toHyperlink(String visible_text, String link) {
        return "["+visible_text+"]("+link+")";
    }
    public static int sumIntList(List<Integer> to_sum) {
        int sum = 0;
        for(Integer i:to_sum) {
            sum += i;
        }
        return sum;
    }
    public static <T> void alwaysSet(List<T> a, T datum, int loc) {
        if(a.size() <= loc)
            a.add(datum);
        else
            a.set(loc, datum);
    }
    public static String apiPost(URL u, String payload) {
        try {
            HttpURLConnection connection = (HttpURLConnection) u.openConnection();
            String text = apiPostFromOpenConnection(connection, payload);
            connection.disconnect();
            return text;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String apiPostFromOpenConnection(HttpURLConnection connection, String payload) {
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Hi rideride {:D");
            OutputStream os = connection.getOutputStream();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
            pw.write(payload);
            pw.close();

            int status = connection.getResponseCode();
            if (status > 299) {
                System.out.println("Connection failed with status "+status+". Not retrying");
                throw new RuntimeException();
            }
            return new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))
                    .lines()
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String apiGet(URL u) {
        int max_attempts = 5;
        int attempts = 0;
        while(attempts < max_attempts) {
            try {
                HttpURLConnection connection = (HttpURLConnection) u.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Hi rideride {:D");
                int status = connection.getResponseCode();
                if (status > 299) {
                    System.out.println("Connection failed with status " + status + ". Retrying");
                    attempts++;
                    java.lang.Thread.sleep(55000);
                    continue;
                }
                String text = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))
                        .lines()
                        .collect(Collectors.joining("\n"));;
                connection.disconnect();
                return text;
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Connection failed. Not retrying.");
    }

    public static String findNthPermutationNoLeadingZero(char[] set, int k, int n) {
        n *= (int)Util.factorial(set.length-k+1);
        int factorial = (int)Util.factorial(set.length);
        int index = n/factorial;
        String newPrefix = String.valueOf(set[index]);
        String chars = String.valueOf(set);
        chars = "0"+chars.substring(0,index)+chars.substring(index+1);
        return findNthPermutation(chars.toCharArray(), newPrefix, k-1, n%factorial);
    }
    public static String findNthPermutation(char[] set, String prefix, int k, int n) {
        if(k==0) {
            return prefix;
        }
        int factorial = (int)Util.factorial(set.length-1);
        int index = n/factorial;
        String newPrefix = prefix+set[index];
        String chars = String.valueOf(set);
        chars = chars.substring(0,index)+chars.substring(index+1);
        return findNthPermutation(chars.toCharArray(), newPrefix, k-1, n%factorial);
    }
}
