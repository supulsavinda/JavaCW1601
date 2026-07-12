package com.example.javacw1601.backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class LegacyDataHandler {
    public static String[][] loadData(String filePath, int noElements) {
        ArrayList<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        String[] rawLines = lines.toArray(new String[0]);
        return txt_formatter(rawLines, noElements);
    }

    private static String[][] txt_formatter(String[] lst, int no_elements) {
        String[][] elements = new String[lst.length][no_elements];
        Pattern datePattern = Pattern.compile("([A-Za-z]+\\s+\\d{1,2}),\\s*(\\d{4})");

        for (int i = 0; i < lst.length; i++) {
            String line = datePattern.matcher(lst[i]).replaceAll("$1 $2");
            String[] component = line.split("[,|;]", -1);

            for (int j = 0; j < no_elements; j++) {
                if (j < component.length) {
                    elements[i][j] = component[j].trim();
                } else {
                    elements[i][j] = "";
                }
            }
        }
        return elements;
    }

    public static ArrayList<Inventory> loadItemsList(String filePath) {
        String[][] grid = loadData(filePath, 8);
        ArrayList<Inventory> itemList = new ArrayList<>();

        for (int i = 0; i < grid.length; i++) {
            try {
                String partCode = grid[i][0];
                String name = grid[i][1];
                String brand = grid[i][2];

                String cleanPrice = grid[i][3].replace("Rs.", "").trim();
                double price = Double.parseDouble(cleanPrice);

                int quantity = Integer.parseInt(grid[i][4]);
                String category = grid[i][5];
                String dateAdded = grid[i][6];
                String image = grid[i][7].isEmpty() ? "no_image.png" : grid[i][7];

                itemList.add(new Inventory(partCode, name, brand, price, quantity, category, dateAdded, image));
            } catch (Exception e) {
                System.out.println("Skipping corrupted item row at line " + (i + 1) + ": " + e.getMessage());
            }
        }
        return itemList;
    }

    public static ArrayList<Dealers> loadDealersList(String filePath) {
        String[][] grid = loadData(filePath, 4);
        ArrayList<Dealers> dealerList = new ArrayList<>();

        for (int i = 0; i < grid.length; i++) {
            try {
                String dealerCode = grid[i][0];
                String name = grid[i][1];
                String phone = grid[i][2].isEmpty() ? "Unknown" : grid[i][2];
                String location = grid[i][3];

                dealerList.add(new Dealers(dealerCode, name, phone, location));
            } catch (Exception e) {
                System.out.println("Skipping corrupted dealer row at line " + (i + 1) + ": " + e.getMessage());
            }
        }
        return dealerList;
    }
}