package User;
import java.sql.SQLException;

public abstract class User {
    String username;
    public  User(String username) {
        this.username = username;
    }
    public abstract void showDashboard() throws SQLException;
}