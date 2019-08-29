package marvin.handlers;

import marvin.irc.IrcBot;
import marvin.irc.MessageHandler;

import java.io.File;

public class FileRequestMessageHandler implements MessageHandler {

    private IrcBot bot;
    private String requestChannel;

    public FileRequestMessageHandler(IrcBot bot, String requestChannel) {
        this.bot = bot;
        this.requestChannel = requestChannel;
    }

    @Override
    public void onMessage(String channelName, String nick, String message) {

        // request file
        String botNick = this.bot.getNick();
        String requestPrefix = "!" + botNick;
        if (channelName.equals(this.requestChannel) && message.startsWith(requestPrefix)) {
            String fileName = message.replace(requestPrefix, "").trim();
            bot.sendToChannel(this.requestChannel, "Sending " + fileName + " to " + nick);
            File file = new File(fileName);
            // TODO: ensure the file is w/in the music directory, or this could be super dangerous!
            if (file.exists() && !file.isDirectory()) {
                bot.sendFile(nick, file);
            }
        }
    }
}
