import org.pircbotx.Configuration;
import org.pircbotx.IdentServer;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;

import java.io.IOException;

public class Client {
    public static void main(String[] args) throws IOException, IrcException {

        boolean useIdent;

        String server = args[0];
        String nick = args[1];
        String channel = args[2];
        String ident = args.length > 3 ? args[3] : null;
        String listPath = args.length > 4 ? args[4] : null;
        if (ident == null) {
            useIdent = true;
        } else {
            useIdent = Boolean.parseBoolean(ident);
        }

        //Before anything else
        if (useIdent) {
            IdentServer.startServer();
        }

        Configuration configuration = new Configuration.Builder()
                .addServer(server)
                .setName(nick)
                .setRealName(nick)
                .setLogin(nick)
                .addAutoJoinChannel(channel)
                .setIdentServerEnabled(useIdent)
                .addListener(new QueueProcessorListener(channel, listPath))
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
