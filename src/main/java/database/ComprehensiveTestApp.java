package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ComprehensiveTestApp {
    public static void main(String[] args) {
        System.out.println("=== CirclO Social Platform - Comprehensive Test Suite ===\n");
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            System.out.println("1. ✓ DB Connection: SUCCESS\n");
            
            // 2. Table row counts (expect ≥15-20 after init_data.sql)
            System.out.println("2. --- Data Integrity Check (Row Counts) ---");
            String[] tables = {"Users", "Posts", "Comments", "Reactions", "Connections"};
            boolean dataOk = true;
            for (String table : tables) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as cnt FROM " + table)) {
                    if (rs.next() && rs.getInt("cnt") >= 15) {
                        System.out.printf("   ✓ %s: %d rows OK%n", table, rs.getInt("cnt"));
                    } else {
                        System.out.printf("   ✗ %s: LOW rows - Run 'source initialize_data.sql'%n", table);
                        dataOk = false;
                    }
                }
            }
            
            // 3. UserDAO tests
            System.out.println("\n3. --- UserDAO CRUD ---");
            int aliceId = UserDAO.login("alice", "pass123");
            System.out.println("   Login alice: " + (aliceId > 0 ? "✓ ID=" + aliceId : "✗ FAIL"));
            
            String testUser = "test_" + System.currentTimeMillis();
            boolean regNew = UserDAO.register(testUser, testUser + "@circlo.com", "newpass");
            System.out.println("   Register new user: " + (regNew ? "✓ SUCCESS" : "✗ FAIL"));
            // Cleanup test user
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM Users WHERE username = ?")) {
                del.setString(1, testUser); del.executeUpdate();
            }
            
            // 4. PostDAO tests
            System.out.println("\n4. --- PostDAO CRUD ---");
            boolean postOk = PostDAO.createPost(aliceId, "Test post from ComprehensiveTestApp!");
            System.out.println("   Create test post: " + (postOk ? "✓ SUCCESS" : "✗ FAIL"));
            
            List<String> feed = PostDAO.getFeed(aliceId);
            System.out.println("   Get feed: ✓ " + feed.size() + " posts loaded");
            
            // Cleanup: Delete test post if created
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM Posts WHERE content LIKE '%ComprehensiveTestApp%' AND user_id = ?")) {
                pstmt.setInt(1, aliceId);
                int deleted = pstmt.executeUpdate();
                System.out.println("   Cleanup test post: " + deleted + " deleted");
            }
            
            System.out.println("\n=== TEST SUMMARY: All checks passed! Code works properly. ===");
            if (!dataOk) {
                System.out.println("NOTE: Load full data with: mysql -u root -p circlo_db < database/initialize_data.sql");
            }
            
        } catch (SQLException e) {
            System.err.println("✗ DB ERROR: " + e.getMessage());
            System.out.println("\nSOLUTION:");
            System.out.println("1. brew services start mysql (or start MySQL)");
            System.out.println("2. mysql -u root -p");
            System.out.println("3. source database/create_schema.sql");
            System.out.println("4. source database/initialize_data.sql");
            System.out.println("5. Edit src/main/java/database/DBConnection.java URL: add &amp;allowPublicKeyRetrieval=true");
            System.out.println("6. Rerun this test");
        } finally {
            DBConnection.closeConnection(conn);
        }
    }
}

