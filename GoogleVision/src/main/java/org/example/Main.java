package org.example;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception{
        String filePath = "src/images/example4.png";

        List<AnnotateImageRequest> requests = new ArrayList<>();

        ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            client.close();

            List<WordText> words = new ArrayList<WordText>();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.getError().getMessage());
                    return;
                }

                // For full list of available annotations, see http://g.co/cloud/vision/docs
                for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
                    String text = annotation.getDescription();
                    BoundingPoly boundingPoly = annotation.getBoundingPoly();

                    //System.out.format("Text: %s%n", text);
                    //System.out.format("Position : %s%n", boundingPoly);

                    // everything below is "ordering"
                    words.add(new WordText(text, boundingPoly));
                }
            }
            System.out.println(words.get(0)); // print the full text
            System.out.println("!!!NOW THE ORDERED VERSION!!!");
            words.remove(0); // (skip the first text as it's the full text)
            String orderedText = getOrderedText(words);
            System.out.println(orderedText);
        }

    }

    private static String getOrderedText(List<WordText> words) {
        StringBuilder textSoFar = new StringBuilder();
        List<WordText> removedWords = new ArrayList<WordText>();
        for (WordText word1: words) {

            // if the word is already written in the text, don't write it again
            if (!removedWords.contains(word1)) {

                // if it's out of the y-coordinate range of the previous word, make a line break
                textSoFar.append("\n");

                // add the word to the text
                textSoFar.append(word1.getText());
                removedWords.add(word1);
            }

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