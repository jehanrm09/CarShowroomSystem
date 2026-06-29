package CarList;
import java.sql.*;
public class CarList {
    public static class Node {
        String customerName;
        String phone;
        int carId;
        Node next;

        Node(String customerName, String phone, int carId)
        {
            this.customerName = customerName;
            this.phone = phone;
            this.carId = carId;
            this.next = null;
        }
    }

    public Node head;
    public Connection conn;

    public CarList(Connection connection) {
        head = null;
        this.conn = connection;
        loadFromDatabase();
    }

    // Check if list is empty
    public boolean isEmpty() {
        return head == null;
    }


    // Add customer to waiting list and database
    public void addCustomer(String customerName, String phone, int carId) {
        Node newNode = new Node(customerName, phone, carId);

        if (head == null) {
            head = newNode;
        } else {
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }

        // Add to database
        try {
            String insertSql = "INSERT INTO waiting_lists (customer_name, phone, car_id) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, customerName);
            insertStmt.setString(2, phone);
            insertStmt.setInt(3, carId);
            insertStmt.executeUpdate();

            System.out.println(customerName + " added to waiting list for car ID: " + carId);
        } catch (SQLException e) {
            System.out.println("Error adding to database: " + e.getMessage());
        }
    }

    // Remove first customer from waiting list and database
    public void removeFirstCustomer() {
        if (isEmpty()) {
            System.out.println("Waiting list is empty");
            return;
        }

        Node firstNode = head;
        String customerName = firstNode.customerName;
        int carId = firstNode.carId;

        head = head.next;

        // Remove from database
        try {
            String deleteSql = "DELETE FROM waiting_lists WHERE customer_name = ? AND car_id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setString(1, customerName);
            deleteStmt.setInt(2, carId);
            int rowsDeleted = deleteStmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println(customerName + " removed from waiting list");
            } else {
                System.out.println("Failed to remove " + customerName + " from database");
            }
        } catch (SQLException e) {
            System.out.println("Error removing from database: " + e.getMessage());
        }

    }

    // Display all customers in waiting list
    public void displayList() {
        if (isEmpty()) {
            System.out.println("Waiting list is empty");
            return;
        }

        System.out.println("\n=== Waiting List ===");
        Node current = head;
        int position = 1;

        while (current != null) {
            System.out.println(position + ". Customer: " + current.customerName +
                    ", Phone: " + current.phone +
                    ", Car ID: " + current.carId);
            current = current.next;
            position++;
        }
    }

    // Load waiting list from database
    public void loadFromDatabase() {
        try {
            String sql = "SELECT customer_name, phone, car_id FROM waiting_lists";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // Clear current list before loading
            head = null;

            while (rs.next()) {
                // Add to list without saving to database (to avoid duplicate entries)
                Node newNode = new Node(
                        rs.getString("customer_name"),
                        rs.getString("phone"),
                        rs.getInt("car_id")
                );

                if (head == null) {
                    head = newNode;
                } else {
                    Node current = head;
                    while (current.next != null) {
                        current = current.next;
                    }
                    current.next = newNode;
                }
            }

            System.out.println("Loaded all customers from waiting list database");
        } catch (SQLException e) {
            System.out.println("Error loading waiting list from database: " + e.getMessage());
        }
    }

    // Clear both list and database
    public void clearAll() {
        // Clear database first
        try {
            String clearSql = "DELETE FROM waiting_lists";
            PreparedStatement clearStmt = conn.prepareStatement(clearSql);
            int rowsDeleted = clearStmt.executeUpdate();

            System.out.println("Removed " + rowsDeleted + " customers from database");
        } catch (SQLException e) {
            System.out.println("Error clearing database: " + e.getMessage());
        }

        // Clear list
        head = null;
        System.out.println("Waiting list cleared");
    }

    // Check if a customer exists in the list
    public boolean customerExists(String customerName) {
        Node current = head;
        while (current != null) {
            if (current.customerName.equalsIgnoreCase(customerName)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    // Get customer details by name
    public String getCustomerDetails(String customerName) {
        Node current = head;
        while (current != null) {
            if (current.customerName.equalsIgnoreCase(customerName)) {
                return "Customer: " + current.customerName +
                        ", Phone: " + current.phone +
                        ", Car ID: " + current.carId;
            }
            current = current.next;
        }
        return "Customer not found in waiting list";
    }
}
