package bookapp;

public class CustomerState implements UserState {
    @Override
    public void login(UserContext user) {
        System.out.println("Customer logged in, switching to Customer UI...");
        // UI transition handled in SceneController
    }

    @Override
    public void logout(UserContext user) {
        System.out.println("Customer logged out, returning to Login UI...");
    }
}
