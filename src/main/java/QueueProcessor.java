import org.pircbotx.PircBotX;

public class QueueProcessor implements Runnable {

    private final PircBotX bot;
    private final String channel;
    private int quiet = 0;
    private DownloadQueue downloadQueue;
    private boolean previouslyEmpty = false;

    public QueueProcessor(DownloadQueue downloadQueue,
                          PircBotX bot,
                          int quietSeconds,
                          String channel) {
        this.bot = bot;
        this.quiet = quietSeconds;
        this.downloadQueue = downloadQueue;
        this.channel = channel;
    }

    @Override
    public void run() {

        while (true) {
            processQueue();
            sleep(quiet);
        }
    }

    public void processQueue() {
        FileRequest request = downloadQueue.request();
        if (request != null) {
            this.previouslyEmpty = false;
            if (bot != null) {
                bot.send().message(this.channel, request.toString());
            }
        } else {
            if (!this.previouslyEmpty) {
                System.out.println("Queue empty");
                this.previouslyEmpty = true;
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
