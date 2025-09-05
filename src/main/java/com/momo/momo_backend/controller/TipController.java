package com.momo.momo_backend.controller;

import com.momo.momo_backend.dto.ErrorResponse;
import com.momo.momo_backend.dto.MessageResponse;
import com.momo.momo_backend.dto.TipDto;
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

    // 꿀팁 생성 (AI 정보 미리보기)
    @PostMapping("/generate")
    public ResponseEntity<TipDto.CreateResponse> createTip(
            @RequestBody TipDto.CreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        String who = (user != null) ? user.getUsername() : "anonymous";
        log.info("꿀팁 생성 요청 by={}, url={}, title(pre):{}, tags(pre):{}",
                who, request.getUrl(), request.getTitle(), request.getTags());
        try {
            TipDto.CreateResponse response = tipService.createTip(request);
            log.info("꿀팁 생성 미리보기 완료 title={}, tags={}", response.getTitle(), response.getTags());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("꿀팁 생성 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 꿀팁 등록(저장)
    @PostMapping("/register")
    public ResponseEntity<TipDto.DetailResponse> registerTip(
            @RequestBody @Valid TipDto.RegisterRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userNo = userDetails.getUser().getNo();

        // DTO 스펙에 맞춘 로깅(= tipId/tipNo/storageId 없음)
        log.info("꿀팁 등록 요청 userNo={}, storageNo={}, isPublic={}, url={}, title={}, tags={}",
                userNo, request.getStorageNo(), request.getIsPublic(),
                request.getUrl(), request.getTitle(), request.getTags());

        TipDto.DetailResponse response = tipService.registerTip(request, userNo);
        log.info("꿀팁 등록 완료: tipNo={}, storageNo={}, public={}, title={}",
                response.getNo(), response.getStorageNo(), response.getIsPublic(), response.getTitle());
        return ResponseEntity.ok(response);
}

    // 꿀팁 수정
    @PutMapping("/{no}")
    public ResponseEntity<TipDto.DetailResponse> update(
            @PathVariable Long no,
            @Valid @RequestBody TipDto.UpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userNo = userDetails.getUser().getNo();
        return ResponseEntity.ok(tipService.update(no, userNo, request));
    }

    // 꿀팁 삭제
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
