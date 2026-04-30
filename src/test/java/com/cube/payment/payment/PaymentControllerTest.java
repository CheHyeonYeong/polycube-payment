package com.cube.payment.payment;

import com.cube.payment.common.exception.CustomException;
import com.cube.payment.common.exception.ErrorCode;
import com.cube.payment.payment.entity.PaymentMethod;
import com.cube.payment.payment.response.PaymentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@DisplayName("결제 컨트롤러 슬라이스 테스트")
class PaymentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean PaymentService paymentService;

    @Test
    @DisplayName("결제 요청이 성공하면 201 상태코드와 결제 정보를 반환한다")
    void 결제_요청_201_반환() throws Exception {
        PaymentResponse response = new PaymentResponse(
                1L, 5L, "상품A", 10_000L, 1_000L, 9_000L,
                PaymentMethod.CREDIT_CARD, LocalDateTime.now());
        given(paymentService.pay(any())).willReturn(response);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("orderId", 5, "paymentMethod", "CREDIT_CARD"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.discountAmount").value(1_000))
                .andExpect(jsonPath("$.data.finalAmount").value(9_000))
                .andExpect(jsonPath("$.data.paymentMethod").value("CREDIT_CARD"));
    }

    @Test
    @DisplayName("주문 ID 없이 결제 요청 시 400 상태코드를 반환한다")
    void 주문ID_없는_결제_요청_400_반환() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("paymentMethod", "CREDIT_CARD"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("결제 수단 없이 결제 요청 시 400 상태코드를 반환한다")
    void 결제수단_없는_결제_요청_400_반환() throws Exception {
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("orderId", 5))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("결제 단건 조회 요청이 성공하면 200 상태코드와 결제 정보를 반환한다")
    void 결제_단건_조회_200_반환() throws Exception {
        PaymentResponse response = new PaymentResponse(
                1L, 5L, "상품B", 20_000L, 2_000L, 18_000L,
                PaymentMethod.POINT, LocalDateTime.now());
        given(paymentService.findById(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.paymentId").value(1))
                .andExpect(jsonPath("$.data.finalAmount").value(18_000));
    }

    @Test
    @DisplayName("존재하지 않는 결제 조회 시 404 상태코드를 반환한다")
    void 존재하지_않는_결제_조회_404_반환() throws Exception {
        given(paymentService.findById(999L)).willThrow(new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/payments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PAYMENT_NOT_FOUND.getMessage()));
    }
}
