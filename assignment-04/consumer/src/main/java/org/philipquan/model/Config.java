package org.philipquan.model;

import java.io.IOException;
import java.util.Properties;

/**
 * A class that holds all config values the application needs at runtime.
 * Requires the following attributes to be present in the properties file.
 * <ul>
 * <li>db.url</li>
 * <li>db.user</li>
 * <li>db.password</li>
 * <li>mq.url</li>
 * <li>mq.user</li>
 * <li>mq.password</li>
 * </ul>
 */
public class Config {

	private final String queueName;
	private final String mqUrl;
	private final String mqUser;
	private final String mqPassword;
	private final String databasePassword;
	private final String databaseUser;
	private final String databaseUrl;

	/**
	 * @param fileName the full path to the file from the resource folder
	 */
    public Config(String fileName) {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Error initializing Config...", e);
        }

        this.queueName = properties.getProperty("mq.queueName");
		this.mqUrl = properties.getProperty("mq.url");
		this.mqUser = properties.getProperty("mq.user");
		this.mqPassword = properties.getProperty("mq.password");

		this.databaseUrl = properties.getProperty("db.url");
		this.databaseUser = properties.getProperty("db.user");
		this.databasePassword = properties.getProperty("db.password");
    }

	public String getQueueName() {
		return this.queueName;
	}

	public String getMQUrl() {
		return this.mqUrl;
	}

	public String getMQUser() {
		return this.mqUser;
	}

	public String getMQPassword() {
		return this.mqPassword;
	}

	public String getDatabaseUrl() {
		return this.databaseUrl;
	}

	public String getDatabaseUser() {
		return this.databaseUser;
	}

	public String getDatabasePassword() {
		return this.databasePassword;
	}
}
