package org.philipquan;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import org.philipquan.dal.ConnectionManager;
import org.philipquan.dal.ReactionsDao;
import org.philipquan.model.Worker;
import org.philipquan.mq.FactoryManager;

public class Main {

    private final static String CONFIG_FILE = "config.properties";

    public static void main(String[] args) throws IOException, TimeoutException {
        if (args.length != 1) {
            throw new RuntimeException("Expected 1 argument for \"threadCount\".");
        }

        // TODO trying to refactor everything.
        // What's the problem? Should all classes use properties.getProperty? or config.getUsername (for example)...
        // Probably the second because what if the property key changes? You're only duplicating the get method in Config.

        Config config = new Config(CONFIG_FILE);
        final int threadCount = Integer.parseInt(args[0]);

        // How to set MaxActive in ConnectionManager.
        // REFACTOR TO THIS
        ConnectionManager connectionManager = new ConnectionManager(threadCount);
        ReactionsDao reactionsDao = new ReactionsDao(connectionManager);
        // end refactor

        System.out.println(String.format("Creating %d consumers", threadCount));
        System.out.println("Type CTRL+C to exit program...");

        FactoryManager factoryManager = new FactoryManager(config);
        Connection connection = factoryManager.getConnection();
        for (int i = 0; i < threadCount; i++) {
            Worker worker = new Worker(reactionsDao, connection, config);
            new Thread(worker).start();
        }
    }

}