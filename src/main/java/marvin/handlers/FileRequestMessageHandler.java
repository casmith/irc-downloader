package marvin.handlers;

import marvin.irc.IrcBot;
import marvin.irc.MessageHandler;
import marvin.irc.QueueManager;

import java.io.File;

import static java.text.MessageFormat.format;

public class FileRequestMessageHandler implements MessageHandler {

    private IrcBot bot;
    private String requestChannel;
    private File listRoot;
    private final QueueManager sendQueueManager;

    public FileRequestMessageHandler(IrcBot bot,
                                     QueueManager sendQueueManager,
                                     String requestChannel,
                                     File listRoot) {
        this.bot = bot;
        this.sendQueueManager = sendQueueManager;
        this.requestChannel = requestChannel;
        this.listRoot = listRoot;
    }

    @Override
    public void onMessage(String channelName, String nick, String message, String hostmask) {
        String botNick = this.bot.getNick();
        String requestPrefix = "!" + botNick;
        if (channelName.equals(this.requestChannel) && message.startsWith(requestPrefix)) {
            String filePath = message.replace(requestPrefix, "").trim();
            File file = new File(filePath);
            if (isWithinDownloadDirectory(file) && file.exists() && !file.isDirectory()) {
                bot.sendToChannel(this.requestChannel,
                        format("Accepted request from {0} for file {1}", nick, filePath));
                sendQueueManager.enqueue(nick, filePath);
            } else {
                bot.sendPrivateMessage(nick, file.getAbsolutePath() + " was not found");
            }
        }
    }

    private boolean isWithinDownloadDirectory(File file) {
        return file.getAbsolutePath().startsWith(listRoot.getAbsolutePath());
    }
}
