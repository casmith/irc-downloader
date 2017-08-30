import org.pircbotx.Configuration;
import org.pircbotx.IdentServer;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;

import java.io.IOException;

public class Client {
    public static void main(String[] args) throws IOException, IrcException {

        final boolean useIdent = true;

        //Before anything else
        if (useIdent) {
            IdentServer.startServer();
        }

        String server = args[0];
        String nick = args[1];
        String channel = args[2];

        Configuration configuration = new Configuration.Builder()
                .addServer(server)
                .setName(nick)
                .setRealName(nick)
                .setLogin(nick)
                .addAutoJoinChannel(channel)
                .setIdentServerEnabled(useIdent)
                .addListener(new QueueProcessorListener(channel, "queue.txt"))
                .addListener(new IncomingFileTransferListener())
                .buildConfiguration();

        PircBotX bot = new PircBotX(configuration);

        //Connect to the server
        try {
            bot.startBot();
        } catch (IrcException|IOException ex) {
            if (useIdent) {
                IdentServer.stopServer();
            }
            System.out.println("Shutting down!");
        }
    }
}
