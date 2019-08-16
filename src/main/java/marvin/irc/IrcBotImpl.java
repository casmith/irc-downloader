package marvin.irc;

import marvin.irc.events.DownloadCompleteEvent;
import marvin.irc.events.DownloadStartedEvent;
import marvin.irc.events.EventSource;
import org.pircbotx.*;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                .setAutoReconnectDelay(5000)
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
                        messageHandlers.forEach(handler -> handler.onMessage(channelName, nick, message));
                    }
                })
                .buildConfiguration();

        eventSource.subscribe(event -> {
            try {
                LOG.info("event observed {}", event);
                if (event instanceof DownloadStartedEvent) {
                    DownloadStartedEvent dse = (DownloadStartedEvent) event;
                    messageControlChannel("Download {0} from {1} started", dse.getFileName(), dse.getNick());
                }

                if (event instanceof DownloadCompleteEvent) {
                    DownloadCompleteEvent dce = (DownloadCompleteEvent) event;
                    queueManager.dec(dce.getNick());
                    if (!dce.isSuccess()) {
                        messageControlChannel("Download {0} from {1} failed, retrying", dce.getFileName(), dce.getNick());
                        queueManager.retry(dce.getNick(), dce.getFileName());
                    } else {
                        messageControlChannel("Download {0} from {1} finished", dce.getFileName(), dce.getNick());
                    }
                }
            } catch (Exception e) {
                LOG.debug("Exception occurred {}", e.getMessage(), e);
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
        LOG.info("Sending message to {}: {}", controlChannel, formattedMessage);
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
}
