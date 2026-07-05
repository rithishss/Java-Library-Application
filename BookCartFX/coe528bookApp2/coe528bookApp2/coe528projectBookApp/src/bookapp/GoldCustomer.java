/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookapp;

/**
 *
 * @author ssurk
 */
// GoldCustomer.java
public class GoldCustomer implements CustomerPurchaseState {
    @Override
    public void rankUp(Customer c) {
        // Already gold, no upgrade
    }

    @Override
    public void rankDown(Customer c) {
        if (c.getPoints() < 1000) {
            c.setCustomerPurchaseState(new SilverCustomer());
        }
    }
}
