package org.example;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;


public class Main {
    static String SRC_PATH = "src/images/";
    static String fileName = "example15";
    static String fileExtension = ".png"; // didn't work with .pdf
    static boolean is_processed = false;
    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();

        // using opencv-4.5.5 to preprocess the image.
        // ImageProcessor ig = new ImageProcessor(SRC_PATH, fileName, fileExtension);

        // if you want to process the data with opencv
        String filePath = SRC_PATH + fileName + fileExtension;
        if (is_processed) {
            filePath = SRC_PATH + fileName + "GrayCanny" + fileExtension;
        }

        // read the image with GoogleVision and get the orderedText if it is a receipt
        GoogleVision gv = new GoogleVision(filePath);
        String orderedText = gv.getOrderedText();

        if (!orderedText.equals("")) {
            ReceiptMaster rm = new ReceiptMaster(orderedText);
        }
        else {
            System.out.println("The image is not a receipt");
        }

        System.out.println("It took: " + (System.currentTimeMillis() - start) + " milliseconds.");
    }
}