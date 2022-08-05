package com.solvia.solviavision.services;

import java.util.Map;

public interface ReceiptService {

    public Map<String, String> getImportantReceiptInfo(String text, String csvFilePath);
}
