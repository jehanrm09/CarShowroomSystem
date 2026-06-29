package CarShowroomsystem;
import User.User;
import Manager.Manager;
import Employee.*;
import java.sql.*;
import java.util.*;

class CarShowroomSystem {
    String DB_PASSWORD = "";
    String DB_URL = "jdbc:mysql://localhost:3306/car_showroom";
    String DB_USER = "root";
    Connection con;

    CarShowroomSystem() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        catch (Exception e) {
            System.out.println("Error connecting to database: " + e.getMessage());
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        CarShowroomSystem system = new CarShowroomSystem();
        Scanner sc = new Scanner(System.in);

        while (true) {
            try {
                System.out.println("\n=== Car Showroom Management System ===");
                System.out.println("1. Login");
                System.out.println("2. Register New User (Manager Only)");
                System.out.println("3. Exit");
                System.out.println();
                System.out.print("Select an option: ");

                String input = sc.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("Please enter a valid option.");
                    continue;
                }

                int choice;
                try {
                    choice = Integer.parseInt(input);
                }
                catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number (1-3).");
                    continue;
                }

                switch (choice) {
                    case 1:
                        system.login();
                        break;
                    case 2:
                        system.registerNewUser();
                        break;
                    case 3:
                        System.out.println("Exiting system. Goodbye!");
                        sc.close();
                        System.exit(0);
                    default:
                        System.out.println("Invalid option. Please enter 1, 2, or 3.");
                }
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    // Validation methods
    public boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String trimmedUsername = username.trim();
        if (trimmedUsername.length() < 3) {
            System.out.println("Username must be at least 3 characters long.");
            return false;
        }

        // Check if username contains only letters
        for (int i = 0; i < trimmedUsername.length(); i++) {
            char c = trimmedUsername.charAt(i);
            if (!Character.isLetter(c)) {
                System.out.println("Username can only contain letters. No numbers or special characters allowed.");
                return false;
            }
        }

        return true;
    }

    public boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            System.out.println("Password cannot be empty.");
            return false;
        }

        String trimmedPassword = password.trim();
        if (trimmedPassword.length() < 6) {
            System.out.println("Password must be at least 6 characters long.");
            return false;
        }

        boolean hasUpperCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";

        for (int i = 0; i < trimmedPassword.length(); i++) {
            char c = trimmedPassword.charAt(i);

            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            }
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (specialChars.indexOf(c) != -1) {
                hasSpecialChar = true;
            }
        }

        if (!hasUpperCase) {
            System.out.println("Password must contain at least one uppercase letter.");
            return false;
        }
        if (!hasDigit) {
            System.out.println("Password must contain at least one digit.");
            return false;
        }
        if (!hasSpecialChar) {
            System.out.println("Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?).");
            return false;
        }

        return true;
    }

    public void login() {
        Scanner sc = new Scanner(System.in);

        try {
            System.out.println("\n=== Login ===");

            System.out.println();
            System.out.println("Username must be letters only and having minimum 3 characters.");
            String username;
            while (true) {
                System.out.print("Username: ");
                username = sc.nextLine().trim();
                if (isValidUsername(username))
                {
                    break;
                }
                System.out.println("Please enter a valid username (letters only, min 3 characters).");
            }

            System.out.println();
            System.out.println("Password must be of minimum 6 characters in which 1 uppercase 1 digit and 1 special char");
            String password;
            while (true) {
                System.out.print("Password: ");
                password = sc.nextLine().trim();
                if (isValidPassword(password)) {
                    break;
                }
                System.out.println("Please enter a valid password (min 6 chars, 1 uppercase, 1 digit, 1 special char).");
            }

            String sql = "select * from users where username = ? and password = ?";
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, username);
            st.setString(2, password);

            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                System.out.println("Welcome Back " + username + "!");

                User user;
                if (role.equals("MANAGER")) {
                    user = new Manager(username);
                } else {
                    user = new Employee(username);
                }

                user.showDashboard();
            }
            else {
                System.out.println("Access Denied – Invalid credentials.");
            }
        }
        catch (SQLException e) {
            System.out.println("Database error during login: " + e.getMessage());
        }
        catch (Exception e) {
            System.out.println("An unexpected error occurred during login: " + e.getMessage());
        }
    }

    public void registerNewUser() {
        Scanner sc = new Scanner(System.in);

        try {
            System.out.println("\n=== Manager Authentication Required ===");

            String managerUsername;
            while (true) {
                System.out.print("Manager Username: ");
                managerUsername = sc.nextLine().trim();
                if (isValidUsername(managerUsername)) {
                    break;
                }
                System.out.println("Please enter a valid manager username.");
            }

            String managerPassword;
            while (true) {
                System.out.print("Manager Password: ");
                managerPassword = sc.nextLine().trim();
                if (isValidPassword(managerPassword)) {
                    break;
                }
                System.out.println("Please enter a valid manager password.");
            }

            // Verify manager details
            String authSql = "select * from users where username = ? and password = ? and role = 'MANAGER'";
            PreparedStatement auth = con.prepareStatement(authSql);
            auth.setString(1, managerUsername);
            auth.setString(2, managerPassword);

            ResultSet authRs = auth.executeQuery();

            if (!authRs.next()) {
                System.out.println("Access Denied – Only managers can register new users.");
                return;
            }

            System.out.println("\n=== Register New User ===");

            String newUsername;
            while (true) {
                System.out.print("Enter new username: ");
                newUsername = sc.nextLine().trim();
                if (isValidUsername(newUsername)) {
                    // Check if username already exists
                    String checkSql = "select * from users where username = ?";
                    PreparedStatement checkSt = con.prepareStatement(checkSql);
                    checkSt.setString(1, newUsername);
                    ResultSet checkRs = checkSt.executeQuery();

                    if (checkRs.next()) {
                        System.out.println("Username already exists. Please choose a different username.");
                    } else {
                        break;
                    }
                } else {
                    System.out.println("Please enter a valid username.");
                }
            }

            String newPassword;
            while (true) {
                System.out.print("Enter new password: ");
                newPassword = sc.nextLine().trim();
                if (isValidPassword(newPassword)) {
                    break;
                }
                System.out.println("Please enter a valid password.");
            }

            String role;
            while (true) {
                System.out.println("Select role:");
                System.out.println("1. EMPLOYEE");
                System.out.println("2. MANAGER");
                System.out.print("Enter choice (1 or 2): ");

                String roleInput = sc.nextLine().trim();
                if (roleInput.equals("1")) {
                    role = "EMPLOYEE";
                    break;
                } else if (roleInput.equals("2")) {
                    role = "MANAGER";
                    break;
                } else {
                    System.out.println("Invalid choice. Please enter 1 for EMPLOYEE or 2 for MANAGER.");
                }
            }

            // Insert new user
            String insertSql = "insert into users (username, password, role) values (?, ?, ?)";
            PreparedStatement insertSt = con.prepareStatement(insertSql);
            insertSt.setString(1, newUsername);
            insertSt.setString(2, newPassword);
            insertSt.setString(3, role);

            int r = insertSt.executeUpdate();

            if (r > 0) {
                System.out.println("User '" + newUsername + "' registered successfully as " + role + "!");
            }
            else {
                System.out.println("Failed to register user.");
            }
        }
        catch (SQLException e) {
            System.out.println("Database error during registration: " + e.getMessage());
        }
        catch (Exception e) {
            System.out.println("An unexpected error occurred during registration: " + e.getMessage());
        }
    }
}