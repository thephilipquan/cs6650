package org.philipquan.model;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import java.io.IOException;
import org.philipquan.dal.ReactionsDao;

public class Consumer implements DeliverCallback {

    protected final Channel channel;
    protected final boolean ackAllPreviousMessages;

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