package com.example.javacw1601.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class DataParserTest {

    @TempDir
    Path tempDir;

    private File testInventoryFile;
    private File testDealerFile;

    @BeforeEach
    void setUp() throws IOException {
        testInventoryFile = tempDir.resolve("test_inventory.txt").toFile();
        testDealerFile = tempDir.resolve("test_dealers.txt").toFile();
    }

    @Test
    void testLoadItemsList_ValidRecord_ReturnsInventoryObject() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(testInventoryFile))) {
            pw.println("P001, Brake Pad, Bosch, Rs.2500.50, 15, Brakes, 2025-01-15, brake_pad.png");
        }

        ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(testInventoryFile.getAbsolutePath());

        assertNotNull(items);
        assertEquals(1, items.size());

        Inventory item = items.get(0);
        assertEquals("P001", item.getCode());
        assertEquals("Brake Pad", item.getName());
        assertEquals("Bosch", item.getBrand());
        assertEquals(2500.50, item.getPrice(), 0.01);
        assertEquals(15, item.getQuantity());
        assertEquals("Brakes", item.getCategory());
        assertEquals("2025-01-15", item.getDate());
        assertEquals("brake_pad.png", item.getImage());
    }

    @Test
    void testLoadItemsList_MixedDelimiters_ParsesCorrectly() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(testInventoryFile))) {
            pw.println("P002, Oil Filter, Mann, Rs.1200.00, 20, Filters, 2025-01-20, oil_filter.png");
            pw.println("P003|Fuel Filter|Bosch|Rs.1800.00|12|Filters|2025-01-25|fuel_filter.png");
            pw.println("P004;Air Filter;Mann;Rs.950.00;25;Filters;2025-02-01;air_filter.png");
        }

        ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(testInventoryFile.getAbsolutePath());

        assertNotNull(items);
        assertEquals(3, items.size());
        assertEquals("P002", items.get(0).getCode());
        assertEquals("P003", items.get(1).getCode());
        assertEquals("P004", items.get(2).getCode());
    }

    @Test
    void testLoadItemsList_MissingImageField_UsesDefaultImage() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(testInventoryFile))) {
            pw.println("P005, Spark Plug, NGK, Rs.850.00, 30, Electrical, 2025-02-05,");
        }

        ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(testInventoryFile.getAbsolutePath());

        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("no_image.png", items.get(0).getImage());
    }

    @Test
    void testLoadItemsList_InvalidPrice_HandlesGracefully() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(testInventoryFile))) {
            pw.println("P006, Test Item, Brand, InvalidPrice, 10, Category, 2025-01-01, image.png");
            pw.println("P007, Valid Item, Brand, Rs.500.00, 10, Category, 2025-01-01, image.png");
        }

        ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(testInventoryFile.getAbsolutePath());

        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("P007", items.get(0).getCode());
    }

    @Test
    void testLoadDealersList_ValidRecords_ReturnsDealerObjects() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(testDealerFile))) {
            pw.println("D001, City Spares, 011-2345678, Colombo");
            pw.println("D002, Rural Parts, 011-8765432, Kandy");
        }

        ArrayList<Dealers> dealers = LegacyDataHandler.loadDealersList(testDealerFile.getAbsolutePath());

        assertNotNull(dealers);
        assertEquals(2, dealers.size());

        Dealers dealer = dealers.get(0);
        assertEquals("D001", dealer.getCode());
        assertEquals("City Spares", dealer.getName());
        assertEquals("011-2345678", dealer.getPhone());
        assertEquals("Colombo", dealer.getLocation());
    }

    @Test
    void testNormalizeCategory_VariousFormats_ReturnsNormalized() {
        assertEquals("Engine", LegacyDataHandler.normalizeCategory("engine"));
        assertEquals("Engine", LegacyDataHandler.normalizeCategory("ENGINE"));
        assertEquals("Engine Parts", LegacyDataHandler.normalizeCategory("engine parts"));
        assertEquals("Body & Chassis", LegacyDataHandler.normalizeCategory("body & chassis"));
        assertEquals("Exhaust System", LegacyDataHandler.normalizeCategory("exhaust system"));
        assertEquals("", LegacyDataHandler.normalizeCategory(""));
        assertEquals("", LegacyDataHandler.normalizeCategory(null));
    }

    @Test
    void testNormalizeDate_VariousFormats_ReturnsStandardFormat() {
        String result = LegacyDataHandler.normalizeDate("2025-01-15");
        assertEquals("2025-01-15", result);

        result = LegacyDataHandler.normalizeDate("15/01/2025");
        assertEquals("2025-01-15", result);

        result = LegacyDataHandler.normalizeDate("");
        assertEquals("Unknown", result);

        result = LegacyDataHandler.normalizeDate("   ");
        assertEquals("Unknown", result);
    }
}