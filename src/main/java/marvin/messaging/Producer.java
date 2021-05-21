package marvin.messaging;

public interface Producer {
    void enqueue(String queue, String message);
    void publishTopic(String topicName, String message);
}
