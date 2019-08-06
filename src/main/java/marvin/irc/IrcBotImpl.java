package marvin.irc;

import org.pircbotx.*;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class IrcBotImpl implements IrcBot {

    private final boolean useIdent = true;
    private static final Logger LOG = LoggerFactory.getLogger(IrcBotImpl.class);
    private PircBotX bot;
    private boolean isRunning;
    private User authorizedUser;
    private String adminPassword;
    private String requestChannel = "#mp3passion";
    private Configuration configuration;
    private EventSource eventSource = new EventSource();
    private QueueManager queueManager = new QueueManager();

    public IrcBotImpl(String server, int port, String nick, String password, String autoJoinChannel, String adminPassword, String requestChannel) {
        this.adminPassword = adminPassword;
        this.requestChannel = requestChannel;
        configuration = new Configuration.Builder()
                .addServer(server, port)
                .setName(nick)
                .setRealName(nick)
                .setLogin(nick)
                .setServerPassword(password)
                .addAutoJoinChannel(autoJoinChannel)
                .addAutoJoinChannel(requestChannel)
                .setIdentServerEnabled(useIdent)
                .setAutoReconnectAttempts(10)
                .setAutoReconnectDelay(5000)
                .setAutoReconnect(true)
                .addListener(new QueueProcessorListener(autoJoinChannel, "queue.txt"))
                .addListener(new IncomingFileTransferListener(eventSource))
                .addListener(new ListenerAdapter() {
                    @Override
                    public void onPrivateMessage(PrivateMessageEvent event) {
                        IrcBotImpl.this.onPrivateMessage(event);
                    }

                    @Override
                    public void onNotice(NoticeEvent event) throws Exception {
                        User user = event.getUser();
                        String nick = "";
                        if (user != null) {
                            nick = user.getNick();
                        }

                        String message = Colors.removeColors(event.getMessage());
                        LOG.info("NOTICE {} - {}", nick, message);
                        Pattern pattern = Pattern.compile(".*Allowed: ([0-9]+) of ([0-9]+).*");
                        Matcher matcher = pattern.matcher(message);
                        if (matcher.find()) {
                            queueManager.updateLimit(nick, parseInt(matcher.group(2)));
                        }
                    }
                })
                .buildConfiguration();

        eventSource.subscribe(event -> {
            if (event instanceof DownloadCompleteEvent) {
                DownloadCompleteEvent dce = (DownloadCompleteEvent) event;
                queueManager.dec(dce.getNick());
                // TODO: retry on failure
            }
        });
    }

    private void onPrivateMessage(PrivateMessageEvent event) {
        User user = event.getUser();
        String nick = user != null ? user.getNick() : "";
        String message = event.getMessage();
        LOG.info("{}: {}", nick, maskPassword(message));
        if (message.startsWith("auth")) {
            authenticateUser(event, message.split(" ")[1], adminPassword);
        } else {
            processMessage(event);
        }
    }

    private String maskPassword(String message) {
        return message.replace(adminPassword, "********");
    }

    private void processMessage(PrivateMessageEvent event) {
        String message = event.getMessage();
        if (event.getUser() != null && event.getUser().equals(authorizedUser)) {
            if (message.startsWith("!")) {
                enqueue(event);
            } else if (message.startsWith("direct")) {
                messageChannel(message.substring("direct ".length()));
            } else if (message.equals("die!")) {
                this.shutdown();
            }
        }
    }

    private void enqueue(PrivateMessageEvent event) {
        // nick is assumed to be the text between the ! and the first space
        String message = event.getMessage();
        String nick = message.split(" ")[0].substring(1);
        queueManager.enqueue(nick, message);
        event.respondPrivateMessage("Enqueued '" + message + "'");
    }

    private void authenticateUser(PrivateMessageEvent event, String password, String adminPassword) {
        if (password.equals(adminPassword)) {
            event.respondPrivateMessage("authenticated");
            authorizedUser = event.getUser();
        } else {
            event.respondPrivateMessage("nope");
        }
    }

    @Override
    public void start() {
        isRunning = true;
        new Thread(() -> {
            while (isRunning) {
                queueManager.getQueues().forEach((nick, queue) -> {
                    if (!queue.isEmpty()) {
                        if (queueManager.inc(nick)) {
                            String message = queue.poll();
                            LOG.info("Requesting: {}", message);
                            messageChannel(message);
                        }
                    }
                });
                sleep(10);
            }
        }).start();
        bot = new PircBotX(configuration);
        startIdentServer();
        startIrcBot();
    }

    private void messageChannel(String requestMessage) {
        bot.send().message(requestChannel, requestMessage);
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        isRunning = false;
        try {
            IdentServer.stopServer();
            bot.stopBotReconnect();
            bot.send().quitServer();
        } catch (IOException e) {
            throw new IrcBotException("Failed to stop ident server", e);
        }
    }
}
