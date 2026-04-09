import java.sql.*;

class DatabaseSetup {
    public static void init() {
        try (Connection c = DBConnection.connect(); Statement s = c.createStatement()) {

            s.execute("CREATE TABLE IF NOT EXISTS Users(" +
                    "Id INTEGER PRIMARY KEY, " +
                    "Username TEXT UNIQUE, " +
                    "Password TEXT, " +
                    "Email TEXT, " +
                    "Phone TEXT, " +
                    "Role TEXT, " +
                    "Status TEXT DEFAULT 'Active')");

            s.execute("CREATE TABLE IF NOT EXISTS Vehicles(" +
                    "Id INTEGER PRIMARY KEY, " +
                    "UserId INT, " +
                    "Plate TEXT UNIQUE, " +
                    "Make TEXT, " +
                    "Model TEXT, " +
                    "Color TEXT, " +
                    "Status TEXT DEFAULT 'Active', " +
                    "FOREIGN KEY(UserId) REFERENCES Users(Id))");

            s.execute("CREATE TABLE IF NOT EXISTS Zones(" +
                    "Id INTEGER PRIMARY KEY, " +
                    "Name TEXT UNIQUE, " +
                    "Rate REAL, " +
                    "Status TEXT DEFAULT 'Active')");

            s.execute("CREATE TABLE IF NOT EXISTS Slots(" +
                    "Id INTEGER PRIMARY KEY, " +
                    "ZoneId INT, " +
                    "Status TEXT DEFAULT 'Available', " +
                    "FOREIGN KEY(ZoneId) REFERENCES Zones(Id))");

            s.execute("CREATE TABLE IF NOT EXISTS Sessions(" +
                    "Id INTEGER PRIMARY KEY, " +
                    "VehicleId INT, " +
                    "SlotId INT, " +
                    "Start TEXT, " +
                    "End TEXT, " +
                    "Fee REAL DEFAULT 0, " +
                    "FOREIGN KEY(VehicleId) REFERENCES Vehicles(Id), " +
                    "FOREIGN KEY(SlotId) REFERENCES Slots(Id))");

            s.execute("CREATE TABLE IF NOT EXISTS Tickets(" +
                    "Id INTEGER PRIMARY KEY, " +
                    "Plate TEXT, " +
                    "SessionId INT, " +
                    "ZoneId INT, " +
                    "ViolationType TEXT, " +
                    "Amount REAL, " +
                    "Status TEXT DEFAULT 'Pending', " +
                    "DueDate TEXT, " +
                    "IssuedAt TEXT, " +
                    "FOREIGN KEY(SessionId) REFERENCES Sessions(Id), " +
                    "FOREIGN KEY(ZoneId) REFERENCES Zones(Id))");

            s.execute("CREATE TABLE IF NOT EXISTS Notifications(" +
                    "Id INTEGER PRIMARY KEY, " +
                    "UserId INT, " +
                    "Message TEXT, " +
                    "Channel TEXT, " +
                    "Status TEXT DEFAULT 'Pending', " +
                    "CreatedAt TEXT, " +
                    "FOREIGN KEY(UserId) REFERENCES Users(Id))");

            s.execute("INSERT OR IGNORE INTO Users(Id,Username,Password,Email,Phone,Role,Status) VALUES (1,'admin','240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9','admin@parking.com','1234567890','Admin','Active')");
            s.execute("INSERT OR IGNORE INTO Users(Id,Username,Password,Email,Phone,Role,Status) VALUES (2,'officer1','ecd71870d196cb6a38d13db1834d0a72d5d66cb946b3a501b99108d9f23e5c3e','officer1@parking.com','1234567891','Officer','Active')");

            ResultSet rs = s.executeQuery("SELECT COUNT(*) as cnt FROM Zones");
            if (rs.next() && rs.getInt("cnt") == 0) {
                s.execute("INSERT INTO Zones(Id,Name,Rate,Status) VALUES (1,'Zone A',50.0,'Active')");
                for (int i = 1; i <= 10; i++) {
                    s.execute("INSERT INTO Slots(ZoneId,Status) VALUES (1,'Available')");
                }
                s.execute("INSERT INTO Zones(Id,Name,Rate,Status) VALUES (2,'Zone B',30.0,'Active')");
                for (int i = 1; i <= 10; i++) {
                    s.execute("INSERT INTO Slots(ZoneId,Status) VALUES (2,'Available')");
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}