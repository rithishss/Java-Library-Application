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

    // File paths
    private static final String CUSTOMER_FILE = "customers.txt";
    private static final String BOOKS_FILE = "books.txt";
    
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
    private Text totalCost;   

    
    @FXML
    public void initialize() {
        if (usernames != null && passwords != null) {
            usernames.setCellValueFactory(new PropertyValueFactory<>("username"));
            passwords.setCellValueFactory(new PropertyValueFactory<>("password"));
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
    }



    // Load customer data from customer.txt
    private void loadCustomersFromFile() {
        customerList.clear();
        userCredentials.clear(); // Clear previous credentials to avoid duplicates

        try (BufferedReader br = new BufferedReader(new FileReader(CUSTOMER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] customerData = line.split(",");
                if (customerData.length == 3) {
                    String username = customerData[0];
                    String password = customerData[1];
                    int points = Integer.parseInt(customerData[2]);

                    customerList.add(new Customer(username, password, points)); 
                    userCredentials.put(username, password); // Store in HashMap
                }
            }

        } catch (IOException e) {
            System.out.println("Error loading customers: " + e.getMessage());
        }
    }




    private void saveCustomersToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(CUSTOMER_FILE))) {
            for (Customer customer : customerList) {
                bw.write(customer.getUsername() + "," + customer.getPassword() + "," + customer.getPoints());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving customers to file: " + e.getMessage());
        }
    }


    // Load books data from books.txt
    private void loadBooksFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] bookData = line.split(",");
                if (bookData.length == 3) {
                    String title = bookData[0];
                    String author = bookData[1];
                    double price = Double.parseDouble(bookData[2]);
                    bookList.add(new Book(title, author, price));  // Add to the observable list
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading books from file: " + e.getMessage());
        }
    }


    // Save books data to books.txt
    private void saveBooksToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            for (Book book : bookList) {
                bw.write(book.getTitle() + "," + book.getAuthor() + "," + book.getPrice());
                bw.newLine();  // Write each book on a new line
            }
        } catch (IOException e) {
            System.out.println("Error saving books to file: " + e.getMessage());
        }
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
        } else if (userCredentials.containsKey(user) && userCredentials.get(user).equals(pass)) {
            // Customer login
            System.out.println("Customer login successful!");
            userContext.setState(new CustomerState());
            switchScene(event, "CustomerStartScreen.fxml");
            customerName.setText(user);
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

            Customer newCustomer = new Customer(newCustomerName, newCustomerPassword, 0); // Set points to 0
            customerList.add(newCustomer);  
            userCredentials.put(newCustomerName, newCustomerPassword);
            usernameTable.setItems(customerList);
            saveCustomersToFile();  

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
            saveCustomersToFile();
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
            saveBooksToFile();  // Save the updated books to the file
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
            saveBooksToFile();  // Save the updated list to the file
            System.out.println("Book deleted successfully!");
        } else {
            showAlert("Error", "Select a book to delete!");
        }
    }

    public void setCustomerDetails(String points, String status, double price) {
            customerPoints2.setText(points);
            customerStatus2.setText(status);
            totalCost.setText(String.format("$%.2f", price));
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
    void goBuyScreen(ActionEvent event) throws IOException {
        Book selectedBook = bookCustomerTable.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            showAlert("Error", "Please select a book to purchase.");
            return;
        }

        switchScene(event, "CustomerCostScreen.fxml");

        // Set the values in CustomerCostScreen after switching
        customerPoints2.setText(customerPoints.getText());
        customerStatus2.setText(customerStatus.getText());
        totalCost.setText(String.format("$%.2f", selectedBook.getPrice()));
    }


    @FXML
    void goBuyScreenRedeem(ActionEvent event)throws IOException {
        switchScene(event, "CustomerCostScreen.fxml");
    }
}
