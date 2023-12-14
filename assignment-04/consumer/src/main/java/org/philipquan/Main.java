package org.philipquan;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import org.philipquan.dal.ConnectionManager;
import org.philipquan.dal.ReactionsDao;
import org.philipquan.model.Reaction;

public class Main {

    public static void main(String[] args) throws IOException, TimeoutException {
        if (args.length != 1) {
            throw new RuntimeException("Expected 1 argument for \"threadCount\".");
        }

        Properties properties = new Properties();
        try {
            properties.load(Main.class.getClassLoader().getResourceAsStream("server.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Error initializing ConnectionManager...", e);
        }

        final int threadCount = Integer.parseInt(args[0]);
        ConnectionManager.MAX_ACTIVE = threadCount;

        System.out.println(String.format("Creating %d consumers", threadCount));
        System.out.println("Type CTRL+C to exit program...");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.getProperty("mq.url"));
        factory.setUsername(properties.getProperty("mq.user"));
        factory.setPassword(properties.getProperty("mq.password"));

        Connection connection = factory.newConnection();

        final int preFetchCount = 1;
        for (int i = 0; i < threadCount; i++) {
            Runnable instruction = () -> {
                Channel channel;
                try {
                    channel = connection.createChannel();
                    channel.basicQos(preFetchCount);

                    DeliverCallback callback = (String consumerTag, Delivery delivery) -> {
                        String body = new String(delivery.getBody(), "UTF-8");
                        Reaction reaction = Reaction.fromJson(body);
                        ReactionsDao.getInstance().addReaction(reaction);

                        final boolean ackAllPreviousMessages = false;
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), ackAllPreviousMessages);
                    };

                    final boolean isDurable = false;
                    final boolean isExclusive = false;
                    final boolean autoDelete = false;
                    Map<String, Object> arguments = null;
                    channel.queueDeclare("reactions", isDurable, isExclusive, autoDelete, arguments);

                    final boolean autoAck = false;
                    channel.basicConsume("reactions", autoAck, callback, consumerTag -> {});
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };
            new Thread(instruction).start();
        }
    }

}