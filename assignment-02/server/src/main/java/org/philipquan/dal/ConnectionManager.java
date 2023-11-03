package org.philipquan.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionManager {

    private final String user = "root";
//    private final String password = "AlbumAppToor";
//    private final String hostname = "albumapptrial.ctt3ctvkrpkp.us-west-2.rds.amazonaws.com";
    private final String password = "toor";
    private final String hostname = "localhost";
    private final int port = 3306;
    private final String database = "AlbumApp";
    private final String timezone = "UTC";

    public Connection getConnection() throws SQLException {
        Connection connection = null;
        Properties properties = new Properties();
        properties.put("user", this.user);
        properties.put("password", this.password);
        properties.put("timezone", this.timezone);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException(e);
        }
        final String url = "jdbc:mysql://" + this.hostname + ":" + this.port +
          "/" + this.database + "?useSSL=false&allowPublicKeyRetrieval=true";
        connection = DriverManager.getConnection(url, properties);
        return connection;
    }

    public void closeConnection(Connection connection) throws SQLException {
        connection.close();
    }
}