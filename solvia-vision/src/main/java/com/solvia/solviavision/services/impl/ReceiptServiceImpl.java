package com.solvia.solviavision.services.impl;

import com.solvia.solviavision.services.ReceiptService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Service
public class ReceiptServiceImpl implements ReceiptService {

    private static final double SIMILARITY_THRESHOLD = 0.8;

    public Map<String, String> getImportantReceiptInfo(String text, String csvFilePath) {
        Map<String, String> importantInfoMap = new HashMap<String, String>();

        String tarih = "";
        String belgeNo = "";
        String belgeAdi = "";
        String vergiDairesi = "";
        String vergiNo = "";
        String tutar = "";
        String kdv = "";
        String toplam = "";

        /*
        List<String>[] altNames = getAlternativeNames();
        List<String> tarihList = altNames[0]; List<String> belgeNoList = altNames[1];
        List<String> kdvList = altNames[2]; List<String> toplamList = altNames[3];
        List<String> vergiList = altNames[4];
         */
        List<List<String>> alternativeNames = getAlternativeNamesFromCSV(csvFilePath);

        List<String> tarihList = alternativeNames.get(0);
        List<String> belgeNoList = alternativeNames.get(1);
        List<String> kdvList = alternativeNames.get(2);
        List<String> toplamList = alternativeNames.get(3);
        List<String> vergiList = alternativeNames.get(4);

        String[] textLines = text.split("\\n");
        String prevLine = "";

        // get belgeAdi
        belgeAdi = textLines[0];
        if (belgeAdi.equals("")) {
            try {
                belgeAdi = textLines[1];
            }
            catch (ArrayIndexOutOfBoundsException aioobe) {
                System.out.println("Encountered empty text");
            }
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
                        if (findStringSimilarity(vergiAlt, tLine.substring(i - vergiAlt.length(), i)) >= SIMILARITY_THRESHOLD) {
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

        importantInfoMap.put("Tarih", tarih);
        importantInfoMap.put("Belge Adi", belgeAdi);
        importantInfoMap.put("Belge No", belgeNo);
        importantInfoMap.put("Vergi Dairesi", vergiDairesi);
        importantInfoMap.put("Vergi No", vergiNo);
        importantInfoMap.put("KDV", kdv);
        importantInfoMap.put("Tutar", tutar);
        importantInfoMap.put("Toplam", toplam);

        return importantInfoMap;
    }

    private List<List<String>> getAlternativeNamesFromCSV(String csvFilePath) {
        List<List<String>> alternativeNames = new ArrayList<>();

        List<String> tarihList = new ArrayList<>();
        List<String> belgeNoList = new ArrayList<>();
        List<String> kdvList = new ArrayList<>();
        List<String> toplamList = new ArrayList<>();
        List<String> vergiList = new ArrayList<>();

        String line = "";
        try {
            //parsing a CSV file into BufferedReader class constructor
            BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
            while ((line = br.readLine()) != null)
            {
                //use comma as separator
                List<String> csvLine = Arrays.asList(line.split(","));
                if ("*TARIH*".equals(csvLine.get(0))) {tarihList.addAll(csvLine);}
                if ("*BELGENO*".equals(csvLine.get(0))) {belgeNoList.addAll(csvLine);}
                if ("*KDV*".equals(csvLine.get(0))) {kdvList.addAll(csvLine);}
                if ("*TOPLAM*".equals(csvLine.get(0))) {toplamList.addAll(csvLine);}
                if ("*VERGI*".equals(csvLine.get(0))) {vergiList.addAll(csvLine);}
            }
        }
        catch(IOException ioe) {
            System.out.println("CSV file not found");
        }

        alternativeNames.add(tarihList);
        alternativeNames.add(belgeNoList);
        alternativeNames.add(kdvList);
        alternativeNames.add(toplamList);
        alternativeNames.add(vergiList);

        return alternativeNames;
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
