import java.sql.*;
import java.util.Scanner;

class VehicleService {
    Scanner sc = new Scanner(System.in);

    public void registerVehicle(int userId) {
        try (Connection c = DBConnection.connect()) {
            System.out.println("\n=== Register Vehicle ===");
            System.out.print("Plate Number: ");
            String plate = sc.nextLine();
            System.out.print("Make: ");
            String make = sc.nextLine();
            System.out.print("Model: ");
            String model = sc.nextLine();
            System.out.print("Color: ");
            String color = sc.nextLine();

            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO Vehicles(UserId,Plate,Make,Model,Color,Status) VALUES(?,?,?,?,?,'Active')");
            ps.setInt(1, userId);
            ps.setString(2, plate);
            ps.setString(3, make);
            ps.setString(4, model);
            ps.setString(5, color);
            ps.executeUpdate();

            System.out.println("Vehicle registered successfully.");
            NotificationService.sendNotification("Vehicle " + plate + " registered successfully.");
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("Error: Plate number already exists.");
            } else {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public void viewMyVehicles(int userId) {
        try (Connection c = DBConnection.connect()) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT Id, Plate, Make, Model, Color, Status FROM Vehicles WHERE UserId=?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\n=== My Vehicles ===");
            System.out.println("ID | Plate | Make | Model | Color | Status");
            System.out.println("-------------------------------------------");
            while (rs.next()) {
                System.out.println(rs.getInt("Id") + " | " + rs.getString("Plate") + 
                        " | " + rs.getString("Make") + " | " + rs.getString("Model") + 
                        " | " + rs.getString("Color") + " | " + rs.getString("Status"));
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void editVehicle(int userId) {
        try (Connection c = DBConnection.connect()) {
            viewMyVehicles(userId);
            System.out.print("\nEnter Vehicle ID to edit: ");
            int vid = Integer.parseInt(sc.nextLine());

            PreparedStatement ps = c.prepareStatement(
                    "SELECT * FROM Vehicles WHERE Id=? AND UserId=?");
            ps.setInt(1, vid);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("Vehicle not found or not owned by you.");
                return;
            }

            System.out.println("\n=== Edit Vehicle ===");
            System.out.print("New Make (" + rs.getString("Make") + "): ");
            String make = sc.nextLine();
            System.out.print("New Model (" + rs.getString("Model") + "): ");
            String model = sc.nextLine();
            System.out.print("New Color (" + rs.getString("Color") + "): ");
            String color = sc.nextLine();

            if (make.isEmpty()) make = rs.getString("Make");
            if (model.isEmpty()) model = rs.getString("Model");
            if (color.isEmpty()) color = rs.getString("Color");

            PreparedStatement us = c.prepareStatement(
                    "UPDATE Vehicles SET Make=?, Model=?, Color=? WHERE Id=?");
            us.setString(1, make);
            us.setString(2, model);
            us.setString(3, color);
            us.setInt(4, vid);
            us.executeUpdate();

            System.out.println("Vehicle updated.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public int selectVehicle(int userId) {
        try (Connection c = DBConnection.connect()) {
            PreparedStatement ps = c.prepareStatement(
                    "SELECT Id, Plate FROM Vehicles WHERE UserId=? AND Status='Active'");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            System.out.println("\nAvailable Vehicles:");
            while (rs.next()) {
                System.out.println(rs.getInt("Id") + ". " + rs.getString("Plate"));
            }

            System.out.print("Select Vehicle ID: ");
            return Integer.parseInt(sc.nextLine());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return -1;
        }
    }
}