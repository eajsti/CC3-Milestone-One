import java.sql.*;
import java.util.Scanner;

class NotificationService {
    static Scanner sc = new Scanner(System.in);

    public static void sendNotification(String msg) {
        System.out.println("[NOTIFICATION]: " + msg);
    }

    public static void sendNotification(String userId, String msg, String channel) {
        try (Connection c = DBConnection.connect()) {
            System.out.println("[DEBUG] Inserting notification for UserId: " + userId + ", Message: " + msg);
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO Notifications(UserId,Message,Channel,Status,CreatedAt) VALUES(?,?,?,?,datetime('now'))");
            ps.setString(1, userId);
            ps.setString(2, msg);
            ps.setString(3, channel);
            ps.setString(4, "Pending");
            int rows = ps.executeUpdate();
            System.out.println("[DEBUG] Inserted " + rows + " notification row(s)");
            System.out.println("[NOTIFICATION-" + channel.toUpperCase() + "]: " + msg);
        } catch (Exception e) {
            System.out.println("[NOTIFICATION ERROR]: " + e.getMessage());
            System.out.println("[NOTIFICATION]: " + msg);
        }
    }

    public static void viewMyNotifications(int userId) {
        try (Connection c = DBConnection.connect()) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT Id, Message, Channel, Status, CreatedAt FROM Notifications WHERE UserId=? ORDER BY CreatedAt DESC");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n=== My Notifications ===");
            System.out.println("ID | Message | Channel | Status | Created At");
            System.out.println("-------------------------------------------------");
            while (rs.next()) {
                String msg = rs.getString("Message");
                if (msg.length() > 25) msg = msg.substring(0, 25) + "...";
                System.out.println(rs.getInt("Id") + " | " + msg + " | " + 
                        rs.getString("Channel") + " | " + rs.getString("Status") + " | " + rs.getString("CreatedAt"));
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void markNotificationAsRead(int userId) {
        System.out.println("\n=== Mark Notification as Read ===");
        viewMyNotifications(userId);
        System.out.print("\nEnter Notification ID: ");
        int nid = Integer.parseInt(sc.nextLine());

        try (Connection c = DBConnection.connect()) {
            PreparedStatement ps = c.prepareStatement(
                    "UPDATE Notifications SET Status='Read' WHERE Id=? AND UserId=?");
            ps.setInt(1, nid);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Notification marked as read.");
            } else {
                System.out.println("Notification not found.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void notificationPreferences() {
        System.out.println("\n=== Notification Preferences ===");
        System.out.println("Email Alerts     : ON");
        System.out.println("SMS Alerts       : ON");
        System.out.println("Ticket Alerts    : ON");
        System.out.println("Payment Alerts   : ON");
        System.out.println("Due Date Alerts  : ON");
        System.out.println("\nNote: Notification preferences are stored in the system.");
    }

    public static void sendAnnouncement() {
        System.out.println("\n=== Send Announcement [Admin] ===");
        System.out.print("Subject: ");
        String subject = sc.nextLine();
        System.out.print("Message: ");
        String msg = sc.nextLine();
        System.out.print("Target Role (All/Driver/Officer/Admin): ");
        String role = sc.nextLine();
        System.out.print("Channel (Email/SMS/Both): ");
        String channel = sc.nextLine();

        try (Connection c = DBConnection.connect()) {
            String query = "SELECT Id FROM Users";
            if (!role.equalsIgnoreCase("All")) {
                query += " WHERE Role='" + role + "'";
            }
            ResultSet rs = c.createStatement().executeQuery(query);

            int count = 0;
            while (rs.next()) {
                PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO Notifications(UserId,Message,Channel,Status,CreatedAt) VALUES(?,?,?,?,datetime('now'))");
                ps.setInt(1, rs.getInt("Id"));
                ps.setString(2, subject + " - " + msg);
                ps.setString(3, channel);
                ps.setString(4, "Pending");
                ps.executeUpdate();
                count++;
            }
            System.out.println("Announcement sent to " + count + " user(s).");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}