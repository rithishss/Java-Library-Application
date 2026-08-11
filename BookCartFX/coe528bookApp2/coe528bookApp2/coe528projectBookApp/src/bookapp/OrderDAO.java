package bookapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// OrderDAO.java
// Writes and reads the order history. Used from Stage 3A onwards, when the
// purchase flow is wired up.
public class OrderDAO {

    // Writes the order and all of its items as one transaction, so a failure
    // part way through cannot leave an order with no books on it. Returns the
    // new order id, or 0 if nothing was written.
    public int insert(String username, double total, int pointsEarned,
            int pointsRedeemed, List<Book> books) {
        String orderSql = "INSERT INTO orders (username, total, points_earned, points_redeemed) "
                + "VALUES (?, ?, ?, ?)";
        String itemSql = "INSERT INTO order_items (order_id, book_id, title, author, price) "
                + "VALUES (?, ?, ?, ?, ?)";

        Connection c = Database.getConnection();
        int orderId = 0;

        try {
            c.setAutoCommit(false);

            try (PreparedStatement ps = c.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setDouble(2, total);
                ps.setInt(3, pointsEarned);
                ps.setInt(4, pointsRedeemed);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        orderId = keys.getInt(1);
                    }
                }
            }

            try (PreparedStatement ps = c.prepareStatement(itemSql)) {
                for (Book book : books) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, book.getId());
                    ps.setString(3, book.getTitle());
                    ps.setString(4, book.getAuthor());
                    ps.setDouble(5, book.getPrice());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            c.commit();
        } catch (SQLException e) {
            System.out.println("Error saving order: " + e.getMessage());
            orderId = 0;
            try {
                c.rollback();
            } catch (SQLException rollbackError) {
                System.out.println("Error rolling back order: " + rollbackError.getMessage());
            }
        } finally {
            try {
                c.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error restoring autocommit: " + e.getMessage());
            }
        }
        return orderId;
    }

    // Newest order first. One query, with the items joined on and grouped back
    // together afterwards.
    public List<Order> findByCustomer(String username) {
        String sql = "SELECT o.id, o.username, o.ordered_at, o.total, o.points_earned, "
                + "o.points_redeemed, i.id AS item_id, i.book_id, i.title, i.author, i.price "
                + "FROM orders o "
                + "LEFT JOIN order_items i ON i.order_id = o.id "
                + "WHERE o.username = ? "
                + "ORDER BY o.ordered_at DESC, o.id DESC, i.id";

        Map<Integer, Order> orders = new LinkedHashMap<>();

        try (PreparedStatement ps = Database.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("id");
                    Order order = orders.get(orderId);
                    if (order == null) {
                        order = new Order(orderId, rs.getString("username"),
                                rs.getString("ordered_at"), rs.getDouble("total"),
                                rs.getInt("points_earned"), rs.getInt("points_redeemed"));
                        orders.put(orderId, order);
                    }

                    int itemId = rs.getInt("item_id");
                    if (!rs.wasNull()) {
                        order.addItem(new OrderItem(itemId, orderId, rs.getInt("book_id"),
                                rs.getString("title"), rs.getString("author"),
                                rs.getDouble("price")));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading orders: " + e.getMessage());
        }
        return new ArrayList<>(orders.values());
    }
}
