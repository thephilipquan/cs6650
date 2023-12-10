package org.philipquan.model;

import java.io.IOException;
import java.util.Properties;
import org.philipquan.Main;

public class Config {

    public Config(String fileName) {
        Properties properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Error initializing Config...", e);
        }
        this.queueName = properties.getProperty("mq.queueName");
        this.preFetchCount = Integer.parseInt(properties.getProperty("mq.preFetch"));
        this.isDurable = Boolean.parseBoolean(properties.getProperty("mq.isDurable"));
        this.isExclusive = Boolean.parseBoolean(properties.getProperty("mq.isExclusive"));
        this.autoDelete = Boolean.parseBoolean(properties.getProperty("mq.autoDelete"));
        this.autoAck = Boolean.parseBoolean(properties.getProperty("mq.autoAck"));
    }
}