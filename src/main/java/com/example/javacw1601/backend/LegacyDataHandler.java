package com.example.javacw1601.backend;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
        for (int i = 0; i < lst.length; i++) {
            String[] component = lst[i].split("(?!(?<=\\d{1,2})(?=,\\s?\\d{4}\\b)),|[|;]", -1);
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
        String[][] grid = loadData(filePath, 9);
        ArrayList<Inventory> itemList = new ArrayList<>();
        for (int i = 0; i < grid.length; i++) {
            try {
                String partCode = grid[i][0];
                String name = grid[i][1];
                String brand = grid[i][2];
                String cleanPrice = grid[i][3].replaceAll("(?i)rs\\.?\\s*", "").replaceAll("(?i)lkr\\.?\\s*", "").replaceAll("[^\\d.]", "");
                double price = Double.parseDouble(cleanPrice);
                int quantity = Integer.parseInt(grid[i][4]);
                String category = grid[i][5];
                String dateAdded = normalizeDate(grid[i][6]);
                int threshold = 5;
                try {
                    threshold = Integer.parseInt(grid[i][7].trim());
                } catch (NumberFormatException e) {
                    // keep default
                }
                String image = grid[i][8].isEmpty() ? "no_image.png" : grid[i][8];
                itemList.add(new Inventory(partCode, name, brand, price, quantity, category, dateAdded, threshold, image));
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

    private static String normalizeDate(String norDate) {
        norDate = norDate.trim();
        if (norDate.isEmpty()) return "Unknown";
        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("dd-MMM-yyyy"),
                DateTimeFormatter.ofPattern("MMM d, yyyy"),
                DateTimeFormatter.ofPattern("MMM dd, yyyy")
        );
        for (DateTimeFormatter fmt : formatters) {
            try {
                LocalDate date = LocalDate.parse(norDate, fmt);
                return date.toString();
            } catch (DateTimeParseException ignored) {}
        }
        return norDate;
    }

    public static void saveInventory(String filePath, ArrayList<Inventory> items) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            for (Inventory item : items) {
                pw.printf("%s, %s, %s, Rs.%.2f, %d, %s, %s, %d, %s%n",
                        item.getCode(),
                        item.getName(),
                        item.getBrand(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getCategory(),
                        item.getDate(),
                        item.getThreshold(),
                        item.getImage()
                );
            }
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }

    public static void saveDealers(String filePath, ArrayList<Dealers> dealers) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            for (Dealers d : dealers) {
                pw.printf("%s, %s, %s, %s%n",
                        d.getCode(),
                        d.getName(),
                        d.getPhone(),
                        d.getLocation()
                );
            }
        } catch (IOException e) {
            System.out.println("Error writing dealer file: " + e.getMessage());
        }
    }
}