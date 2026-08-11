package bookapp;

// OrderItem.java
// One book as it was at the moment it was bought. The title, author and price
// are stored on the row itself, so the record does not change if the owner
// later edits or deletes the book.
public class OrderItem {
    private int id;
    private int orderId;
    private int bookId;      // 0 once the book it pointed at has been deleted
    private String title;
    private String author;
    private double price;

    public OrderItem(String title, String author, double price) {
        this.title = title;
        this.author = author;
        this.price = price;
    }

    public OrderItem(int id, int orderId, int bookId, String title, String author, double price) {
        this(title, author, price);
        this.id = id;
        this.orderId = orderId;
        this.bookId = bookId;
    }

    public int getId() {
        return id;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public double getPrice() {
        return price;
    }
}
