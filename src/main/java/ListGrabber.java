import marvin.irc.IrcBot;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListGrabber {
    public static final Pattern PATTERN = Pattern.compile("Type: (@.*) for my list");
    private IrcBot ircBot;
    private Set<String> lists = new HashSet<>();

    public ListGrabber(IrcBot ircBot) {
        this.ircBot = ircBot;
    }

    public boolean check(String message) {
        return PATTERN.matcher(message).matches();
    }

    public void grab(String channelName, String message) {
        Matcher matcher = PATTERN.matcher(message);
        String listQuery = matcher.group(1);
        if (lists.add(listQuery)) {
            ircBot.sendToChannel(channelName, listQuery);
        }
    }
}
