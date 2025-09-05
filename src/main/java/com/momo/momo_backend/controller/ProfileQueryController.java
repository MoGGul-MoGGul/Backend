package com.momo.momo_backend.controller;

import com.momo.momo_backend.dto.ErrorResponse;
import com.momo.momo_backend.dto.ProfileDto;
import com.momo.momo_backend.security.CustomUserDetails;
import com.momo.momo_backend.service.ProfileQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileQueryController {

    private final ProfileQueryService profileQueryService;

    @GetMapping("/{userNo}")
    public ResponseEntity<?> getUserProfile(
            @PathVariable Long userNo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // 로그인한 사용자 본인의 userNo를 서비스에 두 번째 인자로 전달합니다.
            ProfileDto.DetailResponse response = profileQueryService.getUserProfile(userNo, userDetails.getUser().getNo());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}