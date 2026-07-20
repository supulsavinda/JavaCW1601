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
        if (quantity <= 0) return false;
        Inventory item = inventoryManager.findItemByCode(itemCode);
        if (item == null || item.getQuantity() < quantity) return false;

        for (CartItem ci : cart) {
            if (ci.getItem().getCode().equalsIgnoreCase(itemCode)) {
                int newQty = ci.getQuantity() + quantity;
                if (newQty > item.getQuantity()) return false;
                ci.setQuantity(newQty);
                return true;
            }
        }
        cart.add(new CartItem(item, quantity));
        return true;
    }

    public boolean removeFromCart(String itemCode) {
        return cart.removeIf(ci -> ci.getItem().getCode().equalsIgnoreCase(itemCode));
    }

    public boolean updateQuantity(String itemCode, int newQuantity) {
        for (CartItem ci : cart) {
            if (ci.getItem().getCode().equalsIgnoreCase(itemCode)) {
                if (newQuantity <= 0) return removeFromCart(itemCode);
                if (newQuantity > ci.getItem().getQuantity()) return false;
                ci.setQuantity(newQuantity);
                return true;
            }
        }
        return false;
    }

    public double calculateTotal(){
        if (cart.isEmpty()) return 0.0;
        double subtotal = 0.0;
        boolean hasEngine = false, hasElectrical = false;

        for (CartItem ci : cart) {
            Inventory item =  ci.getItem();
            int qty = ci.getQuantity();
            double itemTotal = item.getPrice() * qty;
            if (qty >= 3) itemTotal *= 0.95;
            subtotal += itemTotal;
            String cat = item.getCategory().toLowerCase();
            if (cat.equals("engine")) hasEngine = true;
            if (cat.equals("electrical")) hasElectrical = true;
        }
        if (hasEngine && hasElectrical) subtotal *= 0.9;
        return subtotal;
    }
}

