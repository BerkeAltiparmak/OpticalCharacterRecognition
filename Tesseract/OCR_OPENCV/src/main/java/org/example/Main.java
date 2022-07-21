package org.example;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Main {

    Tesseract ts;
    public Main(){
        ts = new Tesseract();
        ts.setDatapath("src/tessdata");
        ts.setLanguage("eng");
        try {
            String text = ts.doOCR(getImage("src/images/example3.png"));
            System.out.println(text);
        }
        catch (TesseractException te){
            System.out.println("TesError: " + te.getMessage());
        }
        catch (IOException ioe){
            System.out.println("IOError: " + ioe.getMessage());
        }
    }

    private BufferedImage getImage(String imgPath) throws IOException {
        Mat mat = Imgcodecs.imread(imgPath);

        // grayscale yaparak Tesseract'in kolay okumasını sagliyoruz.
        Mat gray = new Mat();
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_BGR2GRAY);

        // resmin boyutunu artirmak yine Tesseract'in kolay okumasını sagliyor.
        Mat resized = new Mat();
        // asagida farklı boyutlarda (*1.9 yerine *1.5) farklı yerleri dogru okuyup farklı yerleri yanlis okuyor
        // kendi gozlemime gore accuracy cok degismiyor.
        Size size = new Size(mat.width() * 1.9f, mat.height() * 1.9f);
        Imgproc.resize(gray, resized, size);

        // ortaya cikan resmi Buffered Image yapmak icin:
        MatOfByte mof = new MatOfByte();
        byte imageByte[];
        Imgcodecs.imencode(".png", resized, mof);
        imageByte = mof.toArray();

        return ImageIO.read(new ByteArrayInputStream(imageByte));
    }

    public static void main(String[] args) {
        // opencv-4.5.5 kullanilmistir.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        new Main();
    }

    // Expected Output (example3 image):
    /*
    IT'S NOT ABOUT
    PERFECT. IT'S ABOUT
    EFFORT. AND WHEN YOU
    BRING THAT EFFORT
    EVERY SINGLE DAY,
    THAT'S WHERE
    TRANSFORMATION
    HAPPENS. THAT'S HOW
    CHANGE OCCURS.
    -JILLIAN MICHAELS

    GH
     */

    // Actual Output when image *1.9:
    /*
    Je3 (ejg (Ne)lty
    PERFECT. IT‘S ABOUT
    3. .e qy »P344, 3, RColt
    HAHNDLCkLEn1iiie 3
    20354A /y (ofM 3 >\
    L EDL SHS
    ILLD HeoelmnyCitle},_
    uds 5) L CUESI s 0) \}
    or, C \\ [ol 3 ol ocod 1 ] 5 )oil
    —JILLIAN MICHAELS

    GH
     */

    // Actual Output when image *1.5 (to show the impact of difference in sizes):
    /*
    kss (ejg c (ellu
    S 5 517 5 o B J ssr .( —{o} 14 j
    32. 2e uBc\ >3 44.3 RielD
    BRING THAT EFFORT
    2U3NYaSA| ) (olM 3 >\ a
    L ED Y 2HS
    ILLV HaoelNyLulle}).
    l s 3 Bs ues I a 0) 4/
    (od, L\) ol 3 ol eced 1| * ):3
    —JILLIAN MICHAELS
    (ela|
     */

}