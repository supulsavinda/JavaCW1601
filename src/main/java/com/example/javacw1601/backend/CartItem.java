package com.example.javacw1601.backend;

public class CartItem {
    private Inventory item;
    private int quantity;

    public CartItem(Inventory item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public Inventory getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
