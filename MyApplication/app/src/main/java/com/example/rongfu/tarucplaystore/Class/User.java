package com.example.rongfu.tarucplaystore.Class;

public class User {
    private int userId;
    private String username;
    private String password;
    private String email;
    private String emailVerificationStatus;
    private String forgetPassStatus;
    private String veriCode;

    public User() {
    }

    public User(int userId, String username, String password, String email, String emailVerificationStatus, String forgetPassStatus) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.emailVerificationStatus = emailVerificationStatus;
        this.forgetPassStatus = forgetPassStatus;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getEmailVerificationStatus() {
        return emailVerificationStatus;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailVerificationStatus(String emailVerificationStatus) {
        this.emailVerificationStatus = emailVerificationStatus;
    }

    public String getForgetPassStatus() {
        return forgetPassStatus;
    }

    public void setForgetPassStatus(String forgetPassStatus) {
        this.forgetPassStatus = forgetPassStatus;
    }

    public String getVeriCode() {
        return veriCode;
    }

    public void setVeriCode(String veriCode) {
        this.veriCode = veriCode;
    }

    @Override
    public String toString() {
        return "User{" + "userId=" + userId + ", username=" + username + ", password=" + password + ", email=" + email + ", emailVerificationStatus=" + emailVerificationStatus + '}';
    }


}
