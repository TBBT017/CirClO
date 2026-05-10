package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Reaction CRUD operations.
 * The Reactions table enforces UNIQUE(post_id, user_id), so each user
 * can only have one reaction per post; updates replace the previous reaction.
 */
public class ReactionDAO {

    /**
     * Add a reaction to a post. If the user has already reacted, the existing
     * reaction is replaced via ON DUPLICATE KEY UPDATE.
     *
     * @param postId       post being reacted to
     * @param userId       user reacting
     * @param reactionType 'like', 'heart', or 'laugh'
     * @return true if a row was inserted or updated
     */
    public static boolean addReaction(int postId, int userId, String reactionType) {
        String sql = "INSERT INTO Reactions (post_id, user_id, reaction_type) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE reaction_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, reactionType);
            pstmt.setString(4, reactionType);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("ReactionDAO.addReaction error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieve all reactions for a post.
     * Returns formatted strings: "reactionId | username: reactionType"
     *
     * @param postId post whose reactions are fetched
     * @return list of formatted reaction strings
     */
    public static List<String> getReactionsByPost(int postId) {
        List<String> results = new ArrayList<>();
        String sql = "SELECT r.reaction_id, u.username, r.reaction_type " +
                     "FROM Reactions r JOIN Users u ON r.user_id = u.user_id " +
                     "WHERE r.post_id = ? ORDER BY r.created_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, postId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(String.format("[%d] %s: %s",
                        rs.getInt("reaction_id"),
                        rs.getString("username"),
                        rs.getString("reaction_type")));
            }
        } catch (SQLException e) {
            System.err.println("ReactionDAO.getReactionsByPost error: " + e.getMessage());
        }
        return results;
    }

    /**
     * Change the reaction type for an existing reaction row.
     * Only the original reactor (userId) may update.
     *
     * @param reactionId     ID of the reaction to update
     * @param userId         ownership check
     * @param newReactionType replacement type
     * @return true if a row was updated
     */
    public static boolean updateReaction(int reactionId, int userId, String newReactionType) {
        String sql = "UPDATE Reactions SET reaction_type = ? WHERE reaction_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newReactionType);
            pstmt.setInt(2, reactionId);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("ReactionDAO.updateReaction error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Remove a reaction. Only the original reactor may delete it.
     *
     * @param reactionId ID of the reaction to remove
     * @param userId     ownership check
     * @return true if a row was deleted
     */
    public static boolean deleteReaction(int reactionId, int userId) {
        String sql = "DELETE FROM Reactions WHERE reaction_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reactionId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("ReactionDAO.deleteReaction error: " + e.getMessage());
        }
        return false;
    }
}
