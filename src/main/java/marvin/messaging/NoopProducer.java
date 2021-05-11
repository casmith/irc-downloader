package marvin.messaging;

public class NoopProducer implements Producer {
    @Override
    public void publish(String queue, String message) {
    }
}
