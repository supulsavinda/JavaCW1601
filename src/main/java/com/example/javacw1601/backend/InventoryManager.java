package com.example.javacw1601.backend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class InventoryManager {
    private ArrayList<Inventory> items;

    public InventoryManager() {
        items = new ArrayList<>();
    }

    public InventoryManager(ArrayList<Inventory> initialList) {
        this.items = initialList;
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


    private void bSortByCode(ArrayList<Inventory> list) {
        int n= list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1; j++) {
                if (list.get(j).getCode().compareTo(list.get(j + 1).getCode())>0) {
                    Inventory temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }

    public ArrayList<Inventory> getSortedItems() {
        Set<String> categorySet = new HashSet<>();
        for (Inventory inv : items) {
            categorySet.add(inv.getCategory().toLowerCase());
        }
        ArrayList<Inventory> result = new ArrayList<>();

        for (String cat :  categorySet) {
            ArrayList<Inventory> sublist = new ArrayList<>();
            for (Inventory inv : items) {
                if (inv.getCategory().equalsIgnoreCase(cat)) {
                    sublist.add(inv);
                }
            }
            bSortByCode(sublist);
            result.addAll(sublist);
        }
        return result;
    }
}
