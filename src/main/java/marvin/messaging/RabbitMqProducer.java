package marvin.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import marvin.config.RmqHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class RabbitMqProducer implements Producer {

    private final ConnectionFactory connectionFactory;
    private final static Logger LOG = LoggerFactory.getLogger(RabbitMqProducer.class);

    @Inject
    public RabbitMqProducer(@RmqHost String host) {
        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost(host);
    }

    public void publish(String queueName, String message) {
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(queueName, false, false, false, null);
            channel.basicPublish("", queueName, null, message.getBytes());
            LOG.info("Sent [{}] on queue {}", message, queueName);
        } catch (Exception e) {
            LOG.error("Failed to publish message to queue {} on host {}", queueName, this.connectionFactory.getHost());
        }
    }
}
