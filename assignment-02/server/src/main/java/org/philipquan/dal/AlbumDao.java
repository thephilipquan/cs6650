package org.philipquan.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.philipquan.model.Album;

public class AlbumDao {

    private static final String TABLE_NAME = "albums";
    private static AlbumDao instance;
    protected final ConnectionManager connectionManager;

    private AlbumDao() {
        this.connectionManager = new ConnectionManager();
    }

    /**
     * @return AlbumDao global instance.
     */
    public static AlbumDao getInstance() {
        if (instance == null) {
            instance = new AlbumDao();
        }
        return instance;
    }

    public Album getAlbumById(Long albumId) {
        String query = String.format("SELECT * FROM %s WHERE id = %d;", TABLE_NAME, albumId);
        try (
          Connection connection = this.connectionManager.getConnection();
          PreparedStatement statement = connection.prepareStatement(query);
          ResultSet result = statement.executeQuery();
        ) {
            if (result.next()) {
                return new Album(
                  result.getLong(Album.ID_KEY),
                  result.getString(Album.ARTIST_KEY),
                  result.getString(Album.TITLE_KEY),
                  result.getInt(Album.YEAR_KEY),
                  result.getString(Album.IMAGE_KEY)
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Album createAlbum(Album album) {
        String query = String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?) RETURNING %s;",
          TABLE_NAME,
          Album.ARTIST_KEY, Album.TITLE_KEY, Album.YEAR_KEY, Album.IMAGE_KEY,
          Album.ID_KEY
        );
        ResultSet result = null;
        try (
          Connection connection = this.connectionManager.getConnection();
          PreparedStatement statement = connection.prepareStatement(query);
          ) {
            statement.setString(1, album.getArtist());
            statement.setString(2, album.getTitle());
            statement.setLong(3, album.getYear());
            statement.setBytes(4, album.getImage().getBytes());
            result = statement.executeQuery();
            if (result.next()) {
                return new Album(
                  result.getLong(Album.ID_KEY),
                  album.getArtist(),
                  album.getTitle(),
                  album.getYear(),
                  album.getImage()
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeCloseResultSet(result);
        }
        return null;
    }

    private void safeCloseResultSet(ResultSet result) {
        try {
            if (result != null) result.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}