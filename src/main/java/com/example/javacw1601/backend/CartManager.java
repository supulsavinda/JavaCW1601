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

    public boolean addToCart(String itemCode, int quantity) {
        if(quantity <= 0) return false;
        Inventory item = inventoryManager.findItemByCode(itemCode);
        if(item == null || item.getQuantity() < quantity) return false;

        for(CartItem ci : cart) {
            if(ci.getItem().getCode().equalsIgnoreCase(itemCode)) {
                int newQty = ci.getQuantity() + quantity;
                if(newQty > item.getQuantity()) return false;
                ci.setQuantity(newQty);
                return true;
            }
        }
        cart.add(new CartItem(item, quantity));
        return true;
    }
}
