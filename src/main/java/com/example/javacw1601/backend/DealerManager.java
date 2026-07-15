package com.example.javacw1601.backend;

import java.util.ArrayList;

public class DealerManager {
    private ArrayList<Dealers> allDealers;
    public DealerManager() {
        allDealers = new ArrayList<>();
    }

    public DealerManager(ArrayList<Dealers> initialList) {
        this.allDealers =  new ArrayList<>(initialList);
    }

    public void loadDealers(String filePath) {
        this.allDealers = LegacyDataHandler.loadDealersList()
    }

    public ArrayList<Dealers> getAllDealers() {
        return new ArrayList<>(allDealers);
    }
}
