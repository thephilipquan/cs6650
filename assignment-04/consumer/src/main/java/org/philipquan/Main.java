package org.philipquan;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.philipquan.dal.ConnectionManager;
import org.philipquan.dal.ReactionsDao;
import org.philipquan.model.Config;
import org.philipquan.model.Worker;
import org.philipquan.mq.FactoryManager;

import com.rabbitmq.client.Connection;

public class Main {

	private final static int ARGUMENT_THREAD_COUNT = 0;

    private final static String CONFIG_FILE = "config.properties";

    public static void main(String[] args) throws IOException, TimeoutException {
        if (args.length != 1) {
            throw new RuntimeException("Expected 1 argument for \"threadCount\".");
        }

        Config config = new Config(CONFIG_FILE);
        final int threadCount = Integer.parseInt(args[ARGUMENT_THREAD_COUNT]);

        ConnectionManager connectionManager = new ConnectionManager(config, threadCount);
        ReactionsDao reactionsDao = new ReactionsDao(connectionManager);

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
