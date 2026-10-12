package Bot;

import Stats.CountingThread;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;


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
        new Thread(() -> {
            try {
                UpdateParser.runQueue();
            } catch (InterruptedException | IOException | URISyntaxException e) {
                throw new RuntimeException();
            }
        }).start();
        while(true) {
            dailyDBUpdate();
        }
    }

    public static void waitUntil(Instant target) throws InterruptedException {
        while (true) {
            Duration remaining = Duration.between(Instant.now(), target);
            if (remaining.isNegative() || remaining.isZero()) {
                return;
            }
            long millis = remaining.toMillis();
            int nanos = remaining.minusMillis(millis).getNano();
            Thread.sleep(millis, nanos);
        }
    }

    private static void dailyDBUpdate() throws InterruptedException {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(CountingThread.timezone));
        ZonedDateTime ET_Midnight = now.plusDays(1).minusHours(now.getHour()).minusMinutes(now.getMinute()).minusSeconds(now.getSecond()-10).minusNanos(now.getNano());
        waitUntil(ET_Midnight.toInstant());
        UpdateParser.parse("updateall", "amazingpikachu_37", Stats.Util.getUUIDFromCurrentTime().toString());
    }
}
