package Employee;
import CarList.CarList;
import CarStack.CarStack;
import User.User;
import java.util.*;
import java.io.*;
import java.sql.*;

public class Employee extends User {
    CarStack recentViewedCars = new CarStack();
    CarList waitingList;

    String DB_URL = "jdbc:mysql://localhost:3306/car_showroom";
    String DB_USER = "root";
    String DB_PASSWORD = "";

    public Employee(String username) {
        super(username);
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            waitingList = new CarList(conn);
        } catch (SQLException e) {
            System.out.println("Error creating waiting list: " + e.getMessage());
        }
    }

    // Validation methods
    private boolean isValidName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!Character.isLetter(c) && !Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        // Basic phone validation - at least 10 digits, can contain +, -, spaces
        String cleanedPhone = phone.replaceAll("[^0-9]", "");
        return cleanedPhone.length() >= 10;
    }

    @Override
    public void showDashboard() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            try {
                System.out.println("\n=== Employee Dashboard ===");
                System.out.println("1. View Available Cars");
                System.out.println("2. Book a Car for Customer");
                System.out.println("3. Cancel booking of Customer");
                System.out.println("4. View All Bookings");
                System.out.println("5. View Recently Viewed Cars");
                System.out.println("6. Manage Waiting List");
                System.out.println("7. Back to Main Menu");
                System.out.print("Select an option: ");

                String input = sc.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("Choice cannot be empty. Please select an option.");
                    continue;
                }

                int choice;
                try {
                    choice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number (1-7).");
                    continue;
                }

                switch (choice) {
                    case 1:
                        viewAvailableCars();
                        break;
                    case 2:
                        bookCarForCustomer();
                        break;
                    case 3:
                        cancelBooking();
                        break;
                    case 4:
                        viewAllBookings();
                        break;
                    case 5:
                        recentViewedCars.display();
                        break;
                    case 6:
                        manageWaitingList();
                        break;
                    case 7:
                        return;
                    default:
                        System.out.println("Invalid option. Please enter a number between 1 and 7.");
                }
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    public void viewAvailableCars() {
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

                // Push to stack if you see details
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
        } catch (SQLException e) {
            System.out.println("Error viewing available cars: " + e.getMessage());
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
        } catch (SQLException e) {
            System.out.println("Error viewing cars: " + e.getMessage());
        }
    }

    public void bookCarForCustomer() {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n=== Book a Car for Customer ===");

        String customerName;
        while (true) {
            System.out.print("Customer Name: ");
            customerName = sc.nextLine().trim();
            if (customerName.isEmpty()) {
                System.out.println("Customer name cannot be empty!");
                continue;
            }
            if (!isValidName(customerName)) {
                System.out.println("Customer name can only contain letters and spaces.");
                continue;
            }
            break;
        }

        String phone;
        while (true) {
            System.out.print("Phone Number: ");
            phone = sc.nextLine().trim();
            if (phone.isEmpty()) {
                System.out.println("Phone number cannot be empty!");
                continue;
            }
            if (!isValidPhone(phone)) {
                System.out.println("Please enter a valid phone number (at least 10 digits).");
                continue;
            }
            break;
        }

        viewAllCars();

        int carId;
        while (true) {
            System.out.print("Enter Car ID to book: ");
            String carIdInput = sc.nextLine().trim();
            if (carIdInput.isEmpty()) {
                System.out.println("Car ID cannot be empty!");
                continue;
            }
            try {
                carId = Integer.parseInt(carIdInput);
                if (carId <= 0) {
                    System.out.println("Car ID must be a positive number.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid Car ID format. Please enter a valid number.");
            }
        }

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
                    // insert in booking (trigger will automatically update availability)
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

                    conn.commit();

                    System.out.println("Booking successful! Booking ID: " + bookingId);
                    System.out.println("Amount to pay: Rs" + amount);

                    String generateBill;
                    while (true) {
                        System.out.print("Generate bill now? (Y/N): ");
                        generateBill = sc.nextLine().trim();
                        if (generateBill.equalsIgnoreCase("Y") || generateBill.equalsIgnoreCase("N")) {
                            break;
                        } else {
                            System.out.print("Invalid input. Please enter only Y or N: ");
                        }
                    }

                    if (generateBill.equalsIgnoreCase("Y")) {
                        generateBill(bookingId);
                    }

                } catch (SQLException e) {
                    conn.rollback();
                    System.out.println("Error creating booking: " + e.getMessage());
                } finally {
                    conn.setAutoCommit(true);
                }
            } else {
                // If car not available, add to waiting list
                System.out.println("Car is not available. Adding to waiting list.");
                waitingList.addCustomer(customerName, phone, carId);
            }
        } catch (SQLException e) {
            System.out.println("Database error during booking: " + e.getMessage());
        }
    }

    public void generateBill(int bookingId) {
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
            } else {
                System.out.println("No booking found with ID: " + bookingId);
            }
        } catch (SQLException e) {
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
        } catch (IOException e) {
            System.out.println("Error saving bill to file: " + e.getMessage());
        }
    }

    void cancelBooking() {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n=== Cancel Booking ===");

        viewAllBookings();

        String customerName;
        while (true) {
            System.out.print("\nEnter customer name to cancel booking: ");
            customerName = sc.nextLine().trim();
            if (customerName.isEmpty()) {
                System.out.println("Customer name cannot be empty!");
                continue;
            }
            if (!isValidName(customerName)) {
                System.out.println("Customer name can only contain letters and spaces.");
                continue;
            }
            break;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "{call CancelBooking(?)}";
            CallableStatement cstmt = conn.prepareCall(sql);

            cstmt.setString(1, customerName);

            boolean hasResults = cstmt.execute();

            if (hasResults) {
                try (ResultSet rs = cstmt.getResultSet()) {
                    if (rs.next()) {
                        System.out.println(rs.getString("message"));
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error cancelling booking: " + e.getMessage());
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
        } catch (SQLException e) {
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
            } else {
                System.out.println("No car found with ID: " + carId);
            }
        } catch (SQLException e) {
            System.out.println("Error viewing car details: " + e.getMessage());
        }
    }

    void manageWaitingList() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Waiting List Management ===");
            System.out.println("1. View Waiting List");
            System.out.println("2. Remove First Customer from Waiting List");
            System.out.println("3. Check if Customer Exists");
            System.out.println("4. Get Customer Details");
            System.out.println("5. Back to Employee Dashboard");
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
                    if (choice < 1 || choice > 5) {
                        System.out.print("Invalid option. Please select 1-5: ");
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
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}