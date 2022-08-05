package com.solvia.solviavision.services;

import java.io.IOException;
import java.util.Map;

public interface ExcelService {
    public void writeIntoExcelFile(Map<String, String> associatedMap, String excelFilePath) throws IOException;
}
