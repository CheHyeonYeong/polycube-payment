package com.cube.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import com.cube.member.MemberRepository;
import com.cube.member.entity.Member;
import com.cube.member.entity.MemberGrade;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderControllerTest {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    OrderRepository orderRepository;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpEntity<String> jsonRequest(String body) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, h);
    }

    private Member memberOfGrade(MemberGrade grade) {
        return memberRepository.findAll().stream()
                .filter(m -> m.getGrade() == grade)
                .findFirst()
                .orElseThrow();
    }

    @AfterEach
    void cleanupOrders() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/orders → 201 + Location, GET 으로 동일 데이터 조회")
    void createAndRead() {
        Member vip = memberOfGrade(MemberGrade.VIP);
        String body = """
                {"productName":"테스트상품","originalPrice":12345,"memberId":%d}
                """.formatted(vip.getId());

        ResponseEntity<Map<String, Object>> created = rest.exchange(
                url("/api/v1/orders"), HttpMethod.POST, jsonRequest(body), MAP_TYPE);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getHeaders().getLocation()).isNotNull();
        Long orderId = ((Number) created.getBody().get("id")).longValue();
        assertThat(((Number) created.getBody().get("originalPrice")).longValue()).isEqualTo(12345L);
        assertThat(created.getBody().get("status")).isEqualTo("CREATED");

        ResponseEntity<Map<String, Object>> read = rest.exchange(
                url("/api/v1/orders/" + orderId), HttpMethod.GET, jsonRequest(null), MAP_TYPE);
        assertThat(read.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(read.getBody().get("productName")).isEqualTo("테스트상품");
    }

    @Test
    @DisplayName("POST /api/v1/orders 가 음수 originalPrice → 400 INVALID_REQUEST")
    void rejectsNegativeOriginalPrice() {
        Member vip = memberOfGrade(MemberGrade.VIP);
        String body = """
                {"productName":"음수상품","originalPrice":-1,"memberId":%d}
                """.formatted(vip.getId());

        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                url("/api/v1/orders"), HttpMethod.POST, jsonRequest(body), MAP_TYPE);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().get("code")).isEqualTo("INVALID_REQUEST");
    }

    @Test
    @DisplayName("GET /api/v1/orders/{없는 id} → 404 ORDER_NOT_FOUND")
    void notFoundReturnsAdtPayload() {
        long missing = Long.MAX_VALUE;
        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                url("/api/v1/orders/" + missing), HttpMethod.GET, jsonRequest(null), MAP_TYPE);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody().get("code")).isEqualTo("ORDER_NOT_FOUND");
        assertThat(((Number) resp.getBody().get("orderId")).longValue()).isEqualTo(missing);
    }
}
