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
        Thread vt = new VisionThread(sourcePath + fileName + fileExtension);
        vt.start();
        System.out.println("\n \n NEXT \n \n");
        fileName = "example4";
        Thread vt2 = new VisionThread(sourcePath + fileName + fileExtension);
        vt2.start();
        fileName = "example5";
        Thread vt3 = new VisionThread(sourcePath + fileName + fileExtension);
        vt3.start();
        fileName = "example9";
        Thread vt4 = new VisionThread(sourcePath + fileName + fileExtension);
        vt4.start();
        // String orderedText = vt.getOrderedText();


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