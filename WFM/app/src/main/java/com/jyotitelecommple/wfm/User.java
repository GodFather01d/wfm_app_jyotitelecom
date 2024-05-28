package com.jyotitelecommple.wfm;
public class User {
    private String email;
    private String name;
    private String password;
    private String Tank_id;

    // Empty constructor needed for Firebase
    public User() {
    }

    public User(String Tank_id) {

        this.Tank_id = Tank_id;
    }

    // Getters and setters (not necessary for Firebase, but good practice)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return Tank_id;
    }

    public void setUsername(String username) {
        this.Tank_id = username;
    }
}
