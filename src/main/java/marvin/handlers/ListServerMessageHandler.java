package marvin.handlers;

import marvin.ListServer;
import marvin.irc.MessageHandler;

public class ListServerMessageHandler implements MessageHandler {

    private ListServer listServer;

    public ListServerMessageHandler(ListServer listServer) {
        this.listServer = listServer;
    }

    @Override
    public void onMessage(String channelName, String nick, String message, String hostmask) {
        if (listServer.check(channelName, message)) {
            listServer.send(nick);
        }
    }
}
