package org.philipquan.dal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class ConnectionManager {

    public static int MAX_ACTIVE;
    private DataSource dataSource;

    public ConnectionManager() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("server.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Error initializing ConnectionManager...", e);
        }
        this.dataSource = createDataSource(properties);
    }

    private DataSource createDataSource(Properties properties) {
        // Following instructions per https://github.com/apache/commons-dbcp/blob/fc2af699d818bf980b9ae671282fd17048580ec5/doc/PoolingDataSourceExample.java
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
          properties.getProperty("db.url"),
          properties.getProperty("db.user"),
          properties.getProperty("db.password")
        );

        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(MAX_ACTIVE);
        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory, poolConfig);
        poolableConnectionFactory.setPool(connectionPool);
        PoolingDataSource<PoolableConnection> dataSource = new PoolingDataSource<>(connectionPool);
        return dataSource;
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }
}