package org.example;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.*;
import static org.opencv.imgproc.Imgproc.erode;

public class ImageProcessor {
    private String srcPath;
    private String fileName;
    private String fileExtension;
    public ImageProcessor(String srcPath, String fileName, String fileExtension) {
        this.srcPath = srcPath;
        this.fileName = fileName;
        this.fileExtension = fileExtension;

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat origin = imread(srcPath + fileName + fileExtension);
        process_image_opencv(origin);
    }


    private boolean process_image_opencv(Mat inputMat) {
        Mat gray = new Mat();
        cvtColor(inputMat, gray, COLOR_BGR2GRAY);
        Canny(inputMat, gray, 15, 100);
        imwrite(srcPath + fileName + "GrayCanny" + fileExtension, gray);
        Mat element = getStructuringElement(MORPH_RECT, new Size(2, 2), new Point(1, 1));
        dilate(gray, gray, element);
        erode(gray, gray, element);
        imwrite(srcPath + fileName + "CloseOpen" + fileExtension, gray);
        return true;
    }
}
