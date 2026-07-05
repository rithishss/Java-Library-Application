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
        // Action on initiating a purchase
    }

    @Override
    public void notPurchase(Customer c) {
        // Optional behavior for not purchasing
    }
}
