package org.philipquan.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.philipquan.model.Reaction;

/**
 * Interface for interacting with Data access layer via exposed API.
 */
public class ReactionsDao {

    private final ConnectionManager connectionManager;

    /**
     * @param connectionManager the {@link javax.sql.DataSource} wrapper
     */
    public ReactionsDao(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
	 * Inserts the reaction into the database. If the albumId already exists,
	 * updates the reaction instead.
	 *
     * @param reaction The reaction to insert or update.
     */
    public void addReaction(Reaction reaction) {
        String query = "INSERT INTO reactions values (?,?,?)"
          + " ON CONFLICT (albumId) DO UPDATE"
          + " SET likes = reactions.likes + EXCLUDED.likes,"
          + " dislikes = reactions.dislikes + EXCLUDED.dislikes;";
        try (
          Connection connection = this.connectionManager.getConnection();
          PreparedStatement statement = connection.prepareStatement(query);
          ) {
            statement.setInt(1, reaction.getAlbumId());
            statement.setInt(2, reaction.getLikes());
            statement.setInt(3, reaction.getDislikes());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Something went wrong in addReaction");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}