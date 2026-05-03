package com.cube.discount;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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

import com.cube.discount.entity.DiscountPolicy;
import com.cube.support.TestSeed;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DiscountControllerTest {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    DiscountPolicyRepository policyRepository;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpEntity<String> jsonRequest(String body) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, h);
    }

    @AfterEach
    void cleanupNonSeedPolicies() {
        List<DiscountPolicy> nonSeed = policyRepository.findAll().stream()
                .filter(p -> !TestSeed.POLICY_NAMES.contains(p.getName()))
                .toList();
        policyRepository.deleteAll(nonSeed);
    }

    @Test
    @DisplayName("POST /api/v1/discount-policies 생성 → 201 + Location, FIXED 정책")
    void createFixedPolicy() {
        String body = """
                {"name":"테스트 FIXED","targetGrade":"NORMAL","targetPaymentMethod":null,
                 "discountType":"FIXED","discountValue":500,"priority":42}
                """;
        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                url("/api/v1/discount-policies"), HttpMethod.POST, jsonRequest(body), MAP_TYPE);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getHeaders().getLocation()).isNotNull();
        assertThat(resp.getBody().get("discountType")).isEqualTo("FIXED");
        assertThat(((Number) resp.getBody().get("priority")).intValue()).isEqualTo(42);
    }

    @Test
    @DisplayName("POST /api/v1/discount-policies 생성 → FIXED 에 소수 값이면 400 INVALID_REQUEST")
    void createFixedRejectsFractional() {
        String body = """
                {"name":"잘못된 FIXED","targetGrade":"NORMAL","targetPaymentMethod":null,
                 "discountType":"FIXED","discountValue":1500.5,"priority":10}
                """;
        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                url("/api/v1/discount-policies"), HttpMethod.POST, jsonRequest(body), MAP_TYPE);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().get("code")).isEqualTo("INVALID_REQUEST");
    }

    @Test
    @DisplayName("POST /{id}/deactivate 가 expectedVersion 불일치면 409 POLICY_VERSION_CONFLICT")
    void deactivateConflict() {
        String createBody = """
                {"name":"비활성 대상","targetGrade":"NORMAL","targetPaymentMethod":null,
                 "discountType":"FIXED","discountValue":100,"priority":99}
                """;
        ResponseEntity<Map<String, Object>> created = rest.exchange(
                url("/api/v1/discount-policies"), HttpMethod.POST, jsonRequest(createBody), MAP_TYPE);
        Long policyId = ((Number) created.getBody().get("id")).longValue();

        String deactivateBody = """
                {"expectedVersion":99999}
                """;
        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                url("/api/v1/discount-policies/" + policyId + "/deactivate"),
                HttpMethod.POST, jsonRequest(deactivateBody), MAP_TYPE);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resp.getBody().get("code")).isEqualTo("POLICY_VERSION_CONFLICT");
        assertThat(((Number) resp.getBody().get("policyId")).longValue()).isEqualTo(policyId);
    }

    @Test
    @DisplayName("GET /api/v1/discount-policies → 200 + 정확히 시드 3개 정책 (cleanup 직후)")
    void readAll() {
        ParameterizedTypeReference<List<Map<String, Object>>> listOfMap =
                new ParameterizedTypeReference<>() {
                };
        ResponseEntity<List<Map<String, Object>>> resp = rest.exchange(
                url("/api/v1/discount-policies"), HttpMethod.GET, jsonRequest(null), listOfMap);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        List<String> names = resp.getBody().stream()
                .map(p -> (String) p.get("name"))
                .toList();
        assertThat(names).hasSameElementsAs(TestSeed.POLICY_NAMES);
    }
}
