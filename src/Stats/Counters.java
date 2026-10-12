package Stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Counters {
    public static List<CounterData> counters;
    public static final Map<Integer, Integer> reverse_lookup = new HashMap<>();
    public static final Map<UUID, Integer> uuid_lookup = new HashMap<>();
    public static final Map<String, Integer> username_lookup = new HashMap<>();
    private static final Map<String, Long> money = new HashMap<>();
    static {
        readMoneyFile();
        List<CounterData> counters_temp = StatLoader.loadFromBytes("SidethreadData/Counters");
        if(counters_temp == null) {
            try {
                updateCounters();
            } catch (JsonProcessingException | MalformedURLException e) {
                throw new RuntimeException(e);
            }
        } else {
            counters = counters_temp;
            for(int i=0;i<counters.size();i++) {
                reverse_lookup.put(counters.get(i).id(), i);
                uuid_lookup.put(new UUID(counters.get(i).uuid()), i);
                username_lookup.put(counters.get(i).username(), i);
            }
        }
    }
    public static void updateCounters() throws JsonProcessingException, MalformedURLException {
        counters = new ArrayList<>();
        CounterData system = new CounterData(0,"[SYSTEM]",null, "[SYSTEM]","[SYSTEM]","#069420",List.of("bot"),null, null, null, List.of("it", "it", "its", "its"),null,"0","0",1,null,"\uD83E\uDD16");
        counters.add(system);
        ObjectMapper mapper = new ObjectMapper();
        String payload = "{\"uuid\": \"17192906-4250-4569-8230-530657569230\"}";
        URL u = new URL("https://api.counting.gg/api/counter/load_counter");
        JsonNode root = mapper.readTree(Util.apiPost(u, payload));
        CounterData pikabot = mapper.convertValue(root.get("loadedCounter"), new TypeReference<>() {});
        int page = 1;
        while(true) {
            u = new URL("https://api.counting.gg/api/counter/counters/" + page);
            root = mapper.readTree(Util.apiGet(u));
            List<CounterData> counter_list = mapper.convertValue(root.get("counters"), new TypeReference<>() {});
            if(counter_list.isEmpty()) break;
            counters.addAll(counter_list);
            page++;
        }
        counters.add(421,pikabot);
        for(int i=0;i<counters.size();i++) {
            reverse_lookup.put(counters.get(i).id(), i);
            uuid_lookup.put(new UUID(counters.get(i).uuid()), i);
            username_lookup.put(counters.get(i).username(), i);
        }
        StatLoader.saveToBytes(counters, "SidethreadData/Counters");
    }
    public static void addCounter(CounterData counter) {
        counters.add(counter);
        reverse_lookup.put(counter.id(), counters.size()-1);
        uuid_lookup.put(new UUID(counter.uuid()), counters.size()-1);
        username_lookup.put(counter.username(), counters.size()-1);
        StatLoader.saveToBytes(counters, "SidethreadData/Counters");
    }
    private static void readMoneyFile() {
        File f = new File("SidethreadData/MoneyData");
        try(Scanner in = new Scanner(f)) {
            while(in.hasNextLine()) {
                String[] map = in.nextLine().split(" ");
                if(map.length!=2) continue;
                money.put(map[0],Long.parseLong(map[1]));
            }
        } catch(FileNotFoundException e) {
            updateMoneyFile();
            readMoneyFile();
        }
    }
    private static void updateMoneyFile() {
        File f = new File("SidethreadData/MoneyData");
        try(PrintWriter out = new PrintWriter(f)) {
            for(Map.Entry<String, Long> entry: money.entrySet()) {
                out.println(entry.getKey()+" "+entry.getValue());
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    public static Long addMoney(String user, long amount) {
        if(username_lookup.containsKey(user)) {
            long current_money = money.getOrDefault(user, 0L)+amount;
            money.put(user, current_money);
            updateMoneyFile();
            return current_money;
        } else return null;
    }
}
