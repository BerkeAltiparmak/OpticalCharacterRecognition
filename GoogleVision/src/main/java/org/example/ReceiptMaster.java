package org.example;


import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class ReceiptMaster {
    private final double threshold = 0.8;
    public String excelFilePath = "src/tables/Vergilers.xlsx";
    public ReceiptMaster(String text) {
        Map<String, String> associatedMap = new HashMap<>();
        associatedMap = getAssociatedCategories(text);
        try {
            writeIntoExcelFile(associatedMap);
        }
        catch (IOException ioe) {
            System.out.println("IOException detected");
        }
    }

    private void writeIntoExcelFile(Map<String, String> associatedMap) throws IOException {
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

    private Map<String, String> getAssociatedCategories(String text) {
        Map<String, String> associatedMap = new HashMap<String, String>();

        String tarih = "";
        String belgeNo = "";
        String belgeAdi = "";
        String vergiDairesi = "";
        String vergiNo = "";
        String tutar = "";
        String kdv = "";
        String toplam = "";

        List<String>[] altNames = getAlternativeNames();
        List<String> tarihList = altNames[0]; List<String> belgeNoList = altNames[1];
        List<String> kdvList = altNames[2]; List<String> toplamList = altNames[3];
        List<String> vergiList = altNames[4];

        String textLines[] = text.split("\\n");
        String prevLine = "";

        // get belgeAdi
        belgeAdi = textLines[0];
        if (belgeAdi.equals("")) {
            belgeAdi = textLines[1];
        }

        for (String tLine: textLines) {
            for (int i = 0; i <= tLine.length(); i++) {

                // get tarih
                if (i >= 5) {
                    if (tarihList.contains(tLine.substring(i - 5, i))) {
                        try {
                            tarih = tLine.substring(i - 10, i);
                        }
                        catch (Exception ignored) {}
                    }
                }

                // get belgeNo
                for (String belgeAlt: belgeNoList) {
                    if (i >= belgeAlt.length() + 1) { // accounting for the "space" by adding +1
                        if (belgeAlt.equals(tLine.substring(i - belgeAlt.length() - 1, i).toUpperCase()
                                .replaceAll("\\s", ""))) {
                            for (int j = i; j < tLine.length(); j++) {
                                if (isIntOrAccep(tLine.substring(j, j + 1))) {
                                    if (isInt(tLine.substring(j, j + 1))) {
                                        belgeNo += tLine.substring(j, j + 1);
                                    }
                                } else {
                                    if (!belgeNo.equals(""))
                                        break;
                                }
                            }
                        }
                    }
                }

                // get vergiDairesi and vergiNo
                for (String vergiAlt: vergiList) {
                    if (i >= vergiAlt.length()) {
                        if (findStringSimilarity(vergiAlt, tLine.substring(i - vergiAlt.length(), i)) >= threshold) {
                            for (int j = i; j < tLine.length(); j++) {
                                if (isIntOrAccep(tLine.substring(j, j + 1))) {
                                    if (isInt(tLine.substring(j, j + 1))) {
                                        // get vergiNo, but it might not exist here
                                        vergiNo += tLine.substring(j, j + 1);
                                    }
                                } else {
                                    break;
                                }
                            }
                            vergiDairesi = tLine.substring(0, i - vergiAlt.length());

                            if (vergiNo.equals("")) {
                                vergiNo = prevLine;
                            }
                        }
                    }
                }

                // get kdv
                for (String kdvAlt: kdvList) {
                    if (i >= kdvAlt.length() && kdv.equals("")) {
                        if (kdvAlt.equals(tLine.substring(i - kdvAlt.length(), i).toUpperCase())) {
                            for (int j = i; j < tLine.length(); j++) {
                                if (isIntOrAccep(tLine.substring(j, j + 1))) {
                                    if (isInt(tLine.substring(j, j + 1))) {
                                        kdv += tLine.substring(j, j + 1);
                                    }

                                    if (tLine.substring(j, j + 1).equals(",") || tLine.substring(j, j + 1).equals(".")) {
                                        kdv += '.';
                                    }
                                }
                                else {
                                    if (!kdv.equals(""))
                                        break;
                                }
                            }
                        }
                    }
                }

                // get toplam
                // problem: there may be 'ara toplam' issues
                for (String toplamAlt: toplamList) {
                    if (i >= toplamAlt.length() && toplam.equals("")) {
                        if (toplamAlt.equals(tLine.substring(i - toplamAlt.length(), i).toUpperCase())) {
                            for (int j = i; j < tLine.length(); j++) {
                                if (isIntOrAccep(tLine.substring(j, j + 1))) {
                                    if (isInt(tLine.substring(j, j + 1))) {
                                        toplam += tLine.substring(j, j + 1);
                                    }

                                    if (tLine.substring(j, j + 1).equals(",") || tLine.substring(j, j + 1).equals(".")) {
                                        toplam += '.';
                                    }
                                } else {
                                    if (!toplam.equals(""))
                                        break;
                                }
                            }
                        }
                    }
                }


            }
            prevLine = tLine;
        }

        if (StringUtils.isNotEmpty(tarih)) {
            tarih = tarih.replace(".", "/");
            tarih = tarih.replace("-", "/");
        }
        if (StringUtils.isNotEmpty(kdv)) {
            kdv = kdv.replace(".", "");
            kdv = kdv.substring(0, kdv.length() - 2) + "." + kdv.substring(kdv.length() - 2);
        }
        if (StringUtils.isNotEmpty(toplam)) {
            toplam = toplam.replace(".", "");
            toplam = toplam.substring(0, toplam.length() - 2) + "." + toplam.substring(toplam.length() - 2);
        }
        if (StringUtils.isNotEmpty(kdv) && StringUtils.isNotEmpty(toplam)) {
            tutar = String.valueOf(Integer.parseInt(toplam.replace(".", "")) -
                    Integer.parseInt(kdv.replace(".", "")));
            tutar = tutar.substring(0, tutar.length() - 2) + "." + tutar.substring(tutar.length() - 2);
        }

        System.out.println("Tarih: " + tarih);
        System.out.println("Belge Adi: " + belgeAdi);
        System.out.println("Belge No: " + belgeNo);
        System.out.println("Vergi Dairesi: " + vergiDairesi);
        System.out.println("Vergi No: " + vergiNo);
        System.out.println("KDV: " + kdv);
        System.out.println("Tutar: " + tutar);
        System.out.println("Toplam: " + toplam);

        associatedMap.put("Tarih", tarih);
        associatedMap.put("Belge Adi", belgeAdi);
        associatedMap.put("Belge No", belgeNo);
        associatedMap.put("Vergi Dairesi", vergiDairesi);
        associatedMap.put("Vergi No", vergiNo);
        associatedMap.put("KDV", kdv);
        associatedMap.put("Tutar", tutar);
        associatedMap.put("Toplam", toplam);

        return associatedMap;
    }

    private List<String>[] getAlternativeNames() {
        List<String>[] alternatives = new ArrayList[5];

        List<String> tarihList = new ArrayList<>();
        List<String> belgeNoList = new ArrayList<>();
        List<String> kdvList = new ArrayList<>();
        List<String> toplamList = new ArrayList<>();
        List<String> vergiList = new ArrayList<>();

        // other things might include these numbers as well (cellphone numbers, vergiNo, etc.)
        tarihList.add("/2019");tarihList.add("/2020");tarihList.add("/2021");tarihList.add("/2022");tarihList.add("/2023");
        tarihList.add(".2019");tarihList.add(".2020");tarihList.add(".2021");tarihList.add(".2022");tarihList.add(".2023");
        tarihList.add("-2019");tarihList.add("-2020");tarihList.add("-2021");tarihList.add("-2022");tarihList.add("-2023");

        belgeNoList.add("FİŞNO");belgeNoList.add("FIŞNO");belgeNoList.add("FISNO");belgeNoList.add("FİSNO");
        belgeNoList.add("SATIŞNO");belgeNoList.add("SATİŞNO");belgeNoList.add("SATİSNO");belgeNoList.add("SATISNO");

        kdvList.add("TOPKDV");kdvList.add("TOPLAM KDV");

        toplamList.add("TOPLAM");toplamList.add("TPLM");

        vergiList.add(" VD");vergiList.add("V.D");vergiList.add("Vergi Dairesi");

        alternatives[0] = tarihList;
        alternatives[1] = belgeNoList;
        alternatives[2] = kdvList;
        alternatives[3] = toplamList;
        alternatives[4] = vergiList;

        return alternatives;
    }

    private boolean isIntOrAccep(String st) {
        Set<String> chs = Set.of(",", ".", " ", ":", "*", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        for (int i = 0; i < st.length(); i++) {
            if(!chs.contains(st.substring(i, i+1))) {
                return false;
            }
        }
        return true;
    }

    private boolean isInt(String st) {
        Set<String> chs = Set.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        for (int i = 0; i < st.length(); i++) {
            if(!chs.contains(st.substring(i, i+1))) {
                return false;
            }
        }
        return true;
    }

    //return similarity between 0 and 1
    public static double findStringSimilarity(String x, String y) {

        double maxLength = Double.max(x.length(), y.length());
        if (maxLength > 0) {
            // not case sensitive
            return (maxLength - StringUtils.getLevenshteinDistance(x.toUpperCase(), y.toUpperCase())) / maxLength;
        }
        return 1.0;
    }
}
