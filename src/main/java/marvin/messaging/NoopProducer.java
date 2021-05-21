package marvin.messaging;

public class NoopProducer implements Producer {
    @Override
    public void enqueue(String queue, String message) {
    }

    @Override
    public void publishTopic(String topicName, String message) {
    }
}
