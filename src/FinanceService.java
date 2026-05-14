import java.sql.*;

class FinanceService {
    public void viewIncomeStatement() {
        try (Connection c = DBConnection.connect()) {
            ResultSet rs = c.createStatement().executeQuery(
                    "SELECT Type, COUNT(*) as TransactionCount, SUM(FinalAmount) as TotalRevenue " +
                            "FROM Payments GROUP BY Type");

            System.out.println("\n=== Income Statement ===");
            System.out.println("Type | Transactions | Total Revenue");
            System.out.println("------------------------------------");
            double grandTotal = 0;
            while (rs.next()) {
                String type = rs.getString("Type");
                int count = rs.getInt("TransactionCount");
                double total = rs.getDouble("TotalRevenue");
                grandTotal += total;
                System.out.println(type + " | " + count + " | $" + String.format("%.2f", total));
            }
            System.out.println("------------------------------------");
            System.out.println("Grand Total Income: $" + String.format("%.2f", grandTotal));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewPaymentHistory() {
        try (Connection c = DBConnection.connect()) {
            ResultSet rs = c.createStatement().executeQuery(
                    "SELECT TransactionID, Name, Type, ReferenceId, Amount, VAT, DiscountRate, FinalAmount, CreatedAt " +
                            "FROM Payments ORDER BY CreatedAt DESC");

            System.out.println("\n=== Payment History ===");
            System.out.println("Txn ID | Name | Type | Ref | Amount | Final | Date");
            System.out.println("--------------------------------------------------------------");
            while (rs.next()) {
                System.out.println(rs.getString("TransactionID") + " | " + rs.getString("Name") + " | " +
                        rs.getString("Type") + " | " + rs.getInt("ReferenceId") + " | $" +
                        rs.getDouble("Amount") + " | $" + rs.getDouble("FinalAmount") + " | " +
                        rs.getString("CreatedAt"));
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewMyPayments(int userId) {
        try (Connection c = DBConnection.connect(); PreparedStatement ps = c.prepareStatement(
                "SELECT p.TransactionID, p.Type, p.ReferenceId, p.Amount, p.FinalAmount, p.CreatedAt, " +
                        "COALESCE((SELECT Plate FROM Vehicles v WHERE v.Id = (SELECT s.VehicleId FROM Sessions s WHERE p.Type='Parking' AND s.Id = p.ReferenceId)), " +
                        "(SELECT Plate FROM Tickets t WHERE p.Type='Fine' AND t.Id = p.ReferenceId), 'N/A') AS Plate " +
                        "FROM Payments p " +
                        "WHERE (p.Type='Parking' AND p.ReferenceId IN (SELECT s.Id FROM Sessions s WHERE s.VehicleId IN (SELECT Id FROM Vehicles WHERE UserId=?))) " +
                        "OR (p.Type='Fine' AND p.ReferenceId IN (SELECT t.Id FROM Tickets t WHERE t.Plate IN (SELECT Plate FROM Vehicles WHERE UserId=?))) " +
                        "ORDER BY p.CreatedAt DESC")) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n=== My Payments ===");
            System.out.println("Txn ID | Type | Ref | Plate | Amount | Final | Date");
            System.out.println("--------------------------------------------------------------");
            while (rs.next()) {
                System.out.println(rs.getString("TransactionID") + " | " + rs.getString("Type") + " | " +
                        rs.getInt("ReferenceId") + " | " + rs.getString("Plate") + " | $" +
                        rs.getDouble("Amount") + " | $" + rs.getDouble("FinalAmount") + " | " +
                        rs.getString("CreatedAt"));
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}

