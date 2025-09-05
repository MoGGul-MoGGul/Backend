package com.momo.momo_backend.controller;

import com.momo.momo_backend.dto.ErrorResponse;
import com.momo.momo_backend.dto.MessageResponse;
import com.momo.momo_backend.dto.FollowDto;
import com.momo.momo_backend.security.CustomUserDetails;
import com.momo.momo_backend.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    // 팔로우 요청 처리
    @PostMapping
    public ResponseEntity<Object> followUser(
            @RequestBody FollowDto.Request request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // followerId는 토큰에서, followeeId는 요청 본문에서 가져옴
            followService.followUser(userDetails.getUser().getNo(), request.getFolloweeId());
            MessageResponse response = MessageResponse.builder()
                    .message(request.getFolloweeId() + "님을 팔로우했습니다.")
                    .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // 언팔로우 요청 처리
    @DeleteMapping
    public ResponseEntity<Object> unfollowUser(
            @RequestBody FollowDto.Request request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            followService.unfollowUser(userDetails.getUser().getNo(), request.getFolloweeId());
            MessageResponse response = MessageResponse.builder()
                    .message(request.getFolloweeId() + "님을 언팔로우했습니다.")
                    .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
