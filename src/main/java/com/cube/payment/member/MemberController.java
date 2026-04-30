package com.cube.payment.member;

import com.cube.payment.common.response.ApiResponse;
import com.cube.payment.member.request.MemberCreateRequest;
import com.cube.payment.member.response.MemberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> create(@RequestBody @Valid MemberCreateRequest request) {
        return ApiResponse.ok(memberService.create(request));
    }

    @GetMapping("/{memberId}")
    public ApiResponse<MemberResponse> findById(@PathVariable Long memberId) {
        return ApiResponse.ok(memberService.findById(memberId));
    }
}
