package com.buzz.fortyall_desk.account.controller;

import com.buzz.fortyall_desk.account.service.MemberService;
import com.buzz.fortyall_desk.account.dto.MemberDto.*;
import com.buzz.fortyall_desk.common.dto.ApiResponse;
import com.buzz.fortyall_desk.pass.dto.PassDto.PassView;
import com.buzz.fortyall_desk.pass.service.PassService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {
    private final MemberService memberService;
    private final PassService passService;

    @PostMapping
    public ApiResponse<MemberView> create(@Valid @RequestBody CreateRequest req) {
        return ApiResponse.of(memberService.create(req));
    }

    @GetMapping
    public ApiResponse<List<MemberView>> list() {
        return ApiResponse.of(memberService.list());
    }

    @GetMapping("/{membershipId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long membershipId) {
        MemberView member = memberService.get(membershipId);
        List<PassView> passes = passService.listByMembership(membershipId);
        return ApiResponse.of(Map.of("member", member, "passes", passes));
    }

    @GetMapping("/{membershipId}/passes")
    public ApiResponse<List<PassView>> passes(@PathVariable Long membershipId) {
        return ApiResponse.of(passService.listByMembership(membershipId));
    }
}
