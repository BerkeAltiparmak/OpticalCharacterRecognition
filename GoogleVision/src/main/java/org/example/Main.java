package org.example;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;


public class Main {
    static String SRC_PATH = "src/images/";
    static String fileName = "example4";
    static String fileExtension = ".png"; // didn't work with .pdf
    static boolean is_processed = false;
    public static void main(String[] args) throws Exception{

        long start = System.currentTimeMillis();

        // using opencv-4.5.5 to preprocess the image.
        // ImageProcessor ig = new ImageProcessor(SRC_PATH, fileName, fileExtension);

        List<AnnotateImageRequest> requests = new ArrayList<>();

        String filePath = SRC_PATH + fileName + fileExtension;
        if (is_processed) {
            filePath = SRC_PATH + fileName + "GrayCanny" + fileExtension;
        }
        ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature textFeat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest textRequest =
                AnnotateImageRequest.newBuilder().addFeatures(textFeat).setImage(img).build();
        requests.add(textRequest);

        Feature labelFeat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
        AnnotateImageRequest labelRequest =
                AnnotateImageRequest.newBuilder().addFeatures(labelFeat).setImage(img).build();
        requests.add(labelRequest);

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            client.close();

            List<WordText> words = new ArrayList<WordText>();

            String imageLabel = "";

            // starting the annotation of the image
            // full list of available annotations can be found at http://g.co/cloud/vision/docs
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.getError().getMessage());
                    return;
                }

                // get text annotation
                for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
                    String text = annotation.getDescription();
                    BoundingPoly boundingPoly = annotation.getBoundingPoly();

                    // System.out.format("Text: %s%n", text);
                    // System.out.format("Position : %s%n", boundingPoly);

                    // in order to properly order the words, we put them and their properties to an ArrayList
                    words.add(new WordText(text, boundingPoly));
                }

                // get label annotation
                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                    annotation
                            .getAllFields()
                            .forEach((k, v) -> System.out.format("%s : %s%n", k, v.toString()));
                }

                // get the most confident image label when res is regarding Label Response
                try {
                    imageLabel = res.getLabelAnnotations(0).getDescription();
                }
                catch (Exception ignored) {
                }
            }
            System.out.println("The image is analyzed to be: " + imageLabel);

            System.out.println("The Original Text: ");
            System.out.println(words.get(0).getText()); // print the full text

            if ("Receipt".equals(imageLabel) || "Font".equals(imageLabel)) {
                System.out.println("\n Since this image is analyzed to be a receipt," +
                        " here is the ordered version of it: ");
                words.remove(0); // (skip the first text as it's the full text)
                String orderedText = getOrderedText(words);
                System.out.println(orderedText);

                ReceiptMaster rm = new ReceiptMaster(orderedText);
            }

            System.out.println("It took: " + (System.currentTimeMillis() - start) + " milliseconds.");

        }

    }

    private static String getOrderedText(List<WordText> words) {
        StringBuilder textSoFar = new StringBuilder();
        List<WordText> removedWords = new ArrayList<WordText>();

        // iterate through every word in the text in the order that GoogleVision captured
        for (WordText word1: words) {

            // if the word is already written in the text, don't write it again
            if (!removedWords.contains(word1)) {

                // if it's out of the y-coordinate range of the previous word, make a line break
                textSoFar.append("\n");

                // add the word to the text
                textSoFar.append(word1.getText());
                removedWords.add(word1);
            }

            // iterate through every other word to compare other word's y-coordinates with word1
            for (WordText word2: words) {

                // if there exists another word that is within the y-coordinate range of this word, add it to the text
                if (word2 != word1 && !removedWords.contains(word2)) {
                    if (word2.getCenterY() >= word1.getY1() && word2.getCenterY() <= word1.getY4()) {

                        // if words are not close to each other, add a space in between
                        if (word2.getX1() - word1.getX2() > 2) {
                            textSoFar.append(" ");
                        }
                        textSoFar.append(word2.getText());
                        removedWords.add(word2);
                    }
                }
            }
        }

        return textSoFar.toString();
    }
}