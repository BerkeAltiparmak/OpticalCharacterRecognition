package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class ReceiptMaster {
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
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Vergi Verileri");

        Object[][] vergiData = new Object[2][8];
        String[] infos = {"Tarih", "Belge Adi", "Belge No", "Vergi Dairesi", "Vergi No", "KDV", "Tutar", "Toplam"};

        vergiData[0][0] = infos[0];
        vergiData[0][1] = infos[1];
        vergiData[0][2] = infos[2];
        vergiData[0][3] = infos[3];
        vergiData[0][4] = infos[4];
        vergiData[0][5] = infos[5];
        vergiData[0][6] = infos[6];
        vergiData[0][7] = infos[7];

        for (int i = 0; i < 8; i ++) {
            vergiData[1][i] = associatedMap.get(infos[i]);
        }



        int rowCount = 0;

        for (Object[] aBook : vergiData) {
            Row row = sheet.createRow(++rowCount);

            int columnCount = 0;

            for (Object field : aBook) {
                Cell cell = row.createCell(++columnCount);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
            }

        }


        try (FileOutputStream outputStream = new FileOutputStream("src/tables/Vergiler.xlsx")) {
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
                if (i >= 4) {
                    if (tarihList.contains(tLine.substring(i - 4, i))) {
                        try {
                            tarih = tLine.substring(i - 10, i);
                        }
                        catch (Exception ignored) {}
                    }
                }

                // get belgeNo
                if (i >= 6) {
                    if (belgeNoList.contains(tLine.substring(i - 6, i).toUpperCase()
                            .replaceAll("\\s",""))) {
                        for (int j = i; j < tLine.length(); j++) {
                            if (isIntOrAccep(tLine.substring(j, j + 1))) {
                                if (isInt(tLine.substring(j, j + 1))) {
                                    belgeNo += tLine.substring(j, j + 1);
                                }
                            }
                            else {
                                break;
                            }
                        }
                    }
                }

                // get vergiDairesi and vergiNo
                if (i >= 3) {
                    if (vergiList.contains(tLine.substring(i - 3, i))) {
                        for (int j = i; j < tLine.length(); j++) {
                            if (isIntOrAccep(tLine.substring(j, j + 1))) {
                                if (isInt(tLine.substring(j, j + 1))) {
                                    // get vergiNo, but it might not exist here
                                    vergiNo += tLine.substring(j, j + 1);
                                }
                            }
                            else {
                                break;
                            }
                        }
                        vergiDairesi = tLine.substring(0, i - 3);

                        if (vergiNo.equals("")) {
                            vergiNo = prevLine;
                        }
                    }
                }

                // get kdv
                if (i >= 6 && kdv.equals("")) {
                    if ("TOPKDV".equals(tLine.substring(i - 6, i).toUpperCase())) {
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
                                break;
                            }
                        }
                    }
                }

                // get toplam
                // problem: there may be 'ara toplam' issues
                if (i >= 6 && toplam.equals("")) {
                    if (toplamList.contains(tLine.substring(i - 6, i).toUpperCase())) {
                        for (int j = i; j < tLine.length(); j++) {
                            if (isIntOrAccep(tLine.substring(j, j + 1))) {
                                if (isInt(tLine.substring(j, j + 1))) {
                                    toplam += tLine.substring(j, j + 1);
                                }

                                if (tLine.substring(j, j + 1).equals(",") || tLine.substring(j, j + 1).equals(".")) {
                                    toplam += '.';
                                }
                            }
                            else {
                                break;
                            }
                        }
                    }
                }


            }
            prevLine = tLine;
        }
        kdv = kdv.replace(".", "");
        kdv = kdv.substring(0, kdv.length() - 2) + "." + kdv.substring(kdv.length() - 2);
        toplam = toplam.replace(".", "");
        toplam = toplam.substring(0, toplam.length() - 2) + "." + toplam.substring(toplam.length() - 2);
        tutar = String.valueOf(Integer.parseInt(toplam.replace(".", "")) -
                Integer.parseInt(kdv.replace(".", "")));
        tutar = tutar.substring(0, tutar.length() - 2) + "." + tutar.substring(tutar.length() - 2);

        tarih = tarih.replace(".", "/");
        tarih = tarih.replace("-", "/");

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
        tarihList.add("2019");tarihList.add("2020");tarihList.add("2021");tarihList.add("2022");tarihList.add("2023");

        belgeNoList.add("FİŞNO");belgeNoList.add("FIŞNO");belgeNoList.add("FISNO");belgeNoList.add("FİSNO");

        kdvList.add("TOPKDV");kdvList.add("KDV");

        toplamList.add("TOPLAM");toplamList.add("TPLM");toplamList.add("TOP");

        vergiList.add(" VD");vergiList.add("V.D");

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
}