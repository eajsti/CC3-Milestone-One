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
            recordPayment(transactionId, customerName, type, referenceId,
                    payment.getAmount(), payment.vatRate, payment.getDiscountRate(),
                    payment.getFinalAmount());
        }
        return success;
    }

    private void recordPayment(String transactionId, String name, String type,
            int referenceId, double amount, double vatRate, double discountRate,
            double finalAmount) {
        try (Connection c = DBConnection.connect(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO Payments(TransactionID, Name, Type, ReferenceId, Amount, VAT, DiscountRate, FinalAmount, Status, CreatedAt) " +
                "VALUES(?,?,?,?,?,?,?,?,?,datetime('now'))")) {
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
        } catch (Exception e) {
            System.out.println("Payment record error: " + e.getMessage());
        }
    }
}
