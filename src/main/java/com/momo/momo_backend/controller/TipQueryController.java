package com.momo.momo_backend.controller;

import com.momo.momo_backend.dto.ErrorResponse;
import com.momo.momo_backend.entity.Tip;
import com.momo.momo_backend.dto.TipDto;
import com.momo.momo_backend.service.TipQueryService;
import com.momo.momo_backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/query/tips")
@RequiredArgsConstructor
public class TipQueryController {

    private final TipQueryService tipQueryService;

    // 사용자가 작성한 팁 조회 (등록된 팁만) - 토큰 필요
    @GetMapping("/my")
    public ResponseEntity<List<TipDto.DetailResponse>> getMyTips(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getNo();
        List<Tip> tips = tipQueryService.getTipsByUser(userId);
        List<TipDto.DetailResponse> responseList = tips.stream()
                .map(TipDto.DetailResponse::from)
                .toList();
        return ResponseEntity.ok(responseList);
    }

    // 공개된 팁 목록 조회 (등록된 팁만) - 토큰 불필요
    @GetMapping("/all")
    public ResponseEntity<List<TipDto.DetailResponse>> getAllPublicTips() {
        List<Tip> tips = tipQueryService.getAllPublicTips();
        List<TipDto.DetailResponse> responseList = tips.stream()
                .map(TipDto.DetailResponse::from)
                .toList();
        return ResponseEntity.ok(responseList);
    }

    // 특정 보관함에 속한 팁 조회 (등록된 팁만) - 토큰 필요
    @GetMapping("/storage/{storageNo}")
    public ResponseEntity<List<TipDto.DetailResponse>> getTipsByStorage(@PathVariable Long storageNo,
                                                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Tip> tips = tipQueryService.getTipsByStorage(storageNo);
        List<TipDto.DetailResponse> responseList = tips.stream()
                .map(TipDto.DetailResponse::from)
                .toList();
        return ResponseEntity.ok(responseList);
    }

    // 상세 팁 조회 - 토큰 불필요
    @GetMapping("/{tipNo}")
    public ResponseEntity<TipDto.DetailResponse> getTipDetails(@PathVariable Long tipNo) {
        Tip tip = tipQueryService.getTipDetails(tipNo);
        TipDto.DetailResponse response = TipDto.DetailResponse.from(tip);
        return ResponseEntity.ok(response);
    }

    // 특정 사용자의 공개 꿀팁 목록 조회 - 토큰 불필요
    @GetMapping("/user/{userNo}")
    public ResponseEntity<Object> getPublicTipsByUser(@PathVariable Long userNo) {
        try {
            List<Tip> tips = tipQueryService.getPublicTipsByUser(userNo);
            List<TipDto.DetailResponse> responseList = tips.stream()
                    .map(TipDto.DetailResponse::from)
                    .toList();
            return ResponseEntity.ok(responseList);
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
