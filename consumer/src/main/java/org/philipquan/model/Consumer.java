package org.philipquan.model;

import java.io.IOException;

import org.philipquan.dal.ReactionsDao;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

public class Consumer implements DeliverCallback {

	private final ReactionsDao reactionsDao;
    private final Channel channel;
    private final boolean ackAllPreviousMessages;

    /**
     * @param reactionsDao the database DAO
     * @param channel the message queue channel in which to consume from
     */
    public Consumer(ReactionsDao reactionsDao, Channel channel) {
        this.reactionsDao = reactionsDao;
        this.channel = channel;
        this.ackAllPreviousMessages = false;
    }

    @Override
    public void handle(String s, Delivery delivery) throws IOException {
        String body = new String(delivery.getBody(), "UTF-8");
        Reaction reaction = Reaction.fromJson(body);
        this.reactionsDao.addReaction(reaction);

        this.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), this.ackAllPreviousMessages);
    }
}
