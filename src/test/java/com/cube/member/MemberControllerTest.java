package com.cube.member;

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

import com.cube.member.entity.Member;
import com.cube.support.TestSeed;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MemberControllerTest {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    MemberRepository memberRepository;

    private HttpEntity<String> jsonRequest(String body) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, h);
    }

    @AfterEach
    void cleanupNonSeedMembers() {
        List<Member> nonSeed = memberRepository.findAll().stream()
                .filter(m -> !TestSeed.MEMBER_NAMES.contains(m.getName()))
                .toList();
        memberRepository.deleteAll(nonSeed);
    }

    @Test
    @DisplayName("POST /api/v1/members → 201 + Location, GET 으로 동일 데이터 조회")
    void createAndRead() {
        String body = """
                {"name":"신규","grade":"VIP","point":3000}
                """;
        ResponseEntity<Map<String, Object>> created = rest.exchange(
                "http://localhost:" + port + "/api/v1/members",
                HttpMethod.POST, jsonRequest(body), MAP_TYPE);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getHeaders().getLocation()).isNotNull();
        Long id = ((Number) created.getBody().get("id")).longValue();
        assertThat(created.getBody().get("grade")).isEqualTo("VIP");
        assertThat(((Number) created.getBody().get("point")).longValue()).isEqualTo(3000L);

        ResponseEntity<Map<String, Object>> read = rest.exchange(
                "http://localhost:" + port + "/api/v1/members/" + id,
                HttpMethod.GET, jsonRequest(null), MAP_TYPE);
        assertThat(read.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(read.getBody().get("name")).isEqualTo("신규");
    }

    @Test
    @DisplayName("GET /api/v1/members/{없는 id} → 404 + MEMBER_NOT_FOUND ADT 페이로드")
    void notFoundReturnsAdtPayload() {
        long missing = Long.MAX_VALUE;
        ResponseEntity<Map<String, Object>> resp = rest.exchange(
                "http://localhost:" + port + "/api/v1/members/" + missing,
                HttpMethod.GET, jsonRequest(null), MAP_TYPE);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody().get("code")).isEqualTo("MEMBER_NOT_FOUND");
        assertThat(((Number) resp.getBody().get("memberId")).longValue()).isEqualTo(missing);
    }
}
