package marvin.irc;

public class SendQueueManager extends AbstractQueueManager
        implements QueueManager {

    @Override
    public void addInProgress(String nick, String message) {
    }

    @Override
    public void retry(String nick, String filename) {
    }

    @Override
    public Integer getLimit(String nick) {
        return 1;
    }
}
