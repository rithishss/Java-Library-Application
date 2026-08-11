package bookapp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// BookDAO.java
// Every read and write of the books table goes through here.
public class BookDAO {

    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author, price FROM books ORDER BY id";

        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                books.add(new Book(rs.getInt("id"), rs.getString("title"),
                        rs.getString("author"), rs.getDouble("price")));
            }
        } catch (SQLException e) {
            System.out.println("Error loading books: " + e.getMessage());
        }
        return books;
    }

    // Sets the generated id back onto the book so the caller can delete it later.
    public void insert(Book book) {
        String sql = "INSERT INTO books (title, author, price) VALUES (?, ?, ?)";

        try (PreparedStatement ps = Database.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setDouble(3, book.getPrice());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    book.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error adding book: " + e.getMessage());
        }
    }

    // order_items keeps its own copy of the title and price, so past orders are
    // unaffected by this.
    public void delete(Book book) {
        String sql = "DELETE FROM books WHERE id = ?";

        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setInt(1, book.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting book: " + e.getMessage());
        }
    }
}
