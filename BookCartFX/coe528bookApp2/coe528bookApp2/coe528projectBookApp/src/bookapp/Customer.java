/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookapp;

/**
 *
 * @author ssurk
 */
// Customer.java
public class Customer {
    private String username;
    private String password;
    private int points = 0;
    private BookPurchaseState bookState = new NotPurchased();
    private CustomerPurchaseState customerState = new SilverCustomer();

    public Customer(String username, String password) {
        this.username = username;
        this.password = password;
        this.points = 0; // New users start with 0 points
    }
    
    public Customer(String username, String password, int points) {
        this.username = username;
        this.password = password;
        this.points = points;
        updateStatus();  // Someone loaded from the database with 1000+ points is already Gold
    }

    public void purchase() {
        bookState.purchase(this);
    }

    public void notPurchase() {
        bookState.notPurchase(this);
    }

    public void rankUp() {
        customerState.rankUp(this);
    }

    public void rankDown() {
        customerState.rankDown(this);
    }

    public void setBookPurchaseState(BookPurchaseState state) {
        this.bookState = state;
    }

    public void setCustomerPurchaseState(CustomerPurchaseState state) {
        this.customerState = state;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // What the owner's customer table shows. The stored value is a hash, so
    // there is nothing useful to display and every reason not to.
    public String getMaskedPassword() {
        return "••••••••";
    }

    public int getPoints() {
        return points;
    }

    public String getStatus() {
        return customerState.getStatusName();
    }

    public void addPoints(int amount) {
        points += amount;
        updateStatus();
    }

    public void redeemPoints(double cost) {
        int redeemable = Math.min(points, (int) (cost * 100));
        points -= redeemable;
        updateStatus();
    }

    // Asks the current state to move, rather than deciding here, so that
    // GoldCustomer and SilverCustomer are what actually govern the transition.
    private void updateStatus() {
        rankUp();    // Silver -> Gold once the balance reaches 1000
        rankDown();  // Gold -> Silver once it falls back below
    }
}
