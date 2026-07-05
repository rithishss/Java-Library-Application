package bookapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class UserContext {
    private UserState state;

    public UserContext() {
        this.state = new CustomerState(); // Default to Customer
    }

    public void setState(UserState state) {
        this.state = state;
    }

    public void login() {
        state.login(this);
    }

    public void logout() {
        state.logout(this);
    }
    @FXML
    public void goCustomerCostScreenRedeemed(ActionEvent event) {
        // Your code here
    }
}
