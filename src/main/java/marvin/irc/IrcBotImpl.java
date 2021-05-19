package marvin.irc;

import com.google.common.collect.ImmutableSortedSet;
import marvin.config.BotConfig;
import marvin.data.QueueEntryDao;
import marvin.irc.events.DownloadCompleteEvent;
import marvin.irc.events.Event;
import marvin.irc.events.EventSource;
import marvin.irc.events.Listener;
import marvin.messaging.Producer;
import org.pircbotx.*;
import org.pircbotx.delay.StaticDelay;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
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
    private final boolean useIdent = false;

    private final List<String> channels = new ArrayList<>();

    private PircBotX bot;

    @Inject
    public IrcBotImpl(BotConfig config, ReceiveQueueManager queueManager, QueueEntryDao queueEntryDao, Producer producer) {
        this(config.getServer(),
                config.getPort(),
                config.getNick(),
                config.getPassword(),
                config.getAdminPassword(),
                config.getControlChannel(),
                config.getRequestChannel(),
                config.getDownloadDirectory(),
                config.getDownloadDirectories(),
                queueManager,
                queueEntryDao,
                producer);
    }

    public IrcBotImpl(String server,
                      int port,
                      String nick,
                      String password,
                      String adminPassword,
                      String controlChannel,
                      String requestChannel,
                      String downloadDirectory,
                      Map<String, File> downloadDirectories,
                      ReceiveQueueManager queueManager,
                      QueueEntryDao queueEntryDao,
                      Producer producer) {
        this.adminPassword = adminPassword;
        this.requestChannel = requestChannel;
        this.controlChannel = controlChannel;
        IncomingFileTransferListener.Configuration configuration = new IncomingFileTransferListener.Configuration(downloadDirectory);
        downloadDirectories.forEach(configuration::withMapping);
        this.configuration = new Configuration.Builder()
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
                .addListener(new IncomingFileTransferListener(eventSource, configuration, queueManager, producer, queueEntryDao))
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

                    @Override
                    public void onJoin(JoinEvent event) throws Exception {
                        super.onJoin(event);
                        User user = event.getUser();
                        if (user != null) {
                            String joiningNick = user.getNick();
                            if (nick.equals(joiningNick)) {
                                // NOTE: this is a good indication of when the bot should be considered "ready"
                                LOG.info("Joining channel [{}]", event.getChannel().getName());
                                channels.add(event.getChannel().getName());
                            }
                        }
                    }

                    @Override
                    public void onPart(PartEvent event) throws Exception {
                        super.onPart(event);
                        User user = event.getUser();
                        if (user != null) {
                            String partingNick = user.getNick();
                            if (nick.equals(partingNick)) {
                                LOG.info("Parting channel [{}]", event.getChannel().getName());
                                channels.remove(event.getChannel().getName());
                            }
                        }
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
        bot.sendIRC().message(requestChannel, formatMessage(message, args));
    }

    public void messageControlChannel(String message, Object... args) {
        String formattedMessage = formatMessage(message, args);
        bot.sendIRC().message(controlChannel, formattedMessage);
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
            if (useIdent) {
                IdentServer.startServer();
            }
        } catch (Exception e) {
            throw new IrcBotException("Failed to start ident server", e);
        }
    }

    @Override
    public void shutdown() {
        try {
            IdentServer.stopServer();
            bot.stopBotReconnect();
            bot.sendIRC().quitServer();
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
        bot.sendIRC().message(channel, message);
    }

    @Override
    public void sendPrivateMessage(String recipient, String message) {
        bot.sendIRC().message(recipient, message);
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

    @Override
    public boolean isNickOnline(String nick) {
        return bot.getUserChannelDao().containsUser(nick);
    }

    @Override
    public boolean isOnline() {
        return this.bot != null &&
            this.bot.isConnected() &&
            (getServerName() != null);
    }

    @Override
    public boolean isInChannel(String channelName) {
        return channels.contains(channelName);
    }

    @Override
    public void joinChannel(String channelName) {
        this.bot.sendIRC().joinChannel(channelName);
    }

    public String getServerName() {
        return this.bot.getServerInfo().getServerName();
    }

    public String getRequestChannel() {
        return requestChannel;
    }

    public String getControlChannel() {
        return controlChannel;
    }

    @Override
    public List<String> listUsers() {
        final Channel channel = bot.getUserChannelDao().getChannel(requestChannel);
        final ImmutableSortedSet<User> users = bot.getUserChannelDao().getUsers(channel);
        return users.stream().map(UserHostmask::getNick).collect(Collectors.toList());
    }
}
