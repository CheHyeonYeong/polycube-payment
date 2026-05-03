package com.cube.member.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import com.cube.member.entity.MemberGrade;

public record MemberCreateRequest(
        @NotBlank(message = "이름은 비어 있을 수 없습니다.")
        String name,
        @NotNull(message = "회원 등급은 필수입니다.")
        MemberGrade grade,
        @PositiveOrZero(message = "포인트는 0 이상이어야 합니다.")
        long point
) {
}
