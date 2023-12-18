package org.philipquan.dal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * Creates and manages database connection lifecycle.
 */
public class DatabaseConnectionManager {

	public static final String CONFIG_FILE = "server.properties";
	private final DataSource dataSource;

	/**
	 * Constructor. Know that this constructor attempts to load a properties
	 * file from the project's resource folder called {@value #CONFIG_FILE}.
	 */
	public DatabaseConnectionManager() {
		Properties properties = new Properties();
		try {
			properties.load(getClass().getClassLoader().getResourceAsStream(CONFIG_FILE));
		} catch (IOException e) {
			throw new RuntimeException("Error initializing ConnectionManager...", e);
		}

		PoolProperties p = new PoolProperties();
		p.setUrl(properties.getProperty("db.url"));
		p.setDriverClassName(properties.getProperty("db.driver"));
		p.setUsername(properties.getProperty("db.user"));
		p.setPassword(properties.getProperty("db.password"));
		p.setJmxEnabled(true);
		p.setTestWhileIdle(false);
		p.setTestOnBorrow(true);
		p.setValidationQuery("SELECT 1");
		p.setTestOnReturn(false);
		p.setValidationInterval(30000);
		p.setTimeBetweenEvictionRunsMillis(30000);
		p.setMaxActive(30);
		p.setInitialSize(10);
		p.setMaxWait(10000);
		p.setRemoveAbandonedTimeout(60);
		p.setMinEvictableIdleTimeMillis(30000);
		p.setMinIdle(10);
		p.setMaxIdle(10);
		p.setLogAbandoned(true);
		p.setRemoveAbandoned(true);

		this.dataSource = new DataSource();
		this.dataSource.setPoolProperties(p);
	}

	/**
	 * Returns an open connection from the connection pool. If there are no
	 * connections to reuse, the {@link DataSource} will attempt to open a
	 * new one if the current pool is less than {@link PoolProperties#setMaxActive}.
	 *
	 * @return a connection to the database
	 */
	public Connection getConnection() throws SQLException {
		return this.dataSource.getConnection();
	}
}
