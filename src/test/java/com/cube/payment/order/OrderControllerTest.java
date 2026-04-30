package com.cube.payment.order;

import com.cube.payment.common.exception.CustomException;
import com.cube.payment.common.exception.ErrorCode;
import com.cube.payment.order.response.OrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("주문 컨트롤러 슬라이스 테스트")
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean OrderService orderService;

    @Test
    @DisplayName("주문 생성 요청이 성공하면 201 상태코드와 주문 정보를 반환한다")
    void 주문_생성_201_반환() throws Exception {
        OrderResponse response = new OrderResponse(1L, 10L, "노트북", 1_500_000L);
        given(orderService.create(any())).willReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("memberId", 10, "productName", "노트북", "originalPrice", 1500000))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("노트북"))
                .andExpect(jsonPath("$.data.originalPrice").value(1_500_000));
    }

    @Test
    @DisplayName("상품명 없이 주문 요청 시 400 상태코드를 반환한다")
    void 상품명_없는_주문_요청_400_반환() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("memberId", 10, "originalPrice", 1500000))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("회원 ID 없이 주문 요청 시 400 상태코드를 반환한다")
    void 회원ID_없는_주문_요청_400_반환() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("productName", "노트북", "originalPrice", 1500000))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("주문 단건 조회 요청이 성공하면 200 상태코드와 주문 정보를 반환한다")
    void 주문_단건_조회_200_반환() throws Exception {
        OrderResponse response = new OrderResponse(1L, 10L, "마우스", 50_000L);
        given(orderService.findById(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.productName").value("마우스"));
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회 시 404 상태코드를 반환한다")
    void 존재하지_않는_주문_조회_404_반환() throws Exception {
        given(orderService.findById(999L)).willThrow(new CustomException(ErrorCode.ORDER_NOT_FOUND));

        mockMvc.perform(get("/api/v1/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ORDER_NOT_FOUND.getMessage()));
    }
}
