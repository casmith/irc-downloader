import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class QueueProcessorListener extends ListenerAdapter {

    private String channel;
    private String listPath;

    public QueueProcessorListener(String channel, String listPath) {
        this.channel = channel;
        this.listPath = listPath;
    }

    @Override
    public void onJoin(JoinEvent event) throws Exception {
        PircBotX bot = event.getBot();
        String serverNick = bot.getNick();
        if (event.getUser() != null) {
            String nick = event.getUser().getNick();
            String channel = event.getChannel().getName().toLowerCase();
            if (serverNick.equals(nick) && channel.equals(this.channel)) {
                processQueue(bot);
            }
        }
    }

    private void processQueue(PircBotX bot) {
        DownloadQueue downloadQueue = new DownloadQueue();

        try {
            Files.readAllLines(Paths.get(this.listPath)).forEach(downloadQueue::enqueue);
        } catch (IOException e) {
            e.printStackTrace();
        }

        QueueProcessor queueProcessor = new QueueProcessor(downloadQueue, bot, 10, this.channel);
        queueProcessor.run();
    }

}

