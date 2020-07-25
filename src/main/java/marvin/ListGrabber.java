package marvin;

import marvin.data.DatabaseException;
import marvin.data.KnownUserDao;
import marvin.irc.IrcBot;
import marvin.model.KnownUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListGrabber {

    private static Logger LOG = LoggerFactory.getLogger(ListGrabber.class);

    private final String listManagerFileName;
    private final KnownUserDao knownUserDao;
    private final boolean isEnabled;
    private IrcBot ircBot;
    private ListManager listManager;
    public ListGrabber(IrcBot ircBot, KnownUserDao knownUserDao, String listManagerFileName, boolean isEnabled) {
        this.isEnabled = isEnabled;
        this.ircBot = ircBot;
        this.listManagerFileName = listManagerFileName;
        this.listManager = initializeListManager(listManagerFileName);
        this.knownUserDao = knownUserDao;
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

    public boolean grab(String channelName, String message) {
        if (!isEnabled) {
            return false;
        }
        Pattern pattern = getPattern();
        Matcher matcher = pattern.matcher(message.toLowerCase());
        if (!matcher.matches()) {
            return false;
        }
        String listQuery = matcher.group(1);
        if (listManager.add(listQuery)) {
            saveListManager();
            ircBot.sendToChannel(channelName, listQuery);
            return true;
        }
        return false;
    }

    private void saveListManager() {
        if (this.listManagerFileName != null) {
            ListManager.save(listManager, listManagerFileName);
        }
    }

    public static void main(String[] args) {
        System.out.println(ListManager.load("list-manager.dat"));
    }

    public void updateLastSeen(String nick, String message, String hostmask) {
        Pattern pattern = getPattern();
        Matcher matcher = pattern.matcher(message.toLowerCase());
        if (!matcher.matches()) {
            return;
        }
        String name = matcher.group(1);
        try {
            knownUserDao.insert(new KnownUser(name, nick, hostmask, LocalDateTime.now()));
        } catch (DatabaseException e) {
            LOG.error("Failed to update last known", e);
        }
    }
}
