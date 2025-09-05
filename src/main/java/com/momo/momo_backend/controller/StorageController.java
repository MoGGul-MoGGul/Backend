package com.momo.momo_backend.controller;

import com.momo.momo_backend.dto.ErrorResponse;
import com.momo.momo_backend.dto.MessageResponse;
import com.momo.momo_backend.dto.StorageDto;
import com.momo.momo_backend.entity.Storage;
import com.momo.momo_backend.security.CustomUserDetails;
import com.momo.momo_backend.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Slf4j
public class StorageController {

    private final StorageService storageService;

    // 보관함 생성
    @PostMapping
    public ResponseEntity<StorageDto.Response> create(
            @RequestBody StorageDto.CreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 로그인한 사용자의 정보를 사용
        Long loginUserNo = userDetails.getUser().getNo();
        Storage created = storageService.create(request, loginUserNo);

        StorageDto.Response response = StorageDto.Response.builder()
                .storageNo(created.getNo())
                .name(created.getName())
                .userNo(created.getUser().getNo())
                .build();

        return ResponseEntity.ok(response);
    }

    // 보관함 수정
    @PutMapping("/{storageNo}")
    public ResponseEntity<?> updateStorage( // 반환 타입을 와일드카드로 변경하여 ErrorResponse도 반환 가능하게 함
        @PathVariable Long storageNo,
        @RequestBody StorageDto.UpdateRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("보관함 수정 요청 - storageNo: {}, 사용자: {}, 권한: {}",
                storageNo,
                userDetails.getUsername(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        try {
            Long loginUserNo = userDetails.getUser().getNo();
            Storage updatedStorage = storageService.update(storageNo, loginUserNo, request);
            log.info("보관함 수정 성공");

            StorageDto.Response response = StorageDto.Response.builder()
                    .storageNo(updatedStorage.getNo())
                    .name(updatedStorage.getName())
                    .userNo(updatedStorage.getUser().getNo())
                    .build();

            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            log.error("보관함 수정 권한 없음: {}", e.getMessage());
            // AccessDeniedException 발생 시 ErrorResponse DTO를 반환
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.FORBIDDEN.value()) // 403 상태 코드
                    .message(e.getMessage()) // 예외 메시지 포함
                    .error(e.getClass().getSimpleName()) // 예외 클래스 이름
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
    }

    // 보관함 삭제
    @DeleteMapping("/{storageNo}")
    public ResponseEntity<?> deleteStorage( // 반환 타입을 ResponseEntity<?>로 변경
                                            @PathVariable Long storageNo,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long loginUserNo = userDetails.getUser().getNo();
            storageService.delete(storageNo, loginUserNo);
            log.info("보관함 삭제 성공: storageNo {}", storageNo);

            MessageResponse response = MessageResponse.builder()
                    .message("보관함 삭제 완료.")
                    .build();
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            log.error("보관함 삭제 권한 없음: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.FORBIDDEN.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (IllegalArgumentException e) {
            log.error("보관함 삭제 실패: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}
