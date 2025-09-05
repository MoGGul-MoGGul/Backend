package com.momo.momo_backend.controller;

import com.momo.momo_backend.dto.BookmarkAndSaveRequest;
import com.momo.momo_backend.dto.ErrorResponse;
import com.momo.momo_backend.dto.MessageResponse;
import com.momo.momo_backend.security.CustomUserDetails;
import com.momo.momo_backend.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/bookmark")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // 즐겨찾기 발생 시 알람용
    @PostMapping("/{tipNo}")
    public ResponseEntity<Void> bookmarkTip(@PathVariable Long tipNo,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        bookmarkService.addBookmark(tipNo, userDetails.getUser());
        return ResponseEntity.ok().build();
    }

    // 북마크 생성
    @PostMapping
    public ResponseEntity<Object> bookmarkAndSaveTip(@RequestBody BookmarkAndSaveRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            bookmarkService.bookmarkAndSaveTip(request, userDetails.getUser().getNo());
            MessageResponse response = MessageResponse.builder()
                    .message("꿀팁을 즐겨찾기하고 보관함에 저장했습니다.")
                    .build();
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.FORBIDDEN.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }


    // 북마크 삭제
    @DeleteMapping("/{bookmarkNo}")
    public ResponseEntity<Object> deleteBookmark(@PathVariable Long bookmarkNo,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            bookmarkService.delete(bookmarkNo, userDetails.getUser().getNo());
            MessageResponse response = MessageResponse.builder()
                    .message("즐겨찾기를 취소했습니다.")
                    .build();
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.FORBIDDEN.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
    }
}