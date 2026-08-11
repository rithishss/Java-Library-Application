/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookapp;

/**
 *
 * @author ssurk
 */
// NotPurchased.java
public class NotPurchased implements BookPurchaseState {
    @Override
    public void purchase(Customer c) {
        c.setBookPurchaseState(new Purchased());  // Order confirmed
    }

    @Override
    public void notPurchase(Customer c) {
        // Already not purchased, nothing to change
    }
}
