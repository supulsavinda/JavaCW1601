package com.example.javacw1601.backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
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
            String[] component = lst[i].split("[,|;]", -1);

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

                String cleanPrice = grid[i][3];
                cleanPrice = cleanPrice.replaceAll("(?i)rs\\.?\\s*", "").replaceAll("(?i)lkr\\.?\\s*", "");
                cleanPrice = cleanPrice.replaceAll("[^\\d.]", "");
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

    private static String normalizeDate(String norDate){
        norDate = norDate.trim();
        if(norDate.isEmpty()) return "Unknown";
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
        for(DateTimeFormatter fmt : formatters){
            try{
                LocalDate date = LocalDate.parse(norDate, fmt);
                return date.toString();
            } catch(DateTimeParseException ignored){}
        }
        return norDate;
    }
}