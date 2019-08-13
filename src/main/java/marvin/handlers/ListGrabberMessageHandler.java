package marvin.handlers;

import marvin.ListGrabber;
import marvin.irc.MessageHandler;

public class ListGrabberMessageHandler implements MessageHandler {

    private ListGrabber listGrabber;

    public ListGrabberMessageHandler(ListGrabber listGrabber) {
        this.listGrabber = listGrabber;
    }

    @Override
    public void onMessage(String channelName, String nick, String message) {
        if (listGrabber.check(message)) {
            listGrabber.grab(channelName, message);
        }
    }
}
