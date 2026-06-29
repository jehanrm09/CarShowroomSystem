import java.io.*;
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
            System.out.println("\n=== Car Showroom Management System ===");
            System.out.println("1. Login");
            System.out.println("2. Register New User (Manager Only)");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");

            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
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
                    System.exit(0);
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    void login() {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n=== Login ===");
        System.out.print("Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();

        try {
            String sql = "select * from users where username = ? and password = ?";
            PreparedStatement st = con.prepareStatement(sql);
            st.setString(1, username);
            st.setString(2, password);

            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                System.out.println("Welcome Back");

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
    }

    void registerNewUser() {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n=== Manager Authentication Required ===");
        System.out.print("Manager Username: ");
        String managerUsername = sc.nextLine();
        System.out.print("Manager Password: ");
        String managerPassword = sc.nextLine();

        try {
            //verify manage details

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
            System.out.print("Enter new username: ");
            String newUsername = sc.nextLine();

            String checkSql = "select * from users where username = ?";
            PreparedStatement checkSt = con.prepareStatement(checkSql);
            checkSt.setString(1, newUsername);
            ResultSet checkRs = checkSt.executeQuery();

            //check kre chee
            if (checkRs.next()) {
                System.out.println("Username already exists. Please choose a different username.");
                return;
            }
            System.out.print("Enter new password: ");
            String newPassword = sc.nextLine();

            System.out.println("Select role:");
            System.out.println("1. EMPLOYEE");
            System.out.println("2. MANAGER");
            System.out.print("Enter choice: ");
            int n = sc.nextInt();
            String role;
            if(n==1){
                role = "EMPLOYEE";
            }
            else{
                role = "MANAGER";
            }

            //insert new user
            String insertSql = "insert into users (username, password, role) values (?, ?, ?)";
            PreparedStatement insertSt = con.prepareStatement(insertSql);
            insertSt.setString(1, newUsername);
            insertSt.setString(2, newPassword);
            insertSt.setString(3, role);

            int r = insertSt.executeUpdate();

            if (r > 0) {
                System.out.println("User registered successfully!");
            }
            else {
                System.out.println("Failed to register user.");
            }
        }
        catch (SQLException e) {
            System.out.println("Database error during registration: " + e.getMessage());
        }
    }
}

abstract class User {
    String username;
    User(String username) {
        this.username = username;
    }
    abstract void showDashboard() throws SQLException;
}

class Manager extends User {
    CarStack recentViewedCars = new CarStack();
    Manager(String username) {
        super(username);
    }

    String DB_URL = "jdbc:mysql://localhost:3306/car_showroom";
    String DB_USER = "root";
    String DB_PASSWORD = "";

    //manager dashboard
    @Override
    void showDashboard() throws SQLException {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== Manager Dashboard ===");
            System.out.println("1. View All Cars");
            System.out.println("2. Add New Car");
            System.out.println("3. Update Car Details");
            System.out.println("4. Delete Car");
            System.out.println("5. View All Bookings");
            System.out.println("6. View Available Cars");
            System.out.println("7. View Recently Viewed Cars");
            System.out.println("8. Back to Main Menu");
            System.out.print("Select an option: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    viewAllCars();
                    break;
                case 2:
                    addNewCar();
                    break;
                case 3:
                    updateCar();
                    break;
                case 4:
                    deleteCar();
                    break;
                case 5:
                    viewAllBookings();
                    break;
                case 6:
                    viewAvailableCars();
                    break;
                case 7:
                    recentViewedCars.display();
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    void viewAllCars() {
        System.out.println("\n=== All Cars ===");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "select * from cars";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            System.out.println("ID\tName\tBrand\tModel\tPrice\tFuel Type\tAvailable");

            while (rs.next()) {
                System.out.println(
                        rs.getInt("id") + "\t" +
                                rs.getString("name") + "\t" +
                                rs.getString("brand") + "\t" +
                                rs.getString("model") + "\t" +
                                rs.getDouble("price") + "\t" +
                                rs.getString("fuel_type") + "\t" +
                                (rs.getBoolean("availability") ? "Yes" : "No")
                );
            }
        }
        catch (SQLException e) {
            System.out.println("Error viewing cars: " + e.getMessage());
        }
    }

    void addNewCar() {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n=== Add New Car ===");

        System.out.print("Car Name: ");
        String name = sc.nextLine();
        System.out.print("Brand: ");
        String brand = sc.nextLine();
        System.out.print("Model: ");
        String model = sc.nextLine();
        System.out.print("Price: ");
        double price = sc.nextDouble();
        System.out.print("Fuel Type (Petrol/Diesel/Electric/Hybrid): ");
        sc.nextLine();
        String fuelType = sc.nextLine();
        System.out.print("Available (Y/N): ");
        boolean available = sc.nextLine().equalsIgnoreCase("Y");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "insert into cars (name, brand, model, price, fuel_type, availability) values (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, brand);
            stmt.setString(3, model);
            stmt.setDouble(4, price);
            stmt.setString(5, fuelType);
            stmt.setBoolean(6, available);

            int r = stmt.executeUpdate();
            if (r > 0) {
                System.out.println("Car added successfully!");
            } else {
                System.out.println("Failed to add car.");
            }
        }
        catch (SQLException e) {
            System.out.println("Error adding car: " + e.getMessage());
        }
    }

    void updateCar() {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n=== Update Car ===");
        viewAllCars();

        System.out.print("Enter ID of car to update: ");
        int id = sc.nextInt();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            String checkSql = "select * from cars where id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("Car with ID " + id + " not found.");
                return;
            }

            // display current detail
            System.out.println("\nCurrent Details:");
            System.out.println("Name: " + rs.getString("name"));
            System.out.println("Brand: " + rs.getString("brand"));
            System.out.println("Model: " + rs.getString("model"));
            System.out.println("Price: " + rs.getDouble("price"));
            System.out.println("Fuel Type: " + rs.getString("fuel_type"));
            System.out.println("Available: " + (rs.getBoolean("availability") ? "Yes" : "No"));

            // entre new detail
            System.out.println("\nEnter new details (leave blank to keep current value):");

            System.out.print("Name [" + rs.getString("name") + "]: ");
            String name = sc.nextLine();
            name = name.isEmpty() ? rs.getString("name") : name;

            System.out.print("Brand [" + rs.getString("brand") + "]: ");
            String brand = sc.nextLine();
            brand = brand.isEmpty() ? rs.getString("brand") : brand;

            System.out.print("Model [" + rs.getString("model") + "]: ");
            String model = sc.nextLine();
            model = model.isEmpty() ? rs.getString("model") : model;

            System.out.print("Price [" + rs.getDouble("price") + "]: ");
            String priceInput = sc.nextLine();
            double price = priceInput.isEmpty() ? rs.getDouble("price") : Double.parseDouble(priceInput);

            System.out.print("Fuel Type [" + rs.getString("fuel_type") + "]: ");
            String fuelType = sc.nextLine();
            fuelType = fuelType.isEmpty() ? rs.getString("fuel_type") : fuelType;

            System.out.print("Available (Y/N) [" + (rs.getBoolean("availability") ? "Y" : "N") + "]: ");
            String availableI = sc.nextLine();
            boolean available = availableI.isEmpty() ? rs.getBoolean("availability") : availableI.equalsIgnoreCase("Y");

            // update details
            String updateSql = "update cars set name = ?, brand = ?, model = ?, price = ?, fuel_type = ?, availability = ? where id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, name);
            updateStmt.setString(2, brand);
            updateStmt.setString(3, model);
            updateStmt.setDouble(4, price);
            updateStmt.setString(5, fuelType);
            updateStmt.setBoolean(6, available);
            updateStmt.setInt(7, id);

            int r = updateStmt.executeUpdate();
            if (r > 0) {
                System.out.println("Car updated successfully!");
            } else {
                System.out.println("Failed to update car.");
            }
        }
        catch (SQLException e) {
            System.out.println("Error updating car: " + e.getMessage());
        }
    }

    void deleteCar() {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n=== Delete Car ===");
        viewAllCars();

        System.out.print("Enter ID of car to delete: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Are you sure you want to delete car with ID " + id + "? (Y/N): ");
        String conf = sc.nextLine();
        if (!conf.equalsIgnoreCase("Y")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "delete from cars where id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            int r = stmt.executeUpdate();
            if (r > 0) {
                System.out.println("Car deleted successfully!");
            }
            else {
                System.out.println("No car found with ID " + id);
            }
        }
        catch (SQLException e) {
            System.out.println("Error deleting car: " + e.getMessage());
        }
    }

    void viewAllBookings() throws SQLException {
        System.out.println("\n=== All Bookings ===");
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        try {
            String sql = """
                select b.booking_id, b.customer_name, b.phone, b.booking_date, b.amount,
                c.name as car_name, c.brand, c.model
                from bookings b join cars c on b.car_id = c.id
                order by b.booking_date desc;
            """;


            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            System.out.println("Booking ID\tCustomer\tPhone\tBooking Date\tAmount\tCar");
            while (rs.next()) {
                System.out.println(
                        rs.getInt("booking_id") + "\t" +
                                rs.getString("customer_name") + "\t" +
                                rs.getString("phone") + "\t" +
                                rs.getTimestamp("booking_date") + "\t" +
                                rs.getDouble("amount") + "\t" +
                                rs.getString("brand") + " " + rs.getString("model")
                );
            }
        }
        catch (SQLException e) {
            System.out.println("Error viewing bookings: " + e.getMessage());
        }
    }

    void viewAvailableCars() {
        System.out.println("\n=== Available Cars ===");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "select * from cars where availability = true";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            System.out.println("ID\tName\tBrand\tModel\tPrice\tFuel Type");

            while (rs.next()) {
                System.out.println(
                        rs.getInt("id") + "\t" +
                                rs.getString("name") + "\t" +
                                rs.getString("brand") + "\t" +
                                rs.getString("model") + "\t" +
                                rs.getDouble("price") + "\t" +
                                rs.getString("fuel_type")
                );

                System.out.print("View details of this car? (Y/N): ");
                Scanner sc = new Scanner(System.in);
                String choice = sc.nextLine();
                if (choice.equalsIgnoreCase("Y")) {
                    String carName = rs.getString("name");
                    recentViewedCars.push(carName);
                    viewCarDetails(rs.getInt("id"));
                }
            }
        }
        catch (SQLException e) {
            System.out.println("Error viewing available cars: " + e.getMessage());
        }
    }

    void viewCarDetails(int carId) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        try {
            String sql = "select * from cars where id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n=== Car Details ===");
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Brand: " + rs.getString("brand"));
                System.out.println("Model: " + rs.getString("model"));
                System.out.println("Price: " + rs.getDouble("price"));
                System.out.println("Fuel Type: " + rs.getString("fuel_type"));
                System.out.println("Available: " + (rs.getBoolean("availability") ? "Yes" : "No"));
            }
        }
        catch (SQLException e) {
            System.out.println("Error viewing car details: " + e.getMessage());
        }
    }
}

class Employee extends User {
    CarStack recentViewedCars = new CarStack();

    String DB_URL = "jdbc:mysql://localhost:3306/car_showroom";
    String DB_USER = "root";
    String DB_PASSWORD = "";

    Employee(String username) {
        super(username);
    }

    @Override
    void showDashboard()  {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== Employee Dashboard ===");
            System.out.println("1. View Available Cars");
            System.out.println("2. Book a Car for Customer");
            System.out.println("3. View All Bookings");
            System.out.println("4. View Recently Viewed Cars");
            System.out.println("5. Back to Main Menu");
            System.out.print("Select an option: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    viewAvailableCars();
                    break;
                case 2:
                    bookCarForCustomer();
                    break;
                case 3:
                    viewAllBookings();
                    break;
                case 4:
                    recentViewedCars.display();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    void viewAvailableCars() {
        System.out.println("\n=== Available Cars ===");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "select * from cars where availability = true";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            System.out.println("ID\tName\tBrand\tModel\tPrice\tFuel Type");

            while (rs.next()) {
                System.out.println(
                        rs.getInt("id") + "\t" +
                                rs.getString("name") + "\t" +
                                rs.getString("brand") + "\t" +
                                rs.getString("model") + "\t" +
                                rs.getDouble("price") + "\t" +
                                rs.getString("fuel_type")
                );

                //push to stack if you see details
                System.out.print("View details of this car? (Y/N): ");
                Scanner sc = new Scanner(System.in);
                String choice = sc.nextLine();

                if (choice.equalsIgnoreCase("Y")) {
                    String carName = rs.getString("name");
                    recentViewedCars.push(carName);
                    viewCarDetails(rs.getInt("id"));
                }
            }
        }
        catch (SQLException e) {
            System.out.println("Error viewing available cars: " + e.getMessage());
        }
    }

    void bookCarForCustomer() {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n=== Book a Car for Customer ===");

        System.out.print("Customer Name: ");
        String customerName = sc.nextLine();

        if (customerName.trim().isEmpty()) {
            System.out.println("Customer name cannot be empty!");
            return;
        }

        System.out.print("Phone Number: ");
        String phone = sc.nextLine();

        if (phone.trim().isEmpty()) {
            System.out.println("Phone number cannot be empty!");
            return;
        }

        viewAvailableCars();

        System.out.print("Enter Car ID to book: ");
        int carId = sc.nextInt();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String carSql = "select price, availability from cars where id = ?";
            PreparedStatement carStmt = conn.prepareStatement(carSql);
            carStmt.setInt(1, carId);
            ResultSet carRs = carStmt.executeQuery();

            if (!carRs.next()) {
                System.out.println("Invalid Car ID!");
                return;
            }

            double amount = carRs.getDouble("price");
            boolean isAvailable = carRs.getBoolean("availability");

            if (isAvailable) {
                conn.setAutoCommit(false);

                try {
                    // insert in booking
                    String insertSql = "insert into bookings (customer_name, phone, car_id, amount) values (?, ?, ?, ?)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                    insertStmt.setString(1, customerName);
                    insertStmt.setString(2, phone);
                    insertStmt.setInt(3, carId);
                    insertStmt.setDouble(4, amount);

                    int rowsAffected = insertStmt.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new SQLException("Creating booking failed, no rows affected.");
                    }

                    int bookingId;
                    try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            bookingId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creating booking failed, no id obtained.");
                        }
                    }

                    // new car availabilitu
                    String updateSql = "update cars set availability = false where id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setInt(1, carId);
                    updateStmt.executeUpdate();

                    conn.commit();

                    System.out.println("Booking successful! Booking ID: " + bookingId);
                    System.out.println("Amount to pay: Rs" + amount);
                    sc.nextLine();

                    System.out.print("Generate bill now? (Y/N): ");
                    String generateBill = sc.nextLine();
                    if (generateBill.equalsIgnoreCase("Y")) {
                        generateBill(bookingId);
                    }

                }
                catch (SQLException e) {
                    conn.rollback();
                    System.out.println("Error creating booking: " + e.getMessage());
                }
                finally {
                    conn.setAutoCommit(true);
                }
            }
            else {
                // if car not available adding it in wait list
                System.out.println("Car is not available.");
            }
        }
        catch (SQLException e) {
            System.out.println("Database error during booking: " + e.getMessage());
        }
    }

    void generateBill(int bookingId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "select b.booking_id, b.customer_name, b.phone, b.booking_date, b.amount, " +
                    "c.name as car_name, c.brand, c.model " +
                    "from bookings b join cars c on b.car_id = c.id " +
                    "where b.booking_id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // display bill
                System.out.println("\n=== BILL ===");
                System.out.println("Booking ID: " + rs.getInt("booking_id"));
                System.out.println("Customer: " + rs.getString("customer_name"));
                System.out.println("Phone: " + rs.getString("phone"));
                System.out.println("Car: " + rs.getString("brand") + " " +
                        rs.getString("model") + " (" + rs.getString("car_name") + ")");
                System.out.println("Booking Date: " + rs.getTimestamp("booking_date"));
                System.out.println("Amount: Rs." + rs.getDouble("amount"));
                System.out.println("=========================");

                saveBillToFile(rs);
                System.out.println("Bill saved to file: bill_" + bookingId + ".txt");
            }
        }
        catch (SQLException e) {
            System.out.println("Error generating bill: " + e.getMessage());
        }
    }

    void saveBillToFile(ResultSet rs) throws SQLException {
        int bookingId = rs.getInt("booking_id");
        String fileName = "bill_" + bookingId + ".txt";

        try (FileWriter fw = new FileWriter(fileName)) {
            fw.write("=== CAR SHOWROOM BILL ===\n\n");
            fw.write("Booking ID: " + bookingId + "\n");
            fw.write("Customer: " + rs.getString("customer_name") + "\n");
            fw.write("Phone: " + rs.getString("phone") + "\n");
            fw.write("Car: " + rs.getString("brand") + " " +
                    rs.getString("model") + " (" + rs.getString("car_name") + ")\n");
            fw.write("Booking Date: " + rs.getTimestamp("booking_date") + "\n");
            fw.write("Amount: Rs." + rs.getDouble("amount") + "\n");
            fw.write("\n=========================\n");
            fw.write("Thank you for your business!\n");
        }
        catch (IOException e) {
            System.out.println("Error saving bill to file: " + e.getMessage());
        }
    }

    void viewAllBookings() {
        System.out.println("\n=== All Bookings ===");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "select b.booking_id, b.customer_name, b.phone, b.booking_date, b.amount, " +
                    "c.name as car_name, c.brand, c.model " +
                    "from bookings b join cars c on b.car_id = c.id " +
                    "order by b.booking_date desc";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            System.out.println("Booking ID\tCustomer\tPhone\tBooking Date\tAmount\tCar");

            while (rs.next()) {
                System.out.println(
                        rs.getInt("booking_id") + "\t" +
                                rs.getString("customer_name") + "\t" +
                                rs.getString("phone") + "\t" +
                                rs.getTimestamp("booking_date").toString() + "\t" +
                                rs.getDouble("amount") + "\t" +
                                rs.getString("brand") + " " + rs.getString("model")
                );
            }
        }

        catch (SQLException e) {
            System.out.println("Error viewing bookings: " + e.getMessage());
        }
    }

    void viewCarDetails(int carId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "select * from cars where id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, carId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("\n=== Car Details ===");
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Brand: " + rs.getString("brand"));
                System.out.println("Model: " + rs.getString("model"));
                System.out.println("Price: Rs" + rs.getDouble("price"));
                System.out.println("Fuel Type: " + rs.getString("fuel_type"));
                System.out.println("Available: " + (rs.getBoolean("availability") ? "Yes" : "No"));
            }
        } catch (SQLException e) {
            System.out.println("Error viewing car details: " + e.getMessage());
        }
    }
}


class CarStack {
    int max = 5;
    String[] stack;
    int top;

    CarStack() {
        stack = new String[max];
        top = -1;
    }


    void push(String carName) {
        if (carName == null || carName.trim().isEmpty()) {
            return;
        }
        if (top == max - 1) {
            for (int i = 0; i <max- 1; i++) {
                stack[i] = stack[i + 1];
            }
            top--;
        }
        stack[++top] = carName;
    }

    void display() {
        if (top == -1) {
            System.out.println("No recently viewed cars.");
            return;
        }

        System.out.println("\n=== Recently Viewed Cars ===");
        for (int i = top; i >= 0; i--) {
            System.out.println((top - i + 1) + ". " + stack[i]);
        }
    }
}