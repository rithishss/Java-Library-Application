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

    public int getPoints() {
        return points;
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

    private void updateStatus() {
        if (points >= 1000) {
            setCustomerPurchaseState(new GoldCustomer());
        } else {
            setCustomerPurchaseState(new SilverCustomer());
        }
    }
}
