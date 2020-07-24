package marvin.irc;

import marvin.config.BotConfig;
import marvin.irc.events.DownloadCompleteEvent;
import marvin.irc.events.Event;
import marvin.irc.events.EventSource;
import marvin.irc.events.Listener;
import org.pircbotx.*;
import org.pircbotx.delay.StaticDelay;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class IrcBotImpl implements IrcBot {

    private static final Logger LOG = LoggerFactory.getLogger(IrcBotImpl.class);
    private final String adminPassword;
    private final String requestChannel;
    private final String controlChannel;
    private final Configuration configuration;
    private final EventSource eventSource = new EventSource();
    private final List<MessageHandler> messageHandlers = new ArrayList<>();
    private final List<PrivateMessageHandler> privateMessageHandlers = new ArrayList<>();
    private final List<NoticeHandler> noticeHandlers = new ArrayList<>();
    private final boolean useIdent = true;

    private PircBotX bot;

    @Inject
    public IrcBotImpl(BotConfig config, QueueManager queueManager) {
        this(config.getServer(),
                config.getPort(),
                config.getNick(),
                config.getPassword(),
                config.getAdminPassword(),
                config.getControlChannel(),
                config.getRequestChannel(),
                config.getDownloadDirectory(),
                queueManager);
    }

    public IrcBotImpl(String server,
                      int port,
                      String nick,
                      String password,
                      String adminPassword,
                      String controlChannel,
                      String requestChannel,
                      String downloadDirectory,
                      QueueManager queueManager) {
        this.adminPassword = adminPassword;
        this.requestChannel = requestChannel;
        this.controlChannel = controlChannel;
        configuration = new Configuration.Builder()
                .addServer(server, port)
                .setName(nick)
                .setRealName(nick)
                .setLogin(nick)
                .setServerPassword(password)
                .addAutoJoinChannel(controlChannel)
                .addAutoJoinChannel(requestChannel)
                .setIdentServerEnabled(useIdent)
                .setAutoReconnectAttempts(10)
                .setAutoReconnectDelay(new StaticDelay(5000))
                .setAutoReconnect(true)
                .addListener(new QueueProcessorListener(requestChannel, "queue.txt"))
                .addListener(new IncomingFileTransferListener(eventSource, downloadDirectory))
                .addListener(new ListenerAdapter() {
                    @Override
                    public void onPrivateMessage(PrivateMessageEvent event) {
                        IrcBotImpl.this.onPrivateMessage(event);
                    }

                    @Override
                    public void onNotice(NoticeEvent event) throws Exception {
                        super.onNotice(event);
                        String message = Colors.removeColors(event.getMessage());
                        String nick = getNick(event.getUser());
                        noticeHandlers.forEach(handler -> handler.onNotice(nick, message));
                    }

                    @Override
                    public void onMessage(MessageEvent event) throws Exception {
                        super.onMessage(event);
                        String nick = getNick(event.getUser());
                        String channelName = event.getChannel().getName();
                        String message = Colors.removeColors(event.getMessage());
                        final UserHostmask userHostmask = event.getUserHostmask();
                        final String hostmask = userHostmask.getHostmask();
                        messageHandlers.forEach(handler -> handler.onMessage(channelName, nick, message, hostmask));
                    }
                })
                .buildConfiguration();

        eventSource.subscribe(event -> {
            try {
                if (event instanceof DownloadCompleteEvent) {
                    DownloadCompleteEvent dce = (DownloadCompleteEvent) event;
                    LOG.info("Download completed [{}]", dce.toString());
                    queueManager.dec(dce.getNick());
                    if (!dce.isSuccess()) {
                        messageControlChannel("Download {0} from {1} failed", dce.getFileName(), dce.getNick());
                        queueManager.retry(dce.getNick(), dce.getFileName());
                    }
                }
            } catch (Exception e) {
                LOG.debug("Exception occurred {}", e.getMessage(), e);
            }
        });
    }

    public void on(Class<? extends Event> eventClass, Listener listener) {
        eventSource.subscribe(event -> {
            if (event.getClass().isAssignableFrom(eventClass)) {
                listener.notify(event);
            }
        });
    }

    public String getNick() {
        return bot.getNick();
    }

    private String getNick(User user) {
        String nick = null;
        if (user != null) {
            nick = user.getNick();
        }
        return nick;
    }

    private void onPrivateMessage(PrivateMessageEvent event) {
        String nick = getNick(event.getUser());
        String message = event.getMessage();
        LOG.debug("{}: {}", nick, maskPassword(message));
        privateMessageHandlers.forEach(handler -> handler.onMessage(nick, message));
    }

    private String maskPassword(String message) {
        return message.replace(adminPassword, "********");
    }

    @Override
    public void start() {
        bot = new PircBotX(configuration);
        startIdentServer();
        startIrcBot();
    }

    public void messageChannel(String message, Object... args) {
        bot.send().message(requestChannel, formatMessage(message, args));
    }

    public void messageControlChannel(String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        bot.send().message(controlChannel, formattedMessage);
    }

    public String formatMessage(String message, Object... args) {
        return MessageFormat.format(message, args);
    }

    private void startIrcBot() {
        try {
            bot.startBot();
        } catch (IOException | IrcException e) {
            throw new IrcBotException("Failed to start IRC bot", e);
        }
    }

    private void startIdentServer() {
        try {
            IdentServer.startServer();
        } catch (Exception e) {
            throw new IrcBotException("Failed to start ident server", e);
        }
    }

    @Override
    public void shutdown() {
        try {
            IdentServer.stopServer();
            bot.stopBotReconnect();
            bot.send().quitServer();
        } catch (IOException e) {
            throw new IrcBotException("Failed to stop ident server", e);
        }
    }

    public void registerMessageHandler(MessageHandler handler) {
        this.messageHandlers.add(handler);
    }

    public void registerPrivateMessageHandler(PrivateMessageHandler handler) {
        this.privateMessageHandlers.add(handler);
    }

    public void registerNoticeHandler(NoticeHandler handler) {
        this.noticeHandlers.add(handler);
    }

    @Override
    public void sendToChannel(String channel, String message) {
        bot.send().message(channel, message);
    }

    @Override
    public void sendPrivateMessage(String recipient, String message) {
        bot.send().message(recipient, message);
    }

    public EventSource getEventSource() {
        return eventSource;
    }

    @Override
    public void sendFile(String nick, File file) {
        User user = bot.getUserChannelDao().getUser(nick);
        LOG.debug(user.toString());
        LOG.debug(file.toString());
        try {
            user.send().dccFile(file).transfer();
        } catch (IOException | InterruptedException e) {
            LOG.error("Failed to send file", e);
        }
    }
}
