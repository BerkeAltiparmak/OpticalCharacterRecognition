package com.solvia.solviavision.services;

import com.solvia.solviavision.models.TextModel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface OcrService {
   void convertImageToText(MultipartFile... fl) throws IOException;
   String orderReceiptText(List<TextModel> words);
}
