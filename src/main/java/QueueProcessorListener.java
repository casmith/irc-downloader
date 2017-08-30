import com.google.common.collect.ImmutableSortedSet;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;

import java.io.BufferedReader;
import java.io.FileReader;

public class QueueProcessorListener extends ListenerAdapter {

    private String channel;
    private String fileName;

    public QueueProcessorListener(String channel, String fileName) {
        this.channel = channel;
        this.fileName = fileName;
    }

    @Override
    public void onJoin(JoinEvent event) throws Exception {

        String serverNick = event.getBot().getNick();
        if (event.getUser() != null) {
            String nick = event.getUser().getNick();
            if (serverNick.equals(nick)) {
                ImmutableSortedSet<String> usersNicks = event.getChannel().getUsersNicks();
                usersNicks.forEach(System.out::println);

                if (event.getChannel().getName().toLowerCase().equals(this.channel)) {
                    BufferedReader fileReader = new BufferedReader(new FileReader(fileName));
                    String s = fileReader.readLine();
                    while (s != null) {
                        // skip commented-out lines
                        if (!s.startsWith("//")) {
                            System.out.println("Requesting " + s);
                            event.getBot().send().message(this.channel, s);
                            sleep(30);
                        }
                        s = fileReader.readLine();
                    }
                }
            }
        }
    }

    private void sleep(int s) {
        try {
            Thread.sleep(s * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

