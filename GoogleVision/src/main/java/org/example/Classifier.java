package org.example;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class Classifier {
    private CascadeClassifier diceCascade;
    private Mat image;
    private String loc;
    private String output = "src/images/example16opencv.png";

    public Classifier(String loc) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        diceCascade = new CascadeClassifier("src/images/diceCascade.xml");
        this.loc = loc;
        detImg();
    }

    public void detImg() {

        Mat image = Imgcodecs.imread(loc); // Reads the image

        MatOfRect diceDetections = new MatOfRect(); // Output container
        diceCascade.detectMultiScale(image, diceDetections); // Performs the detection

        // Draw a bounding box around each detection.
        for (Rect rect : diceDetections.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));
        }

        // Save the visualized detection.
        Imgcodecs.imwrite(output, image);

    }
}