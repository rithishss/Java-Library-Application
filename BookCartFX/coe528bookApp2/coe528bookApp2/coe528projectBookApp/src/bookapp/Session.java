package bookapp;

// Session.java
// Every FXML screen gets its own SceneController instance, so anything that has
// to survive a screen change cannot live on the controller. It lives here.
public class Session {
    private static Customer currentCustomer;
    private static Book selectedBook;
    private static boolean redeemingPoints;

    private Session() {
    }

    public static Customer getCurrentCustomer() {
        return currentCustomer;
    }

    public static void setCurrentCustomer(Customer customer) {
        currentCustomer = customer;
    }

    public static Book getSelectedBook() {
        return selectedBook;
    }

    public static void setSelectedBook(Book book) {
        selectedBook = book;
    }

    public static boolean isRedeemingPoints() {
        return redeemingPoints;
    }

    public static void setRedeemingPoints(boolean redeeming) {
        redeemingPoints = redeeming;
    }

    public static void clear() {
        currentCustomer = null;
        selectedBook = null;
        redeemingPoints = false;
    }
}
