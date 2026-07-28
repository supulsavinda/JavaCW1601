package com.example.javacw1601.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InventoryManagerTest {

    private InventoryManager manager;

    private static Inventory inv(String code, String name, String brand, double price, int qty, String category) {
        return new Inventory(code, name, brand, price, qty, category, "2023-01-01", "img.png");
    }

    @BeforeEach
    void setUp() {
        manager = new InventoryManager();
        manager.addItem(inv("A1", "Widget", "BrandX", 100, 10, "Engine"));
        manager.addItem(inv("B2", "Gadget", "BrandY", 200, 2, "Electrical"));
        manager.addItem(inv("C3", "Thing", "BrandZ", 50, 5, "engine"));   // lowercase category on purpose
        manager.addItem(inv("D4", "Doohickey", "BrandX", 10, 4, "Body & Trim"));
    }

    // ---------------------------------------------------------------
    // CRUD + invalid input handling
    // ---------------------------------------------------------------

    @Test
    void addItem_rejectsDuplicateCodeCaseInsensitively() {
        assertFalse(manager.addItem(inv("a1", "Other", "BrandY", 50, 5, "Misc")));
        assertEquals(4, manager.getTotalCount());
    }

    @Test
    void addItem_acceptsNewUniqueCode() {
        assertTrue(manager.addItem(inv("E5", "New Item", "BrandQ", 30, 1, "Misc")));
        assertEquals(5, manager.getTotalCount());
    }

    @Test
    void findItemByCode_isCaseInsensitiveAndReturnsNullWhenMissing() {
        assertNotNull(manager.findItemByCode("a1"));
        assertNotNull(manager.findItemByCode("A1"));
        assertNull(manager.findItemByCode("ZZZ"));
    }

    @Test
    void updateItem_onNonexistentCode_returnsFalseAndLeavesInventoryUnchanged() {
        assertFalse(manager.updateItem("ZZZ", inv("ZZZ", "x", "y", 1, 1, "misc")));
        assertEquals(4, manager.getTotalCount());
    }

    @Test
    void updateItem_onExistingCode_replacesItemButKeepsOriginalCode() {
        boolean updated = manager.updateItem("A1", inv("IGNORED", "Widget v2", "BrandX", 150, 20, "Engine"));
        assertTrue(updated);
        Inventory result = manager.findItemByCode("A1");
        assertNotNull(result);
        assertEquals("Widget v2", result.getName());
        assertEquals(150, result.getPrice(), 0.0001);
        assertEquals("A1", result.getCode()); // code forced back to the lookup key
    }

    @Test
    void deleteItem_onNonexistentCode_returnsFalse() {
        assertFalse(manager.deleteItem("ZZZ"));
        assertEquals(4, manager.getTotalCount());
    }

    @Test
    void deleteItem_onExistingCode_removesItAndReturnsTrue() {
        assertTrue(manager.deleteItem("D4"));
        assertEquals(3, manager.getTotalCount());
        assertNull(manager.findItemByCode("D4"));
    }

    @Test
    void getAllItems_returnsDefensiveCopy() {
        ArrayList<Inventory> snapshot = manager.getAllItems();
        snapshot.clear();
        assertEquals(4, manager.getTotalCount(), "clearing the returned list must not affect internal state");
    }

    @Test
    void getTotalValue_sumsPriceTimesQuantityAcrossAllItems() {
        assertEquals(1690.0, manager.getTotalValue(), 0.0001);
    }

    @Test
    void getCategories_returnsNormalizedDistinctCategories() {
        ArrayList<String> categories = manager.getCategories();
        assertEquals(3, categories.size());
        assertTrue(categories.contains("Engine"));
        assertTrue(categories.contains("Electrical"));
        assertTrue(categories.contains("Body & Trim"));
    }


    @Test
    void getLowStockItems_usesDefaultThresholdOfFive_strictlyLessThan() {
        // qty < 5: B2(2), D4(4). C3 has qty == 5, which is NOT below threshold.
        ArrayList<Inventory> lowStock = manager.getLowStockItems();
        assertEquals(2, lowStock.size());
        assertTrue(lowStock.stream().anyMatch(i -> i.getCode().equals("B2")));
        assertTrue(lowStock.stream().anyMatch(i -> i.getCode().equals("D4")));
        assertFalse(lowStock.stream().anyMatch(i -> i.getCode().equals("C3")));
    }

    @Test
    void getLowStockItems_withCustomThreshold_respectsBoundary() {
        ArrayList<Inventory> lowStock = manager.getLowStockItems(3);
        assertEquals(1, lowStock.size());
        assertEquals("B2", lowStock.get(0).getCode());
    }

    @Test
    void setLowStockThreshold_rejectsNegativeValues() {
        int original = manager.getLowStockThreshold();
        manager.setLowStockThreshold(-1);
        assertEquals(original, manager.getLowStockThreshold(), "negative threshold must be ignored");
    }

    @Test
    void setLowStockThreshold_acceptsZeroAndPositiveValues() {
        manager.setLowStockThreshold(0);
        assertEquals(0, manager.getLowStockThreshold());
        manager.setLowStockThreshold(3);
        assertEquals(3, manager.getLowStockThreshold());
    }


    @Test
    void getSortedItems_groupsByCategoryAlphabeticallyThenByCodeWithinCategory() {
        ArrayList<Inventory> sorted = manager.getSortedItems();
        assertEquals("Body & Trim", sorted.get(0).getCategory());
        assertEquals("Electrical", sorted.get(1).getCategory());
        assertEquals("Engine", sorted.get(2).getCategory());
        assertEquals("Engine", sorted.get(3).getCategory());
        assertEquals("A1", sorted.get(2).getCode());
        assertEquals("C3", sorted.get(3).getCode());
    }

    @Test
    void groupByCategoryThenCode_treatsDifferentlyCasedCategoriesAsTheSameGroup() {
        assertEquals("Engine", manager.findItemByCode("C3").getCategory());
    }

    @Test
    void search_filtersByCategoryCaseInsensitively() {
        ArrayList<Inventory> results = manager.search("engine", 0, Double.MAX_VALUE, null);
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(i -> i.getCode().equals("A1")));
        assertTrue(results.stream().anyMatch(i -> i.getCode().equals("C3")));
    }

    @Test
    void search_withCategoryAll_isTreatedAsNoCategoryFilter() {
        ArrayList<Inventory> results = manager.search("All", 0, Double.MAX_VALUE, null);
        assertEquals(4, results.size());
    }

    @Test
    void search_filtersByKeywordAcrossNameAndBrand() {
        ArrayList<Inventory> byName = manager.search(null, 0, Double.MAX_VALUE, "gadget");
        assertEquals(1, byName.size());
        assertEquals("B2", byName.get(0).getCode());

        ArrayList<Inventory> byBrand = manager.search(null, 0, Double.MAX_VALUE, "brandx");
        assertEquals(2, byBrand.size());
    }

    @Test
    void search_filtersByPriceRangeInclusive() {
        ArrayList<Inventory> results = manager.search(null, 40, 60, null);
        assertEquals(1, results.size());
        assertEquals("C3", results.get(0).getCode());
    }

    @Test
    void search_withMinPriceGreaterThanMaxPrice_skipsThePriceFilterEntirely() {
        ArrayList<Inventory> results = manager.search(null, 100, 10, null);
        assertEquals(4, results.size());
    }

    @Test
    void search_withNoMatches_returnsEmptyListNotNull() {
        ArrayList<Inventory> results = manager.search(null, 0, Double.MAX_VALUE, "doesnotexist");
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void search_combinesCategoryPriceAndKeywordFilters() {
        ArrayList<Inventory> results = manager.search("Engine", 0, 60, "thing");
        assertEquals(1, results.size());
        assertEquals("C3", results.get(0).getCode());
    }
}