package org.philipquan.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

    private final String user = "root";
    private final String password = "roottoor";

    public Connection getConnection() throws SQLException {
        final String hostname = "albumappdatabase.ctt3ctvkrpkp.us-west-2.rds.amazonaws.com";
        final int port = 5432;
        final String database = "postgres";
        final String schema = "album_app";
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException(e);
        }
        final String url = String.format(
          "jdbc:postgresql://%s:%d/%s?currentSchema=%s",
          hostname, port, database, schema
        );
        return DriverManager.getConnection(url, user, password);
    }
}