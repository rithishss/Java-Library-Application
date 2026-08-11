package bookapp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// CustomerDAO.java
// Every read and write of the customers table goes through here.
public class CustomerDAO {

    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT username, password, points FROM customers ORDER BY username";

        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                customers.add(new Customer(rs.getString("username"),
                        rs.getString("password"), rs.getInt("points")));
            }
        } catch (SQLException e) {
            System.out.println("Error loading customers: " + e.getMessage());
        }
        return customers;
    }

    public Customer findByUsername(String username) {
        String sql = "SELECT username, password, points FROM customers WHERE username = ?";

        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(rs.getString("username"),
                            rs.getString("password"), rs.getInt("points"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding customer: " + e.getMessage());
        }
        return null;
    }

    public void insert(Customer customer) {
        String sql = "INSERT INTO customers (username, password, points, joined_at) "
                + "VALUES (?, ?, ?, datetime('now','localtime'))";

        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, customer.getUsername());
            ps.setString(2, customer.getPassword());
            ps.setInt(3, customer.getPoints());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding customer: " + e.getMessage());
        }
    }

    // Called after a purchase changes a customer's points balance.
    public void updatePoints(Customer customer) {
        String sql = "UPDATE customers SET points = ? WHERE username = ?";

        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, customer.getPoints());
            ps.setString(2, customer.getUsername());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating points: " + e.getMessage());
        }
    }

    // The orders table cascades on delete, so a customer's history goes with them.
    public void delete(Customer customer) {
        String sql = "DELETE FROM customers WHERE username = ?";

        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, customer.getUsername());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting customer: " + e.getMessage());
        }
    }
}
