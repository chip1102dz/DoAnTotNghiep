package com.example.doantotnghiep.model;

import java.io.Serializable;

public class ProductOrder implements Serializable {

    private long id;
    private String name;
    private String description;
    private int count;
    private int price;
    private String image;

    public ProductOrder() {}

    public ProductOrder(long id, String name, String description, int count, int price, String image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.count = count;
        this.price = price;
        this.image = image;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
