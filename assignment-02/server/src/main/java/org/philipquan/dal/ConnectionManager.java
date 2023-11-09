package org.philipquan.dal;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.commons.dbcp.BasicDataSource;

public class ConnectionManager {

    private final BasicDataSource dataSource;

    public ConnectionManager() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("database.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Error initializing ConnectionManager...", e);
        }

        this.dataSource = new BasicDataSource();
        this.dataSource.setDriverClassName(properties.getProperty("driver"));
        this.dataSource.setUrl(properties.getProperty("url"));
        this.dataSource.setUsername(properties.getProperty("user"));
        this.dataSource.setPassword(properties.getProperty("password"));

        this.dataSource.setMinIdle(8);
        this.dataSource.setMaxIdle(15);
        this.dataSource.setMaxActive(30);
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }
}