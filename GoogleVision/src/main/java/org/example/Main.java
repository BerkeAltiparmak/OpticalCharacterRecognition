package org.example;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;


public class Main {
    static String sourcePath = "src/images/";
    static String fileName = "example15";
    static String fileExtension = ".png"; // didn't work with .pdf
    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();

        // read the image with GoogleVision and get the orderedText if it is a receipt
        // GoogleVision gv = new GoogleVision(filePath);
        // String orderedText = gv.getOrderedText();


        // an attempt at multithreading
        String[] fileNameArr = {"example15", "example4", "example5", "example9"};
        List<VisionThread> threadList = new ArrayList<>();

        for (String fileName: fileNameArr) {
            VisionThread vt = new VisionThread(sourcePath + fileName + fileExtension);
            vt.start();
            threadList.add(vt);
            System.out.println("Starting: " + vt.getName());
        }

        int nonReceiptImageCounter = 0;
        for (VisionThread vt: threadList) {
            System.out.println("Joining: " + vt.getName());
            vt.join();
            System.out.println("Joined: " + vt.getName());
            String orderedText = vt.getOrderedText();
            if (!orderedText.equals("")) {
                ReceiptMaster rm = new ReceiptMaster(orderedText);
            }
            else {
                nonReceiptImageCounter++;
            }
        }
        System.out.println(nonReceiptImageCounter);


        /*
        if (!orderedText.equals("")) {
            ReceiptMaster rm = new ReceiptMaster(orderedText);
        }
        else {
            System.out.println("The image is not a receipt");
        }
        */

        System.out.println("It took: " + (System.currentTimeMillis() - start) + " milliseconds.");
    }

    private static String process_image_with_opencv(String newFileName) {
        // using opencv-4.5.5 to preprocess the image.
        ImageProcessor ig = new ImageProcessor(sourcePath, newFileName, fileExtension);

        // returns the address of the newly processed image
        return sourcePath + fileName + "GrayCanny" + fileExtension;
    }
}