package com.example.javacw1601.backend;

import java.util.ArrayList;
import java.util.Random;

public class DealerManager {
    private ArrayList<Dealers> allDealers;

    public DealerManager() {
        allDealers = new ArrayList<>();
    }

    public DealerManager(ArrayList<Dealers> initialList) {
        this.allDealers = new ArrayList<>(initialList);
    }

    public void loadDealers(String filePath) {
        this.allDealers = LegacyDataHandler.loadDealersList(filePath);
    }

    public ArrayList<Dealers> getAllDealers() {
        return new ArrayList<>(allDealers);
    }

    public int getTotalCount() {
        return allDealers.size();
    }

    private void bSortByLocation(ArrayList<Dealers> list) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (list.get(j).getLocation().compareToIgnoreCase(list.get(j + 1).getLocation()) > 0) {
                    Dealers temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }

    public ArrayList<Dealers> select4Random() {
        if (allDealers.size() < 4) {
            throw new IllegalStateException("Not enough dealers");
        }
        Random rand = new Random();
        ArrayList<Integer> chosenIndices = new ArrayList<>();
        ArrayList<Dealers> selected = new ArrayList<>();
        while (selected.size() < 4) {
            int index = rand.nextInt(allDealers.size());
            if (!chosenIndices.contains(index)) {
                chosenIndices.add(index);
                selected.add(allDealers.get(index));
            }
        }
        bSortByLocation(selected);
        return selected;
    }
}