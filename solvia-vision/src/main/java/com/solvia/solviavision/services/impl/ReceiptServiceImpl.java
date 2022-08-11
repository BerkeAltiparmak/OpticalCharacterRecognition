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
        Map<String, String> importantInfoMap = new HashMap<>();

        // below are the list of pieces of information that we found useful to extract
        String tarih = "";
        String belgeNo = "";
        String belgeAdi = "";
        String vergiDairesi = "";
        String vergiNo = "";
        String kdv = "";
        String toplam = "";
        String tutar = ""; // tutar is not found on the receipt, but calculated as TUTAR = TOPLAM - KDV

        // for example, KDV has different names ("TOPKDV", "KDV", etc.). We read those alternative names
        // from a CSV file so that while we are iterating through the text, we check if we come across
        // those alternative names, and if we do (for example if we find the word TOPKDV), we find the
        // information associated with it (we find how much the KDV is.)
        List<List<String>> alternativeNames = getAlternativeNamesFromCSV(csvFilePath);

        List<String> tarihList = alternativeNames.get(0);
        List<String> belgeNoList = alternativeNames.get(1);
        List<String> kdvList = alternativeNames.get(2);
        List<String> toplamList = alternativeNames.get(3);
        List<String> vergiList = alternativeNames.get(4);

        boolean checkNextLineForKdv = false;
        boolean checkNextLineForToplam = false;

        String[] allTextLines = text.split("\\n");
        String prevLine = "";

        // iterate through each line in the text
        for (String textLine: allTextLines) {

            // sometimes, Google Vision doesn't put 5.00TL next to the keyword TOPLAM. When that happens,
            // it puts 5.00TL below TOPLAM, so we have to check the next line to find what TOPLAM is.
            // if, in the previous line, we deduced that the information is in the next line due to the incorrect
            // ordering by Google Vision API, then get the information now.

            // get kdv from this line, since we found the keyword KDV in the previous line
            if (checkNextLineForKdv) {
                kdv = getInfoFromNextLine(textLine);
                checkNextLineForKdv = false;
            }

            // get toplam from this line, since we found the keyword TOPLAM in the previous line
            if (checkNextLineForToplam) {
                toplam = getInfoFromNextLine(textLine);
                checkNextLineForToplam = false;
            }

            // get belgeAdi, check its method for more information
            if (belgeAdi.equals("")) {
                 belgeAdi = getBelgeAdi(textLine);
            }

            // the inner for loop that iterates through each word in the text line
            for (int i = 0; i <= textLine.length(); i++) {

                // get tarih, check its method for more information
                if (tarih.equals("")) {
                    tarih = getTarih(i, textLine, tarihList);
                }

                // get belgeNo, check its method for more information
                if (belgeNo.equals("")) {
                    belgeNo = getBelgeNo(i, textLine, belgeNoList);
                }

                // get vergiDairesi and vergiNo, check its method for more information
                if (vergiDairesi.equals("") || vergiNo.equals("")) {
                    String[] vergiInfos = getVergiDairesiAndVergiNo(i, textLine, vergiList);
                    vergiNo = vergiInfos[0];
                    vergiDairesi = vergiInfos[1];

                    // sometimes, Receipts don't have vergiNo (6220529513) next to the keyword Vergi Dairesi.
                    // When that happens, the trend is that vergiNo exists ABOVE Vergi Dairesi instead of next to it.
                    // so, if we found the Vergi Dairesi keyword, get the previous line to get what vergiNo is.
                    if (vergiNo.equals("prevLine")) {
                        vergiNo = prevLine;
                    }
                }

                // get kdv, check its method for more information
                if (kdv.equals("")) {
                    kdv = getKdv(i, textLine, kdvList);

                    // sometimes, Google Vision doesn't put 1.00TL next to the keyword KDV. When that happens,
                    // it puts 1.00TL BELOW the keyword KDV, so we have to check the next line to find KDV's value
                    if (kdv.equals("checkNextLineForKdv")) {
                        checkNextLineForKdv = true;
                    }
                }

                // get toplam, check its method for more information
                if (toplam.equals("")) {
                    toplam = getToplam(i, textLine, toplamList);

                    // sometimes, Google Vision doesn't put 5.00TL next to the keyword TOPLAM. When that happens,
                    // it puts 5.00TL BELOW the keyword TOPLAM, so we have to check the next line to find TOPLAM's value
                    if (toplam.equals("checkNextLineForToplam")) {
                        checkNextLineForToplam = true;
                    }
                }

            }

            // the only reason why prevLine is important is for vergiNo currently
            prevLine = textLine;
        }
        // since the loop is over, we should have extracted all the important pieces of information now

        // replace "-" and "." with "/" for tarih (turn 28.02.2002 into 28/02/2002)
        if (StringUtils.isNotEmpty(tarih)) {
            tarih = tarih.replace(".", "/");
            tarih = tarih.replace("-", "/");
        }

        // add a "." before the last two digits of KDV, since the last two are kurus.
        if (StringUtils.isNotEmpty(kdv) && kdv.length() > 2) {
            kdv = kdv.replace(".", "");
            kdv = kdv.substring(0, kdv.length() - 2) + "." + kdv.substring(kdv.length() - 2);
        }

        // add a "." before the last two digits of KDV, since the last two are kurus.
        if (StringUtils.isNotEmpty(toplam) && toplam.length() > 2) {
            toplam = toplam.replace(".", "");
            toplam = toplam.substring(0, toplam.length() - 2) + "." + toplam.substring(toplam.length() - 2);
        }

        // calculate tutar as TUTAR = TOPLAM - KDV
        if (StringUtils.isNotEmpty(kdv) && StringUtils.isNotEmpty(toplam) && kdv.length() > 2 && toplam.length() > 2) {
            tutar = String.valueOf(Integer.parseInt(toplam.replace(".", "")) -
                    Integer.parseInt(kdv.replace(".", "")));
            try {
                tutar = tutar.substring(0, tutar.length() - 2) + "." + tutar.substring(tutar.length() - 2);
            }
            catch (StringIndexOutOfBoundsException sioobe) {
                System.out.println("why do we have string index out of bounds exception? Tutar: " + tutar);
            }
        }

        System.out.println("Tarih: " + tarih);
        System.out.println("Belge Adi: " + belgeAdi);
        System.out.println("Belge No: " + belgeNo);
        System.out.println("Vergi Dairesi: " + vergiDairesi);
        System.out.println("Vergi No: " + vergiNo);
        System.out.println("KDV: " + kdv);
        System.out.println("Toplam: " + toplam);
        System.out.println("Tutar: " + tutar);

        importantInfoMap.put("Tarih", tarih);
        importantInfoMap.put("Belge Adi", belgeAdi);
        importantInfoMap.put("Belge No", belgeNo);
        importantInfoMap.put("Vergi Dairesi", vergiDairesi);
        importantInfoMap.put("Vergi No", vergiNo);
        importantInfoMap.put("KDV", kdv);
        importantInfoMap.put("Toplam", toplam);
        importantInfoMap.put("Tutar", tutar);

        return importantInfoMap;
    }

    private List<List<String>> getAlternativeNamesFromCSV(String csvFilePath) {
        // a method to get alternative names for important pieces of information from a CSV file

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

    private String getBelgeAdi(String textLine) {
        // getBelgeAdi works by getting the first valid line in the receipt

        if (textLine.length() > 1) {
            return textLine;
        }

        return "";
    }

    private String getTarih(int i, String textLine, List<String> tarihList) {
        // getTarih works by finding phrases like "/2022", and then goes back 10 characters from the end to get a
        // string like "28/02/2002" as tarih.

        String tarih = "";
        if (i >= 5) {
            if (tarihList.contains(textLine.substring(i - 5, i))) {
                try {
                    tarih = textLine.substring(i - 10, i);
                }
                catch (Exception ignored) {}
            }
        }

        return tarih;
    }

    private String getBelgeNo(int i, String textLine, List<String> belgeNoList) {
        // getBelgeNo works by finding phrases like "FİŞ NO," and then goes forward to get all integers to get a
        // string like "0050" as belgeNo.

        String belgeNo = "";

        for (String belgeAlt: belgeNoList) {
            if (i >= belgeAlt.length() + 1) { // accounting for the "space" by adding +1
                if (belgeAlt.equals(textLine.substring(i - belgeAlt.length() - 1, i).toUpperCase()
                        .replaceAll("\\s", ""))) {
                    for (int j = i; j < textLine.length(); j++) {
                        if (isNotLetter(textLine.substring(j, j + 1))) {
                            if (isInt(textLine.substring(j, j + 1))) {
                                belgeNo += textLine.substring(j, j + 1);
                            }
                        } else {
                            if (!belgeNo.equals(""))
                                break;
                        }
                    }
                }
            }
        }

        return belgeNo;
    }

    private String[] getVergiDairesiAndVergiNo(int i, String textLine, List<String> vergiList) {
        // getVergiDairesiAndVergiNo works by finding phrases like "V.D",
        // and then goes forward from the end to get all integers to find a string like "6220529513" as vergiNo.
        // then, it goes back from the front to get all strings to find a string like "USKUDAR" as vergiDairesi.

        String[] vergiInfos = new String[2];
        String vergiNo = "";
        String vergiDairesi = "";

        for (String vergiAlt: vergiList) {
            if (i >= vergiAlt.length()) {
                if (findStringSimilarity(vergiAlt, textLine.substring(i - vergiAlt.length(), i)) >= SIMILARITY_THRESHOLD) {
                    for (int j = i; j < textLine.length(); j++) {
                        if (isNotLetter(textLine.substring(j, j + 1))) {
                            if (isInt(textLine.substring(j, j + 1))) {
                                // get vergiNo, but it might not exist here
                                vergiNo += textLine.substring(j, j + 1);
                            }
                        } else {
                            break;
                        }
                    }
                    vergiDairesi = textLine.substring(0, i - vergiAlt.length());

                    if (vergiNo.equals("")) {
                        vergiNo = "prevLine";
                    }
                }
            }
        }

        vergiInfos[0] = vergiNo;
        vergiInfos[1] = vergiDairesi;
        return vergiInfos;
    }

    private String getKdv(int i, String textLine, List<String> kdvList) {
        // getKdv works by finding phrases like "TOP KDV," and then goes forward to get all integers to get a
        // string like "1.00" as kdv.

        String kdv = "";

        for (String kdvAlt: kdvList) {
            if (i >= kdvAlt.length() && kdv.equals("")) {
                if (kdvAlt.equals(textLine.substring(i - kdvAlt.length(), i).toUpperCase())) {
                    for (int j = i; j < textLine.length(); j++) {
                        if (isNotLetter(textLine.substring(j, j + 1))) {
                            if (isInt(textLine.substring(j, j + 1))) {
                                kdv += textLine.substring(j, j + 1);
                            }

                            if (textLine.charAt(j) == ',' || textLine.charAt(j) == '.') {
                                kdv += '.';
                            }
                        }
                        else {
                            if (!kdv.equals(""))
                                break;
                        }
                    }

                    // if kdv is still empty, check the next line
                    if (kdv.equals("")) {
                        kdv = "checkNextLineForKdv";
                    }
                }
            }
        }

        return kdv;
    }

    private String getToplam(int i, String textLine, List<String> toplamList) {
        // getToplam works by finding phrases like "TOPLAM," and then goes forward to get all integers to get a
        // string like "5.00" as TOPLAM.

        String toplam = "";

        // problem: there may be 'ara toplam' issues
        for (String toplamAlt: toplamList) {
            if (i >= toplamAlt.length() && toplam.equals("")) {
                if (toplamAlt.equals(textLine.substring(i - toplamAlt.length(), i).toUpperCase())) {
                    for (int j = i; j < textLine.length(); j++) {
                        if (isNotLetter(textLine.substring(j, j + 1))) {
                            if (isInt(textLine.substring(j, j + 1))) {
                                toplam += textLine.substring(j, j + 1);
                            }

                            if (textLine.charAt(j) == ',' || textLine.charAt(j) == '.') {
                                toplam += '.';
                            }
                        } else {
                            if (!toplam.equals(""))
                                break;
                        }
                    }

                    // if toplam is still empty, it's probably in the next line due to the
                    // incorrect ordering by Google Vision
                    if (toplam.equals("")) {
                        toplam = "checkNextLineForToplam";
                    }
                }
            }
        }

        return toplam;
    }

    private String getInfoFromNextLine(String textLine) {
        // used when KDV or TOPLAM values should appear in the next line

        String info = "";
        for (int j = 0; j < textLine.length(); j++) {
            if (isNotLetter(textLine.substring(j, j + 1))) {
                if (isInt(textLine.substring(j, j + 1))) {
                    info += textLine.substring(j, j + 1);
                }

                if (textLine.charAt(j) == ',' || textLine.charAt(j) == '.') {
                    info += '.';
                }
            }
            else {
                break;
            }
        }

        return info;
    }

    private boolean isNotLetter(String st) {
        // returns whether the string is not a letter

        Set<String> chs = Set.of("/", ",", ".", " ", ":", "*", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        for (int i = 0; i < st.length(); i++) {
            if(!chs.contains(st.substring(i, i+1))) {
                return false;
            }
        }
        return true;
    }

    private boolean isInt(String st) {
        // returns whether the string is an integer

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
        // finds string similarity using Levenshtein Distance algorithm

        double maxLength = Double.max(x.length(), y.length());
        if (maxLength > 0) {
            // not case-sensitive
            return (maxLength - StringUtils.getLevenshteinDistance(x.toUpperCase(), y.toUpperCase())) / maxLength;
        }
        return 1.0;
    }
}
