package com.example.doantotnghiep.model;

public class TopUpAmount {
    private double amount;
    private String displayText;
    private boolean isSelected;

    public TopUpAmount(double amount, String displayText) {
        this.amount = amount;
        this.displayText = displayText;
        this.isSelected = false;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}