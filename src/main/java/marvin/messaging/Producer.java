package marvin.messaging;

public interface Producer {
    void publish(String queue, String message);
}
