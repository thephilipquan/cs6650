package org.philipquan.dal;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Creates and manages connections to RabbitMQ.
 */
public class MQConnectionManager {

	// this is duplicated in DatabaseConnectionManager
	public static final String CONFIG_FILE = "config.properties";
	public static final String REACTION_QUEUE = "reactions";
	private ConnectionFactory factory;
	private Connection connection;
	private Channel channel;

	/**
	 * Constructor. Know that this constructor attempts to load a properties
	 * file from the project's resource folder called {@value #CONFIG_FILE}.
	 */
	public MQConnectionManager() {
		Properties properties = new Properties();
		try {
			properties.load(getClass().getClassLoader().getResourceAsStream(CONFIG_FILE));
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

			// WARNING only works because Tomcat is single threaded at the moment.
			this.channel = this.connection.createChannel();

			channel.queueDeclare(REACTION_QUEUE, isDurable, isExclusive, autoDelete, arguments);
		} catch (IOException | TimeoutException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return a single shared channel.
	 */
	public Channel getChannel() throws TimeoutException {
		return this.channel;
	}

	/**
	 * Closes ths connection and channel.
	 */
	public void close() {
		try {
			this.channel.close();
			this.connection.close();
		} catch (IOException | TimeoutException e) {
			throw new RuntimeException(e);
		}
	}
}
