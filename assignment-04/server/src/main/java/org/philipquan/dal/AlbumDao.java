package org.philipquan.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.philipquan.model.Album;

/**
 * Data access layer for interacting with the associated database.
 */
public class AlbumDao {

	private static AlbumDao instance;
	protected final DatabaseConnectionManager connectionManager;

	private AlbumDao() {
		this.connectionManager = new DatabaseConnectionManager();
	}

	/**
	 * @return {@link AlbumDao} global instance.
	 */
	public static AlbumDao getInstance() {
		if (instance == null) {
			instance = new AlbumDao();
		}
		return instance;
	}

	/**
	 * @param albumId the albumId to query from the database
	 * @return queried {@link Album}
	 * @throws RuntimeException if a {@link SQLException} is thrown
	 */
	public Album getAlbumById(Long albumId) {
		String query = String.format("SELECT * FROM %s WHERE albumId = %d;", Album.TABLE_NAME, albumId);
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

	/**
	 * @param album the album to post
	 * @return the same album with its newly generated {@code albumId}
	 * @throws RuntimeException if a {@link SQLException} is thrown
	 */
	public Album createAlbum(Album album) {
		String query = String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?) RETURNING %s;",
		  Album.TABLE_NAME,
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

	/**
	 * Queries the database for the last album id. Know that this method returns
	 * {@code 1} when there is {@code 0} and {@code 1} records in the table.
	 *
	 * @return the last album id in the database.
	 */
	public long getLastAlbumId() {
		String query = String.format("SELECT last_value FROM %s_%s_seq;", Album.TABLE_NAME, Album.ID_KEY);
		try (
		  Connection connection = this.connectionManager.getConnection();
		  PreparedStatement statement = connection.prepareStatement(query);
		  ResultSet result = statement.executeQuery();
		  ) {
			if (result.next()) {
				return result.getLong(1);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return -1; // this should never be reached.
	}

	private void safeCloseResultSet(ResultSet result) {
		try {
			if (result != null) result.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
