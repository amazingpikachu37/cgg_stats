package Threads;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Counters {
    private static final Map<UUID, String> counters;
    private static final Map<String, Long> money = new HashMap<>();
    private static final Map<String, Color> colors;
    public static Map<UUID, String> getCounters() {
        return counters;
    }
    public static Map<String, Color> getColors() {
        return colors;
    }
    static {
        readMoneyFile();
        ObjectMapper mapper = new ObjectMapper();
        int page = 1;
        counters = new HashMap<>();
        colors = new HashMap<>();
        counters.put(new UUID("[SYSTEM]"),"[SYSTEM]");
        colors.put("[SYSTEM]",Color.decode("#000000"));
        counters.put(new UUID("17192906-4250-4569-8230-530657569230"),"pikabot");
        colors.put("pikabot",Color.decode("#ffff00"));
        while(true) {
            URL u = null;
            try {
                u = new URL("https://api.counting.gg/api/counter/counters/" + page);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) u.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Hi rideride {:D");
                int status = connection.getResponseCode();
                if (status > 299) {
                    System.out.println("Connection failed. Retrying");
                    java.lang.Thread.sleep(55000);
                    continue;
                }
                String text = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))
                        .lines()
                        .collect(Collectors.joining("\n"));
                ArrayList<LinkedHashMap<String, Object>> counterlist = ((Map<String,ArrayList<LinkedHashMap<String, Object>>>)mapper.readValue(text, Object.class)).get("counters");
                if(counterlist.size() ==0) {
                    break;
                }
                for(LinkedHashMap<String, Object> counter:counterlist) {
                    String color = (String)counter.get("color");
                    if(color.equals("")) color = "#000000";
                    counters.put(new UUID((String)counter.get("uuid")),(String)counter.get("username"));
                    colors.put((String)counter.get("username"),Color.decode(color));
                }
                page++;
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private static void readMoneyFile() {
        File f = new File("SidethreadData//MoneyData");
        try(Scanner in = new Scanner(f)) {
            while(in.hasNextLine()) {
                String[] map = in.nextLine().split(" ");
                money.put(map[0],Long.parseLong(map[1]));
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    private static void updateMoneyFile() {
        File f = new File("SidethreadData//MoneyData");
        try(PrintWriter out = new PrintWriter(f)) {
            for(Map.Entry<String, Long> entry: money.entrySet()) {
                out.println(entry.getKey()+" "+entry.getValue());
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static Long addMoney(String user, long amount) {
        if(counters.containsValue(user)) {
            long current_money = money.getOrDefault(user, 0L)+amount;
            money.put(user, current_money);
            updateMoneyFile();
            return current_money;
        } else return null;
    }
}
