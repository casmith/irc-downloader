import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.JoinEvent;

import java.util.Queue;

public class QueueProcessorListener extends ListenerAdapter {

    private String channel;
    private DownloadQueueRepository queueRepository;

    public QueueProcessorListener(String channel, String fileName) {
        this.channel = channel;
        this.queueRepository = new FlatFileDownloadQueueRepository(fileName);
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
        FileRequest request = dequeue();
        while (request != null) {
            bot.send().message(this.channel, request.toString());
            sleep(30);
            request = dequeue();
        }
    }

    private FileRequest dequeue() {
        Queue<FileRequest> queue = queueRepository.get();
        FileRequest request = queue.poll();
        queueRepository.save(queue);
        return request;
    }

    private void sleep(int s) {
        try {
            Thread.sleep(s * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

