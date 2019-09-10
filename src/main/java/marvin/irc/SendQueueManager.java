package marvin.irc;

public class SendQueueManager extends AbstractQueueManager
        implements QueueManager {

    @Override
    public void update(String nick, int current) {

    }

    @Override
    public boolean inc(String nick) {
        return false;
    }

    @Override
    public boolean dec(String nick) {
        return false;
    }

    @Override
    public void addInProgress(String nick, String message) {

    }

    @Override
    public void retry(String nick, String filename) {

    }
}
