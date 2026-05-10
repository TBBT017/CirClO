package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Comment CRUD operations.
 * Each public method opens its own connection and closes it via try-with-resources.
 */
public class CommentDAO {

    /**
     * Insert a new comment on a post.
     *
     * @param postId  ID of the post being commented on
     * @param userId  ID of the commenting user
     * @param content comment text
     * @return true if the row was inserted
     */
    public static boolean createComment(int postId, int userId, String content) {
        String sql = "INSERT INTO Comments (post_id, user_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, content);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("CommentDAO.createComment error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieve all comments for a given post, ordered oldest-first.
     * Returns formatted strings: "commentId | username: content"
     *
     * @param postId post whose comments are fetched
     * @return list of formatted comment strings
     */
    public static List<String> getCommentsByPost(int postId) {
        List<String> results = new ArrayList<>();
        String sql = "SELECT c.comment_id, u.username, c.content, c.created_at " +
                     "FROM Comments c JOIN Users u ON c.user_id = u.user_id " +
                     "WHERE c.post_id = ? ORDER BY c.created_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, postId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(String.format("[%d] %s: %s",
                        rs.getInt("comment_id"),
                        rs.getString("username"),
                        rs.getString("content")));
            }
        } catch (SQLException e) {
            System.err.println("CommentDAO.getCommentsByPost error: " + e.getMessage());
        }
        return results;
    }

    /**
     * Update the text of an existing comment.
     * Only the comment owner (userId) may update.
     *
     * @param commentId ID of the comment to update
     * @param userId    ID of the requesting user (ownership check)
     * @param newContent replacement text
     * @return true if a row was updated
     */
    public static boolean updateComment(int commentId, int userId, String newContent) {
        String sql = "UPDATE Comments SET content = ? WHERE comment_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newContent);
            pstmt.setInt(2, commentId);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("CommentDAO.updateComment error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete a comment by ID, restricted to the comment owner.
     *
     * @param commentId ID of the comment to delete
     * @param userId    ID of the requesting user (ownership check)
     * @return true if a row was deleted
     */
    public static boolean deleteComment(int commentId, int userId) {
        String sql = "DELETE FROM Comments WHERE comment_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, commentId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("CommentDAO.deleteComment error: " + e.getMessage());
        }
        return false;
    }
}
