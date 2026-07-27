package com.example.javacw1601.backend;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class AuditLogger {
    public static final String LOG_FILE = "audit_log.txt";

    private void log(String message) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pw.println(timestamp + " | " + message);
        } catch (IOException e) {
            System.err.println("Error writing to audit log: " + e.getMessage());
        }
    }

    public void logAddItem(Inventory item) {
        log("ADD | Code: " + item.getCode() + " | Name: " + item.getName() + " | Qty: " + item.getQuantity());
    }

    public void logDeleteItem(String code) {
        log("DELETE | Code: " + code);
    }

    public void logUpdateItem(String code, Inventory updated) {
        log("UPDATE | Code: " + code + " | New details: " + updated.getName() + ", " + updated.getQuantity());
    }

    public void logCheckout(ArrayList<CartItem> cart, double total) {
        StringBuilder sb = new StringBuilder("CHECKOUT | Total: Rs." + String.format("%.2f", total) + " | Items: ");
        for (CartItem ci : cart) {
            sb.append(ci.getItem().getCode()).append("(x").append(ci.getQuantity()).append(") ");
        }
        log(sb.toString());
    }

    public void logAddDealer(Dealers dealer) {
        log("ADD DEALER | Code: " + dealer.getCode() + " | Name: " + dealer.getName() + " | Location: " + dealer.getLocation());
    }

    public void logUpdateDealer(String code, Dealers updated) {
        log("UPDATE DEALER | Code: " + code + " | New name: " + updated.getName() + ", Location: " + updated.getLocation());
    }

    public void logDeleteDealer(String code) {
        log("DELETE DEALER | Code: " + code);
    }
}