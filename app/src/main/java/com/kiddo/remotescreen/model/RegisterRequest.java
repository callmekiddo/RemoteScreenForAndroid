package com.kiddo.remotescreen.model;

public class RegisterRequest {
    private final String fullName;
    private final String email;
    private final String password;

    public RegisterRequest(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
