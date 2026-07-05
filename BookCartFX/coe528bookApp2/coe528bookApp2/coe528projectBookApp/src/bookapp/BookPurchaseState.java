/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookapp;

/**
 *
 * @author ssurk
 */
// BookPurchaseState.java
public interface BookPurchaseState {
    void purchase(Customer c);
    void notPurchase(Customer c);
}