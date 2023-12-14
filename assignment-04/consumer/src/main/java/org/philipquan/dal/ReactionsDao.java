package org.philipquan.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.philipquan.model.Reaction;

public class ReactionsDao {

    private static ReactionsDao instance;
    private ConnectionManager connectionManager;

    private ReactionsDao() {
        this.connectionManager = new ConnectionManager();
    }

    public static ReactionsDao getInstance() {
        if (instance == null) {
            instance = new ReactionsDao();
        }
        return instance;
    }

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