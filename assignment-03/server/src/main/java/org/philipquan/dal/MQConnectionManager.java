package org.philipquan.dal;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class MQConnectionManager {

    public static final String REACTION_QUEUE = "reactions";
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;

    public MQConnectionManager() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("server.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Error initializing ConnectionManager...", e);
        }
        this.factory = new ConnectionFactory();
        this.factory.setHost(properties.getProperty("mq.url"));
        this.factory.setUsername(properties.getProperty("mq.username"));
        this.factory.setPassword(properties.getProperty("mq.password"));

        boolean isDurable = false;
        boolean isExclusive = false;
        boolean autoDelete = false;
        Map<String, Object> arguments = null;

        try {
            this.connection = this.factory.newConnection();
            this.channel = this.connection.createChannel();
            channel.queueDeclare(REACTION_QUEUE, isDurable, isExclusive, autoDelete, arguments);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public Channel getChannel() throws TimeoutException {
        return this.channel;
    }

    public void close() {
        try {
            this.channel.close();
            this.connection.close();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}