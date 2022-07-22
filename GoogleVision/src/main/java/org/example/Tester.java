package org.example;

public class Tester {
    static String SRC_PATH = "src/images/";
    static String fileName = "example4";
    static String fileExtension = ".png"; // didn't work with .pdf
    public static void main(String[] args) {
        ImageProcessor ip = new ImageProcessor(SRC_PATH, fileName, fileExtension);
    }
}
