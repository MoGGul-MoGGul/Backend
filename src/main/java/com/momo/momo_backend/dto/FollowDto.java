package com.momo.momo_backend.dto;

import com.momo.momo_backend.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 팔로우 관련 DTO를 모아두는 컨테이너 클래스
 */
public final class FollowDto {

    private FollowDto() {}

    // 팔로우/언팔로우 요청 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        private String followeeId; // 팔로우/언팔로우할 대상의 로그인 아이디
    }

    // 팔로워/팔로잉/사용자 목록 조회 응답 DTO
    @Getter
    @Builder
    public static class Response {
        private Long userNo;
        private String loginId;
        private String nickname;
        private String profileImageUrl;
        private Boolean isFollowing; // 현재 로그인한 사용자가 이 사용자를 팔로우하는지 여부

        public static Response from(User user, Boolean isFollowing) {
            return Response.builder()
                    .userNo(user.getNo())
                    .loginId(user.getLoginId())
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImage())
                    .isFollowing(isFollowing)
                    .build();
        }
    }
}
