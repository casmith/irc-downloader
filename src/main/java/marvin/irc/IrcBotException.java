package marvin.irc;

public class IrcBotException extends RuntimeException {

    public IrcBotException(String message, Throwable cause) {
        super(message, cause);
    }
}
