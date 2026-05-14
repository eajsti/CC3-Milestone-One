import java.sql.*;
import java.util.Scanner;

class PaymentService {
    Scanner sc = new Scanner(System.in);

    public boolean chargeTransaction(String name, int referenceId, String type, double amount) {
        System.out.println("\n=== Payment Transaction ===");
        String customerName = name == null || name.isEmpty() ? "" : name;
        if (customerName.isEmpty()) {
            System.out.print("Customer Name: ");
            customerName = sc.nextLine();
        } else {
            System.out.println("Customer Name: " + customerName);
        }
        System.out.print("Valid payment method? (y/n): ");
        boolean validMethod = sc.nextLine().trim().equalsIgnoreCase("y");
        System.out.print("Available credit balance: ");
        double creditBalance;
        try {
            creditBalance = Double.parseDouble(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid balance input. Payment cancelled.");
            return false;
        }
        System.out.print("Discount rate (0.0 - 1.0): ");
        double discountRate;
        try {
            discountRate = Double.parseDouble(sc.nextLine());
            if (discountRate < 0 || discountRate > 1) {
                discountRate = 0;
            }
        } catch (Exception e) {
            discountRate = 0;
        }

        String transactionId = type + "-" + referenceId + "-" + System.currentTimeMillis();
        ReservationPayment payment = new ReservationPayment(customerName, transactionId,
                amount, validMethod, creditBalance, discountRate);

        boolean success = payment.execute();
        if (success) {
            success = recordPayment(transactionId, customerName, type, referenceId,
                    payment.getAmount(), payment.vatRate, payment.getDiscountRate(),
                    payment.getFinalAmount());
            if (!success) {
                System.out.println("Payment recording failed. Please contact support.");
            }
        }
        return success;
    }

    private boolean recordPayment(String transactionId, String name, String type,
            int referenceId, double amount, double vatRate, double discountRate,
            double finalAmount) {
        try (Connection c = DBConnection.connect()) {
            c.setAutoCommit(false);

            String sql;
            if (type.equals("Parking")) {
                sql = "INSERT INTO Payments(TransactionID, Name, Type, SessionReferenceId, Amount, VAT, DiscountRate, FinalAmount, Status, CreatedAt) " +
                      "VALUES(?,?,?,?,?,?,?,?,?,datetime('now'))";
            } else { // Fine
                sql = "INSERT INTO Payments(TransactionID, Name, Type, FineReferenceId, Amount, VAT, DiscountRate, FinalAmount, Status, CreatedAt) " +
                      "VALUES(?,?,?,?,?,?,?,?,?,datetime('now'))";
            }
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, transactionId);
            ps.setString(2, name);
            ps.setString(3, type);
            ps.setInt(4, referenceId);
            ps.setDouble(5, amount);
            ps.setDouble(6, vatRate);
            ps.setDouble(7, discountRate);
            ps.setDouble(8, finalAmount);
            ps.setString(9, "Completed");
            ps.executeUpdate();

            // Deduct from user's credit balance atomically
            if (type.equals("Parking")) {
                try (PreparedStatement balPs = c.prepareStatement(
                        "UPDATE Users SET CreditBalance = CreditBalance - ? " +
                        "WHERE Id IN (SELECT u.Id FROM Users u JOIN Vehicles v ON u.Id = v.UserId " +
                        "JOIN Sessions s ON v.Id = s.VehicleId WHERE s.Id = ?)")) {
                    balPs.setDouble(1, finalAmount);
                    balPs.setInt(2, referenceId);
                    int rows = balPs.executeUpdate();
                    if (rows == 0) {
                        throw new SQLException("Failed to update user balance: no matching user found for session " + referenceId);
                    }
                }
            } else { // Fine
                try (PreparedStatement balPs = c.prepareStatement(
                        "UPDATE Users SET CreditBalance = CreditBalance - ? " +
                        "WHERE Id IN (SELECT u.Id FROM Users u JOIN Vehicles v ON u.Id = v.UserId " +
                        "JOIN Tickets t ON v.Plate = t.Plate WHERE t.Id = ?)")) {
                    balPs.setDouble(1, finalAmount);
                    balPs.setInt(2, referenceId);
                    int rows = balPs.executeUpdate();
                    if (rows == 0) {
                        throw new SQLException("Failed to update user balance: no matching user found for ticket " + referenceId);
                    }
                }
            }

            c.commit();
            return true;
        } catch (Exception e) {
            System.out.println("Payment record error: " + e.getMessage());
            return false;
        }
    }
}
