package org.philipquan.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.philipquan.model.Album;

public class AlbumDao {

    private static final String TABLE_NAME = "Albums";
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
        String query = "SELECT * FROM " + TABLE_NAME +
          " WHERE id=?";
        ResultSet result = null;
        try (
          Connection connection = this.connectionManager.getConnection();
          PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setLong(1, albumId);
            result = statement.executeQuery();
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
        } finally {
            safeCloseResultSet(result);
        }
        return null;
    }

    public Album createAlbum(Album album) {
        String query = "INSERT INTO " + TABLE_NAME + " (" + Album.ARTIST_KEY + ", " + Album.TITLE_KEY + ", " + Album.YEAR_KEY + ", " + Album.IMAGE_KEY + ") " +
          " VALUES (?, ?, ?, ?)";
        ResultSet result = null;
        try (
          Connection connection = this.connectionManager.getConnection();
          PreparedStatement statement = connection.prepareStatement(
            query,
            Statement.RETURN_GENERATED_KEYS)
          ) {
            statement.setString(1, album.getArtist());
            statement.setString(2, album.getTitle());
            statement.setInt(3, album.getYear());
            statement.setString(4, album.getImage());
            if (statement.executeUpdate() == 0) {
                throw new RuntimeException("There was an error when creating the given album info: " + album);
            }
            result = statement.getGeneratedKeys();
            result.next();
            return new Album(
              result.getLong(1),
              album.getArtist(),
              album.getTitle(),
              album.getYear(),
              album.getImage()
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            safeCloseResultSet(result);
        }
    }

    private void safeCloseResultSet(ResultSet result) {
        try {
            result.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}