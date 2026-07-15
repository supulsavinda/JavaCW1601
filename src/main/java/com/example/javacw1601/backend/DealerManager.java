package com.example.javacw1601.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DealerManager {
    private ArrayList<Dealers> allDealers;
    public DealerManager() {
        allDealers = new ArrayList<>();
    }

    public DealerManager(ArrayList<Dealers> initialList) {
        this.allDealers =  new ArrayList<>(initialList);
    }

    public void loadDealers(String filePath) {
        this.allDealers = LegacyDataHandler.loadDealersList(filePath);
    }

    public ArrayList<Dealers> getAllDealers() {
        return new ArrayList<>(allDealers);
    }

    private void bSortByLocation(ArrayList<Dealers> list) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                if (list.get(j).getLocation().compareToIgnoreCase(list.get(j+1).getLocation()) > 0) {
                    Dealers temp = list.get(j);
                    list.set(j, list.get(j+1));
                    list.set(j+1, temp);
                }
            }
        }
    }
}
