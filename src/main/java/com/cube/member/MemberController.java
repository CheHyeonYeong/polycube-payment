package com.cube.member;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cube.member.request.MemberCreateRequest;
import com.cube.member.response.MemberResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberCreateRequest request) {
        MemberResponse response = memberService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/members/" + response.id())).body(response);
    }

    @GetMapping("/{memberId}")
    public MemberResponse read(@PathVariable Long memberId) {
        return memberService.read(memberId);
    }
}
