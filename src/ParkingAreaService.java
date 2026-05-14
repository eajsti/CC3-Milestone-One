import java.sql.*;
import java.util.Scanner;

class ParkingAreaService {
    Scanner sc = new Scanner(System.in);

    public void addZone() {
        try (Connection c = DBConnection.connect()) {
            System.out.println("\n=== Add Zone ===");
            System.out.print("Zone Name: ");
            String name = sc.nextLine();
            System.out.print("Hourly Rate: ");
            double rate = Double.parseDouble(sc.nextLine());

            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO Zones(Name,Rate,Status) VALUES(?,?,'Active')");
            ps.setString(1, name);
            ps.setDouble(2, rate);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int zoneId = rs.getInt(1);

            for (int i = 1; i <= 10; i++) {
                PreparedStatement slotPs = c.prepareStatement(
                        "INSERT INTO Slots(ZoneId,Status) VALUES(?,'Available')");
                slotPs.setInt(1, zoneId);
                slotPs.executeUpdate();
            }

            System.out.println("Zone added successfully. (10 slots created)");
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("Error: Zone name already exists.");
            } else {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public void editZone() {
        try (Connection c = DBConnection.connect()) {
            viewZones();
            System.out.print("\nEnter Zone ID to edit: ");
            int zid = Integer.parseInt(sc.nextLine());

            PreparedStatement ps = c.prepareStatement("SELECT * FROM Zones WHERE Id=?");
            ps.setInt(1, zid);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("Zone not found.");
                return;
            }

            System.out.println("\n=== Edit Zone ===");
            System.out.print("New Name (" + rs.getString("Name") + "): ");
            String name = sc.nextLine();
            System.out.print("New Rate (" + rs.getDouble("Rate") + "): ");
            String rateStr = sc.nextLine();

            if (name.isEmpty()) name = rs.getString("Name");
            double rate = rateStr.isEmpty() ? rs.getDouble("Rate") : Double.parseDouble(rateStr);

            PreparedStatement us = c.prepareStatement("UPDATE Zones SET Name=?, Rate=? WHERE Id=?");
            us.setString(1, name);
            us.setDouble(2, rate);
            us.setInt(3, zid);
            us.executeUpdate();

            System.out.println("Zone updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void removeZone() {
        try (Connection c = DBConnection.connect()) {
            viewZones();
            System.out.print("\nEnter Zone ID to remove: ");
            int zid = Integer.parseInt(sc.nextLine());

            c.createStatement().execute("DELETE FROM Slots WHERE ZoneId=" + zid);
            c.createStatement().execute("DELETE FROM Zones WHERE Id=" + zid);
            System.out.println("Zone removed.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewZones() {
        try (Connection c = DBConnection.connect()) {
            ResultSet rs = c.createStatement().executeQuery(
                    "SELECT Id, Name, Rate, Status FROM Zones");
            System.out.println("\n=== Zones ===");
            System.out.println("ID | Name | Rate | Status");
            System.out.println("-------------------------");
            while (rs.next()) {
                System.out.println(rs.getInt("Id") + " | " + rs.getString("Name") + 
                        " | $" + rs.getDouble("Rate") + "/hr | " + rs.getString("Status"));
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void toggleZoneStatus() {
        try (Connection c = DBConnection.connect()) {
            viewZones();
            System.out.print("\nEnter Zone ID: ");
            int zid = Integer.parseInt(sc.nextLine());

            PreparedStatement ps = c.prepareStatement("SELECT Status FROM Zones WHERE Id=?");
            ps.setInt(1, zid);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String newStatus = rs.getString("Status").equals("Active") ? "Inactive" : "Active";
                PreparedStatement us = c.prepareStatement("UPDATE Zones SET Status=? WHERE Id=?");
                us.setString(1, newStatus);
                us.setInt(2, zid);
                us.executeUpdate();
                System.out.println("Zone status changed to " + newStatus);
            } else {
                System.out.println("Zone not found.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void addSlot() {
        try (Connection c = DBConnection.connect()) {
            viewZones();
            System.out.print("\nZone ID: ");
            int zid = Integer.parseInt(sc.nextLine());
            System.out.print("Number of slots to add: ");
            int count = Integer.parseInt(sc.nextLine());

            for (int i = 0; i < count; i++) {
                c.createStatement().execute(
                        "INSERT INTO Slots(ZoneId,Status) VALUES(" + zid + ",'Available')");
            }
            System.out.println(count + " slots added.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewSlotAvailability() {
        try (Connection c = DBConnection.connect()) {
            ResultSet rs = c.createStatement().executeQuery(
                    "SELECT z.Name, s.Id, s.Status FROM Slots s JOIN Zones z ON s.ZoneId=z.Id WHERE z.Status='Active'");
            System.out.println("\n=== Slot Availability ===");
            System.out.println("Zone | Slot ID | Status");
            System.out.println("------------------------");
            while (rs.next()) {
                System.out.println(rs.getString("Name") + " | " + rs.getInt("Id") + " | " + rs.getString("Status"));
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public int selectAvailableSlot(int zoneId) {
        try (Connection c = DBConnection.connect()) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT Id FROM Slots WHERE ZoneId=? AND Status='Available' LIMIT 1");
            ps.setInt(1, zoneId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("Id");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return -1;
    }
}