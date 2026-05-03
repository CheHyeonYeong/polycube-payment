package com.cube.payment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentControllerValidationTest {

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("Idempotency-Key 헤더가 누락되면 400 MISSING_HEADER")
    void missingIdempotencyHeader() throws Exception {
        String body = """
                {"orderId": 1, "means": [{"method":"CREDIT_CARD","amount":1000}]}
                """;
        mvc.perform(post("/api/v1/payments").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_HEADER"))
                .andExpect(jsonPath("$.header").value("Idempotency-Key"));
    }

    @Test
    @DisplayName("means 가 비어 있으면 400 INVALID_REQUEST")
    void emptyMeans() throws Exception {
        String body = """
                {"orderId": 1, "means": []}
                """;
        mvc.perform(post("/api/v1/payments")
                        .header("Idempotency-Key", "k-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.fields[0].field").value("means"));
    }

    @Test
    @DisplayName("means 항목 amount 가 음수면 400")
    void negativeAmount() throws Exception {
        String body = """
                {"orderId": 1, "means": [{"method":"POINT","amount":-1}]}
                """;
        mvc.perform(post("/api/v1/payments")
                        .header("Idempotency-Key", "k-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
