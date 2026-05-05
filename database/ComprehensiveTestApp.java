package database;

import java.util.Arrays;
import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class ComprehensiveTestApp {
    public static void main(String[] args) {
        System.out.println("=== CirclO Social Platform - Comprehensive Test ===\n");
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            System.out.println("✓ DB Connection: SUCCESS");
            
            // Test 1: Check table counts (should match ~20 each after init)
            System.out.println("\n--- Table Counts ---");
            String[] tables = {"Users", "Posts", "Comments", "Reactions", "Connections"};
            for (String table : tables) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM " + table)) {
                    if (rs.next()) {
                        System.out.printf("✓ %s: %d rows%n", table, rs.getInt("cnt"));
                    }
                }
            }
            
            // Test 2: UserDAO - Register new user
            System.out.println("\n--- UserDAO Tests ---");
            boolean registered = UserDAO.register("testuser", "test@circlo.com", "testpass");
            System.out.println("Test Register: " + (registered ? "✓ SUCCESS" : "✗ FAILED"));
            
            // Test 3: UserDAO - Login
            int userId = UserDAO.login("alice", "password123");  // From sample data
            System.out.println("Test Login (alice): " + (userId > 0 ? "✓ ID=" + userId : "✗ FAILED"));
            
            // Test 4: PostDAO - Create post
            boolean postCreated = PostDAO.createPost(userId, "Test post from comprehensive test!");
            System.out.println("Test Create Post: " + (postCreated ? "✓ SUCCESS" : "✗ FAILED"));
            
            // Test 5: PostDAO - Get feed
            List<String> feed = PostDAO.getFeed(userId);
            System.out.println("Test Feed: ✓ " + feed.size() + " posts");
            
            // Test 6: PostDAO - Delete post (find recent test post)
            String sqlFindTestPost = "SELECT post_id FROM Posts WHERE content LIKE '%comprehensive test%' ORDER BY created_at DESC LIMIT 1";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlFindTestPost);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int testPostId = rs.getInt("post_id");
                    boolean deleted = PostDAO.deletePost(testPostId, userId);
                    System.out.println("Test Delete Post: " + (deleted ? "✓ SUCCESS" : "✗ FAILED"));
                }
            }
            
            System.out.println("\n=== COMPREHENSIVE TESTS COMPLETED! ===");
            System.out.println("Run 'source database/initialize_data.sql' if counts low.");
            
        } catch (SQLException e) {
            System.err.println("✗ DB Error: " + e.getMessage());
            System.out.println("\nFix: 1) Start MySQL 2) source create_schema.sql + initialize_data.sql 3) Fix DBConnection URL (?allowPublicKeyRetrieval=true)");
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
}

