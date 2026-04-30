package com.cube.payment.member;

import com.cube.payment.common.exception.CustomException;
import com.cube.payment.common.exception.ErrorCode;
import com.cube.payment.member.entity.MemberGrade;
import com.cube.payment.member.request.MemberCreateRequest;
import com.cube.payment.member.response.MemberResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("회원 서비스 통합 테스트")
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("회원 가입 시 이름과 등급이 정상 저장된다")
    void 회원_가입_성공() {
        MemberCreateRequest request = createRequest("홍길동", MemberGrade.VIP);

        MemberResponse response = memberService.create(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getGrade()).isEqualTo(MemberGrade.VIP);
    }

    @Test
    @DisplayName("NORMAL 등급으로 회원 가입이 가능하다")
    void NORMAL_등급_회원_가입() {
        MemberCreateRequest request = createRequest("김철수", MemberGrade.NORMAL);

        MemberResponse response = memberService.create(request);

        assertThat(response.getGrade()).isEqualTo(MemberGrade.NORMAL);
    }

    @Test
    @DisplayName("VVIP 등급으로 회원 가입이 가능하다")
    void VVIP_등급_회원_가입() {
        MemberCreateRequest request = createRequest("박지성", MemberGrade.VVIP);

        MemberResponse response = memberService.create(request);

        assertThat(response.getGrade()).isEqualTo(MemberGrade.VVIP);
    }

    @Test
    @DisplayName("가입한 회원을 ID로 조회할 수 있다")
    void 회원_단건_조회_성공() {
        MemberResponse saved = memberService.create(createRequest("이영희", MemberGrade.VIP));

        MemberResponse found = memberService.findById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("이영희");
        assertThat(found.getGrade()).isEqualTo(MemberGrade.VIP);
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 조회하면 MEMBER_NOT_FOUND 예외가 발생한다")
    void 존재하지_않는_회원_조회_예외() {
        assertThatThrownBy(() -> memberService.findById(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_NOT_FOUND));
    }

    private MemberCreateRequest createRequest(String name, MemberGrade grade) {
        MemberCreateRequest request = new MemberCreateRequest();
        setField(request, "name", name);
        setField(request, "grade", grade);
        return request;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
