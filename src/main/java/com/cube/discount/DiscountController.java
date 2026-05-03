package com.cube.discount;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cube.discount.request.DiscountPolicyCreateRequest;
import com.cube.discount.request.DiscountPolicyDeactivateRequest;
import com.cube.discount.request.DiscountPolicyUpdateRequest;
import com.cube.discount.response.DiscountPolicyResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/discount-policies")
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping
    public List<DiscountPolicyResponse> readAll() {
        return discountService.readAll();
    }

    @PostMapping
    public ResponseEntity<DiscountPolicyResponse> create(@Valid @RequestBody DiscountPolicyCreateRequest request) {
        DiscountPolicyResponse response = discountService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/discount-policies/" + response.id())).body(response);
    }

    @PatchMapping("/{policyId}")
    public DiscountPolicyResponse update(@PathVariable Long policyId,
                                         @Valid @RequestBody DiscountPolicyUpdateRequest request) {
        return discountService.updateValue(policyId, request);
    }

    @PostMapping("/{policyId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long policyId,
                                           @Valid @RequestBody DiscountPolicyDeactivateRequest request) {
        discountService.deactivate(policyId, request.expectedVersion());
        return ResponseEntity.noContent().build();
    }
}
