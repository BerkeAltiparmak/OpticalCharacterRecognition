package com.solvia.solviavision.controllers;

import com.solvia.solviavision.services.ExcelService;
import com.solvia.solviavision.services.OcrService;
import com.solvia.solviavision.services.ReceiptService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
public class ImageController {
    private OcrService ocrService;

    private ReceiptService receiptService;
    private ExcelService excelService;
    public final String INFO_FINDER_PATH = "src/main/resources/alternativeNames.csv";
    public final String EXCEL_PATH = "src/main/resources/excels/VergilerTest11.xlsx";


    @PostMapping("api/v1/ocr")
    public String getStringFromImages(@RequestParam("files") MultipartFile... imgs) {
        long start = System.currentTimeMillis();
        try {
            // get texts in all the images sent by the client.
            List<String> orderedTextList = ocrService.convertImageToText(imgs);
            long timeForImageToText = System.currentTimeMillis() - start;
            System.out.println("Time to perform OCR operation: " + timeForImageToText);

            for (String orderedText: orderedTextList) {

                // for each text returned by GoogleVision (which is improved with our algorithm),
                // get important receipt information (such as tarih, vergi dairesi, kdv, toplam, etc.) in those receipts
                Map<String, String> importantReceiptInfo = receiptService.getImportantReceiptInfo(orderedText,
                        INFO_FINDER_PATH);

                // then, put those important receipt information into an excel file
                excelService.writeIntoExcelFile(importantReceiptInfo,
                        EXCEL_PATH);
            }

            long totalTime = (System.currentTimeMillis() - start);
            System.out.println("Time it took to perform OCR, to extract important receipt information, " +
                    "and to write it into an excel file: " + totalTime);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        long time = (System.currentTimeMillis() - start);
        return String.valueOf(time);

    }

}
