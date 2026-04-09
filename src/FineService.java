import java.sql.*;
import java.util.Scanner;

class FineService {
    Scanner sc = new Scanner(System.in);

    public void applyPenalties() {
        try (Connection c = DBConnection.connect()) {
            int updated = c.createStatement().executeUpdate(
                    "UPDATE Tickets SET Amount=Amount+50, Status='Overdue' " +
                    "WHERE Status='Pending' AND DueDate < date('now')");
            System.out.println(updated + " penalties applied.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void payFine() {
        System.out.println("\n=== Pay Fine ===");
        System.out.print("Ticket ID: ");
        int tid;
        try {
            tid = Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid ticket ID.");
            return;
        }
        System.out.print("Plate Number: ");
        String plate = sc.nextLine();
        if (plate.trim().isEmpty()) {
            System.out.println("Plate number required.");
            return;
        }

        try (Connection c = DBConnection.connect()) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT Amount, Status FROM Tickets WHERE Id=? AND Plate=?");
            ps.setInt(1, tid);
            ps.setString(2, plate);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                if (rs.getString("Status").equals("Paid")) {
                    System.out.println("Fine already paid.");
                    return;
                }
                double amount = rs.getDouble("Amount");
                System.out.println("Total due: $" + amount);
                System.out.print("Confirm payment (y/n): ");
                if (sc.nextLine().equalsIgnoreCase("y")) {
                    PreparedStatement us = c.prepareStatement(
                            "UPDATE Tickets SET Status='Paid' WHERE Id=?");
                    us.setInt(1, tid);
                    us.executeUpdate();
                    System.out.println("Payment successful.");
                    
                    try {
                        PreparedStatement findUser = c.prepareStatement(
                                "SELECT v.UserId FROM Vehicles v JOIN Tickets t ON v.Plate=t.Plate WHERE t.Id=?");
                        findUser.setInt(1, tid);
                        ResultSet userRs = findUser.executeQuery();
                        if (userRs.next()) {
                            int ownerId = userRs.getInt("UserId");
                            if (ownerId > 0) {
                                PreparedStatement notif = c.prepareStatement(
                                        "INSERT INTO Notifications(UserId,Message,Channel,Status,CreatedAt) VALUES(?,?,?,?,datetime('now'))");
                                notif.setInt(1, ownerId);
                                notif.setString(2, "Payment confirmed for ticket " + tid + ". Thank you!");
                                notif.setString(3, "Email");
                                notif.setString(4, "Pending");
                                notif.executeUpdate();
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Notification error: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("Ticket not found.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewOutstandingBalances() {
        try (Connection c = DBConnection.connect()) {
            ResultSet rs = c.createStatement().executeQuery(
                    "SELECT Plate, SUM(Amount) as TotalDue, COUNT(*) as TicketCount " +
                    "FROM Tickets WHERE Status='Pending' OR Status='Overdue' " +
                    "GROUP BY Plate");

            System.out.println("\n=== Outstanding Balances ===");
            System.out.println("Plate | Total Due | Ticket Count");
            System.out.println("--------------------------------");
            while (rs.next()) {
                System.out.println(rs.getString("Plate") + " | $" + rs.getDouble("TotalDue") + 
                        " | " + rs.getInt("TicketCount"));
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewFinesByStatus() {
        System.out.println("\n=== View Fines by Status ===");
        System.out.println("1. Pending");
        System.out.println("2. Paid");
        System.out.println("3. Overdue");
        System.out.print("Choice: ");
        String choice = sc.nextLine();

        try (Connection c = DBConnection.connect()) {
            String status = choice.equals("1") ? "Pending" : choice.equals("2") ? "Paid" : "Overdue";
            ResultSet rs = c.createStatement().executeQuery(
                    "SELECT Id, Plate, ViolationType, Amount, DueDate FROM Tickets WHERE Status='" + status + "'");
            System.out.println("\n=== " + status + " Fines ===");
            System.out.println("ID | Plate | Violation | Amount | Due Date");
            System.out.println("------------------------------------------");
            while (rs.next()) {
                System.out.println(rs.getInt("Id") + " | " + rs.getString("Plate") + 
                        " | " + rs.getString("ViolationType") + " | $" + rs.getDouble("Amount") + 
                        " | " + rs.getString("DueDate"));
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewFineDetails() {
        System.out.println("\n=== View Fine Details ===");
        System.out.print("Enter Ticket ID: ");
        int tid = Integer.parseInt(sc.nextLine());

        try (Connection c = DBConnection.connect()) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT t.*, v.Make, v.Model FROM Tickets t " +
                    "LEFT JOIN Sessions s ON t.SessionId = s.Id " +
                    "LEFT JOIN Vehicles v ON s.VehicleId = v.Id OR t.Plate = v.Plate " +
                    "WHERE t.Id = ?");
            ps.setInt(1, tid);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("\n+--------------------------------------+");
                System.out.println("|  FINE DETAILS                        |");
                System.out.println("+--------------------------------------+");
                System.out.println("|  Fine ID      : " + rs.getInt("Id") + "                |");
                System.out.println("|  Plate        : " + rs.getString("Plate") + "                  |");
                System.out.println("|  Violation    : " + rs.getString("ViolationType") + "                |");
                System.out.printf("|  Base Fine    : $%.2f                |%n", rs.getDouble("Amount"));
                System.out.println("|  Due Date     : " + rs.getString("DueDate") + "           |");
                System.out.println("|  Status       : " + rs.getString("Status") + "               |");
                System.out.println("|  Issued At    : " + rs.getString("IssuedAt") + "     |");
                if (rs.getString("Make") != null) {
                    System.out.println("|  Vehicle      : " + rs.getString("Make") + " " + rs.getString("Model") + "          |");
                }
                System.out.println("+--------------------------------------+");
            } else {
                System.out.println("Ticket not found.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewMyFines(String plate) {
        try (Connection c = DBConnection.connect()) {
            ResultSet rs = c.createStatement().executeQuery(
                    "SELECT Id, ViolationType, Amount, Status, DueDate FROM Tickets WHERE Plate='" + plate + "'");

            System.out.println("\n=== My Outstanding Fines ===");
            System.out.println("ID | Violation | Amount | Status | Due Date");
            System.out.println("--------------------------------------------");
            double total = 0;
            while (rs.next()) {
                System.out.println(rs.getInt("Id") + " | " + rs.getString("ViolationType") + 
                        " | $" + rs.getDouble("Amount") + " | " + rs.getString("Status") + 
                        " | " + rs.getString("DueDate"));
                if (!rs.getString("Status").equals("Paid")) {
                    total += rs.getDouble("Amount");
                }
            }
            System.out.println("------------------------------------------");
            System.out.println("Total Outstanding Balance: $" + total);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}