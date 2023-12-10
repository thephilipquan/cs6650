package org.philipquan.model;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.philipquan.dal.ReactionsDao;

public class Worker implements Runnable {

    protected final Connection connection;
    protected final String queueName;
    protected int preFetchCount;
    protected boolean isDurable;
    protected boolean isExclusive;
    protected boolean autoDelete;
    protected boolean autoAck;
    protected Map<String, Object> arguments = null;

    public Worker(ReactionsDao reactionsDao, Connection connection, Config config) {
        this.connection = connection;
        this.queueName = properties.getProperty("mq.queueName");
        this.preFetchCount = Integer.parseInt(properties.getProperty("mq.preFetch"));
        this.isDurable = Boolean.parseBoolean(properties.getProperty("mq.isDurable"));
        this.isExclusive = Boolean.parseBoolean(properties.getProperty("mq.isExclusive"));
        this.autoDelete = Boolean.parseBoolean(properties.getProperty("mq.autoDelete"));
        this.autoAck = Boolean.parseBoolean(properties.getProperty("mq.autoAck"));
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