package org.philipquan.dal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

public class ConnectionManager {

    private final DataSource dataSource;

    public ConnectionManager() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("database.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Error initializing ConnectionManager...", e);
        }

        PoolProperties p = new PoolProperties();
        p.setUrl(properties.getProperty("url"));
        p.setDriverClassName(properties.getProperty("driver"));
        p.setUsername(properties.getProperty("user"));
        p.setPassword(properties.getProperty("password"));
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

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }
}