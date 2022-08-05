package com.solvia.solviavision;

import com.solvia.solviavision.services.ReceiptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SolviaVisionApplicationTests {

    @Autowired
    private ReceiptService receiptService;

    @Test
    void contextLoads() {

    }

    @Test
    void receiptServiceTest() {
        receiptService.getImportantReceiptInfo("", "src/main/resources/alternativeNames.csv");
    }

}
