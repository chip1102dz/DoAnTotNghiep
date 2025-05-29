package com.example.doantotnghiep.model;

import java.io.Serializable;

public class StoreLocation implements Serializable {

    private String address;
    private String phone;
    private double latitude;
    private double longitude;

    public StoreLocation() {
    }

    public StoreLocation(String address, String phone, double latitude, double longitude) {
        this.address = address;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}