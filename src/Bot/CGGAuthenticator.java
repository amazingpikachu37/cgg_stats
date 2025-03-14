package Bot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.client.SocketOptionBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class CGGAuthenticator {
    static ObjectMapper mapper = new ObjectMapper();
    static Socket socket = null;
    private static String readToken() {
        String token = "";
        try(FileReader fr = new FileReader("token.txt");
            BufferedReader br = new BufferedReader(fr)) {
            token = br.readLine();
        } catch (IOException ignored) {
            throw new RuntimeException("No token found");
        }
        return token;
    }
    @SuppressWarnings("unchecked")
    public static void connectToWebsocket() throws URISyntaxException, InterruptedException, IOException {
        CountDownLatch latch = new CountDownLatch(1);
        SocketOptionBuilder options = SocketOptionBuilder.builder();
        options.setSecure(true);
        options.setReconnection(true);
        options.setTransports(new String[]{"websocket","polling"});
        options.setExtraHeaders(Map.of("Cookie", List.of("connect.sid="+readToken())));
        socket = IO.socket("https://api.counting.gg", options.build());
        socket.on("post", args -> {
            Map<String, Object> map;
            try {
                map = new HashMap<>(mapper.readValue(args[0].toString(), new TypeReference<Map<String, Object>>() {}));
            } catch (IOException e) {
                return;
            }
            String to_send = null;
            try {
                Map<String, Object> update = ((Map<String, Object>) map.get("post"));
                String author = (String) ((Map<String, Object>)map.get("counter")).get("username");
                String id = (String) update.get("uuid");
                if(!Objects.equals(author,"pikabot")) {
                    to_send = UpdateParser.parse((String) update.get("rawText"), author, id);
                }
            } catch(Exception e) {
                e.printStackTrace();
                to_send = "Something went wrong";
            }
            if (to_send != null) {
                postToAPI(to_send);
            }
        });
        socket.on("disconnect", args -> {
            System.out.println("Reconnecting "+System.currentTimeMillis());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            latch.countDown();
        });
        socket.connect();
        socket.emit("watch","test");
        latch.await();
    }
    public static void postToAPI(String body) {
        try {
            JSONObject json = new JSONObject();
            json.put("thread_name", "test");
            json.put("text", body);
            json.put("post_hash", Long.toString((long) (Math.random() * 100000000000000000L), 36));
            json.put("refScroll", new String[0]);
            json.put("postScroll",new String[0]);
            json.put("latency", false);
            socket.emit("post", json);
        } catch (JSONException e) {
            throw new RuntimeException();
        }
    }
}
