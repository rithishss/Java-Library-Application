/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookapp;

/**
 *
 * @author ssurk
 */
public class Main {
    public static void main(String[] args) {
        // Create an admin account and customer account
        Account ownerAccount = new Account("admin", "admin123", "admin@example.com", "admin");
        Account customerAccount = new Account("user1", "password", "user1@example.com", "customer");

        // Create an owner and a customer
        Owner owner = new Owner();
        Customer customer = new Customer("user1", "password");

        // Add customer to owner's customer list
        owner.addCustomer(customer);

        // Create books and add them to owner's store
        Book book1 = new Book("Java Programming", "John Doe", 29.99);
        Book book2 = new Book("Design Patterns", "Jane Doe", 39.99);

        owner.addBook(book1);
        owner.addBook(book2);

        // Customer purchases a book
        customer.purchase();
        book1.purchase();
        customer.addPoints(1500); // Add points for purchase

        // Display customer rank
        System.out.println("Customer rank before ranking up: " + customer.getPoints());
        customer.rankUp();
        System.out.println("Customer rank after ranking up: " + (customer.getPoints() >= 1000 ? "Gold" : "Silver"));

        // Display books available in store
        System.out.println("Books available in store:");
        for (Book book : owner.getBooks()) {
            System.out.println(book.getTitle() + " - $" + book.getPrice());
        }
    }
}