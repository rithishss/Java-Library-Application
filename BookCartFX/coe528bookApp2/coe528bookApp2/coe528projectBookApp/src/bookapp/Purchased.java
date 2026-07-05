/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookapp;

/**
 *
 * @author ssurk
 */
// Purchased.java
public class Purchased implements BookPurchaseState {
    @Override
    public void purchase(Customer c) {
        // Action on purchase confirmation
    }

    @Override
    public void notPurchase(Customer c) {
        // Optional behavior for not purchasing
    }
}
