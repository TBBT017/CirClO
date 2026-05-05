package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestApp {
    public static void main(String[] args) {
        System.out.println("=== CirclO Social Platform - MySQL Connection Test ===\n");
        
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("✓ Successfully connected to MySQL!");
            
            // Test query
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT DATABASE() as db")) {
                if (rs.next()) {
                    System.out.println("✓ Current database: " + rs.getString("db"));
                }
            }
            
            // Test user login
            System.out.println("\n--- Testing User Login ---");
            int userId = UserDAO.login("alice", "password123");
            if (userId > 0) {
                System.out.println("✓ Login successful! User ID: " + userId);
            } else {
                System.out.println("✗ Login failed - user not found");
            }
            
            // Test get feed
            System.out.println("\n--- Testing Get Feed ---");
            var posts = PostDAO.getFeed(userId);
            System.out.println("✓ Found " + posts.size() + " post(s) in feed");
            for (String post : posts) {
                System.out.println("  - " + post);
            }
            
            System.out.println("\n=== All tests passed! ===");
            
        } catch (Exception e) {
            System.err.println("\n✗ Connection failed!");
            System.err.println("Error: " + e.getMessage());
            System.err.println("\nMake sure MySQL server is running and credentials are correct.");
            System.err.println("Edit DBConnection.java to update your MySQL credentials.");
        }
    }
}
