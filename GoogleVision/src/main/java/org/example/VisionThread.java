package org.example;

public class VisionThread extends Thread {
    private final String filePath;
    public String orderedText = "";

    public VisionThread(String filePath) {
        this.filePath = filePath;
    }

    public void run() {
        try {
            GoogleVision gv = new GoogleVision(filePath);
        } catch (Exception e) {
            System.out.println("thread error somehow");
        }
    }

    public String getOrderedText() {
        return orderedText;
    }

}
