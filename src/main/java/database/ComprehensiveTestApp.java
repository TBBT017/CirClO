package database;

import java.sql.*;
import java.util.List;

/**
 * Automated test suite covering every CRUD operation across all five entities.
 * Each test prints a ✓ / ✗ result. The suite is self-cleaning: every row it
 * inserts is removed before exit so it does not pollute the sample data.
 *
 * Run with:
 *   mvn exec:java -Dexec.mainClass="database.ComprehensiveTestApp"
 */
public class ComprehensiveTestApp {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== CirclO Social Platform — Comprehensive Test Suite ===\n");

        // ------------------------------------------------------------------ //
        // 1. Database connectivity
        // ------------------------------------------------------------------ //
        section("1. Database Connection");
        try (Connection conn = DBConnection.getConnection()) {
            pass("Connection to circlo_db established");
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT DATABASE()")) {
                if (rs.next()) pass("Active database: " + rs.getString(1));
            }
        } catch (SQLException e) {
            fail("Connection: " + e.getMessage());
            printSummary();
            return;
        }

        // ------------------------------------------------------------------ //
        // 2. Row-count integrity check (≥15 rows required per table)
        // ------------------------------------------------------------------ //
        section("2. Data Integrity (Row Counts ≥ 15)");
        String[] tables = {"Users", "Posts", "Comments", "Reactions", "Connections"};
        try (Connection conn = DBConnection.getConnection()) {
            for (String table : tables) {
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    if (rs.next()) {
                        int cnt = rs.getInt(1);
                        if (cnt >= 15) pass(table + ": " + cnt + " rows");
                        else           fail(table + ": only " + cnt + " rows (need ≥15)");
                    }
                }
            }
        } catch (SQLException e) {
            fail("Row-count check: " + e.getMessage());
        }

        // ------------------------------------------------------------------ //
        // 3. UserDAO CRUD
        // ------------------------------------------------------------------ //
        section("3. UserDAO CRUD");

        // READ — login existing user
        int aliceId = UserDAO.login("alice", "pass123");
        assertTrue("Login (alice/pass123) → ID=" + aliceId, aliceId > 0);

        // READ — get by ID
        String profile = UserDAO.getUserById(aliceId);
        assertTrue("getUserById(" + aliceId + ") not null", profile != null);

        // READ — list all users
        List<String> allUsers = UserDAO.getAllUsers();
        assertTrue("getAllUsers() returns ≥20 users", allUsers.size() >= 20);

        // CREATE — register test user
        String tUser = "test_user_" + System.currentTimeMillis();
        String tEmail = tUser + "@test.com";
        boolean registered = UserDAO.register(tUser, tEmail, "testpass");
        assertTrue("register new user '" + tUser + "'", registered);

        int tId = UserDAO.login(tUser, "testpass");
        assertTrue("login newly registered user → ID=" + tId, tId > 0);

        // UPDATE — change email and password
        boolean updated = UserDAO.updateUser(tId, "updated@test.com", "newpass");
        assertTrue("updateUser (email + password)", updated);
        assertTrue("login with new password", UserDAO.login(tUser, "newpass") == tId);

        // DELETE — remove test user
        boolean deleted = UserDAO.deleteUser(tId);
        assertTrue("deleteUser", deleted);
        assertTrue("login after delete returns -1", UserDAO.login(tUser, "newpass") == -1);

        // ------------------------------------------------------------------ //
        // 4. PostDAO CRUD
        // ------------------------------------------------------------------ //
        section("4. PostDAO CRUD");

        // CREATE
        boolean postCreated = PostDAO.createPost(aliceId, "Test post from ComprehensiveTestApp");
        assertTrue("createPost", postCreated);

        // READ — all posts
        List<String> allPosts = PostDAO.getAllPosts();
        assertTrue("getAllPosts() returns ≥1 post", !allPosts.isEmpty());

        // READ — feed for alice
        List<String> feed = PostDAO.getFeed(aliceId);
        assertTrue("getFeed(alice) returns list (size=" + feed.size() + ")", feed != null);

        // Find the test post ID
        int testPostId = -1;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT post_id FROM Posts WHERE content LIKE ? AND user_id = ? ORDER BY created_at DESC LIMIT 1")) {
            ps.setString(1, "%ComprehensiveTestApp%");
            ps.setInt(2, aliceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) testPostId = rs.getInt("post_id");
        } catch (SQLException e) {
            fail("Locate test post: " + e.getMessage());
        }
        assertTrue("test post found in DB (ID=" + testPostId + ")", testPostId > 0);

        // READ — by ID
        String postById = PostDAO.getPostById(testPostId);
        assertTrue("getPostById(" + testPostId + ") not null", postById != null);

        // UPDATE
        boolean postUpdated = PostDAO.updatePost(testPostId, aliceId, "Updated content");
        assertTrue("updatePost", postUpdated);

        // DELETE
        boolean postDeleted = PostDAO.deletePost(testPostId, aliceId);
        assertTrue("deletePost", postDeleted);
        assertTrue("post gone after delete", PostDAO.getPostById(testPostId) == null);

        // ------------------------------------------------------------------ //
        // 5. CommentDAO CRUD
        // ------------------------------------------------------------------ //
        section("5. CommentDAO CRUD");

        // Use post 1 from sample data
        int targetPost = 1;

        // CREATE
        boolean commentCreated = CommentDAO.createComment(targetPost, aliceId, "Test comment from suite");
        assertTrue("createComment on post " + targetPost, commentCreated);

        // READ
        List<String> comments = CommentDAO.getCommentsByPost(targetPost);
        assertTrue("getCommentsByPost(" + targetPost + ") not empty", !comments.isEmpty());

        // Find test comment ID
        int testCommentId = -1;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT comment_id FROM Comments WHERE content LIKE ? AND user_id = ? ORDER BY created_at DESC LIMIT 1")) {
            ps.setString(1, "%Test comment from suite%");
            ps.setInt(2, aliceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) testCommentId = rs.getInt("comment_id");
        } catch (SQLException e) {
            fail("Locate test comment: " + e.getMessage());
        }
        assertTrue("test comment found (ID=" + testCommentId + ")", testCommentId > 0);

        // UPDATE
        boolean commentUpdated = CommentDAO.updateComment(testCommentId, aliceId, "Updated comment text");
        assertTrue("updateComment", commentUpdated);

        // DELETE
        boolean commentDeleted = CommentDAO.deleteComment(testCommentId, aliceId);
        assertTrue("deleteComment", commentDeleted);

        // ------------------------------------------------------------------ //
        // 6. ReactionDAO CRUD
        // ------------------------------------------------------------------ //
        section("6. ReactionDAO CRUD");

        // Use post 5 (alice does not already have a reaction there in sample data)
        int rxPost = 5;

        // CREATE / UPSERT
        boolean rxAdded = ReactionDAO.addReaction(rxPost, aliceId, "like");
        assertTrue("addReaction (like) on post " + rxPost, rxAdded);

        // READ
        List<String> reactions = ReactionDAO.getReactionsByPost(rxPost);
        assertTrue("getReactionsByPost(" + rxPost + ") not empty", !reactions.isEmpty());

        // Find test reaction ID
        int testRxId = -1;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT reaction_id FROM Reactions WHERE post_id = ? AND user_id = ? LIMIT 1")) {
            ps.setInt(1, rxPost);
            ps.setInt(2, aliceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) testRxId = rs.getInt("reaction_id");
        } catch (SQLException e) {
            fail("Locate test reaction: " + e.getMessage());
        }
        assertTrue("test reaction found (ID=" + testRxId + ")", testRxId > 0);

        // UPDATE
        boolean rxUpdated = ReactionDAO.updateReaction(testRxId, aliceId, "heart");
        assertTrue("updateReaction → heart", rxUpdated);

        // DELETE
        boolean rxDeleted = ReactionDAO.deleteReaction(testRxId, aliceId);
        assertTrue("deleteReaction", rxDeleted);

        // ------------------------------------------------------------------ //
        // 7. ConnectionDAO CRUD
        // ------------------------------------------------------------------ //
        section("7. ConnectionDAO CRUD");

        // alice (ID 1) → bob (ID 2): sample data already has this, use an unused pair
        int aliceIdC = aliceId;
        int targetUserId = 3; // charlie

        // Clean up any stale test connection first
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM Connections WHERE requester_id = ? AND receiver_id = ?")) {
            ps.setInt(1, aliceIdC); ps.setInt(2, targetUserId); ps.executeUpdate();
        } catch (SQLException ignored) {}

        // CREATE
        boolean reqSent = ConnectionDAO.sendRequest(aliceIdC, targetUserId);
        assertTrue("sendRequest alice → charlie", reqSent);

        // READ
        List<String> conns = ConnectionDAO.getConnections(aliceIdC);
        assertTrue("getConnections(alice) not empty", !conns.isEmpty());

        // Find test connection ID
        int testConnId = -1;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT connection_id FROM Connections WHERE requester_id = ? AND receiver_id = ? LIMIT 1")) {
            ps.setInt(1, aliceIdC); ps.setInt(2, targetUserId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) testConnId = rs.getInt("connection_id");
        } catch (SQLException e) {
            fail("Locate test connection: " + e.getMessage());
        }
        assertTrue("test connection found (ID=" + testConnId + ")", testConnId > 0);

        // UPDATE — charlie accepts (charlie = user_id 3 = receiver)
        boolean accepted = ConnectionDAO.updateStatus(testConnId, targetUserId, "accepted");
        assertTrue("updateStatus → accepted", accepted);

        // DELETE
        boolean connDeleted = ConnectionDAO.deleteConnection(testConnId, aliceIdC);
        assertTrue("deleteConnection", connDeleted);

        // ------------------------------------------------------------------ //
        // Summary
        // ------------------------------------------------------------------ //
        printSummary();
    }

    // ------------------------------------------------------------------ //
    // Assertion helpers
    // ------------------------------------------------------------------ //

    private static void assertTrue(String label, boolean condition) {
        if (condition) pass(label);
        else           fail(label);
    }

    private static void pass(String label) {
        System.out.println("   ✓ " + label);
        passed++;
    }

    private static void fail(String label) {
        System.out.println("   ✗ " + label);
        failed++;
    }

    private static void section(String title) {
        System.out.println("\n" + title);
        System.out.println("-".repeat(title.length()));
    }

    private static void printSummary() {
        int total = passed + failed;
        System.out.println("\n==========================================");
        System.out.printf("  RESULTS: %d / %d tests passed%n", passed, total);
        if (failed == 0) {
            System.out.println("  ALL TESTS PASSED ✓");
        } else {
            System.out.println("  " + failed + " test(s) FAILED ✗");
            System.out.println("  Check MySQL is running and data is loaded:");
            System.out.println("    mysql -u root -p123456789 < database/create_schema.sql");
            System.out.println("    mysql -u root -p123456789 < database/initialize_data.sql");
        }
        System.out.println("==========================================");
    }
}
