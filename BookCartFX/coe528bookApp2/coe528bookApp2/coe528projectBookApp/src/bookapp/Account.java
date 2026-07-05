/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookapp;

/**
 *
 * @author ssurk
 */
// Account.java

public class Account {
    private String username;
    private String password;
    private String email;
    private String role;

    public Account(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public UserState login(UserContext context) {
        UserState state;
        if (role.equals("admin")) {
            state = new OwnerState();
        } else {
            state = new CustomerState();
        }
        context.setState(state);
        return state;
    }

    public void logout() {
        // Placeholder for logout logic
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}