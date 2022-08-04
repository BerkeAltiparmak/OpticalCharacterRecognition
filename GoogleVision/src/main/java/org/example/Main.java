package org.example;



public class Main {
    static String sourcePath = "src/images/";
    static String fileName = "example15";
    static String fileExtension = ".png"; // didn't work with .pdf
    private static String imageFolderPath = "src/images/receipts";
    private static String excelFilePath = "src/tables/VergilerMulti.xlsx";

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();

        MultiVision mv = new MultiVision();
        mv.convertImageToText(imageFolderPath, excelFilePath);

        System.out.println("It took: " + (System.currentTimeMillis() - start) + " milliseconds.");
    }

}