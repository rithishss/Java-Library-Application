/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
*/
package bookapp;

/**
 *
 * @author ssurk
 */
// Owner.java
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import javafx.stage.Stage;

public class Owner {
    private final List<Customer> customers;
    private final List<Book> books;
    private Stage stage;
    private Scene scene;
    private Parent root;

    public Owner() {
        customers = new ArrayList<>();
        books = new ArrayList<>();
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public void removeBook(Book book) {
        books.remove(book);
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public void removeCustomer(Customer customer) {
        customers.remove(customer);
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<Customer> getCustomers() {
        return customers;
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
    void switchBookScreen(ActionEvent event) throws IOException  {
        switchScene(event, "OwnerBooksScreen.fxml");
    }

    @FXML
    void switchCustomerScreen(ActionEvent event) throws IOException {
        switchScene(event, "OwnerCustomerScreen.fxml");
    }
    
    @FXML
    void logout(ActionEvent event) throws IOException {
        switchScene(event, "LoginScreen.fxml");
    }
}
