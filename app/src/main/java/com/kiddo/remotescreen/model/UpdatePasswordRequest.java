package com.kiddo.remotescreen.model;

public class UpdatePasswordRequest {
    private String email;
    private String newPassword;
    private String confirmPassword;

    public UpdatePasswordRequest(String email, String newPassword, String confirmPassword) {
        this.email = email;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }
}
