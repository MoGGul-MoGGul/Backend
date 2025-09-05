package com.momo.momo_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인증 관련 DTO를 모아두는 컨테이너 클래스
 */
public final class AuthDto {

    // 인스턴스화 방지
    private AuthDto() {}

    // 회원가입 요청 DTO
    @Getter
    @NoArgsConstructor
    public static class SignupRequest {
        private String id;
        private String password;
        private String nickname;
    }

    // 로그인 요청 DTO
    @Getter
    @NoArgsConstructor
    public static class LoginRequest {
        private String id;
        private String password;
    }

    // 로그인 / 토큰 갱신 응답 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private Long userNo;

        public LoginResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.userNo = null;
        }
    }
}
