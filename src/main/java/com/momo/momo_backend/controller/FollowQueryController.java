package com.momo.momo_backend.controller;

import com.momo.momo_backend.dto.FollowResponse;
import com.momo.momo_backend.dto.FollowingResponse;
import com.momo.momo_backend.security.CustomUserDetails;
import com.momo.momo_backend.service.FollowQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowQueryController {

    private final FollowQueryService followQueryService;

    // 특정 사용자의 팔로워 목록 조회
    @GetMapping("/{userNo}/followers")
    public ResponseEntity<List<FollowResponse>> getFollowers(
            @PathVariable Long userNo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // userNo: 조회하고 싶은 대상의 ID
        // userDetails: 로그인한 '나'의 정보 (isFollowing 계산용)
        List<FollowResponse> followers = followQueryService.getFollowers(userNo, userDetails.getUser().getNo());
        return ResponseEntity.ok(followers);
    }

    // 특정 사용자의 팔로잉 목록 조회
    @GetMapping("/{userNo}/followings")
    public ResponseEntity<List<FollowingResponse>> getFollowings(
            @PathVariable Long userNo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // userNo: 조회하고 싶은 대상의 ID
        // userDetails: 로그인한 '나'의 정보 (isFollowing 계산용)
        List<FollowingResponse> followings = followQueryService.getFollowings(userNo, userDetails.getUser().getNo());
        return ResponseEntity.ok(followings);
    }
}