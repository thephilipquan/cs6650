package org.philipquan.mq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.philipquan.model.Config;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * A class that manages connecitons to rabbitmq's {@link ConnectionFactory)
 */
public class FactoryManager {

    private final ConnectionFactory factory;

    /**
     * @param config the application's {@link Config)
     */
    public FactoryManager(Config config) {
        this.factory = new ConnectionFactory();
        this.factory.setHost(config.getMQUrl());
        this.factory.setUsername(config.getMQUser());
        this.factory.setPassword(config.getMQPassword());
    }

    /**
     * @return a new {@link Connection)
     * @throws IOException
     * @throws TimeoutException
     */
    public Connection getConnection() throws IOException, TimeoutException {
        return this.factory.newConnection();
    }
}