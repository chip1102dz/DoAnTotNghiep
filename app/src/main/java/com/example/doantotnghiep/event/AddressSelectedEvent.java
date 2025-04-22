package com.example.doantotnghiep.event;


import com.example.doantotnghiep.model.Address;

public class AddressSelectedEvent {

    private Address address;

    public AddressSelectedEvent(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
