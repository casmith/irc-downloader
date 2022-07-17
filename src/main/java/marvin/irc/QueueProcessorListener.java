package marvin.irc;

import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.function.Consumer;

public class QueueProcessorListener extends ListenerAdapter {

    private final String channel;
    private final String fileName;

    public QueueProcessorListener(String channel, String fileName) {
        this.channel = channel;
        this.fileName = fileName;
    }

    @Override
    public void onJoin(JoinEvent event) throws Exception {
        if (joinerIsMe(event) && channelMatches(event)) {
            readQueueFile(line -> requestDownload(event, line));
        }
    }

    private boolean joinerIsMe(JoinEvent event) {
        String serverNick = event.getBot().getNick();
        User eventUser = event.getUser();
        return eventUser != null && serverNick.equals(eventUser.getNick());
    }

    private boolean channelMatches(JoinEvent event) {
        return event.getChannel().getName().toLowerCase().equals(this.channel);
    }

    private void readQueueFile(Consumer<String> consumer) throws Exception {
        BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
        String line = fileReader.readLine();
        while (line != null) {
            if (!lineIsComment(line)) {
                consumer.accept(line);
            }
            line = fileReader.readLine();
        }
    }

    private boolean lineIsComment(String line) {
        return line.startsWith("//");
    }

    private void requestDownload(JoinEvent event, String s) {
        System.out.println("Requesting " + s);
        event.getBot().sendIRC().message(this.channel, s);
        sleep(30);
    }

    private void sleep(int s) {
        try {
            Thread.sleep(s * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

