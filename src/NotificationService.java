import java.sql.*;

class NotificationService {
    public static void sendNotification(String msg) {
        System.out.println("[NOTIFICATION]: " + msg);
    }

    public static void sendNotification(String userId, String msg, String channel) {
        try (Connection c = DBConnection.connect()) {
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO Notifications(UserId,Message,Channel,Status,CreatedAt) VALUES(?,?,?,?,datetime('now'))");
            ps.setString(1, userId);
            ps.setString(2, msg);
            ps.setString(3, channel);
            ps.setString(4, "Pending");
            ps.executeUpdate();
            System.out.println("[NOTIFICATION-" + channel.toUpperCase() + "]: " + msg);
        } catch (Exception e) {
            System.out.println("[NOTIFICATION]: " + msg);
        }
    }
}