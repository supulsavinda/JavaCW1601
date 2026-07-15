package com.example.javacw1601.backend;

import com.example.javacw1601.backend.Inventory;
import com.example.javacw1601.backend.Dealers;
import com.example.javacw1601.backend.LegacyDataHandler;
import com.example.javacw1601.backend.InventoryManager;
import java.util.ArrayList;

    public class Main {
        public static void main(String[] args) {
            String inventoryFile = "C:\\Users\\supul\\Downloads\\inventory_legacy.txt";
            String dealerFile = "C:\\Users\\supul\\Downloads\\dealers_legacy.txt";

            ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(inventoryFile);
            ArrayList<Dealers> dealers = LegacyDataHandler.loadDealersList(dealerFile);

            System.out.println("Loaded " + items.size() + " inventory items.");
            System.out.println("Loaded " + dealers.size() + " dealers.");

            InventoryManager manager = new InventoryManager(items);
            System.out.println("Total items: " + manager.getTotalCount());
            System.out.println("Total value: Rs." + manager.getTotalValue());
            System.out.println("Low stock (<5): " + manager.getLowStockItems(5).size() + " items");

            System.out.println("Grouped & Sorted by Category, then Code:");
            ArrayList<Inventory> sorted = manager.getSortedItems();
            String cat = "";
            for (Inventory inv : sorted) {
                if (!inv.getCategory().equalsIgnoreCase(cat)) {
                    cat = inv.getCategory();
                    System.out.println("  [" + cat + "]");
                }
                System.out.println("    " + inv.getCode() + " | " + inv.getName());
            }

            System.out.println("Search: Category=Engine, Price=1000-5000, Keyword=Bajaj");
            ArrayList<Inventory> found = manager.search("Engine", 1000, 5000, "Bajaj");
            for (Inventory inv : found) {
                System.out.println("    " + inv.getCode() + " | " + inv.getName() + " | Rs." + inv.getPrice());
            }

            System.out.println("Dealers (first 3):");
            for (int i = 0; i < Math.min(3, dealers.size()); i++) {
                Dealers d = dealers.get(i);
                System.out.println("    " + d.getCode() + " | " + d.getName() + " | " + d.getLocation());
            }
        }
    }

