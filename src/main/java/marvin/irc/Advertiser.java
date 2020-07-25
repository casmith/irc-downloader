package marvin.irc;

import marvin.list.ListGenerator;
import marvin.config.BotConfig;
import marvin.util.NumberUtils;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.google.common.base.Preconditions.checkArgument;

public class Advertiser {

    private IrcBot bot;
    private BotConfig config;
    private ListGenerator listGenerator;

    @Inject
    public Advertiser(IrcBot bot, BotConfig config, ListGenerator listGenerator) {
        this.bot = bot;
        this.config = config;
        this.listGenerator = listGenerator;
    }

    public void advertise() {
        bot.sendToChannel(this.config.getRequestChannel(), getAdvert(bot.getNick(), listGenerator));
    }

    public String getAdvert(String nick, ListGenerator listGenerator) {
        return MessageFormat.format("Type: {0} for my list of {1} files ({2} GiB) "
                        + "Updated: {3} == "
                        //                                "Free Slots: 0/10 == " +
                        //                                "Files in Que: 0 == " +
                        //                                "Total Speed: 0cps == " +
                        //                                "Next Open Slot: NOW == " +
                        //                                "Files served: 0 == " +
                        + "Using MarvinBot v0.01",
                nick,
                listGenerator.getCount(),
                NumberUtils.format(((double) listGenerator.getBytes()) / NumberUtils.GIGABYTE),
                formatDate(listGenerator.getGeneratedDateTime()));
    }

    private String formatDate(LocalDateTime localDate) {
        final String strDay = DateTimeFormatter.ofPattern("d").format(localDate);
        final int day = Integer.parseInt(strDay);
        final String suffix = getDayOfMonthSuffix(day);
        return DateTimeFormatter.ofPattern("MMM").format(localDate) + " " + strDay + suffix;
    }

    private String getDayOfMonthSuffix(final int n) {
        checkArgument(n >= 1 && n <= 31, "illegal day of month: " + n);
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }
}
