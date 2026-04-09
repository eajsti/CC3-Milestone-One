import java.sql.*;

class DBConnection {
    public static Connection connect() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:parking.db");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}