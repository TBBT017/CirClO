package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Connection CRUD operations.
 * Handles friend/follow requests between users (pending → accepted/rejected).
 */
public class ConnectionDAO {

    /**
     * Send a connection request from one user to another.
     * Status defaults to 'pending' per schema definition.
     *
     * @param requesterId user initiating the request
     * @param receiverId  user receiving the request
     * @return true if the row was inserted
     */
    public static boolean sendRequest(int requesterId, int receiverId) {
        String sql = "INSERT INTO Connections (requester_id, receiver_id, status) VALUES (?, ?, 'pending')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, requesterId);
            pstmt.setInt(2, receiverId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("ConnectionDAO.sendRequest error: " + e.getMessage());
        }
        return false;
    }

    /**
     * List all connections (both directions) for a user.
     * Returns formatted strings: "connectionId | otherUsername (status)"
     *
     * @param userId the user whose connections are listed
     * @return formatted list of connections
     */
    public static List<String> getConnections(int userId) {
        List<String> results = new ArrayList<>();
        // Fetch where user is requester or receiver so both sides appear
        String sql = "SELECT c.connection_id, u.username, c.status " +
                     "FROM Connections c " +
                     "JOIN Users u ON (CASE WHEN c.requester_id = ? THEN c.receiver_id ELSE c.requester_id END) = u.user_id " +
                     "WHERE c.requester_id = ? OR c.receiver_id = ? " +
                     "ORDER BY c.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(String.format("[%d] %s (%s)",
                        rs.getInt("connection_id"),
                        rs.getString("username"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            System.err.println("ConnectionDAO.getConnections error: " + e.getMessage());
        }
        return results;
    }

    /**
     * Update the status of a connection (e.g., 'pending' → 'accepted' or 'rejected').
     * Only the receiver may change the status.
     *
     * @param connectionId ID of the connection row
     * @param receiverId   must match the receiver_id column (ownership check)
     * @param newStatus    'accepted' or 'rejected'
     * @return true if a row was updated
     */
    public static boolean updateStatus(int connectionId, int receiverId, String newStatus) {
        String sql = "UPDATE Connections SET status = ? WHERE connection_id = ? AND receiver_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, connectionId);
            pstmt.setInt(3, receiverId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("ConnectionDAO.updateStatus error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete (remove) a connection. Either party may remove it.
     *
     * @param connectionId ID of the connection to delete
     * @param userId       must be requester or receiver (ownership check)
     * @return true if a row was deleted
     */
    public static boolean deleteConnection(int connectionId, int userId) {
        String sql = "DELETE FROM Connections WHERE connection_id = ? AND (requester_id = ? OR receiver_id = ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, connectionId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("ConnectionDAO.deleteConnection error: " + e.getMessage());
        }
        return false;
    }
}
