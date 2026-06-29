package Manager;
import User.User;
import CarStack.*;
import CarList.CarList;
import java.sql.*;
import java.util.Scanner;

public class Manager extends User {
    CarStack recentViewedCars = new CarStack();
    CarList waitingList;
    String DB_URL = "jdbc:mysql://localhost:3306/car_showroom";
    String DB_USER = "root";
    String DB_PASSWORD = "";

    public Manager(String username) {
        super(username);
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            waitingList = new CarList(conn);
        } catch (SQLException e) {
            System.out.println("Error creating waiting list: " + e.getMessage());
        }
    }

    // Helper validation methods with improved names
    private boolean isValidNameFormat(String input) {
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidModelFormat(String input) {
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!Character.isLetter(c) && !Character.isDigit(c) && !Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidFuelType(String fuelType) {
        String[] validFuelTypes = {"Petrol", "Diesel", "Electric", "Hybrid"};
        for (String validType : validFuelTypes) {
            if (validType.equalsIgnoreCase(fuelType)) {
                return true;
            }
        }
        return false;
    }

    //manager dashboard
    public void showDashboard() throws SQLException {
        Scanner sc = new Scanner(System.in);
        while (true) {
            try {
                System.out.println("\n=== Manager Dashboard ===");
                System.out.println("1. View All Cars");
                System.out.println("2. Add New Car");
                System.out.println("3. Update Car Details");
                System.out.println("4. Delete Car");
                System.out.println("5. View All Bookings");
                System.out.println("6. View Available Cars");
                System.out.println("7. View Recently Viewed Cars");
                System.out.println("8. Manage Waiting List");
                System.out.println("9. View Total Revenue");
                System.out.println("10. Back to Main Menu");
                System.out.print("Select an option: ");

                String input = sc.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Choice can't be empty");
                    continue;
                }

                int choice;
                try {
                    choice = Integer.parseInt(input);
                }
                catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter number only");
                    continue;
                }

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
                        manageWaitingList();
                        break;
                    case 9:
                        viewTotalRevenue();
                        break;
                    case 10:
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
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
        String name;
        while (true) {
            name = sc.nextLine().trim();
            if (name.isEmpty()) {
                System.out.print("Car Name cannot be empty. Please enter Car Name: ");
                continue;
            }
            if (!isValidNameFormat(name)) {
                System.out.print("Car Name can only contain letters and spaces. Please enter valid Car Name: ");
                continue;
            }
            break;
        }

        System.out.print("Brand: ");
        String brand;
        while (true) {
            brand = sc.nextLine().trim();
            if (brand.isEmpty()) {
                System.out.print("Brand cannot be empty. Please enter Brand: ");
                continue;
            }
            if (!isValidNameFormat(brand)) {
                System.out.print("Brand can only contain letters and spaces. Please enter valid Brand: ");
                continue;
            }
            break;
        }

        System.out.print("Model: ");
        String model;
        while (true) {
            model = sc.nextLine().trim();
            if (model.isEmpty()) {
                System.out.print("Model cannot be empty. Please enter Model: ");
                continue;
            }
            if (!isValidModelFormat(model)) {
                System.out.print("Model can contain letters, numbers and spaces. Please enter valid Model: ");
                continue;
            }
            break;
        }

        System.out.print("Price: ");
        double price;
        while (true) {
            String priceInput = sc.nextLine().trim();
            if (priceInput.isEmpty()) {
                System.out.print("Price cannot be empty. Please enter Price: ");
                continue;
            }
            try {
                price = Double.parseDouble(priceInput);
                if (price <= 0) {
                    System.out.print("Price must be greater than 0. Please enter valid Price: ");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.print("Invalid price format. Please enter a valid number for Price: ");
            }
        }

        System.out.print("Fuel Type (Petrol/Diesel/Electric/Hybrid): ");
        String fuelType;
        while (true) {
            fuelType = sc.nextLine().trim();
            if (fuelType.isEmpty()) {
                System.out.print("Fuel Type cannot be empty. Please enter Fuel Type: ");
                continue;
            }
            if (isValidFuelType(fuelType)) {
                break;
            } else {
                System.out.print("Invalid Fuel Type. Please choose from Petrol/Diesel/Electric/Hybrid: ");
            }
        }

        System.out.print("Available (Y/N): ");
        boolean available;
        while (true) {
            String availableInput = sc.nextLine().trim();
            if (availableInput.isEmpty()) {
                System.out.print("Available status cannot be empty. Please enter Y or N: ");
                continue;
            }
            if (availableInput.equalsIgnoreCase("Y")) {
                available = true;
                break;
            } else if (availableInput.equalsIgnoreCase("N")) {
                available = false;
                break;
            } else {
                System.out.print("Invalid input. Please enter only Y or N: ");
            }
        }

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
        int id;
        while (true) {
            try {
                String idInput = sc.nextLine().trim();
                if (idInput.isEmpty()) {
                    System.out.print("ID cannot be empty. Please enter ID: ");
                    continue;
                }
                id = Integer.parseInt(idInput);
                if (id <= 0) {
                    System.out.print("ID must be a positive number. Please enter valid ID: ");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.print("Invalid ID format. Please enter a valid number: ");
            }
        }

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

            // enter new detail
            System.out.println("\nEnter new details (leave blank to keep current value):");

            System.out.print("Name [" + rs.getString("name") + "]: ");
            String name = sc.nextLine().trim();
            name = name.isEmpty() ? rs.getString("name") : name;
            if (!name.equals(rs.getString("name"))) {
                while (!isValidNameFormat(name)) {
                    System.out.print("Name can only contain letters and spaces. Please enter valid Name: ");
                    name = sc.nextLine().trim();
                }
            }

            System.out.print("Brand [" + rs.getString("brand") + "]: ");
            String brand = sc.nextLine().trim();
            brand = brand.isEmpty() ? rs.getString("brand") : brand;
            if (!brand.equals(rs.getString("brand"))) {
                while (!isValidNameFormat(brand)) {
                    System.out.print("Brand can only contain letters and spaces. Please enter valid Brand: ");
                    brand = sc.nextLine().trim();
                }
            }

            System.out.print("Model [" + rs.getString("model") + "]: ");
            String model = sc.nextLine().trim();
            model = model.isEmpty() ? rs.getString("model") : model;
            if (!model.equals(rs.getString("model"))) {
                while (!isValidModelFormat(model)) {
                    System.out.print("Model can contain letters, numbers and spaces. Please enter valid Model: ");
                    model = sc.nextLine().trim();
                }
            }

            System.out.print("Price [" + rs.getDouble("price") + "]: ");
            String priceInput = sc.nextLine().trim();
            double price;
            if (priceInput.isEmpty()) {
                price = rs.getDouble("price");
            } else {
                while (true) {
                    try {
                        price = Double.parseDouble(priceInput);
                        if (price <= 0) {
                            System.out.print("Price must be greater than 0. Please enter valid Price: ");
                            priceInput = sc.nextLine().trim();
                            continue;
                        }
                        break;
                    } catch (NumberFormatException e) {
                        System.out.print("Invalid price format. Please enter a valid number for Price: ");
                        priceInput = sc.nextLine().trim();
                    }
                }
            }

            System.out.print("Fuel Type [" + rs.getString("fuel_type") + "]: ");
            String fuelType = sc.nextLine().trim();
            fuelType = fuelType.isEmpty() ? rs.getString("fuel_type") : fuelType;
            if (!fuelType.equalsIgnoreCase(rs.getString("fuel_type"))) {
                while (!isValidFuelType(fuelType)) {
                    System.out.print("Invalid Fuel Type. Please choose from Petrol/Diesel/Electric/Hybrid: ");
                    fuelType = sc.nextLine().trim();
                }
            }

            System.out.print("Available (Y/N) [" + (rs.getBoolean("availability") ? "Y" : "N") + "]: ");
            String availableI = sc.nextLine().trim();
            boolean available;
            if (availableI.isEmpty()) {
                available = rs.getBoolean("availability");
            } else {
                while (true) {
                    if (availableI.equalsIgnoreCase("Y")) {
                        available = true;
                        break;
                    } else if (availableI.equalsIgnoreCase("N")) {
                        available = false;
                        break;
                    } else {
                        System.out.print("Invalid input. Please enter only Y or N: ");
                        availableI = sc.nextLine().trim();
                    }
                }
            }

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
        int id;
        while (true) {
            try {
                String idInput = sc.nextLine().trim();
                if (idInput.isEmpty()) {
                    System.out.print("ID cannot be empty. Please enter ID: ");
                    continue;
                }
                id = Integer.parseInt(idInput);
                if (id <= 0) {
                    System.out.print("ID must be a positive number. Please enter valid ID: ");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.print("Invalid ID format. Please enter a valid number: ");
            }
        }

        System.out.print("Are you sure you want to delete car with ID " + id + "? (Y/N): ");
        String conf;
        while (true) {
            conf = sc.nextLine().trim();
            if (conf.equalsIgnoreCase("Y") || conf.equalsIgnoreCase("N")) {
                break;
            } else {
                System.out.print("Invalid input. Please enter only Y or N: ");
            }
        }

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
                String choice;
                while (true) {
                    choice = sc.nextLine().trim();
                    if (choice.equalsIgnoreCase("Y") || choice.equalsIgnoreCase("N")) {
                        break;
                    } else {
                        System.out.print("Invalid input. Please enter only Y or N: ");
                    }
                }

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
                System.out.println("Price: " + rs.getDouble("price"));
                System.out.println("Fuel Type: " + rs.getString("fuel_type"));
                System.out.println("Available: " + (rs.getBoolean("availability") ? "Yes" : "No"));
            }
        }
        catch (SQLException e) {
            System.out.println("Error viewing car details: " + e.getMessage());
        }
    }

    // New method to manage waiting list
    void manageWaitingList() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Waiting List Management ===");
            System.out.println("1. View Waiting List");
            System.out.println("2. Remove First Customer from Waiting List");
            System.out.println("3. Check if Customer Exists");
            System.out.println("4. Get Customer Details");
            System.out.println("5. Clear Entire Waiting List");
            System.out.println("6. Back to Manager Dashboard");
            System.out.print("Select an option: ");

            int choice;
            while (true) {
                try {
                    String input = sc.nextLine().trim();
                    if (input.isEmpty()) {
                        System.out.print("Choice cannot be empty. Please select an option: ");
                        continue;
                    }
                    choice = Integer.parseInt(input);
                    if (choice < 1 || choice > 6) {
                        System.out.print("Invalid option. Please select 1-6: ");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.print("Invalid input. Please enter a number: ");
                }
            }

            switch (choice) {
                case 1:
                    waitingList.displayList();
                    break;
                case 2:
                    if (waitingList.isEmpty()) {
                        System.out.println("Waiting list is empty.");
                    } else {
                        waitingList.removeFirstCustomer();
                    }
                    break;
                case 3:
                    System.out.print("Enter customer name to check: ");
                    String checkName = sc.nextLine().trim();
                    if (checkName.isEmpty()) {
                        System.out.println("Customer name cannot be empty.");
                    } else if (waitingList.customerExists(checkName)) {
                        System.out.println("Customer exists in waiting list");
                    } else {
                        System.out.println("Customer does not exist in waiting list");
                    }
                    break;
                case 4:
                    System.out.print("Enter customer name to get details: ");
                    String detailName = sc.nextLine().trim();
                    if (detailName.isEmpty()) {
                        System.out.println("Customer name cannot be empty.");
                    } else {
                        System.out.println(waitingList.getCustomerDetails(detailName));
                    }
                    break;
                case 5:
                    System.out.print("Are you sure you want to clear the entire waiting list? (Y/N): ");
                    String confirm;
                    while (true) {
                        confirm = sc.nextLine().trim();
                        if (confirm.equalsIgnoreCase("Y") || confirm.equalsIgnoreCase("N")) {
                            break;
                        } else {
                            System.out.print("Invalid input. Please enter only Y or N: ");
                        }
                    }
                    if (confirm.equalsIgnoreCase("Y")) {
                        waitingList.clearAll();
                    } else {
                        System.out.println("Operation cancelled.");
                    }
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    void viewTotalRevenue() {
        System.out.println("\n=== Total Revenue ===");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "{? = call CalculateTotalRevenue()}";
            CallableStatement cstmt = conn.prepareCall(sql);

            cstmt.registerOutParameter(1, Types.DECIMAL);

            cstmt.execute();

            double totalRevenue = cstmt.getDouble(1);

            System.out.println("Total Revenue from all bookings: Rs" + totalRevenue);
            System.out.println("================================");

        } catch (SQLException e) {
            System.out.println("Error calculating total revenue: " + e.getMessage());
        }
    }
}