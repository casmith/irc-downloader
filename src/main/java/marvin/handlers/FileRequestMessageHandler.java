package marvin.handlers;

import marvin.irc.IrcBot;
import marvin.irc.MessageHandler;

import java.io.File;

public class FileRequestMessageHandler implements MessageHandler {

    private IrcBot bot;
    private String requestChannel;
    private File listRoot;

    public FileRequestMessageHandler(IrcBot bot, String requestChannel, File listRoot) {
        this.bot = bot;
        this.requestChannel = requestChannel;
        this.listRoot = listRoot;
    }

    @Override
    public void onMessage(String channelName, String nick, String message) {
        String botNick = this.bot.getNick();
        String requestPrefix = "!" + botNick;
        if (channelName.equals(this.requestChannel) && message.startsWith(requestPrefix)) {
            String fileName = message.replace(requestPrefix, "").trim();
            File file = new File(fileName);
            if (isWithinDownloadDirectory(file) && file.exists() && !file.isDirectory()) {
                bot.sendToChannel(this.requestChannel, "Sending " + fileName + " to " + nick);
                bot.sendFile(nick, file);
            } else {
                bot.sendPrivateMessage(nick, file.getAbsolutePath() + " was not found");
            }
        }
    }

    private boolean isWithinDownloadDirectory(File file) {
        return file.getAbsolutePath().startsWith(listRoot.getAbsolutePath());
    }
}
