package com.momo.momo_backend.service;

import com.momo.momo_backend.dto.ProfileDto;
import com.momo.momo_backend.entity.User;
import com.momo.momo_backend.repository.BookmarkRepository;
import com.momo.momo_backend.repository.FollowRepository;
import com.momo.momo_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileQueryService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final BookmarkRepository bookmarkRepository;

    // 사용자 프로필 조회
    public ProfileDto.DetailResponse getUserProfile(Long userNo, Long myUserNo) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        long followerCount = followRepository.countByFollowing_No(userNo);
        long followingCount = followRepository.countByFollower_No(userNo);
        long totalBookmarkCount = bookmarkRepository.countByTip_User_No(userNo);

        // 본인 프로필 조회 시 isFollowing은 null, 타인 프로필 조회 시 팔로우 상태 계산
        Boolean isFollowing = userNo.equals(myUserNo) ? null :
                followRepository.existsByFollower_NoAndFollowing_No(myUserNo, userNo);

        return ProfileDto.DetailResponse.from(user, followerCount, followingCount, totalBookmarkCount, isFollowing);
    }
}