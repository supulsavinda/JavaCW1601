package com.example.javacw1601.backend;

import java.util.ArrayList;

public class CartManager {
    private ArrayList<CartItem> cart;
    private InventoryManager inventoryManager;
    private AuditLogger auditLogger;

    public CartManager(InventoryManager inventoryManager) {
        this.cart = new ArrayList<>();
        this.inventoryManager = inventoryManager;
        this.auditLogger = new AuditLogger();
    }

    public ArrayList<CartItem> getCartItems() {
        return new ArrayList<>(cart);
    }

    public void clearCart() {
        cart.clear();
    }
}
