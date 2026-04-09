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
        int tid = Integer.parseInt(sc.nextLine());
        System.out.print("Plate Number: ");
        String plate = sc.nextLine();

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
                    NotificationService.sendNotification("Fine payment received for ticket " + tid);
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
}