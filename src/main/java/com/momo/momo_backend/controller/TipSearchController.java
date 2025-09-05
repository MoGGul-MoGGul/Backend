package com.momo.momo_backend.controller;

import com.momo.momo_backend.dto.TipDto;
import com.momo.momo_backend.security.CustomUserDetails;
import com.momo.momo_backend.service.TipSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search/tips")
@RequiredArgsConstructor
public class TipSearchController {

    private final TipSearchService tipSearchService;

    // 전체 보관함 검색 — 인증 불필요, public 꿀팁만
    @GetMapping("/public")
    public ResponseEntity<List<TipDto.DetailResponse>> searchPublic(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "OR") String mode, // OR | AND
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(tipSearchService.searchPublic(keyword, mode, page, size));
    }

    //  내 보관함 검색 — 인증 필요
    @GetMapping("/my")
    public ResponseEntity<List<TipDto.DetailResponse>> searchMy(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "OR") String mode, // OR | AND
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userNo = userDetails.getUser().getNo();
        return ResponseEntity.ok(tipSearchService.searchMy(userNo, keyword, mode, page, size));
    }

    // 특정 그룹 검색 — 그룹 멤버만
    @GetMapping("/group/{groupNo}")
    public ResponseEntity<List<TipDto.DetailResponse>> searchGroup(
            @PathVariable Long groupNo,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "OR") String mode, // OR | AND
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userNo = userDetails.getUser().getNo();
        return ResponseEntity.ok(tipSearchService.searchGroup(groupNo, userNo, keyword, mode, page, size));
    }

    // 특정 그룹 검색 — 그룹 멤버만
    @GetMapping("/storage/{storageNo}")
    public ResponseEntity<List<TipDto.DetailResponse>> searchStorage(
            @PathVariable Long storageNo,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "OR") String mode, // OR | AND
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userNo = userDetails.getUser().getNo();
        return ResponseEntity.ok(tipSearchService.searchStorage(storageNo, userNo, keyword, mode, page, size));
    }
}
