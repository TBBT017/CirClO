package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {
    
    /**
     * Create a new post
     * @param userId
     * @param content
     * @return true if successful
     */
    public static boolean createPost(int userId, String content) {
        String sql = "INSERT INTO Posts (user_id, content) VALUES (?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, content);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get all posts from connected users
     * @param userId
     * @return list of posts
     */
    public static List<String> getFeed(int userId) {
        List<String> posts = new ArrayList<>();
        String sql = "SELECT p.content FROM Posts p " +
                     "JOIN Connections c ON (p.user_id = c.receiver_id OR p.user_id = c.requester_id) " +
                     "WHERE c.requester_id = ? OR c.receiver_id = ? " +
                     "ORDER BY p.created_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                posts.add(rs.getString("content"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }
    
    /**
     * Delete a post
     * @param postId
     * @param userId
     * @return true if successful
     */
    public static boolean deletePost(int postId, int userId) {
        String sql = "DELETE FROM Posts WHERE post_id = ? AND user_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, postId);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
