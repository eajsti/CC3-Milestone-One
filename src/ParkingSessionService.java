import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

class ParkingSessionService {
    Scanner sc = new Scanner(System.in);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public int startSession(int vehicleId, int zoneId) {
        try (Connection c = DBConnection.connect()) {
            int slotId = new ParkingAreaService().selectAvailableSlot(zoneId);
            if (slotId == -1) {
                System.out.println("No available slots in this zone.");
                return -1;
            }

            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO Sessions(VehicleId,SlotId,Start) VALUES(?,?,datetime('now'))",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, vehicleId);
            ps.setInt(2, slotId);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int sessionId = -1;
            if (rs.next()) {
                sessionId = rs.getInt(1);
            }

            c.createStatement().execute("UPDATE Slots SET Status='Occupied' WHERE Id=" + slotId);

            System.out.println("Session started. Slot: " + slotId + ", Session ID: " + sessionId);
            NotificationService.sendNotification("Parking session started. Slot: " + slotId);
            return sessionId;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return -1;
        }
    }

    public void endSession(int vehicleId) {
        try (Connection c = DBConnection.connect()) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT s.Id, s.VehicleId, s.SlotId, s.Start, z.Rate FROM Sessions s " +
                    "JOIN Slots sl ON s.SlotId = sl.Id " +
                    "JOIN Zones z ON sl.ZoneId = z.Id " +
                    "WHERE s.VehicleId=? AND s.End IS NULL");
            ps.setInt(1, vehicleId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("No active session found for this vehicle.");
                return;
            }

            int sessionId = rs.getInt("Id");
            int slotId = rs.getInt("SlotId");
            double ratePerHour = rs.getDouble("Rate");

            c.createStatement().execute("UPDATE Sessions SET End=datetime('now') WHERE Id=" + sessionId);

            ResultSet endRs = c.createStatement().executeQuery(
                    "SELECT Start, End FROM Sessions WHERE Id=" + sessionId);
            endRs.next();

            String startStr = endRs.getString("Start");
            String endStr = endRs.getString("End");

            LocalDateTime start = LocalDateTime.parse(startStr, formatter);
            LocalDateTime end = LocalDateTime.parse(endStr, formatter);
            long minutes = Duration.between(start, end).toMinutes();
            double hours = Math.ceil(minutes / 60.0);
            double fee = hours * ratePerHour;

            c.createStatement().execute("UPDATE Sessions SET Fee=" + fee + " WHERE Id=" + sessionId);

            c.createStatement().execute("UPDATE Slots SET Status='Available' WHERE Id=" + slotId);

            System.out.println("Session ended. Duration: " + minutes + " mins, Fee: $" + fee);
            NotificationService.sendNotification("Parking session ended. Fee: $" + fee);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewSessionHistory(int vehicleId) {
        try (Connection c = DBConnection.connect()) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT s.Id, s.Start, s.End, s.Fee, z.Name as ZoneName " +
                    "FROM Sessions s JOIN Slots sl ON s.SlotId=sl.Id " +
                    "JOIN Zones z ON sl.ZoneId=z.Id WHERE s.VehicleId=? ORDER BY s.Start DESC");
            ps.setInt(1, vehicleId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n=== Session History ===");
            System.out.println("ID | Start | End | Fee | Zone");
            System.out.println("--------------------------------");
            while (rs.next()) {
                System.out.println(rs.getInt("Id") + " | " + rs.getString("Start") + 
                        " | " + (rs.getString("End") == null ? "Active" : rs.getString("End")) + 
                        " | $" + rs.getDouble("Fee") + " | " + rs.getString("ZoneName"));
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewAllSessions() {
        try (Connection c = DBConnection.connect()) {
            ResultSet rs = c.createStatement().executeQuery(
                    "SELECT s.Id, v.Plate, s.Start, s.End, s.Fee, z.Name as ZoneName " +
                    "FROM Sessions s JOIN Vehicles v ON s.VehicleId=v.Id " +
                    "JOIN Slots sl ON s.SlotId=sl.Id JOIN Zones z ON sl.ZoneId=z.Id " +
                    "ORDER BY s.Start DESC");

            System.out.println("\n=== All Sessions ===");
            System.out.println("ID | Plate | Start | End | Fee | Zone");
            System.out.println("----------------------------------------");
            while (rs.next()) {
                System.out.println(rs.getInt("Id") + " | " + rs.getString("Plate") + 
                        " | " + rs.getString("Start") + " | " + 
                        (rs.getString("End") == null ? "Active" : rs.getString("End")) + 
                        " | $" + rs.getDouble("Fee") + " | " + rs.getString("ZoneName"));
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}