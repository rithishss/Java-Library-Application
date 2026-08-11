/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookapp;

/**
 *
 * @author ssurk
 */
// Book.java
public class Book {
    private int id;  // Row id in the books table, 0 until it has been saved
    private String title;
    private String author;
    private double price;
    private boolean isPurchased = false;

    public Book(String title, String author, double price) {
        this.title = title;
        this.author = author;
        this.price = price;
    }

    public Book(int id, String title, String author, double price) {
        this(title, author, price);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    
    public String getAuthor(){
        return author;
    }

    public void purchase() {
        isPurchased = true;
    }

    public void returnBook() {
        isPurchased = false;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public boolean isPurchased() {
        return isPurchased;
    }
}
