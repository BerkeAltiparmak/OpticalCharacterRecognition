package com.solvia.solviavision.services.impl;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.solvia.solviavision.models.TextModel;
import com.solvia.solviavision.services.OcrService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OcrServiceImpl implements OcrService {

    @Override
    public List<String> convertImageToText(MultipartFile... files) throws IOException {
        List<String> orderedTextList = new ArrayList<>();

        List<AnnotateImageRequest> requests = new ArrayList<>();


        for (MultipartFile fl: files) {
            ByteString imgBytes = ByteString.copyFrom(fl.getBytes());

            Image img = Image.newBuilder().setContent(imgBytes).build();
            Feature textFeat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
            AnnotateImageRequest textRequest =
                    AnnotateImageRequest.newBuilder().addFeatures(textFeat)
                            .setImageContext(ImageContext.newBuilder().addLanguageHints("tr"))
                            .setImage(img).build();
            requests.add(textRequest);
            /*
            Feature labelFeat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
            AnnotateImageRequest labelRequest =
                    AnnotateImageRequest.newBuilder().addFeatures(labelFeat).setImage(img).build();
            requests.add(labelRequest);
            */
        }

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests); // where API request is made
            List<AnnotateImageResponse> responses = response.getResponsesList();
            client.close();

            // starting the annotation of the image
            // full list of available annotations can be found at http://g.co/cloud/vision/docs
            List<TextModel> words = new ArrayList<>();
            // HashSet<String> imageLabelSet = new HashSet<>();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.getError().getMessage());
                    break;
                }
                // if (!res.getLabelAnnotationsList().isEmpty()) {
                words = new ArrayList<>();
                // imageLabelSet = new HashSet<>();
                // }

                // get text annotation
                for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
                    String text = annotation.getDescription();
                    BoundingPoly boundingPoly = annotation.getBoundingPoly();

                    // System.out.format("Text: %s%n", text);
                    // System.out.format("Position : %s%n", boundingPoly);
                    // System.out.println("Topicality: " + annotation.getTopicality());

                    // in order to properly order the words, we put them and their properties to an ArrayList
                    TextModel word = new TextModel(text, boundingPoly);
                    if (word.isCounterClockwiseFromTopLeft()){
                        words.add(word);
                    }
                }

                /*
                // get label annotation
                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                    imageLabelSet.add(annotation.getDescription());
                }
                */

                System.out.println(" Result of the initial image-to-text operation: ");
                System.out.println(words.get(0).getText()); // print the full text
                String orderedText = "";
                // if (imageLabelSet.contains("Receipt") || imageLabelSet.contains("Font")) {
                System.out.println("\n Since this image is analyzed to be a receipt," +
                        " here is the ordered version of it: ");
                words.remove(0); // (skip the first text as it's the full text)
                orderedText = orderReceiptText(words);
                orderedTextList.add(orderedText);
                System.out.println(orderedText);
                //ReceiptMaster rm = new ReceiptMaster(orderedText, excelFilePath);
                // }
            }
        }

        return orderedTextList;
    }

    public String orderReceiptText(List<TextModel> words) {
        StringBuilder textSoFar = new StringBuilder();
        List<TextModel> removedWords = new ArrayList<>();

        words.sort(Comparator.comparingInt(TextModel::getCenterY));

        // iterate through every word in the text in the order that GoogleVision captured
        for (TextModel word1: words) {

            // if the word is already written in the text or is not in the correct orientation, don't mind it
            if (!removedWords.contains(word1) && word1.isCounterClockwiseFromTopLeft()) {
                List<TextModel> textsInTheSameLine = new ArrayList<>();
                textsInTheSameLine.add(word1);
                removedWords.add(word1);
                int mostBottomPointInTheLine = word1.getBottomY();

                // iterate through every other word to compare other word's y-coordinates with word1
                for (TextModel word2 : words) {

                    // make sure the word is correctly orientated
                    if (word2 != word1 && !removedWords.contains(word2) && word2.isCounterClockwiseFromTopLeft()) {

                        // if there exists another word that is within the y-coordinate range of this word,
                        // add it to the text before or after word1 depending on that word's x coordinate.

                        // if (word2.getCenterY() >= word1.getTopY() && word2.getCenterY() <= word1.getBottomY()) {
                        if (mostBottomPointInTheLine - word2.getTopY() >= word2.getRangeY() / 2) {
                            textsInTheSameLine.add(word2);
                            removedWords.add(word2);
                            mostBottomPointInTheLine = Math.max(word1.getBottomY(), word2.getBottomY());
                        }
                    }
                }
                // for the texts that are in the same line (similar y-coordinates), sort them by their x-coordinates.
                textsInTheSameLine.sort(Comparator.comparingInt(TextModel::getLeftX));
                int mostRightPointInTheLine = 0;
                int lastWordCharacterSize = -1;
                for (TextModel inLineWord: textsInTheSameLine) {
                    textSoFar.append(inLineWord.getText());
                    // if there is some distance between the words, add a space in between.
                    /*
                    if (inLineWord.getLeftX() - mostRightPointInTheLine > lastWordCharacterSize
                            && lastWordCharacterSize > 0) {
                        textSoFar.append(" ".repeat(
                                (inLineWord.getLeftX() - mostRightPointInTheLine) / lastWordCharacterSize));
                    }
                    else */
                    if (inLineWord.getRightX() - mostRightPointInTheLine > 2) {
                        textSoFar.append(" ");
                    }
                    mostRightPointInTheLine = inLineWord.getRightX();
                    lastWordCharacterSize = inLineWord.getRangeX() / inLineWord.getText().length();
                }
                textSoFar.append("\n");
            }
        }

        return textSoFar.toString();
    }
}
