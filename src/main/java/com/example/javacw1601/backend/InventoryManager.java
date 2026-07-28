package com.example.javacw1601.backend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class InventoryManager {
    private ArrayList<Inventory> items;
    private int lowStockThreshold = 5;

    public InventoryManager() {
        items = new ArrayList<>();
    }

    public InventoryManager(ArrayList<Inventory> initialList) {
        this.items = new ArrayList<>(initialList);
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int threshold) {
        if (threshold >= 0) this.lowStockThreshold = threshold;
    }

    public ArrayList<Inventory> getLowStockItems() {
        return getLowStockItems(lowStockThreshold);
    }

    public ArrayList<Inventory> getAllItems() {
        return new ArrayList<>(items);
    }

    public int getTotalCount() {
        return items.size();
    }

    public double getTotalValue() {
        double total = 0;
        for (Inventory inv : items) {
            total += inv.getPrice() * inv.getQuantity();
        }
        return total;
    }

    public Inventory findItemByCode(String code) {
        for (Inventory inv : items) {
            if (inv.getCode().equalsIgnoreCase(code)) {
                return inv;
            }
        }
        return null;
    }

    public boolean addItem(Inventory newItem) {
        for (Inventory inv : items) {
            if (inv.getCode().equalsIgnoreCase(newItem.getCode())) {
                return false;
            }
        }
        items.add(newItem);
        return true;
    }

    public boolean updateItem(String code, Inventory updated) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getCode().equalsIgnoreCase(code)) {
                updated.setCode(code);
                items.set(i, updated);
                return true;
            }
        }
        return false;
    }

    public boolean deleteItem(String code) {
        return items.removeIf(inv -> inv.getCode().equalsIgnoreCase(code));
    }

    public ArrayList<Inventory> getLowStockItems(int threshold) {
        ArrayList<Inventory> lowStock = new ArrayList<>();
        for (Inventory inv : items) {
            if (inv.getQuantity() < threshold) {
                lowStock.add(inv);
            }
        }
        return lowStock;
    }

    public ArrayList<String> getCategories() {
        Set<String> categorySet = new HashSet<>();
        for (Inventory inv : items) {
            if (inv.getCategory() != null && !inv.getCategory().trim().isEmpty()) {
                categorySet.add(inv.getCategory());
            }
        }
        return new ArrayList<>(categorySet);
    }

    private void bSortByCode(ArrayList<Inventory> list) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (list.get(j).getCode().compareTo(list.get(j + 1).getCode()) > 0) {
                    Inventory temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }

    private void bSortStrings(ArrayList<String> list) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (list.get(j).compareToIgnoreCase(list.get(j + 1)) > 0) {
                    String temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }

    public ArrayList<Inventory> groupByCategoryThenCode(ArrayList<Inventory> source) {
        ArrayList<String> categoryOrder = new ArrayList<>();
        for (Inventory inv : source) {
            boolean alreadyPresent = false;
            for (String c : categoryOrder) {
                if (c.equalsIgnoreCase(inv.getCategory())) {
                    alreadyPresent = true;
                    break;
                }
            }
            if (!alreadyPresent) {
                categoryOrder.add(inv.getCategory());
            }
        }
        bSortStrings(categoryOrder);

        ArrayList<Inventory> result = new ArrayList<>();
        for (String cat : categoryOrder) {
            ArrayList<Inventory> sublist = new ArrayList<>();
            for (Inventory inv : source) {
                if (inv.getCategory().equalsIgnoreCase(cat)) {
                    sublist.add(inv);
                }
            }
            bSortByCode(sublist);
            result.addAll(sublist);
        }
        return result;
    }

    public ArrayList<Inventory> getSortedItems() {
        return groupByCategoryThenCode(items);
    }

    public ArrayList<Inventory> search(String category, double minPrice, double maxPrice, String keyword) {
        ArrayList<Inventory> result = new ArrayList<>();
        for (Inventory inv : items) {
            boolean match = true;
            if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("All")) {
                if (!inv.getCategory().equalsIgnoreCase(category)) {
                    match = false;
                }
            }
            if (minPrice <= maxPrice) {
                if (inv.getPrice() < minPrice || inv.getPrice() > maxPrice) {
                    match = false;
                }
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String lowerKeyword = keyword.toLowerCase();
                if (!inv.getName().toLowerCase().contains(lowerKeyword) && !inv.getBrand().toLowerCase().contains(lowerKeyword)) {
                    match = false;
                }
            }
            if (match) {
                result.add(inv);
            }
        }
        return result;
    }
}