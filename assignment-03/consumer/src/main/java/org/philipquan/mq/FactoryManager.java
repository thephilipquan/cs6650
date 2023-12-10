package org.philipquan.mq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

public class FactoryManager {

    private final ConnectionFactory factory;

    public FactoryManager(Properties properties) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.getProperty("mq.url"));
        factory.setUsername(properties.getProperty("mq.user"));
        factory.setPassword(properties.getProperty("mq.password"));
        this.factory = factory;
    }

    public Connection getConnection() throws IOException, TimeoutException {
        return this.factory.newConnection();
    }
}