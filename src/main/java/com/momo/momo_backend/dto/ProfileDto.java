package com.momo.momo_backend.dto;

import com.momo.momo_backend.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 프로필 관련 DTO를 모아두는 컨테이너 클래스
 */
public final class ProfileDto {

    private ProfileDto() {}

    // 프로필 수정 요청 DTO
    @Getter
    @Setter
    public static class UpdateRequest {
        private String nickname;
    }

    // 프로필 수정 응답 DTO
    @Getter
    @Builder
    public static class UpdateResponse {
        private String message;
        private String profileImageUrl;
    }

   // 프로필 상세 조회 응답 DTO
    @Getter
    @Builder
    public static class DetailResponse {
        private Long userNo;
        private String loginId;
        private String nickname;
        private String profileImageUrl;
        private Long followerCount;
        private Long followingCount;
        private Long totalBookmarkCount;
        private Boolean isFollowing;

        public static DetailResponse from(User user, Long followerCount, Long followingCount, Long totalBookmarkCount, Boolean isFollowing) {
            return DetailResponse.builder()
                    .userNo(user.getNo())
                    .loginId(user.getLoginId())
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImage())
                    .followerCount(followerCount)
                    .followingCount(followingCount)
                    .totalBookmarkCount(totalBookmarkCount)
                    .isFollowing(isFollowing)
                    .build();
        }
    }
}