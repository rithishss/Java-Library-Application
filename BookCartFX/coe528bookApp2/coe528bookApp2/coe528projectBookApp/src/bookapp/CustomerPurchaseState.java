/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookapp;

/**
 *
 * @author ssurk
 */
// CustomerPurchaseState.java
public interface CustomerPurchaseState {
    void rankUp(Customer c);
    void rankDown(Customer c);

    // Lets the screens show the rank without testing the state's class.
    String getStatusName();
}
