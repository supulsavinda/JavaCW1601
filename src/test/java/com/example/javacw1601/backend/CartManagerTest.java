package com.example.javacw1601.backend;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartManagerTest {

    private InventoryManager inventoryManager;
    private CartManager cartManager;

    private static Inventory inv(String code, String name, String brand, double price, int qty, String category) {
        return new Inventory(code, name, brand, price, qty, category, "2023-01-01", "img.png");
    }

    @BeforeEach
    void setUp() {
        inventoryManager = new InventoryManager();
        inventoryManager.addItem(inv("E1", "Engine Part", "BX", 1000, 10, "Engine"));
        inventoryManager.addItem(inv("E2", "Elec Part", "BY", 500, 10, "Electrical"));
        inventoryManager.addItem(inv("E3", "Misc Part", "BZ", 100, 2, "Misc"));
        cartManager = new CartManager(inventoryManager);

        deleteAuditLogIfPresent();
    }

    @AfterEach
    void tearDown() {
        deleteAuditLogIfPresent();
    }

    private void deleteAuditLogIfPresent() {
        File logFile = new File(AuditLogger.LOG_FILE);
        if (logFile.exists()) {
            logFile.delete();
        }
    }


    @Test
    void addToCart_rejectsZeroOrNegativeQuantity() {
        assertFalse(cartManager.addToCart("E1", 0));
        assertFalse(cartManager.addToCart("E1", -5));
        assertTrue(cartManager.getCartItems().isEmpty());
    }

    @Test
    void addToCart_rejectsUnknownItemCode() {
        assertFalse(cartManager.addToCart("ZZZ", 1));
    }

    @Test
    void addToCart_rejectsQuantityExceedingAvailableStock() {
        assertFalse(cartManager.addToCart("E3", 5)); // only 2 in stock
        assertTrue(cartManager.getCartItems().isEmpty());
    }

    @Test
    void addToCart_addsNewLineForValidRequest() {
        assertTrue(cartManager.addToCart("E1", 2));
        assertEquals(1, cartManager.getCartItems().size());
        assertEquals(2, cartManager.getCartItems().get(0).getQuantity());
    }

    @Test
    void addToCart_aggregatesQuantityWhenItemAlreadyInCart() {
        cartManager.addToCart("E1", 2);
        assertTrue(cartManager.addToCart("E1", 3));
        assertEquals(1, cartManager.getCartItems().size(), "should update existing line, not add a duplicate");
        assertEquals(5, cartManager.getCartItems().get(0).getQuantity());
    }

    @Test
    void addToCart_rejectsAggregateQuantityExceedingStockWithoutModifyingExistingLine() {
        cartManager.addToCart("E1", 3); // stock is 10
        assertFalse(cartManager.addToCart("E1", 8)); // 3 + 8 = 11 > 10
        assertEquals(3, cartManager.getCartItems().get(0).getQuantity(), "failed add must not change existing quantity");
    }


    @Test
    void removeFromCart_onItemNotInCart_returnsFalse() {
        assertFalse(cartManager.removeFromCart("E1"));
    }

    @Test
    void removeFromCart_onItemInCart_removesItAndReturnsTrue() {
        cartManager.addToCart("E1", 2);
        assertTrue(cartManager.removeFromCart("E1"));
        assertTrue(cartManager.getCartItems().isEmpty());
    }

    @Test
    void updateQuantity_onItemNotInCart_returnsFalse() {
        assertFalse(cartManager.updateQuantity("E1", 5));
    }

    @Test
    void updateQuantity_toZeroOrBelow_removesTheLine() {
        cartManager.addToCart("E1", 2);
        assertTrue(cartManager.updateQuantity("E1", 0));
        assertTrue(cartManager.getCartItems().isEmpty());
    }

    @Test
    void updateQuantity_exceedingStock_isRejected() {
        cartManager.addToCart("E1", 2);
        assertFalse(cartManager.updateQuantity("E1", 999));
        assertEquals(2, cartManager.getCartItems().get(0).getQuantity());
    }

    @Test
    void updateQuantity_withValidAmount_updatesLine() {
        cartManager.addToCart("E1", 2);
        assertTrue(cartManager.updateQuantity("E1", 6));
        assertEquals(6, cartManager.getCartItems().get(0).getQuantity());
    }

    @Test
    void calculateTotal_onEmptyCart_isZero() {
        assertEquals(0.0, cartManager.calculateTotal(), 0.0001);
    }

    @Test
    void calculateTotal_belowBulkThreshold_appliesNoDiscount() {
        cartManager.addToCart("E1", 2);
        assertEquals(2000.0, cartManager.calculateTotal(), 0.0001);
    }

    @Test
    void calculateTotal_atBulkThresholdOfThree_appliesFivePercentLineDiscount() {
        cartManager.addToCart("E1", 3);
        assertEquals(2850.0, cartManager.calculateTotal(), 0.0001);
    }

    @Test
    void calculateTotal_withEngineAndElectricalBoth_appliesAdditionalTenPercentComboDiscount() {
        cartManager.addToCart("E1", 3);
        cartManager.addToCart("E2", 3);
        assertEquals(3847.5, cartManager.calculateTotal(), 0.0001);
    }

    @Test
    void calculateTotal_withoutBothCategories_doesNotApplyComboDiscount() {
        cartManager.addToCart("E1", 1); // Engine only, qty < 3
        cartManager.addToCart("E3", 2); // Misc only, qty < 3 (stock allows up to 2)
        // 1000*1 + 100*2 = 1200, no line discount, no combo discount
        assertEquals(1200.0, cartManager.calculateTotal(), 0.0001);
    }

    // ---------------------------------------------------------------
    // checkout: stock deduction, cart clearing, audit logging
    // ---------------------------------------------------------------

    @Test
    void checkout_onEmptyCart_returnsZeroAndDoesNotThrow() {
        assertEquals(0.0, cartManager.checkout(), 0.0001);
    }

    @Test
    void checkout_deductsPurchasedQuantityFromInventoryStock() {
        cartManager.addToCart("E1", 3);
        cartManager.checkout();
        assertEquals(7, inventoryManager.findItemByCode("E1").getQuantity());
    }

    @Test
    void checkout_clearsTheCart() {
        cartManager.addToCart("E1", 2);
        cartManager.checkout();
        assertTrue(cartManager.getCartItems().isEmpty());
    }

    @Test
    void checkout_returnsSameTotalAsCalculateTotalBeforeCheckout() {
        cartManager.addToCart("E1", 3);
        cartManager.addToCart("E2", 3);
        double expected = cartManager.calculateTotal();

        double actual = cartManager.checkout();

        assertEquals(expected, actual, 0.0001);
    }

    @Test
    void checkout_writesCheckoutEntryToAuditLog() throws Exception {
        cartManager.addToCart("E1", 3);
        cartManager.checkout();

        File logFile = new File(AuditLogger.LOG_FILE);
        assertTrue(logFile.exists(), "checkout should write an audit log entry");

        List<String> lines = Files.readAllLines(logFile.toPath());
        assertFalse(lines.isEmpty());
        String lastLine = lines.get(lines.size() - 1);
        assertTrue(lastLine.contains("CHECKOUT"));
        assertTrue(lastLine.contains("E1(x3)"));
    }

    @Test
    void checkout_onEmptyCart_doesNotWriteAnAuditLogEntry() {
        cartManager.checkout();
        File logFile = new File(AuditLogger.LOG_FILE);
        assertFalse(logFile.exists(), "an empty checkout returns early and should not log anything");
    }
}