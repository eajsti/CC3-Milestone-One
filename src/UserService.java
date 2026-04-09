import java.sql.*;
import java.util.Scanner;

class UserService {
    private static int currentUserId = 0;
    private static String currentRole = null;
    Scanner sc = new Scanner(System.in);

    public void register() {
        try (Connection c = DBConnection.connect()) {
            System.out.println("\n=== Register ===");
            System.out.print("Username: ");
            String u = sc.nextLine();
            System.out.print("Password: ");
            String p = sc.nextLine();
            System.out.print("Email: ");
            String email = sc.nextLine();
            System.out.print("Phone: ");
            String phone = sc.nextLine();
            System.out.print("Role (Driver/Officer/Admin): ");
            String role = sc.nextLine();

            if (!role.equalsIgnoreCase("Driver") && !role.equalsIgnoreCase("Officer") && !role.equalsIgnoreCase("Admin")) {
                System.out.println("Invalid role. Defaulting to Driver.");
                role = "Driver";
            }

            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO Users(Username,Password,Email,Phone,Role,Status) VALUES(?,?,?,?,?,'Active')");
            ps.setString(1, u);
            ps.setString(2, p);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, role);
            ps.executeUpdate();

            System.out.println("Registered successfully as " + role);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public String login() {
        try (Connection c = DBConnection.connect()) {
            System.out.println("\n=== Login ===");
            System.out.print("Username: ");
            String u = sc.nextLine();
            System.out.print("Password: ");
            String p = sc.nextLine();

            PreparedStatement ps = c.prepareStatement(
                    "SELECT Id, Role, Status FROM Users WHERE Username=? AND Password=?");
            ps.setString(1, u);
            ps.setString(2, p);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String status = rs.getString("Status");
                if (!status.equalsIgnoreCase("Active")) {
                    System.out.println("Account is disabled. Contact admin.");
                    return null;
                }
                currentUserId = rs.getInt("Id");
                currentRole = rs.getString("Role");
                System.out.println("Welcome " + u + " (" + currentRole + ")");
                return currentRole;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentRole() {
        return currentRole;
    }

    public void updateProfile() {
        try (Connection c = DBConnection.connect()) {
            System.out.println("\n=== Update Profile ===");
            System.out.print("New Email: ");
            String email = sc.nextLine();
            System.out.print("New Phone: ");
            String phone = sc.nextLine();

            PreparedStatement ps = c.prepareStatement(
                    "UPDATE Users SET Email=?, Phone=? WHERE Id=?");
            ps.setString(1, email);
            ps.setString(2, phone);
            ps.setInt(3, currentUserId);
            ps.executeUpdate();
            System.out.println("Profile updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void changePassword() {
        try (Connection c = DBConnection.connect()) {
            System.out.println("\n=== Change Password ===");
            System.out.print("Current Password: ");
            String oldPass = sc.nextLine();
            System.out.print("New Password: ");
            String newPass = sc.nextLine();

            PreparedStatement ps = c.prepareStatement(
                    "UPDATE Users SET Password=? WHERE Id=? AND Password=?");
            ps.setString(1, newPass);
            ps.setInt(2, currentUserId);
            ps.setString(3, oldPass);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Password changed.");
            } else {
                System.out.println("Incorrect current password.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void manageUsers() {
        System.out.println("\n=== Manage Users ===");
        System.out.println("1. View All Users");
        System.out.println("2. Enable/Disable User");
        System.out.print("Choice: ");
        String choice = sc.nextLine();

        try (Connection c = DBConnection.connect()) {
            if (choice.equals("1")) {
                ResultSet rs = c.createStatement().executeQuery(
                        "SELECT Id, Username, Email, Role, Status FROM Users");
                System.out.println("\nID | Username | Email | Role | Status");
                System.out.println("-----------------------------------");
                while (rs.next()) {
                    System.out.println(rs.getInt("Id") + " | " + rs.getString("Username") + 
                            " | " + rs.getString("Email") + " | " + rs.getString("Role") + 
                            " | " + rs.getString("Status"));
                }
            } else if (choice.equals("2")) {
                System.out.print("User ID: ");
                int uid = Integer.parseInt(sc.nextLine());
                System.out.print("New Status (Active/Disabled): ");
                String status = sc.nextLine();

                PreparedStatement ps = c.prepareStatement(
                        "UPDATE Users SET Status=? WHERE Id=?");
                ps.setString(1, status);
                ps.setInt(2, uid);
                ps.executeUpdate();
                System.out.println("User status updated.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void logout() {
        currentUserId = 0;
        currentRole = null;
        System.out.println("Logged out.");
    }
}