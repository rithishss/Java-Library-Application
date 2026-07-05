package bookapp;

public class OwnerState implements UserState {
    @Override
    public void login(UserContext user) {
        System.out.println("Owner logged in, switching to Owner UI...");
        // UI transition handled in SceneController
    }

    @Override
    public void logout(UserContext user) {
        System.out.println("Owner logged out, returning to Login UI...");
    }
}
