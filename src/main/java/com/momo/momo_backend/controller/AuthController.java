package com.momo.momo_backend.controller;

import com.momo.momo_backend.dto.ErrorResponse;
import com.momo.momo_backend.dto.AuthDto;
import com.momo.momo_backend.security.CustomUserDetails;
import com.momo.momo_backend.security.JwtTokenProvider;
import com.momo.momo_backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public record ResetPwRequest(String id, String nickname, String newPassword) {}
    public record FindIdRequest(String nickname, String password) {}

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody AuthDto.SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<AuthDto.LoginResponse> login(@RequestBody AuthDto.LoginRequest request,
                                                       HttpServletResponse response) {
        AuthDto.LoginResponse body = authService.login(request);

        // Refresh 토큰을 HttpOnly 쿠키로 심어줌
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", body.getRefreshToken())
                .httpOnly(true)
                .secure(true)          // 로컬 http 일 경우 false 로 바꿔도 됨
                .path("/")             // 필요하면 /api/auth 로 좁힐 수 있음
                .sameSite("None")      // 프론트가 다른 포트/도메인이면 None 권장
                .maxAge(60L * 60 * 24 * 14) // 예시 14일; JWT 만료와 맞추면 좋음
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", refreshCookie.toString())
                .body(body);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader,
                                       HttpServletResponse response) {
        authService.logout(authorizationHeader);

        // RT 쿠키 삭제
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", deleteCookie.toString())
                .build();
    }

    // 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request,
                                     HttpServletResponse response) {
        // 1) 쿠키 우선
        String refreshFromCookie = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("refreshToken".equals(c.getName())) {
                    refreshFromCookie = c.getValue();
                    break;
                }
            }
        }
        // 2) 없으면 Authorization 헤더 (Bearer …)
        String refreshToken = (refreshFromCookie != null)
                ? refreshFromCookie
                : jwtTokenProvider.resolveToken(request);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message("리프레시 토큰이 없습니다.")
                            .error("MissingToken")
                            .build());
        }

        try {
            AuthDto.LoginResponse newTokens = authService.refresh(refreshToken);

            // 새 RT로 쿠키 갱신
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newTokens.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(60L * 60 * 24 * 14)
                    .build();

            return ResponseEntity.ok()
                    .header("Set-Cookie", refreshCookie.toString())
                    .body(newTokens);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message(e.getMessage())
                            .error(e.getClass().getSimpleName())
                            .build());
        }
    }

    // 아이디 중복 체크
    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkId(@RequestParam String id) {
        return ResponseEntity.ok(authService.checkIdExists(id));
    }

    // 닉네임 중복 체크
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(authService.checkNicknameExists(nickname));
    }

    // 회원탈퇴
    @DeleteMapping("/withdrawal")
    public ResponseEntity<Void> withdrawal(@AuthenticationPrincipal CustomUserDetails principal) {
        authService.withdraw(principal.getUser().getNo());
        return ResponseEntity.noContent().build();
    }

    // 아이디 찾기
    @PostMapping("/find-id")
    public ResponseEntity<String> findId(@RequestBody FindIdRequest req) {
        return ResponseEntity.ok(authService.findId(req.nickname(), req.password()));
    }

    // 비밀번호 재설정
    @PostMapping("/reset-pw")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPwRequest req) {
        try {
            authService.resetPassword(req.id(), req.nickname(), req.newPassword());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(e.getMessage())
                            .error(e.getClass().getSimpleName())
                            .build());
        }
    }

    // 관리자용: userNo로 회원탈퇴 처리
    @DeleteMapping("/withdraw-by-no/{userNo}")
    public ResponseEntity<Void> withdrawByUserNo(@PathVariable Long userNo) {
        try {
            authService.withdraw(userNo);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // 사용자가 존재하지 않는 경우 등 예외 처리
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}