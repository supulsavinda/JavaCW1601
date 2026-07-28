package com.example.javacw1601.backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AuditLogger.
 *
 * AuditLogger writes to a hard-coded relative path (AuditLogger.LOG_FILE)
 * rather than an injectable stream/writer, so it can't be fully isolated
 * with a mock. These tests exercise it "as-is": each test clears the log
 * file first, invokes the logger, then reads the real file back to check
 * the line that was written, and cleans up afterwards so tests don't leak
 * into each other or into other test classes (e.g. CartManagerTest).
 */
class AuditLoggerTest {

    private AuditLogger logger;

    @BeforeEach
    void setUp() {
        logger = new AuditLogger();
        deleteLogIfPresent();
    }

    @AfterEach
    void tearDown() {
        deleteLogIfPresent();
    }

    private void deleteLogIfPresent() {
        File logFile = new File(AuditLogger.LOG_FILE);
        if (logFile.exists()) {
            logFile.delete();
        }
    }

    private String lastLogLine() throws Exception {
        List<String> lines = Files.readAllLines(new File(AuditLogger.LOG_FILE).toPath());
        assertFalse(lines.isEmpty(), "expected at least one log line to have been written");
        return lines.get(lines.size() - 1);
    }

    @Test
    void logAddItem_writesCodeNameAndQuantity() throws Exception {
        Inventory item = new Inventory("Z1", "Zed", "BrandZ", 99.99, 3, "misc", "2023-01-01", "img.png");
        logger.logAddItem(item);

        String line = lastLogLine();
        assertTrue(line.contains("ADD"));
        assertTrue(line.contains("Code: Z1"));
        assertTrue(line.contains("Name: Zed"));
        assertTrue(line.contains("Qty: 3"));
        // timestamp prefix, e.g. "2026-07-28 06:20:50 | ..."
        assertTrue(line.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\|.*"));
    }

    @Test
    void logDeleteItem_writesTheDeletedCode() throws Exception {
        logger.logDeleteItem("Z9");

        String line = lastLogLine();
        assertTrue(line.contains("DELETE"));
        assertTrue(line.contains("Code: Z9"));
    }

    @Test
    void logUpdateItem_writesCodeAndNewDetails() throws Exception {
        Inventory updated = new Inventory("Z1", "Zed v2", "BrandZ", 120.0, 8, "misc", "2023-01-01", "img.png");
        logger.logUpdateItem("Z1", updated);

        String line = lastLogLine();
        assertTrue(line.contains("UPDATE"));
        assertTrue(line.contains("Code: Z1"));
        assertTrue(line.contains("Zed v2"));
        assertTrue(line.contains("8"));
    }

    @Test
    void logCheckout_writesTotalAndEachCartLineWithQuantity() throws Exception {
        Inventory item1 = new Inventory("E1", "Engine Part", "BX", 1000, 10, "Engine", "2023-01-01", "img.png");
        Inventory item2 = new Inventory("E2", "Elec Part", "BY", 500, 10, "Electrical", "2023-01-01", "img.png");
        ArrayList<CartItem> cart = new ArrayList<>();
        cart.add(new CartItem(item1, 3));
        cart.add(new CartItem(item2, 2));

        logger.logCheckout(cart, 1234.56);

        String line = lastLogLine();
        assertTrue(line.contains("CHECKOUT"));
        assertTrue(line.contains("Total: Rs.1234.56"));
        assertTrue(line.contains("E1(x3)"));
        assertTrue(line.contains("E2(x2)"));
    }

    @Test
    void multipleLogCalls_appendRatherThanOverwrite() throws Exception {
        logger.logDeleteItem("A1");
        logger.logDeleteItem("A2");

        List<String> lines = Files.readAllLines(new File(AuditLogger.LOG_FILE).toPath());
        assertEquals(2, lines.size(), "each log call should append a new line, not overwrite the file");
        assertTrue(lines.get(0).contains("Code: A1"));
        assertTrue(lines.get(1).contains("Code: A2"));
    }
}