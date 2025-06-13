package com.example.doantotnghiep.model;

import com.google.gson.Gson;

public class User {

    private String email;
    private String password;
    private boolean isAdmin;

    // Thêm các thông tin mới
    private String fullName;
    private String phoneNumber;
    private String address;
    private double balance; // Số dư tài khoản
    private String profileImageUrl;
    private String dateOfBirth;
    private String gender;

    public User() {}

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.balance = 0.0; // Khởi tạo số dư = 0
    }

    // Constructor đầy đủ
    public User(String email, String password, String fullName, String phoneNumber, String address) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.balance = 0.0;
    }

    // Getters và Setters cho các thuộc tính cũ
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    // Getters và Setters cho các thuộc tính mới
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    // Phương thức hỗ trợ
    public String getDisplayName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }
        return email != null ? email.split("@")[0] : "Người dùng";
    }

    public String getFormattedBalance() {
        return String.format("%,.0f", balance) + "đ";
    }

    public boolean hasCompleteProfile() {
        return fullName != null && !fullName.trim().isEmpty() &&
                phoneNumber != null && !phoneNumber.trim().isEmpty() &&
                address != null && !address.trim().isEmpty();
    }

    // Phương thức để cập nhật số dư
    public void addBalance(double amount) {
        this.balance += amount;
    }

    public boolean deductBalance(double amount) {
        // Kiểm tra số dư có đủ không (thêm buffer nhỏ để tránh lỗi làm tròn)
        if (this.balance >= amount) {
            this.balance -= amount;
            // Đảm bảo không bị âm do lỗi làm tròn
            if (this.balance < 0) {
                this.balance = 0;
            }
            return true;
        }
        return false;
    }
    public boolean hasEnoughBalance(double amount) {
        return this.balance >= amount;
    }

    //method lấy số dư còn thiếu
    public double getBalanceShortage(double requiredAmount) {
        if (this.balance >= requiredAmount) {
            return 0;
        }
        return requiredAmount - this.balance;
    }

    public String toJSon() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}