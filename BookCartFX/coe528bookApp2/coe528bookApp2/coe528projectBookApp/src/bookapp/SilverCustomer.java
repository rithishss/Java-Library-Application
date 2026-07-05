/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookapp;

/**
 *
 * @author ssurk
 */
// SilverCustomer.java
public class SilverCustomer implements CustomerPurchaseState {
    @Override
    public void rankUp(Customer c) {
        if (c.getPoints() >= 1000) {
            c.setCustomerPurchaseState(new GoldCustomer());
        }
    }

    @Override
    public void rankDown(Customer c) {
        // Already silver, no downgrade
    }
}
