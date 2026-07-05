/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookapp;

/**
 *
 * @author ssurk
 */
// Profile.java
public class Profile {
    private String name;
    private String address;
    private Account account;

    public Profile(String name, String address, Account account) {
        this.name = name;
        this.address = address;
        this.account = account;
    }

    public void updateProfile(String name, String address) {
        this.name = name;
        this.address = address;
    }
}
