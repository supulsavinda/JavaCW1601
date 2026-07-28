package com.example.javacw1601.backend;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


class DealerManagerTest {

    private static Dealers dealer(String code, String name, String phone, String location) {
        return new Dealers(code, name, phone, location);
    }

    private ArrayList<Dealers> fourDealers() {
        ArrayList<Dealers> dealers = new ArrayList<>();
        dealers.add(dealer("D1", "Alpha", "011", "Kandy"));
        dealers.add(dealer("D2", "Beta", "012", "Colombo"));
        dealers.add(dealer("D3", "Gamma", "013", "Galle"));
        dealers.add(dealer("D4", "Delta", "014", "Anuradhapura"));
        return dealers;
    }

    @Test
    void getAllDealers_returnsDefensiveCopy() {
        DealerManager manager = new DealerManager(fourDealers());
        ArrayList<Dealers> snapshot = manager.getAllDealers();
        snapshot.clear();
        assertEquals(4, manager.getTotalCount(), "clearing the returned list must not affect internal state");
    }

    @Test
    void getTotalCount_reflectsNumberOfLoadedDealers() {
        DealerManager manager = new DealerManager(fourDealers());
        assertEquals(4, manager.getTotalCount());
    }


    @Test
    void select4Random_throwsWhenFewerThanFourDealersAvailable() {
        ArrayList<Dealers> onlyThree = new ArrayList<>();
        onlyThree.add(dealer("D1", "Alpha", "011", "Kandy"));
        onlyThree.add(dealer("D2", "Beta", "012", "Colombo"));
        onlyThree.add(dealer("D3", "Gamma", "013", "Galle"));
        DealerManager manager = new DealerManager(onlyThree);

        assertThrows(IllegalStateException.class, manager::select4Random);
    }

    @Test
    void select4Random_throwsOnEmptyDealerList() {
        DealerManager manager = new DealerManager();
        assertThrows(IllegalStateException.class, manager::select4Random);
    }


    @Test
    void select4Random_withExactlyFourDealers_returnsAllFourSortedByLocation() {
        DealerManager manager = new DealerManager(fourDealers());
        ArrayList<Dealers> selected = manager.select4Random();

        assertEquals(4, selected.size());
        assertEquals("Anuradhapura", selected.get(0).getLocation());
        assertEquals("Colombo", selected.get(1).getLocation());
        assertEquals("Galle", selected.get(2).getLocation());
        assertEquals("Kandy", selected.get(3).getLocation());
    }

    @Test
    void select4Random_returnsFourDistinctDealers() {
        ArrayList<Dealers> pool = fourDealers();
        pool.add(dealer("D5", "Epsilon", "015", "Jaffna"));
        pool.add(dealer("D6", "Zeta", "016", "Matara"));
        DealerManager manager = new DealerManager(pool);

        ArrayList<Dealers> selected = manager.select4Random();

        assertEquals(4, selected.size());
        Set<String> distinctCodes = new HashSet<>();
        for (Dealers d : selected) {
            distinctCodes.add(d.getCode());
        }
        assertEquals(4, distinctCodes.size(), "the 4 selected dealers must be distinct, no repeats");
    }

    @Test
    void select4Random_resultIsSortedByLocationEvenWithLargerPool() {
        ArrayList<Dealers> pool = fourDealers();
        pool.add(dealer("D5", "Epsilon", "015", "Badulla"));
        pool.add(dealer("D6", "Zeta", "016", "Matara"));
        DealerManager manager = new DealerManager(pool);

        ArrayList<Dealers> selected = manager.select4Random();

        for (int i = 0; i < selected.size() - 1; i++) {
            String current = selected.get(i).getLocation();
            String next = selected.get(i + 1).getLocation();
            assertTrue(current.compareToIgnoreCase(next) <= 0,
                    "expected \"" + current + "\" to sort before or equal to \"" + next + "\"");
        }
    }

    @Test
    void select4Random_calledManyTimes_eventuallyProducesVaryingSelections() {
        ArrayList<Dealers> pool = fourDealers();
        pool.add(dealer("D5", "Epsilon", "015", "Jaffna"));
        pool.add(dealer("D6", "Zeta", "016", "Matara"));
        pool.add(dealer("D7", "Eta", "017", "Negombo"));
        DealerManager manager = new DealerManager(pool);

        Set<String> seenCombinations = new HashSet<>();
        for (int i = 0; i < 30; i++) {
            ArrayList<Dealers> selected = manager.select4Random();
            StringBuilder key = new StringBuilder();
            for (Dealers d : selected) {
                key.append(d.getCode()).append(",");
            }
            seenCombinations.add(key.toString());
        }
        assertTrue(seenCombinations.size() > 1, "expected different selections across 30 runs with a 7-dealer pool");
    }
}