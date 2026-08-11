package bookapp;

import java.util.ArrayList;
import java.util.List;

// Order.java
// One completed purchase, plus the books it covered.
public class Order {
    private int id;
    private String username;
    private String orderedAt;
    private double total;
    private int pointsEarned;
    private int pointsRedeemed;
    private List<OrderItem> items = new ArrayList<>();

    public Order(int id, String username, String orderedAt, double total,
            int pointsEarned, int pointsRedeemed) {
        this.id = id;
        this.username = username;
        this.orderedAt = orderedAt;
        this.total = total;
        this.pointsEarned = pointsEarned;
        this.pointsRedeemed = pointsRedeemed;
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getOrderedAt() {
        return orderedAt;
    }

    public double getTotal() {
        return total;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public int getPointsRedeemed() {
        return pointsRedeemed;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
