package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class SocialDemoApp {
    private static Scanner scanner = new Scanner(System.in);
    private static int currentUserId = -1;

    public static void main(String[] args) {
        System.out.println("=== CirclO Social Demo - Interactive Test ===\n");
        loginOrRegister();
        showMenu();
    }

    private static void loginOrRegister() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        currentUserId = UserDAO.login(username, password);
        if (currentUserId == -1) {
            System.out.print("User not found. Register? (y/n): ");
            if (scanner.nextLine().toLowerCase().startsWith("y")) {
                System.out.print("Email: ");
                String email = scanner.nextLine();
                if (UserDAO.register(username, email, password)) {
                    currentUserId = UserDAO.login(username, password);
                    System.out.println("✓ Registered & logged in!");
                }
            }
        }
        if (currentUserId != -1) {
            System.out.println("✓ Logged in as " + username + " (ID: " + currentUserId + ")");
        }
    }

    private static void showMenu() {
        while (currentUserId != -1) {
            System.out.println("\n--- Menu ---");
            System.out.println("1. View Feed (posts from connections)");
            System.out.println("2. Create Post (text/photo sim)");
            System.out.println("3. Add Reaction (like/dislike)");
            System.out.println("4. View Connections");
            System.out.println("5. Logout");
            System.out.print("Choose: ");
            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                viewFeed();
            } else if (choice.equals("2")) {
                createPost();
            } else if (choice.equals("3")) {
                addReaction();
            } else if (choice.equals("4")) {
                viewConnections();
            } else if (choice.equals("5")) {
                currentUserId = -1;
            } else {
                System.out.println("Invalid");
            }
        }
    }

    private static void viewFeed() {
        var feed = PostDAO.getFeed(currentUserId);
        System.out.println("\n--- Your Feed ---");
        for (int i = 0; i < feed.size(); i++) {
            System.out.println((i+1) + ". " + feed.get(i));
        }
    }

    private static void createPost() {
        System.out.print("Post content (text or 'photo.jpg' for photo): ");
        String content = scanner.nextLine();
        if (PostDAO.createPost(currentUserId, content)) {
            System.out.println("✓ Post created!");
        } else {
            System.out.println("✗ Post failed");
        }
    }

    private static void addReaction() {
        System.out.print("Post ID to react: ");
        int postId;
        try {
            postId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID");
            return;
        }
        System.out.print("Reaction (like/dislike/heart): ");
        String type = scanner.nextLine();
        String sql = "INSERT INTO Reactions (post_id, user_id, reaction_type) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE reaction_type = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, postId);
            pstmt.setInt(2, currentUserId);
            pstmt.setString(3, type);
            pstmt.setString(4, type);
            if (pstmt.executeUpdate() > 0) {
                System.out.println("✓ Reacted with " + type + "!");
            } else {
                System.out.println("✗ Reaction failed");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewConnections() {
        String sql = "SELECT u.username, c.status FROM Connections c JOIN Users u ON c.receiver_id = u.user_id WHERE c.requester_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            System.out.println("\n--- Your Connections ---");
            while (rs.next()) {
                System.out.println(rs.getString("username") + " - " + rs.getString("status"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

