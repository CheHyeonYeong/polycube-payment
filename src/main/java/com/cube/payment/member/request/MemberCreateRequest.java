package com.cube.payment.member.request;

import com.cube.payment.member.entity.MemberGrade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberCreateRequest {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotNull(message = "등급은 필수입니다.")
    private MemberGrade grade;
}
