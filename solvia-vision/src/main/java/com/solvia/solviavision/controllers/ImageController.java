package com.solvia.solviavision.controllers;

import com.solvia.solviavision.services.OcrService;
import com.solvia.solviavision.services.ReceiptService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@AllArgsConstructor
@RestController
public class ImageController {
    private OcrService ocrService;


    @PostMapping("api/v1/ocr")
    public String getStringFromImages(@RequestParam("file") MultipartFile... imgs) {
        long start = System.currentTimeMillis();
        try {
            ocrService.convertImageToText(imgs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        long time = (System.currentTimeMillis() - start);
        return String.valueOf(time);

    }

}
