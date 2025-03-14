package Bot;

import java.io.IOException;
import java.net.URISyntaxException;

public class BotMain {
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        new Thread(() -> {
            try {
                while(true) {
                    CGGAuthenticator.connectToWebsocket();
                }
            } catch (InterruptedException | URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        Thread.sleep(1000);
        UpdateParser.runQueue();
    }





}
