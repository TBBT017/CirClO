package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Post CRUD operations.
 * All methods use PreparedStatements to prevent SQL injection.
 */
public class PostDAO {

    /**
     * Create a new post for the given user.
     *
     * @param userId  author's user ID
     * @param content post text (may reference a photo filename)
     * @return true if the row was inserted
     */
    public static boolean createPost(int userId, String content) {
        String sql = "INSERT INTO Posts (user_id, content) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, content);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("PostDAO.createPost error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieve a single post by its ID.
     * Returns "postId | username: content" or null if not found.
     *
     * @param postId the post to look up
     * @return formatted string or null
     */
    public static String getPostById(int postId) {
        String sql = "SELECT p.post_id, u.username, p.content, p.created_at " +
                     "FROM Posts p JOIN Users u ON p.user_id = u.user_id " +
                     "WHERE p.post_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, postId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return String.format("[%d] %s: %s",
                        rs.getInt("post_id"),
                        rs.getString("username"),
                        rs.getString("content"));
            }
        } catch (SQLException e) {
            System.err.println("PostDAO.getPostById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Return all posts from every user, newest-first.
     *
     * @return list of formatted post strings
     */
    public static List<String> getAllPosts() {
        List<String> posts = new ArrayList<>();
        String sql = "SELECT p.post_id, u.username, p.content " +
                     "FROM Posts p JOIN Users u ON p.user_id = u.user_id " +
                     "ORDER BY p.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                posts.add(String.format("[%d] %s: %s",
                        rs.getInt("post_id"),
                        rs.getString("username"),
                        rs.getString("content")));
            }
        } catch (SQLException e) {
            System.err.println("PostDAO.getAllPosts error: " + e.getMessage());
        }
        return posts;
    }

    /**
     * Return posts from users the given user is connected with (their social feed).
     *
     * @param userId the logged-in user
     * @return list of formatted post strings from connections
     */
    public static List<String> getFeed(int userId) {
        List<String> posts = new ArrayList<>();
        // Include posts from both directions of an accepted connection
        String sql = "SELECT DISTINCT p.post_id, u.username, p.content " +
                     "FROM Posts p " +
                     "JOIN Users u ON p.user_id = u.user_id " +
                     "JOIN Connections c ON (p.user_id = c.receiver_id OR p.user_id = c.requester_id) " +
                     "WHERE (c.requester_id = ? OR c.receiver_id = ?) " +
                     "  AND c.status = 'accepted' " +
                     "  AND p.user_id != ? " +
                     "ORDER BY p.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                posts.add(String.format("[%d] %s: %s",
                        rs.getInt("post_id"),
                        rs.getString("username"),
                        rs.getString("content")));
            }
        } catch (SQLException e) {
            System.err.println("PostDAO.getFeed error: " + e.getMessage());
        }
        return posts;
    }

    /**
     * Update the content of a post. Only the post owner may edit it.
     *
     * @param postId     post to update
     * @param userId     ownership check
     * @param newContent replacement content
     * @return true if a row was updated
     */
    public static boolean updatePost(int postId, int userId, String newContent) {
        String sql = "UPDATE Posts SET content = ? WHERE post_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newContent);
            pstmt.setInt(2, postId);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("PostDAO.updatePost error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete a post. Only the post owner may delete it.
     * Cascades to associated comments and reactions via FK ON DELETE CASCADE.
     *
     * @param postId post to delete
     * @param userId ownership check
     * @return true if a row was deleted
     */
    public static boolean deletePost(int postId, int userId) {
        String sql = "DELETE FROM Posts WHERE post_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("PostDAO.deletePost error: " + e.getMessage());
        }
        return false;
    }
}
