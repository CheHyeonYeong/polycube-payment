package com.cube.payment.member;

import com.cube.payment.common.exception.CustomException;
import com.cube.payment.common.exception.ErrorCode;
import com.cube.payment.member.entity.MemberGrade;
import com.cube.payment.member.response.MemberResponse;
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

@WebMvcTest(MemberController.class)
@DisplayName("회원 컨트롤러 슬라이스 테스트")
class MemberControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean MemberService memberService;

    @Test
    @DisplayName("회원 가입 요청이 성공하면 201 상태코드와 회원 정보를 반환한다")
    void 회원_가입_201_반환() throws Exception {
        MemberResponse response = new MemberResponse(1L, "홍길동", MemberGrade.VIP);
        given(memberService.create(any())).willReturn(response);

        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "홍길동", "grade", "VIP"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.grade").value("VIP"));
    }

    @Test
    @DisplayName("이름 없이 가입 요청 시 400 상태코드를 반환한다")
    void 이름_없는_가입_요청_400_반환() throws Exception {
        mockMvc.perform(post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("grade", "VIP"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("회원 단건 조회 요청이 성공하면 200 상태코드와 회원 정보를 반환한다")
    void 회원_단건_조회_200_반환() throws Exception {
        MemberResponse response = new MemberResponse(1L, "김철수", MemberGrade.NORMAL);
        given(memberService.findById(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.grade").value("NORMAL"));
    }

    @Test
    @DisplayName("존재하지 않는 회원 조회 시 404 상태코드를 반환한다")
    void 존재하지_않는_회원_조회_404_반환() throws Exception {
        given(memberService.findById(999L)).willThrow(new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        mockMvc.perform(get("/api/v1/members/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }
}
