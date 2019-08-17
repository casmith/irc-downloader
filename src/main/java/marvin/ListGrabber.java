package marvin;

import marvin.irc.IrcBot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListGrabber {
    private final String listManagerFileName;
    private IrcBot ircBot;
    private ListManager listManager;
    public ListGrabber(IrcBot ircBot, String listManagerFileName) {
        this.ircBot = ircBot;
        this.listManagerFileName = listManagerFileName;
        this.listManager = initializeListManager(listManagerFileName);
    }

    private ListManager initializeListManager(String listManagerFileName) {
        ListManager aListManager = loadListManager(listManagerFileName);
        if (listManager == null) {
            aListManager = new ListManager();
        }
        return aListManager;
    }

    private ListManager loadListManager(String listManagerFileName) {
        if (listManagerFileName != null) {
            return ListManager.load(listManagerFileName);
        }
        return null;
    }

    public boolean check(String message) {
        Pattern pattern = getPattern();
        return pattern.matcher(message.toLowerCase()).matches();
    }

    private Pattern getPattern() {
        return Pattern.compile(".*type: (@.*) for my list.*");
    }

    public void grab(String channelName, String message) {
        Pattern pattern = getPattern();
        Matcher matcher = pattern.matcher(message.toLowerCase());
        if (!matcher.matches()) {
            return;
        }
        String listQuery = matcher.group(1);
        if (listManager.add(listQuery)) {
            saveListManager();
            ircBot.sendToChannel(channelName, listQuery);
        }
    }

    private void saveListManager() {
        if (this.listManagerFileName != null) {
            ListManager.save(listManager, listManagerFileName);
        }
    }

    public static void main(String[] args) {
        System.out.println(ListManager.load("list-manager.dat"));
    }
}
