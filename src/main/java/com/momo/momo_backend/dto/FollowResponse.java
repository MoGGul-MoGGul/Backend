package com.momo.momo_backend.dto;

import com.momo.momo_backend.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowResponse {
    private Long userNo;
    private String loginId;
    private String nickname;
    private String profileImageUrl;
    private Boolean isFollowing; // 현재 로그인한 사용자가 이 사용자를 팔로우하는지 여부

    public static FollowResponse from(User user, Boolean isFollowing) {
        return FollowResponse.builder()
                .userNo(user.getNo())
                .loginId(user.getLoginId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImage())
                .isFollowing(isFollowing)
                .build();
    }
}