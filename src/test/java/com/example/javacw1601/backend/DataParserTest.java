package com.example.javacw1601.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class DataParserTest {

    @TempDir
    Path tempDir;

    private Path writeFile(String fileName, String... lines) throws IOException {
        Path file = tempDir.resolve(fileName);
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(file))) {
            for (String line : lines) {
                pw.println(line);
            }
        }
        return file;
    }



    @Test
    void pipeDelimitedRow_withCommaInsideDate_isParsedAsEightFieldsNotSplitOnDateComma() throws IOException {
        Path file = writeFile("pipe.txt",
                "P001|Brake Pad|Bosch|Rs.2500|10|Engine Parts|Jan 5, 2024|brake.png");

        ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(file.toString());

        assertEquals(1, items.size());
        Inventory item = items.get(0);
        assertEquals("P001", item.getCode());
        assertEquals("Brake Pad", item.getName());
        assertEquals("Bosch", item.getBrand());
        assertEquals(2500.0, item.getPrice(), 0.0001);
        assertEquals(10, item.getQuantity());
        assertEquals("Engine Parts", item.getCategory());
        assertEquals("2024-01-05", item.getDate());
        assertEquals("brake.png", item.getImage());
    }

    @Test
    void commaDelimitedRow_withExtraWhitespaceAndLkrPrefix_isTrimmedAndCleaned() throws IOException {
        Path file = writeFile("comma.txt",
                "P002 , Air Filter , Toyota , LKR 1500.00 , 25 , engine , 12/03/2023 , filter.jpg");

        ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(file.toString());

        assertEquals(1, items.size());
        Inventory item = items.get(0);
        assertEquals("Air Filter", item.getName());
        assertEquals("Toyota", item.getBrand());
        assertEquals(1500.00, item.getPrice(), 0.0001);
        assertEquals(25, item.getQuantity());
        assertEquals("Engine", item.getCategory()); // normalized capitalization
        assertEquals("2023-03-12", item.getDate()); // dd/MM/yyyy -> ISO
    }

    @Test
    void semicolonDelimitedRow_withMissingOptionalFields_appliesDefaults() throws IOException {
        Path file = writeFile("semi.txt",
                "P003;Spark Plug;NGK;850; 8 ;electrical;;");

        ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(file.toString());

        assertEquals(1, items.size());
        Inventory item = items.get(0);
        assertEquals(850.0, item.getPrice(), 0.0001);
        assertEquals(8, item.getQuantity());
        assertEquals("Electrical", item.getCategory());
        assertEquals("Unknown", item.getDate());
        assertEquals("no_image.png", item.getImage());
    }

    @Test
    void blankLines_areIgnoredCompletely() throws IOException {
        Path file = writeFile("blank.txt",
                "P010,Item Ten,Brand,100,5,misc,2023-01-01,img.png",
                "",
                " ",
                "P011,Item Eleven,Brand,200,3,misc,2023-01-01,img.png");

        ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(file.toString());

        assertEquals(2, items.size());
    }

    @Test
    void rowWithThousandsSeparatorInPrice_getsSplitIncorrectlyAndIsSkipped() throws IOException {

        Path file = writeFile("thousands.txt",
                "P004,Battery,Exide,Rs. 12,500.00,4,electrical,2023-11-01,batt.png",
                "P005,Good Row,Brand,Rs.100,5,misc,2023-01-01,img.png");

        ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(file.toString());

        assertEquals(1, items.size(), "corrupted row should be skipped, good row kept");
        assertEquals("P005", items.get(0).getCode());
    }

    @Test
    void rowWithNonNumericPrice_isSkipped() throws IOException {
        Path file = writeFile("badprice.txt",
                "P006,Bad Row,Unknown,notaprice,5,misc,2023-01-01,bad.png",
                "P007,Good Row,Brand,Rs.100,5,misc,2023-01-01,img.png");

        ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(file.toString());

        assertEquals(1, items.size());
        assertEquals("P007", items.get(0).getCode());
    }

    @Test
    void rowWithNonNumericQuantity_isSkipped() throws IOException {
        Path file = writeFile("badqty.txt",
                "P008,Bad Qty,Unknown,Rs.100,abc,misc,2023-01-01,bad.png",
                "P009,Good Row,Brand,Rs.100,5,misc,2023-01-01,img.png");

        ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(file.toString());

        assertEquals(1, items.size());
        assertEquals("P009", items.get(0).getCode());
    }

    @Test
    void loadInventoryData_behavesTheSameAsLoadItemsList() throws IOException {
        Path file = writeFile("parity.txt",
                "P020,Item,Brand,Rs.100,5,misc,2023-01-01,img.png");

        ArrayList<Inventory> viaItemsList = LegacyDataHandler.loadItemsList(file.toString());
        ArrayList<Inventory> viaInventoryData = LegacyDataHandler.loadInventoryData(file.toString());

        assertEquals(1, viaItemsList.size());
        assertEquals(1, viaInventoryData.size());
        assertEquals(viaItemsList.get(0).getCode(), viaInventoryData.get(0).getCode());
        assertEquals(viaItemsList.get(0).getPrice(), viaInventoryData.get(0).getPrice(), 0.0001);
    }

    @Test
    void multipleDateFormats_areAllNormalizedToIso() throws IOException {
        Path file = writeFile("dates.txt",
                "D1,Item,Brand,100,5,misc,2023-06-15,img.png",
                "D2,Item,Brand,100,5,misc,15-Mar-2023,img.png",
                "D3,Item,Brand,100,5,misc,Mar 15, 2023,img.png"
        );

        ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(file.toString());

        assertEquals(3, items.size());
        assertEquals("2023-06-15", items.get(0).getDate());
        assertEquals("2023-03-15", items.get(1).getDate());
        assertEquals("2023-03-15", items.get(2).getDate());
    }

    @Test
    void unparseableDate_isKeptAsRawStringRatherThanUnknown() throws IOException {
        Path file = writeFile("baddate.txt",
                "X1,Item One,Brand,100,5,cat,not-a-date,img.png");

        ArrayList<Inventory> items = LegacyDataHandler.loadItemsList(file.toString());

        assertEquals(1, items.size());

        assertEquals("not-a-date", items.get(0).getDate());
    }

    @Test
    void normalizeCategory_capitalizesWordsAndPreservesSeparators() {
        assertEquals("Engine Parts", LegacyDataHandler.normalizeCategory("engine parts"));
        assertEquals("Body-Trim", LegacyDataHandler.normalizeCategory("BODY-TRIM"));
        assertEquals("Air/Fuel System", LegacyDataHandler.normalizeCategory("air/fuel system"));
        assertEquals("Brakes & Suspension", LegacyDataHandler.normalizeCategory("brakes & suspension"));
    }

    @Test
    void normalizeCategory_handlesNullEmptyAndWhitespace() {
        assertEquals("", LegacyDataHandler.normalizeCategory(""));
        assertEquals("", LegacyDataHandler.normalizeCategory(null));
        assertEquals("", LegacyDataHandler.normalizeCategory(" "));
    }

    @Test
    void loadDealersList_parsesMixedDelimitersAndDefaultsMissingPhone() throws IOException {
        Path file = writeFile("dealers.txt",
                "D001, Auto World, , Colombo",
                "D002 | Speed Parts | 0771234567 | Kandy",
                "D003;Moto Hub;0119876543;Galle");

        ArrayList<Dealers> dealers = LegacyDataHandler.loadDealersList(file.toString());

        assertEquals(3, dealers.size());

        Dealers d1 = dealers.get(0);
        assertEquals("D001", d1.getCode());
        assertEquals("Auto World", d1.getName());
        assertEquals("Unknown", d1.getPhone());
        assertEquals("Colombo", d1.getLocation());

        Dealers d2 = dealers.get(1);
        assertEquals("0771234567", d2.getPhone());
        assertEquals("Kandy", d2.getLocation());

        Dealers d3 = dealers.get(2);
        assertEquals("Galle", d3.getLocation());
    }



    @Test
    void saveInventory_thenReload_roundTripsCoreFields() throws IOException {
        Path file = tempDir.resolve("roundtrip.txt");
        ArrayList<Inventory> toSave = new ArrayList<>();
        toSave.add(new Inventory("R1", "Round Trip Item", "Brand", 123.45, 7, "Misc", "2023-01-01", "img.png"));
        LegacyDataHandler.saveInventory(file.toString(), toSave);

        List<String> savedLines = Files.readAllLines(file);
        assertEquals(1, savedLines.size());

        ArrayList<Inventory> reloaded = LegacyDataHandler.loadItemsList(file.toString());
        assertEquals(1, reloaded.size());
        assertEquals("R1", reloaded.get(0).getCode());
        assertEquals(123.45, reloaded.get(0).getPrice(), 0.0001);
        assertEquals(7, reloaded.get(0).getQuantity());
    }
}