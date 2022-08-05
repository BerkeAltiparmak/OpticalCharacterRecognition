package com.solvia.solviavision.services;

import java.util.Map;

public interface ReceiptService {

    Map<String, String> getAssociatedCategories(String text);
}
