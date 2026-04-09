import java.sql.*;
import java.util.Scanner;

class TicketService {
    Scanner sc = new Scanner(System.in);

    public void issueTicket(int officerZoneId) {
        try (Connection c = DBConnection.connect()) {
            System.out.println("\n=== Issue Ticket ===");
            System.out.println("Violation Types:");
            System.out.println("1. Expired Meter - $50");
            System.out.println("2. No Parking Zone - $100");
            System.out.println("3. Wrong Zone - $75");
            System.out.println("4. Blocking - $150");
            System.out.println("5. Expired Registration - $200");
            System.out.print("Select Violation (1-5): ");
            int vtype = Integer.parseInt(sc.nextLine());

            String[] violations = {"", "Expired Meter", "No Parking Zone", "Wrong Zone", "Blocking", "Expired Registration"};
            double[] fines = {0, 50, 100, 75, 150, 200};
            String violation = violations[vtype];
            double amount = fines[vtype];

            System.out.print("Plate Number: ");
            String plate = sc.nextLine();

            int sessionId = -1;
            PreparedStatement ps = c.prepareStatement(
                    "SELECT s.Id FROM Sessions s JOIN Vehicles v ON s.VehicleId=v.Id " +
                    "WHERE v.Plate=? AND s.End IS NULL LIMIT 1");
            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                sessionId = rs.getInt("Id");
            }

            PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO Tickets(Plate,SessionId,ZoneId,ViolationType,Amount,Status,DueDate,IssuedAt) " +
                    "VALUES(?,?,?,?,?,'Pending',date('now','+7 day'),datetime('now'))");
            ins.setString(1, plate);
            if (sessionId > 0) ins.setInt(2, sessionId);
            else ins.setNull(2, Types.INTEGER);
            ins.setInt(3, officerZoneId);
            ins.setString(4, violation);
            ins.setDouble(5, amount);
            ins.executeUpdate();

            System.out.println("Ticket issued: " + violation + " - $" + amount + " (Due in 7 days)");
            
            PreparedStatement findUser = c.prepareStatement(
                    "SELECT v.UserId FROM Vehicles v WHERE v.Plate=?");
            findUser.setString(1, plate);
            ResultSet userRs = findUser.executeQuery();
            if (userRs.next() && userRs.getInt("UserId") > 0) {
                int ownerId = userRs.getInt("UserId");
                NotificationService.sendNotification(String.valueOf(ownerId), 
                    "Ticket issued for vehicle " + plate + ": " + violation + " - $" + amount, "Email");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewTickets() {
        try (Connection c = DBConnection.connect()) {
            ResultSet rs = c.createStatement().executeQuery(
                    "SELECT t.Id, t.Plate, t.ViolationType, t.Amount, t.Status, t.DueDate " +
                    "FROM Tickets t ORDER BY t.IssuedAt DESC");

            System.out.println("\n=== All Tickets ===");
            System.out.println("ID | Plate | Violation | Amount | Status | Due Date");
            System.out.println("---------------------------------------------------");
            while (rs.next()) {
                System.out.println(rs.getInt("Id") + " | " + rs.getString("Plate") + 
                        " | " + rs.getString("ViolationType") + " | $" + rs.getDouble("Amount") + 
                        " | " + rs.getString("Status") + " | " + rs.getString("DueDate"));
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewMyTickets(String plate) {
        try (Connection c = DBConnection.connect()) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT Id, ViolationType, Amount, Status, DueDate FROM Tickets WHERE Plate=?");
            ps.setString(1, plate);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n=== My Tickets ===");
            System.out.println("ID | Violation | Amount | Status | Due Date");
            System.out.println("--------------------------------------------");
            while (rs.next()) {
                System.out.println(rs.getInt("Id") + " | " + rs.getString("ViolationType") + 
                        " | $" + rs.getDouble("Amount") + " | " + rs.getString("Status") + 
                        " | " + rs.getString("DueDate"));
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void updateTicketStatus() {
        System.out.println("\n=== Update Ticket Status [Admin] ===");
        viewTickets();
        System.out.print("\nEnter Ticket ID: ");
        int tid = Integer.parseInt(sc.nextLine());
        System.out.print("New Status (Pending/Paid/Voided/Overdue): ");
        String status = sc.nextLine();

        try (Connection c = DBConnection.connect()) {
            PreparedStatement ps = c.prepareStatement("UPDATE Tickets SET Status=? WHERE Id=?");
            ps.setString(1, status);
            ps.setInt(2, tid);
            ps.executeUpdate();
            System.out.println("Ticket " + tid + " status updated to " + status);
            NotificationService.sendNotification("Your ticket " + tid + " status has been updated to " + status);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}