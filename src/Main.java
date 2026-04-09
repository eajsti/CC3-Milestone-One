import java.sql.*;
import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        DatabaseSetup.init();

        Scanner sc = new Scanner(System.in);

        UserService user = new UserService();
        VehicleService vehicle = new VehicleService();
        ParkingAreaService area = new ParkingAreaService();
        ParkingSessionService session = new ParkingSessionService();
        TicketService ticket = new TicketService();
        FineService fine = new FineService();

        while (true) {
            System.out.println("\n=== Smart Parking System ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choice: ");
            String choice = sc.nextLine();

            if (choice.equals("1")) {
                user.register();
            } else if (choice.equals("2")) {
                String role = user.login();
                if (role == null) {
                    continue;
                }

                if (role.equalsIgnoreCase("Driver")) {
                    driverMenu(sc, user, vehicle, area, session, ticket, fine);
                } else if (role.equalsIgnoreCase("Officer")) {
                    officerMenu(sc, ticket, fine, area);
                } else if (role.equalsIgnoreCase("Admin")) {
                    adminMenu(sc, user, area, session, ticket, fine);
                }
            } else {
                System.out.println("Goodbye!");
                break;
            }
        }
    }

    static void driverMenu(Scanner sc, UserService user, VehicleService vehicle, 
            ParkingAreaService area, ParkingSessionService session, 
            TicketService ticket, FineService fine) {
        int uid = UserService.getCurrentUserId();
        while (true) {
            System.out.println("\n--- DRIVER MENU ---");
            System.out.println("1. Register Vehicle");
            System.out.println("2. View My Vehicles");
            System.out.println("3. Edit Vehicle");
            System.out.println("4. Start Parking");
            System.out.println("5. End Parking");
            System.out.println("6. View Session History");
            System.out.println("7. View My Tickets");
            System.out.println("8. Pay Fine");
            System.out.println("9. Update Profile");
            System.out.println("10. Change Password");
            System.out.println("11. Logout");
            System.out.print("Choice: ");
            String ch = sc.nextLine();

            if (ch.equals("1")) {
                vehicle.registerVehicle(uid);
            } else if (ch.equals("2")) {
                vehicle.viewMyVehicles(uid);
            } else if (ch.equals("3")) {
                vehicle.editVehicle(uid);
            } else if (ch.equals("4")) {
                area.viewZones();
                System.out.print("Select Zone ID: ");
                int zid = Integer.parseInt(sc.nextLine());
                int vid = vehicle.selectVehicle(uid);
                if (vid > 0) {
                    session.startSession(vid, zid);
                }
            } else if (ch.equals("5")) {
                int vid = vehicle.selectVehicle(uid);
                if (vid > 0) {
                    session.endSession(vid);
                }
            } else if (ch.equals("6")) {
                int vid = vehicle.selectVehicle(uid);
                if (vid > 0) {
                    session.viewSessionHistory(vid);
                }
            } else if (ch.equals("7")) {
                int vid = vehicle.selectVehicle(uid);
                if (vid > 0) {
                    try (Connection c = DBConnection.connect()) {
                        PreparedStatement ps = c.prepareStatement("SELECT Plate FROM Vehicles WHERE Id=?");
                        ps.setInt(1, vid);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            ticket.viewMyTickets(rs.getString("Plate"));
                        }
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            } else if (ch.equals("8")) {
                fine.payFine();
            } else if (ch.equals("9")) {
                user.updateProfile();
            } else if (ch.equals("10")) {
                user.changePassword();
            } else if (ch.equals("11")) {
                user.logout();
                break;
            }
        }
    }

    static void officerMenu(Scanner sc, TicketService ticket, FineService fine, ParkingAreaService area) {
        while (true) {
            System.out.println("\n--- OFFICER MENU ---");
            System.out.println("1. Issue Ticket");
            System.out.println("2. View All Tickets");
            System.out.println("3. View Fines by Status");
            System.out.println("4. Logout");
            System.out.print("Choice: ");
            String ch = sc.nextLine();

            if (ch.equals("1")) {
                area.viewZones();
                System.out.print("Select Zone ID (your area): ");
                int zid = Integer.parseInt(sc.nextLine());
                ticket.issueTicket(zid);
            } else if (ch.equals("2")) {
                ticket.viewTickets();
            } else if (ch.equals("3")) {
                fine.viewFinesByStatus();
            } else if (ch.equals("4")) {
                break;
            }
        }
    }

    static void adminMenu(Scanner sc, UserService user, ParkingAreaService area, 
            ParkingSessionService session, TicketService ticket, FineService fine) {
        while (true) {
            System.out.println("\n--- ADMIN MENU ---");
            System.out.println("1. Manage Zones");
            System.out.println("2. Manage Slots");
            System.out.println("3. View All Sessions");
            System.out.println("4. View All Tickets");
            System.out.println("5. Apply Penalties");
            System.out.println("6. View Outstanding Balances");
            System.out.println("7. Manage Users");
            System.out.println("8. Logout");
            System.out.print("Choice: ");
            String ch = sc.nextLine();

            if (ch.equals("1")) {
                zoneMenu(sc, area);
            } else if (ch.equals("2")) {
                slotMenu(sc, area);
            } else if (ch.equals("3")) {
                session.viewAllSessions();
            } else if (ch.equals("4")) {
                ticket.viewTickets();
            } else if (ch.equals("5")) {
                fine.applyPenalties();
            } else if (ch.equals("6")) {
                fine.viewOutstandingBalances();
            } else if (ch.equals("7")) {
                user.manageUsers();
            } else if (ch.equals("8")) {
                break;
            }
        }
    }

    static void zoneMenu(Scanner sc, ParkingAreaService area) {
        while (true) {
            System.out.println("\n--- Zone Management ---");
            System.out.println("1. View Zones");
            System.out.println("2. Add Zone");
            System.out.println("3. Edit Zone");
            System.out.println("4. Remove Zone");
            System.out.println("5. Toggle Zone Status");
            System.out.println("6. Back");
            System.out.print("Choice: ");
            String ch = sc.nextLine();

            if (ch.equals("1")) {
                area.viewZones();
            } else if (ch.equals("2")) {
                area.addZone();
            } else if (ch.equals("3")) {
                area.editZone();
            } else if (ch.equals("4")) {
                area.removeZone();
            } else if (ch.equals("5")) {
                area.toggleZoneStatus();
            } else if (ch.equals("6")) {
                break;
            }
        }
    }

    static void slotMenu(Scanner sc, ParkingAreaService area) {
        while (true) {
            System.out.println("\n--- Slot Management ---");
            System.out.println("1. View Slot Availability");
            System.out.println("2. Add Slots");
            System.out.println("3. Back");
            System.out.print("Choice: ");
            String ch = sc.nextLine();

            if (ch.equals("1")) {
                area.viewSlotAvailability();
            } else if (ch.equals("2")) {
                area.addSlot();
            } else if (ch.equals("3")) {
                break;
            }
        }
    }
}