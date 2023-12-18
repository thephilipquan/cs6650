package org.philipquan.model;

import java.io.IOException;
import java.util.Map;

import org.philipquan.dal.ReactionsDao;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

/**
 * A class that implements Runnable. Serves as the instruction for each thread
 * that consumes from the message queue.
 */
public class Worker implements Runnable {

	private final ReactionsDao reactionsDao;
    private final Connection connection;
    private final String queueName;

    private final int preFetchCount = 1;
    private final boolean isDurable = false;
    private final boolean isExclusive = false;
    private final boolean autoDelete = false;
    private final boolean autoAck = false;
    private final Map<String, Object> arguments = null;

    /**
     * @param reactionsDao the database access object
     * @param connection the message queue's {@link Connection} that all
	 * channels will share
     * @param config the application's config
     */
    public Worker(ReactionsDao reactionsDao, Connection connection, Config config) {
		this.reactionsDao = reactionsDao;
        this.connection = connection;
        this.queueName = config.getQueueName();
    }

    @Override
    public void run() {
        Channel channel;
        try {
            channel = this.connection.createChannel();
            channel.basicQos(this.preFetchCount);
            channel.queueDeclare(this.queueName, this.isDurable, this.isExclusive, this.autoDelete, this.arguments);

            DeliverCallback callback = new Consumer(this.reactionsDao, channel);
            channel.basicConsume(this.queueName, this.autoAck, callback, consumerTag -> {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
