package org.philipquan.dal;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.philipquan.model.Config;

/**
 * A pooling connection manager wrapping {@link DataSource}.
 */
public class ConnectionManager {

    private final int maxActive;
    private DataSource dataSource;

    /**
     * @param config the project's config
     * @param threadCount the number of threads to spawns. Each thread will
	 * manage one consumer
     */
    public ConnectionManager(Config config, int threadCount) {
        this.dataSource = createDataSource(config);
		this.maxActive = threadCount / 2;
    }

    private DataSource createDataSource(Config config) {
        // Following instructions per https://github.com/apache/commons-dbcp/blob/master/doc/PoolingDataSourceExample.java
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
			config.getDatabaseUrl(),
			config.getDatabaseUser(),
			config.getDatabasePassword()
        );

        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(this.maxActive);
        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory, poolConfig);
        poolableConnectionFactory.setPool(connectionPool);
        PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<>(connectionPool);
        return dataSource;
    }

    /**
     * @return a connection from the connection pool
     * @throws SQLException from the database
     */
    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }
}
