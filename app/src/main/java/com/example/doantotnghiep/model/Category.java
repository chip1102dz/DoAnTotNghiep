package com.example.doantotnghiep.model;

import java.io.Serializable;

public class Category implements Serializable {

    private int id;
    private String name;
    private boolean isSelected;
    public Category(String name) {
        this.name = name;
    }
    public Category() {}

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
