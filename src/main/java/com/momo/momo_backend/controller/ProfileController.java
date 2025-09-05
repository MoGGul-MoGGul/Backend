package com.momo.momo_backend.controller;

import com.momo.momo_backend.dto.ErrorResponse;
import com.momo.momo_backend.dto.ProfileDto;
import com.momo.momo_backend.exception.ProfileImageUploadException;
import com.momo.momo_backend.security.CustomUserDetails;
import com.momo.momo_backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // 프로필 수정
    @PutMapping
    public ResponseEntity<Object> updateUserProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        try {
            ProfileDto.UpdateResponse response = profileService.updateProfile(userDetails.getUser().getNo(), nickname, imageFile);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build());
        } catch (ProfileImageUploadException e) { // <-- 새로운 catch 블록 추가
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build());
        }
    }
}