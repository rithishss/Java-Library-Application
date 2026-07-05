/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookapp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;

/**
 *
 * @author ssurk
 */
public class GUI extends Application{
        
    private Stage stage;
    private Scene scene;
    private Parent root;
    
    // Data models
    private Owner owner;
    private UserContext userContext;
    private Customer currentCustomer;
    
    // Login screen components
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    
    // Owner screens
    private Scene ownerStartScene, ownerBooksScene, ownerCustomersScene;
    
    // Owner Start Screen components
    private Button booksButton, customersButton, ownerLogoutButton;
    
    // Owner Books Screen components
    private TableView<Book> booksTable;
    private TextField bookNameField, bookPriceField, bookAuthorField;
    private Button addBookButton, deleteBookButton, backFromBooksButton;
    
    // Owner Customers Screen components
    private TableView<Customer> customersTable;
    private TextField customerUsernameField, customerPasswordField;
    private Button addCustomerButton, deleteCustomerButton, backFromCustomersButton;
    
    // Customer screens
    private Scene customerStartScene, customerCostScene;
    
    // Customer Start Screen components
    private Label welcomeLabel, pointsStatusLabel;
    private TableView<BookSelection> customerBooksTable;
    private Button buyButton, redeemAndBuyButton, customerLogoutButton;
    
    // Customer Cost Screen components
    private Label totalCostLabel, pointsLabel;
    private Button costScreenLogoutButton;

    @Override
    public void start(Stage stage) throws Exception{
        this.stage = stage;
        this.stage.setTitle("Online Bookstore");
        
        // Initialize data models
        owner = new Owner();
        userContext = new UserContext();
        
        // Add some sample data
        initializeSampleData();
        
        // Create all scenes
        createLoginScene();
        createOwnerStartScene();
        createOwnerBooksScene();
        createOwnerCustomersScene();
        createCustomerStartScene();
        createCustomerCostScene();
        
        // Start with login scene
        stage.setScene(scene);
        stage.show();
    }
    
    // Helper class for book selection with checkboxes
    public class BookSelection {
        private Book book;
        private boolean selected;
        
        public BookSelection(Book book) {
            this.book = book;
            this.selected = false;
        }
        
        public String getTitle() {
            return book.getTitle();
        }
        
        public double getPrice() {
            return book.getPrice();
        }
        
        public Book getBook() {
            return book;
        }
        
        public boolean isSelected() {
            return selected;
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }
    
    private void initializeSampleData() {
        // Add sample books
        owner.addBook(new Book("Java Programming", "John Smith", 29.99));
        owner.addBook(new Book("Database Systems", "Alice Johnson", 34.99));
        owner.addBook(new Book("Web Development", "Bob Brown", 24.99));
        
        // Add sample customers
        owner.addCustomer(new Customer("jane", "pass123"));
        owner.addCustomer(new Customer("john", "pass456"));
    }
    
    private void createLoginScene() {
        // Create login form components
        Label titleLabel = new Label("Bookstore Login");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label usernameLabel = new Label("Username:");
        usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        
        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        
        loginButton = new Button("Login");
        loginButton.setOnAction(e -> handleLogin());
        
        // Layout for login form
        VBox loginForm = new VBox(10);
        loginForm.setPadding(new Insets(20));
        loginForm.setAlignment(Pos.CENTER);
        loginForm.getChildren().addAll(
                titleLabel,
                usernameLabel, usernameField,
                passwordLabel, passwordField,
                loginButton
        );
        
        scene = new Scene(loginForm, 400, 300);
    }
    
    private void createOwnerStartScene() {
        // Create owner start screen components
        Label titleLabel = new Label("Owner Dashboard");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        booksButton = new Button("Books");
        booksButton.setPrefWidth(120);
        booksButton.setOnAction(e -> switchToOwnerBookScreen(e));
        
        customersButton = new Button("Customers");
        customersButton.setPrefWidth(120);
        customersButton.setOnAction(e -> switchToOwnerCustomerScreen(e));
        
        ownerLogoutButton = new Button("Logout");
        ownerLogoutButton.setPrefWidth(120);
        ownerLogoutButton.setOnAction(e -> switchToLogin(e));
        
        // Layout for owner start screen
        VBox ownerStartLayout = new VBox(20);
        ownerStartLayout.setPadding(new Insets(30));
        ownerStartLayout.setAlignment(Pos.CENTER);
        ownerStartLayout.getChildren().addAll(
                titleLabel,
                booksButton,
                customersButton,
                ownerLogoutButton
        );
        
        ownerStartScene = new Scene(ownerStartLayout, 400, 300);
    }
    
    private void createOwnerBooksScene() {
        // Create components for top part (table)
        booksTable = new TableView<>();
        
        TableColumn<Book, String> nameColumn = new TableColumn<>("Book Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        nameColumn.setPrefWidth(200);
        
        TableColumn<Book, Double> priceColumn = new TableColumn<>("Book Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setPrefWidth(100);
        
        booksTable.getColumns().addAll(nameColumn, priceColumn);
        booksTable.setItems(FXCollections.observableArrayList(owner.getBooks()));
        
        // Create components for middle part (add book form)
        Label bookNameLabel = new Label("Name:");
        bookNameField = new TextField();
        
        Label bookAuthorLabel = new Label("Author:");
        bookAuthorField = new TextField();
        
        Label bookPriceLabel = new Label("Price:");
        bookPriceField = new TextField();
        
        addBookButton = new Button("Add");
        addBookButton.setOnAction(e -> handleAddBook());
        
        // Layout for middle part
        HBox nameInputBox = new HBox(10, bookNameLabel, bookNameField);
        HBox authorInputBox = new HBox(10, bookAuthorLabel, bookAuthorField);
        HBox priceInputBox = new HBox(10, bookPriceLabel, bookPriceField);
        
        VBox addBookForm = new VBox(10);
        addBookForm.setPadding(new Insets(10));
        addBookForm.getChildren().addAll(nameInputBox, authorInputBox, priceInputBox, addBookButton);
        
        // Create components for bottom part
        deleteBookButton = new Button("Delete");
        deleteBookButton.setOnAction(e -> handleDeleteBook());
        
        backFromBooksButton = new Button("Back");
        backFromBooksButton.setOnAction(e -> switchToOwnerScreen(e));
        
        HBox bottomButtons = new HBox(10, deleteBookButton, backFromBooksButton);
        bottomButtons.setPadding(new Insets(10));
        bottomButtons.setAlignment(Pos.CENTER_RIGHT);
        
        // Main layout for the owner books screen
        BorderPane ownerBooksLayout = new BorderPane();
        ownerBooksLayout.setTop(booksTable);
        ownerBooksLayout.setCenter(addBookForm);
        ownerBooksLayout.setBottom(bottomButtons);
        
        ownerBooksScene = new Scene(ownerBooksLayout, 500, 500);
    }
    
    private void createOwnerCustomersScene() {
        // Create components for top part (table)
        customersTable = new TableView<>();
        
        TableColumn<Customer, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setPrefWidth(150);
        
        TableColumn<Customer, String> passwordColumn = new TableColumn<>("Password");
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        passwordColumn.setPrefWidth(150);
        
        TableColumn<Customer, Integer> pointsColumn = new TableColumn<>("Points");
        pointsColumn.setCellValueFactory(new PropertyValueFactory<>("points"));
        pointsColumn.setPrefWidth(100);
        
        customersTable.getColumns().addAll(usernameColumn, passwordColumn, pointsColumn);
        customersTable.setItems(FXCollections.observableArrayList(owner.getCustomers()));
        
        // Create components for middle part (add customer form)
        Label customerUsernameLabel = new Label("Username:");
        customerUsernameField = new TextField();
        
        Label customerPasswordLabel = new Label("Password:");
        customerPasswordField = new TextField();
        
        addCustomerButton = new Button("Add");
        addCustomerButton.setOnAction(e -> handleAddCustomer());
        
        // Layout for middle part
        HBox usernameInputBox = new HBox(10, customerUsernameLabel, customerUsernameField);
        HBox passwordInputBox = new HBox(10, customerPasswordLabel, customerPasswordField);
        
        VBox addCustomerForm = new VBox(10);
        addCustomerForm.setPadding(new Insets(10));
        addCustomerForm.getChildren().addAll(usernameInputBox, passwordInputBox, addCustomerButton);
        
        // Create components for bottom part
        deleteCustomerButton = new Button("Delete");
        deleteCustomerButton.setOnAction(e -> handleDeleteCustomer());
        
        backFromCustomersButton = new Button("Back");
        backFromCustomersButton.setOnAction(e -> switchToOwnerScreen(e));
        
        HBox bottomButtons = new HBox(10, deleteCustomerButton, backFromCustomersButton);
        bottomButtons.setPadding(new Insets(10));
        bottomButtons.setAlignment(Pos.CENTER_RIGHT);
        
        // Main layout for the owner customers screen
        BorderPane ownerCustomersLayout = new BorderPane();
        ownerCustomersLayout.setTop(customersTable);
        ownerCustomersLayout.setCenter(addCustomerForm);
        ownerCustomersLayout.setBottom(bottomButtons);
        
        ownerCustomersScene = new Scene(ownerCustomersLayout, 500, 500);
    }
    
    private void createCustomerStartScene() {
        // Create components for top part (welcome message)
        welcomeLabel = new Label();
        welcomeLabel.setStyle("-fx-font-size: 16px;");
        pointsStatusLabel = new Label();
        
        VBox welcomeBox = new VBox(5, welcomeLabel, pointsStatusLabel);
        welcomeBox.setPadding(new Insets(10));
        
        // Create components for middle part (books table with checkboxes)
        customerBooksTable = new TableView<>();
        
        TableColumn<BookSelection, String> bookNameColumn = new TableColumn<>("Book Name");
        bookNameColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        bookNameColumn.setPrefWidth(200);
        
        TableColumn<BookSelection, Double> bookPriceColumn = new TableColumn<>("Book Price");
        bookPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        bookPriceColumn.setPrefWidth(100);
        
        TableColumn<BookSelection, Boolean> selectColumn = new TableColumn<>("Select");
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectColumn.setCellFactory(tc -> new CheckBoxTableCell<>());
        selectColumn.setPrefWidth(100);
        selectColumn.setEditable(true);
        
        customerBooksTable.getColumns().addAll(bookNameColumn, bookPriceColumn, selectColumn);
        customerBooksTable.setEditable(true);
        
        // Create components for bottom part (buttons)
        buyButton = new Button("Buy");
        buyButton.setOnAction(e -> handleBuy(false));
        
        redeemAndBuyButton = new Button("Redeem points and Buy");
        redeemAndBuyButton.setOnAction(e -> handleBuy(true));
        
        customerLogoutButton = new Button("Logout");
        customerLogoutButton.setOnAction(e -> switchToLogin(e));
        
        HBox bottomButtons = new HBox(10, buyButton, redeemAndBuyButton, customerLogoutButton);
        bottomButtons.setPadding(new Insets(10));
        bottomButtons.setAlignment(Pos.CENTER);
        
        // Main layout for the customer start screen
        BorderPane customerStartLayout = new BorderPane();
        customerStartLayout.setTop(welcomeBox);
        customerStartLayout.setCenter(customerBooksTable);
        customerStartLayout.setBottom(bottomButtons);
        
        customerStartScene = new Scene(customerStartLayout, 500, 500);
    }
    
    private void createCustomerCostScene() {
        // Create components for cost screen
        totalCostLabel = new Label();
        totalCostLabel.setStyle("-fx-font-size: 16px;");
        
        pointsLabel = new Label();
        pointsLabel.setStyle("-fx-font-size: 14px;");
        
        costScreenLogoutButton = new Button("Logout");
        costScreenLogoutButton.setOnAction(e -> switchToLogin(e));
        
        // Layout for cost screen
        VBox costScreenLayout = new VBox(20);
        costScreenLayout.setPadding(new Insets(30));
        costScreenLayout.setAlignment(Pos.CENTER);
        costScreenLayout.getChildren().addAll(
                totalCostLabel,
                pointsLabel,
                costScreenLogoutButton
        );
        
        customerCostScene = new Scene(costScreenLayout, 400, 300);
    }
    
    // Event handlers
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (username.equals("admin") && password.equals("admin")) {
            // Owner login
            userContext.setState(new OwnerState());
            userContext.login();
            switchScene(ownerStartScene);
        } else {
            // Check if this is a customer
            for (Customer customer : owner.getCustomers()) {
                if (customer.getUsername().equals(username) && customer.getPassword().equals(password)) {
                    currentCustomer = customer;
                    userContext.setState(new CustomerState());
                    userContext.login();
                    
                    // Update customer welcome message
                    updateCustomerWelcome();
                    
                    // Populate books table for customer view
                    populateCustomerBooksTable();
                    
                    switchScene(customerStartScene);
                    return;
                }
            }
            
            // Invalid credentials
            showAlert(Alert.AlertType.ERROR, "Login Error", "Invalid username or password!");
        }
    }
    
    private void handleAddBook() {
        try {
            String bookName = bookNameField.getText();
            String bookAuthor = bookAuthorField.getText();
            double bookPrice = Double.parseDouble(bookPriceField.getText());
            
            if (bookName.isEmpty() || bookAuthor.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Book name and author cannot be empty!");
                return;
            }
            
            // Check if book already exists
            for (Book book : owner.getBooks()) {
                if (book.getTitle().equals(bookName)) {
                    showAlert(Alert.AlertType.ERROR, "Input Error", "A book with this name already exists!");
                    return;
                }
            }
            
            // Create new book and add to owner's books
            Book newBook = new Book(bookName, bookAuthor, bookPrice);
            owner.addBook(newBook);
            
            // Update table
            booksTable.setItems(FXCollections.observableArrayList(owner.getBooks()));
            
            // Clear form
            bookNameField.clear();
            bookAuthorField.clear();
            bookPriceField.clear();
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Price must be a valid number!");
        }
    }
    
    private void handleDeleteBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a book to delete!");
            return;
        }
        
        owner.removeBook(selectedBook);
        booksTable.setItems(FXCollections.observableArrayList(owner.getBooks()));
    }
    
    private void handleAddCustomer() {
        String username = customerUsernameField.getText();
        String password = customerPasswordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Username and password cannot be empty!");
            return;
        }
        
        // Check if customer already exists
        for (Customer customer : owner.getCustomers()) {
            if (customer.getUsername().equals(username)) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "A customer with this username already exists!");
                return;
            }
        }
        
        // Create new customer and add to owner's customers
        Customer newCustomer = new Customer(username, password);
        owner.addCustomer(newCustomer);
        
        // Update table
        customersTable.setItems(FXCollections.observableArrayList(owner.getCustomers()));
        
        // Clear form
        customerUsernameField.clear();
        customerPasswordField.clear();
    }
    
    private void handleDeleteCustomer() {
        Customer selectedCustomer = customersTable.getSelectionModel().getSelectedItem();
        
        if (selectedCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a customer to delete!");
            return;
        }
        
        owner.removeCustomer(selectedCustomer);
        customersTable.setItems(FXCollections.observableArrayList(owner.getCustomers()));
    }
    
    private void handleBuy(boolean redeem) {
        List<Book> selectedBooks = new ArrayList<>();
        double totalCost = 0;
        
        // Get selected books
        for (BookSelection bookSelection : customerBooksTable.getItems()) {
            if (bookSelection.isSelected()) {
                selectedBooks.add(bookSelection.getBook());
                totalCost += bookSelection.getPrice();
            }
        }
        
        if (selectedBooks.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select at least one book to buy!");
            return;
        }
        
        // Process purchase
        if (redeem) {
            // Calculate redeemable points
            int pointsToRedeem = Math.min(currentCustomer.getPoints(), (int)(totalCost * 100));
            double discountAmount = pointsToRedeem / 100.0;
            totalCost = Math.max(0, totalCost - discountAmount);
            
            // Redeem points
            currentCustomer.redeemPoints(totalCost);
        }
        
        // Add points for this purchase
        int pointsEarned = (int)(totalCost * 10);
        currentCustomer.addPoints(pointsEarned);
        
        // Update cost screen
        updateCostScreen(totalCost);
        
        // Switch to cost screen
        switchScene(customerCostScene);
    }
    
    private void updateCustomerWelcome() {
        String status = currentCustomer.getPoints() >= 1000 ? "Gold" : "Silver";
        welcomeLabel.setText("Welcome " + currentCustomer.getUsername());
        pointsStatusLabel.setText("You have " + currentCustomer.getPoints() + " points. Your status is " + status);
    }
    
    private void updateCostScreen(double totalCost) {
        totalCostLabel.setText("Total Cost: $" + String.format("%.2f", totalCost));
        
        String status = currentCustomer.getPoints() >= 1000 ? "Gold" : "Silver";
        pointsLabel.setText("Points: " + currentCustomer.getPoints() + ", Status: " + status);
    }
    
    private void populateCustomerBooksTable() {
        ObservableList<BookSelection> bookSelections = FXCollections.observableArrayList();
        
        for (Book book : owner.getBooks()) {
            bookSelections.add(new BookSelection(book));
        }
        
        customerBooksTable.setItems(bookSelections);
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void switchScene(Scene targetScene) {
        stage.setScene(targetScene);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    // Your existing navigation methods
    public void switchToOwnerScreen(ActionEvent event) {
        stage.setScene(ownerStartScene);
    }

    public void switchToLogin(ActionEvent event) {
        stage.setScene(scene); // Login scene
    }
    
    public void switchToOwnerBookScreen(ActionEvent event) {
        // Refresh book table data
        booksTable.setItems(FXCollections.observableArrayList(owner.getBooks()));
        stage.setScene(ownerBooksScene);
    }
    
    // New navigation methods
    public void switchToOwnerCustomerScreen(ActionEvent event) {
        // Refresh customer table data
        customersTable.setItems(FXCollections.observableArrayList(owner.getCustomers()));
        stage.setScene(ownerCustomersScene);
    }
    
    public void switchToCustomerScreen(ActionEvent event) {
        // Update customer welcome message
        updateCustomerWelcome();
        
        // Refresh book table data
        populateCustomerBooksTable();
        
        stage.setScene(customerStartScene);
    }
}