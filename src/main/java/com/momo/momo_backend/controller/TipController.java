package com.momo.momo_backend.controller;

import com.momo.momo_backend.dto.*;
import com.momo.momo_backend.security.CustomUserDetails;
import com.momo.momo_backend.service.TipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tips")
@RequiredArgsConstructor
@Slf4j
public class TipController {

    private final TipService tipService;

    /** 꿀팁 생성 (AI 요약 요청만; DB 저장/WS 없음) */
    @PostMapping("/generate")
    public ResponseEntity<TipCreateResponse> createTip(
            @RequestBody TipCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user // 있으면 로깅만
    ) {
        String who = (user != null) ? user.getUsername() : "anonymous";
        log.info("꿀팁 생성 요청 by={}, url={}, title(pre):{}, tags(pre):{}",
                who, request.getUrl(), request.getTitle(), request.getTags());
        try {
            TipCreateResponse response = tipService.createTip(request);
            log.info("꿀팁 생성 미리보기 완료 title={}, tags={}", response.getTitle(), response.getTags());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("꿀팁 생성 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** 꿀팁 최종 등록 — 서비스에서 DB저장 + 태그 upsert + 알림/WS 처리 */
    @PostMapping("/register")
    public ResponseEntity<TipRegisterResponse> registerTip(
            @RequestBody @Valid TipRegisterRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userNo = userDetails.getUser().getNo();

        // DTO 스펙에 맞춘 로깅(= tipId/tipNo/storageId 없음)
        log.info("꿀팁 등록 요청 userNo={}, storageNo={}, isPublic={}, url={}, title={}, tags={}",
                userNo, request.getStorageNo(), request.getIsPublic(),
                request.getUrl(), request.getTitle(), request.getTags());

        TipRegisterResponse response = tipService.registerTip(request, userNo);
        log.info("꿀팁 등록 완료: tipNo={}, storageNo={}, public={}, title={}",
                response.getTipNo(), response.getStorageNo(), response.getIsPublic(), response.getTitle());
        return ResponseEntity.ok(response);
    }

    /** 팁 수정 */
    @PutMapping("/{no}")
    public ResponseEntity<TipResponse> update(
            @PathVariable Long no,
            @Valid @RequestBody TipUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userNo = userDetails.getUser().getNo();
        return ResponseEntity.ok(tipService.update(no, userNo, request));
    }

    /** 팁 삭제 */
    @DeleteMapping("/{no}")
    public ResponseEntity<?> delete(
            @PathVariable Long no,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            Long userNo = userDetails.getUser().getNo();
            tipService.delete(no, userNo);
            log.info("꿀팁 삭제 성공: tipNo={}", no);
            return ResponseEntity.ok(MessageResponse.builder().message("꿀팁 삭제 완료.").build());
        } catch (AccessDeniedException e) {
            log.error("꿀팁 삭제 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ErrorResponse.builder()
                            .status(HttpStatus.FORBIDDEN.value())
                            .message(e.getMessage())
                            .error(e.getClass().getSimpleName())
                            .build()
            );
        } catch (IllegalArgumentException e) {
            log.error("꿀팁 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ErrorResponse.builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(e.getMessage())
                            .error(e.getClass().getSimpleName())
                            .build()
            );
        } catch (Exception e) {
            log.error("꿀팁 삭제 중 예상치 못한 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ErrorResponse.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("꿀팁 삭제 중 오류가 발생했습니다.")
                            .error(e.getClass().getSimpleName())
                            .build()
            );
        }
    }
}
