package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Tester {
    static String excelFilePath = "src/tables/Vergilers.xlsx";
    public static void main(String[] args) throws IOException {
        Map<String, String> associatedMap = new HashMap<>();
        associatedMap.put("Tarih", "12/15/2022");
        associatedMap.put("Belge Adi", "Testmigros");
        associatedMap.put("Belge No", "0001");
        associatedMap.put("Vergi Dairesi", "Altiparmak");
        associatedMap.put("Vergi No", "1092889712");
        associatedMap.put("KDV", "15.23");
        associatedMap.put("Tutar", "45.23");
        associatedMap.put("Toplam", "30.00");

        List<Object[]> vergiData = new ArrayList<>();
        Workbook workbook;
        Sheet sheet;
        String[] headers = {"Tarih", "Belge Adi", "Belge No", "Vergi Dairesi", "Vergi No", "KDV", "Tutar", "Toplam"};
        int rowCount = 0;
        try {
            FileInputStream inputStream = new FileInputStream(excelFilePath);

            //Creating workbook from input stream
            workbook = WorkbookFactory.create(inputStream);

            //Reading first sheet of excel file
            sheet = workbook.getSheetAt(0);

            //Getting the count of existing records
            rowCount = sheet.getLastRowNum();
        }
        catch (Exception e) {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Vergi Verileri");
            rowCount = 0;
            vergiData.add(headers);
        }

        Object[] mapToArray = new Object[8];
        for (int i = 0; i < 8; i ++) {
            mapToArray[i] = associatedMap.get(headers[i]);
        }
        vergiData.add(mapToArray);


        for (Object[] vergi : vergiData) {
            Row row = sheet.createRow(++rowCount);

            int columnCount = 0;

            for (Object field : vergi) {
                Cell cell = row.createCell(++columnCount);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
            }

        }


        try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
            workbook.write(outputStream);
        }
    }
}
