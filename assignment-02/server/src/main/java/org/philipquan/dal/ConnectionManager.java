package org.philipquan.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionManager {

    private final String user = "postgres";
//    private final String password = "AlbumAppToor";
//    private final String hostname = "albumapptrial.ctt3ctvkrpkp.us-west-2.rds.amazonaws.com";
    private final String password = "toor";

    public Connection getConnection() throws SQLException {
        final String hostname = "localhost";
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