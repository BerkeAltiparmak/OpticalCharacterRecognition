package com.solvia.solviavision.services.impl;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.solvia.solviavision.models.TextModel;
import com.solvia.solviavision.services.OcrService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OcrServiceImpl implements OcrService {

    private List<String> orderedTextList = new ArrayList<>();
    @Override
    public void convertImageToText(MultipartFile... files) throws IOException {
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
    }

    public String orderReceiptText(List<TextModel> words) {
        StringBuilder textSoFar = new StringBuilder();
        List<TextModel> removedWords = new ArrayList<>();


        // iterate through every word in the text in the order that GoogleVision captured
        for (TextModel word1: words) {

            // if the word is already written in the text or is not in the correct orientation, don't mind it
            if (!removedWords.contains(word1) && word1.isCounterClockwiseFromTopLeft()) {

                // if it's out of the y-coordinate range of the previous word, make a line break,
                // if it's not the first word
                if (!textSoFar.toString().equals("")) {
                    textSoFar.append("\n");
                }

                // two string builders so that one inner for loop is enough.
                StringBuilder textBeforeWord1 = new StringBuilder();
                StringBuilder textAfterWord1 = new StringBuilder();
                String newWord = word1.getText();
                removedWords.add(word1);

                // iterate through every other word to compare other word's y-coordinates with word1
                for (TextModel word2 : words) {

                    // make sure the word is correctly orientated
                    if (word2 != word1 && !removedWords.contains(word2) && word2.isCounterClockwiseFromTopLeft()) {

                        // if there exists words above this word that hasn't been added yet,
                        // add it to the text before word1
                        if (word2.getCenterY() <= word1.getY1() && word2.getCenterY() <= word1.getY2()) {
                            textBeforeWord1.append(word2.getText());
                            removedWords.add(word2);
                            textBeforeWord1.append(" ");
                        }

                        // if there exists another word that is within the y-coordinate range of this word,
                        // add it to the text after word1
                        if (word2.getCenterY() >= word1.getY1() && word2.getCenterY() <= word1.getY4()) {

                            // if words are not close to each other, add a space in between
                            if (word2.getX1() - word1.getX2() > 2) {
                                textAfterWord1.append(" ");
                            }
                            textAfterWord1.append(word2.getText());
                            removedWords.add(word2);

                            word1 = word2;
                        }
                    }
                }

                // combine StringBuilders together to add these new words
                textSoFar.append(textBeforeWord1);
                // create an empty line before word1 as we don't want them to be next to each other.
                if(!textBeforeWord1.toString().equals("")) {
                    textSoFar.append("\n");
                }
                textSoFar.append(newWord);
                textSoFar.append(textAfterWord1);
            }
        }

        return textSoFar.toString();
    }
}
