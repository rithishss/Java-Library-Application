package bookapp;

import java.io.*;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class SceneController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    private UserContext userContext = new UserContext();
    private ObservableList<Book> bookList = FXCollections.observableArrayList();  // Observable list for books
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    // Database access
    private CustomerDAO customerDAO = new CustomerDAO();
    private BookDAO bookDAO = new BookDAO();
    private OrderDAO orderDAO = new OrderDAO();

    // Loyalty scheme. 100 points are worth $1 when redeemed, which is the rate
    // Customer.redeemPoints already assumes, and Gold starts at 1000 points.
    private static final int POINTS_PER_DOLLAR = 10;

    // Hardcoded owner credentials
    private static final String OWNER_USERNAME = "owner";
    private static final String OWNER_PASSWORD = "admin123";

    // Sample users
    private Map<String, String> userCredentials = new HashMap<>(); 

    @FXML
    private PasswordField password;

    @FXML
    private TextField username;

    @FXML
    private TableView<Book> booksTable;  // Table for displaying books
    @FXML
    private TableColumn<Book, String> bookTitleColumn;
    @FXML
    private TableColumn<Book, String> bookAuthorColumn;
    @FXML
    private TableColumn<Book, Double> bookPriceColumn;

    @FXML
    private TextField bookname;  // Text field for book name
    @FXML
    private TextField bookprice;  // Text field for book price
    @FXML
    private TextField bookAuthor;
    
    @FXML
    private Button deleteUser;

    @FXML
    private TextField newPassword;

    @FXML
    private TextField newUser;

    @FXML
    private TableColumn<Customer, String> passwords;

    @FXML
    private TableColumn<Customer, String> points;

    @FXML
    private TableView<Customer> usernameTable;

    @FXML
    private TableColumn<Customer, String> usernames;
    
    @FXML
    private Text customerName;

    @FXML
    private Text customerPoints;

    @FXML
    private Text customerStatus;
    
    @FXML
    private TableColumn<Book, String> bookAuthorColumnCustomer;

    @FXML
    private TableView<Book> bookCustomerTable;

    @FXML
    private TableColumn<Book, String> bookPriceColumnCustomer;

    @FXML
    private TableColumn<Book, String> bookTitleColumnCustomer;
    
    @FXML
    private Text customerPoints2;

    @FXML
    private Text customerStatus2;

    @FXML
    private TableView<PurchaseInfo> purchaseInfo;

    @FXML
    private TableColumn<PurchaseInfo, String> costColumn;

    @FXML
    private TableColumn<PurchaseInfo, String> pointsColumn;

    @FXML
    private TableColumn<PurchaseInfo, String> statusColumn;

    @FXML
    private TableView<HistoryRow> historyTable;

    @FXML
    private TableColumn<HistoryRow, String> historyDateColumn;

    @FXML
    private TableColumn<HistoryRow, String> historyBookColumn;

    @FXML
    private TableColumn<HistoryRow, String> historyTotalColumn;

    @FXML
    private TableColumn<HistoryRow, String> historyEarnedColumn;

    @FXML
    private TableColumn<HistoryRow, String> historyRedeemedColumn;


    @FXML
    public void initialize() {
        if (usernames != null && passwords != null) {
            usernames.setCellValueFactory(new PropertyValueFactory<>("username"));
            passwords.setCellValueFactory(new PropertyValueFactory<>("maskedPassword"));
            points.setCellValueFactory(new PropertyValueFactory<>("points"));

            // Load customers from file
            loadCustomersFromFile();
            usernameTable.setItems(customerList);
        }

        if (bookTitleColumn != null) {
            bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            bookAuthorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
            bookPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

            // Load books from books.txt
            loadBooksFromFile();
            booksTable.setItems(bookList);
        }
        
        if (bookTitleColumnCustomer != null) {
            bookTitleColumnCustomer.setCellValueFactory(new PropertyValueFactory<>("title"));
            bookAuthorColumnCustomer.setCellValueFactory(new PropertyValueFactory<>("author"));
            bookPriceColumnCustomer.setCellValueFactory(new PropertyValueFactory<>("price"));

            // Load books from books.txt
            loadBooksFromFile();
            bookCustomerTable.setItems(bookList);
        }

        // Each screen builds its own controller, so the header is filled in from
        // the session here rather than being pushed across by the screen before.
        if (customerName != null) {
            showCustomerDetails();
        }

        if (costColumn != null) {
            costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
            pointsColumn.setCellValueFactory(new PropertyValueFactory<>("points"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

            showPurchaseSummary();
        }

        if (historyDateColumn != null) {
            historyDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            historyBookColumn.setCellValueFactory(new PropertyValueFactory<>("book"));
            historyTotalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
            historyEarnedColumn.setCellValueFactory(new PropertyValueFactory<>("earned"));
            historyRedeemedColumn.setCellValueFactory(new PropertyValueFactory<>("redeemed"));

            showPurchaseHistory();
        }
    }


    // One row of the summary table on CustomerCostScreen.
    public static class PurchaseInfo {
        private final String cost;
        private final String points;
        private final String status;

        public PurchaseInfo(String cost, String points, String status) {
            this.cost = cost;
            this.points = points;
            this.status = status;
        }

        public String getCost() {
            return cost;
        }

        public String getPoints() {
            return points;
        }

        public String getStatus() {
            return status;
        }
    }


    // One row of the history table on CustomerHistoryScreen.
    public static class HistoryRow {
        private final String date;
        private final String book;
        private final String total;
        private final String earned;
        private final String redeemed;

        public HistoryRow(String date, String book, String total, String earned, String redeemed) {
            this.date = date;
            this.book = book;
            this.total = total;
            this.earned = earned;
            this.redeemed = redeemed;
        }

        public String getDate() {
            return date;
        }

        public String getBook() {
            return book;
        }

        public String getTotal() {
            return total;
        }

        public String getEarned() {
            return earned;
        }

        public String getRedeemed() {
            return redeemed;
        }
    }


    // Only ever the logged-in customer's own orders, newest first.
    private void showPurchaseHistory() {
        Customer customer = Session.getCurrentCustomer();
        if (customer == null) {
            return;
        }

        ObservableList<HistoryRow> rows = FXCollections.observableArrayList();
        for (Order order : orderDAO.findByCustomer(customer.getUsername())) {
            rows.add(new HistoryRow(shortDate(order.getOrderedAt()), titlesOf(order),
                    String.format("$%.2f", order.getTotal()),
                    String.valueOf(order.getPointsEarned()),
                    String.valueOf(order.getPointsRedeemed())));
        }
        historyTable.setItems(rows);
    }


    // ordered_at is stored as "yyyy-MM-dd HH:mm:ss"; the seconds add nothing here.
    private String shortDate(String orderedAt) {
        if (orderedAt == null) {
            return "";
        }
        return orderedAt.length() >= 16 ? orderedAt.substring(0, 16) : orderedAt;
    }


    private String titlesOf(Order order) {
        StringBuilder titles = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            if (titles.length() > 0) {
                titles.append(", ");
            }
            titles.append(item.getTitle());
        }
        return titles.toString();
    }


    // Fills in the name, points and rank across the top of CustomerStartScreen.
    private void showCustomerDetails() {
        Customer customer = Session.getCurrentCustomer();
        if (customer == null) {
            return;
        }

        customerName.setText(customer.getUsername());
        customerPoints.setText(String.valueOf(customer.getPoints()));
        customerStatus.setText(customer.getStatus());
    }


    // Shows what the selected book will cost once any redemption is applied.
    private void showPurchaseSummary() {
        Customer customer = Session.getCurrentCustomer();
        Book book = Session.getSelectedBook();
        if (customer == null || book == null) {
            return;
        }

        setCustomerDetails(String.valueOf(customer.getPoints()), customer.getStatus(),
                costAfterRedemption(customer, book));
    }


    // How many points this customer can put towards this book, 0 when they are
    // buying outright. Capped at the price so a purchase can never go negative.
    private int redeemablePoints(Customer customer, Book book) {
        if (!Session.isRedeemingPoints()) {
            return 0;
        }
        return Math.min(customer.getPoints(), (int) (book.getPrice() * 100));
    }


    private double costAfterRedemption(Customer customer, Book book) {
        return book.getPrice() - (redeemablePoints(customer, book) / 100.0);
    }



    // Load customer data from the database
    private void loadCustomersFromFile() {
        customerList.clear();
        userCredentials.clear(); // Clear previous credentials to avoid duplicates

        for (Customer customer : customerDAO.findAll()) {
            customerList.add(customer);
            userCredentials.put(customer.getUsername(), customer.getPassword()); // Store in HashMap
        }
    }


    // Load books data from the database. setAll replaces the contents rather
    // than appending, so calling this more than once cannot duplicate rows.
    private void loadBooksFromFile() {
        bookList.setAll(bookDAO.findAll());
    }

    @FXML
    void login(ActionEvent event) throws IOException {
        loadCustomersFromFile();  

        String user = username.getText();
        String pass = password.getText();

        if (user.equals(OWNER_USERNAME) && pass.equals(OWNER_PASSWORD)) {
            // Owner login
            System.out.println("Owner login successful!");
            userContext.setState(new OwnerState());
            switchScene(event, "OwnerStartScreen.fxml");
        } else if (userCredentials.containsKey(user)
                && PasswordHasher.matches(pass, userCredentials.get(user))) {
            // Customer login
            System.out.println("Customer login successful!");
            userContext.setState(new CustomerState());
            // Set before switching: the next screen reads this while it initialises.
            Session.setCurrentCustomer(customerDAO.findByUsername(user));
            switchScene(event, "CustomerStartScreen.fxml");
        } else {
            // Invalid credentials
            showAlert("Login Failed", "Invalid username or password.");
        }
    }

    
    @FXML
    void addUser(ActionEvent event) {
        String newCustomerName = newUser.getText();
        String newCustomerPassword = newPassword.getText();

        if (!newCustomerName.isEmpty() && !newCustomerPassword.isEmpty()) {
            if (userCredentials.containsKey(newCustomerName)) {
                showAlert("Error", "Username already exists!");
                return;
            }

            // Hashed here, so nothing downstream ever holds the plain password.
            String hashed = PasswordHasher.hash(newCustomerPassword);
            Customer newCustomer = new Customer(newCustomerName, hashed, 0); // Set points to 0
            customerList.add(newCustomer);
            userCredentials.put(newCustomerName, hashed);
            usernameTable.setItems(customerList);
            customerDAO.insert(newCustomer);

            newUser.clear();
            newPassword.clear();
            System.out.println("Customer added successfully!");
        } else {
            showAlert("Error", "Username and password cannot be empty!");
        }
    }



    @FXML
    void deleteUser(ActionEvent event) {
        Customer selectedCustomer = usernameTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
            customerList.remove(selectedCustomer);
            userCredentials.remove(selectedCustomer.getUsername()); // Remove from map
            usernameTable.setItems(customerList);
            customerDAO.delete(selectedCustomer);
            System.out.println("Customer deleted successfully!");
        } else {
            showAlert("Error", "Select a customer to delete!");
        }
    }



    @FXML
    void addBook(ActionEvent event) {
        String title = bookname.getText();
        String author = bookAuthor.getText();  // Getting author from text field
        double price;

        try {
            price = Double.parseDouble(bookprice.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid price!");
            return;
        }

        if (!title.isEmpty()) {
            Book newBook = new Book(title, author, price);
            bookList.add(newBook);  // Add to the observable list
            booksTable.setItems(bookList);  // Update the table view
            bookDAO.insert(newBook);  // Save the new book to the database
            bookname.clear();
            bookprice.clear();
            bookAuthor.clear();
            System.out.println("Book added successfully!");
        } else {
            showAlert("Error", "Book title cannot be empty!");
        }
    }


    @FXML
    void deleteBook(ActionEvent event) {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            bookList.remove(selectedBook);
            booksTable.setItems(bookList);  // Update the table view
            bookDAO.delete(selectedBook);  // Remove the book from the database
            System.out.println("Book deleted successfully!");
        } else {
            showAlert("Error", "Select a book to delete!");
        }
    }

    public void setCustomerDetails(String points, String status, double price) {
            customerPoints2.setText(points);
            customerStatus2.setText(status);
            purchaseInfo.setItems(FXCollections.observableArrayList(
                    new PurchaseInfo(String.format("$%.2f", price), points, status)));
    }
    @FXML
    void goBack(ActionEvent event) throws IOException {
        switchScene(event, "OwnerStartScreen.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        userContext.logout();
        Session.clear();
        switchScene(event, "LoginScreen.fxml");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void switchBookScreen(ActionEvent event) throws IOException {
        switchScene(event, "OwnerBooksScreen.fxml");
    }

    @FXML
    void switchCustomerScreen(ActionEvent event) throws IOException {
        switchScene(event, "OwnerCustomerScreen.fxml");
    }

    @FXML
    void goHistoryScreen(ActionEvent event) throws IOException {
        switchScene(event, "CustomerHistoryScreen.fxml");
    }

    // Back from the cost screen, which abandons the purchase, and back from the
    // history screen, where there is nothing to abandon.
    @FXML
    void goCustomerStartScreen(ActionEvent event) throws IOException {
        Session.setSelectedBook(null);
        Session.setRedeemingPoints(false);
        switchScene(event, "CustomerStartScreen.fxml");
    }
    
    @FXML
    void goBuyScreen(ActionEvent event) throws IOException {
        Book selectedBook = bookCustomerTable.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            showAlert("Error", "Please select a book to purchase.");
            return;
        }

        // The cost screen reads these while it initialises, so they go in first.
        Session.setSelectedBook(selectedBook);
        Session.setRedeemingPoints(false);
        switchScene(event, "CustomerCostScreen.fxml");
    }


    @FXML
    void goBuyScreenRedeem(ActionEvent event)throws IOException {
        Book selectedBook = bookCustomerTable.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            showAlert("Error", "Please select a book to purchase.");
            return;
        }

        Session.setSelectedBook(selectedBook);
        Session.setRedeemingPoints(true);
        switchScene(event, "CustomerCostScreen.fxml");
    }


    @FXML
    void confirmPurchase(ActionEvent event) throws IOException {
        Customer customer = Session.getCurrentCustomer();
        Book book = Session.getSelectedBook();

        if (customer == null || book == null) {
            showAlert("Error", "No book selected to purchase.");
            return;
        }

        int pointsRedeemed = redeemablePoints(customer, book);
        double cost = costAfterRedemption(customer, book);
        int pointsEarned = (int) (cost * POINTS_PER_DOLLAR);

        // Write the order first. Nothing about the customer changes unless this
        // succeeds, so a failed write cannot leave their points wrong.
        int orderId = orderDAO.insert(customer.getUsername(), cost, pointsEarned,
                pointsRedeemed, Arrays.asList(book));

        if (orderId == 0) {
            showAlert("Error", "Could not save the purchase. Please try again.");
            return;
        }

        if (pointsRedeemed > 0) {
            customer.redeemPoints(book.getPrice());
        }
        customer.addPoints(pointsEarned);  // May promote them to Gold
        customerDAO.updatePoints(customer);

        customer.purchase();  // NotPurchased -> Purchased
        book.purchase();

        Session.setSelectedBook(null);
        Session.setRedeemingPoints(false);

        System.out.println("Purchase complete, order " + orderId);
        switchScene(event, "CustomerStartScreen.fxml");
    }
}
