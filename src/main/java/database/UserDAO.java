package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User CRUD operations.
 * All methods use PreparedStatements to prevent SQL injection.
 */
public class UserDAO {

    /**
     * Authenticate a user by username and plaintext password.
     *
     * @param username login name
     * @param password plaintext password (hashing should be added in production)
     * @return user_id on success, -1 if credentials do not match
     */
    public static int login(String username, String password) {
        String sql = "SELECT user_id FROM Users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            System.err.println("UserDAO.login error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Register a new user account.
     *
     * @param username unique login name
     * @param email    unique email address
     * @param password plaintext password
     * @return true if the row was inserted
     */
    public static boolean register(String username, String email, String password) {
        String sql = "INSERT INTO Users (username, email, password) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("UserDAO.register error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Fetch a single user's info by ID.
     * Returns a formatted string "userId | username | email" or null if not found.
     *
     * @param userId the user to look up
     * @return formatted string or null
     */
    public static String getUserById(int userId) {
        String sql = "SELECT user_id, username, email FROM Users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return String.format("[%d] %s | %s",
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email"));
            }
        } catch (SQLException e) {
            System.err.println("UserDAO.getUserById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Return a list of all registered users (id, username, email).
     *
     * @return formatted list of all users
     */
    public static List<String> getAllUsers() {
        List<String> users = new ArrayList<>();
        String sql = "SELECT user_id, username, email FROM Users ORDER BY user_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(String.format("[%d] %s | %s",
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email")));
            }
        } catch (SQLException e) {
            System.err.println("UserDAO.getAllUsers error: " + e.getMessage());
        }
        return users;
    }

    /**
     * Update the email and/or password for an existing account.
     *
     * @param userId      user to update
     * @param newEmail    replacement email address
     * @param newPassword replacement password
     * @return true if a row was updated
     */
    public static boolean updateUser(int userId, String newEmail, String newPassword) {
        String sql = "UPDATE Users SET email = ?, password = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newEmail);
            pstmt.setString(2, newPassword);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("UserDAO.updateUser error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Permanently delete a user account (cascades to posts, comments, etc.).
     *
     * @param userId user to delete
     * @return true if a row was deleted
     */
    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("UserDAO.deleteUser error: " + e.getMessage());
        }
        return false;
    }
}
