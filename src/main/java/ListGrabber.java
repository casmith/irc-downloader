import marvin.irc.IrcBot;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListGrabber {
    private IrcBot ircBot;
    private Set<String> lists = new HashSet<>();

    public ListGrabber(IrcBot ircBot) {
        this.ircBot = ircBot;
    }

    public boolean check(String message) {
        Pattern pattern = Pattern.compile("Type: (@.*) for my list");
        return pattern.matcher(message).matches();
    }

    public void grab(String channelName, String message) {
        Pattern pattern = Pattern.compile("Type: (@.*) for my list");
        Matcher matcher = pattern.matcher(message);
        if (!matcher.matches()) {
            System.out.println("Oh no");
            return;
        }
        String listQuery = matcher.group(1);
        if (lists.add(listQuery)) {
            ircBot.sendToChannel(channelName, listQuery);
        }
    }
}
