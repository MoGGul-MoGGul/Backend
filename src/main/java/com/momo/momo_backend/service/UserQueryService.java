package com.momo.momo_backend.service;

import com.momo.momo_backend.dto.FollowDto;
import com.momo.momo_backend.entity.User;
import com.momo.momo_backend.repository.FollowRepository;
import com.momo.momo_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    // 모든 사용자 조회
    public List<FollowDto.Response> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> FollowDto.Response.builder()
                        .userNo(user.getNo())
                        .nickname(user.getNickname())
                        .profileImageUrl(user.getProfileImage())
                        .build())
                .collect(Collectors.toList());
    }

    // 사용자 아이디로 검색
    public List<FollowDto.Response> searchUsersByLoginId(String loginId, Long myUserNo) {
        if (loginId == null || loginId.isBlank()) {
            return Collections.emptyList();
        }
        List<User> users = userRepository.findByLoginIdContainingIgnoreCase(loginId);
        return users.stream()
                .map(user -> {
                    Boolean isFollowing = user.getNo().equals(myUserNo) ? null :
                            followRepository.existsByFollower_NoAndFollowing_No(myUserNo, user.getNo());
                    return FollowDto.Response.from(user, isFollowing);
                })
                .collect(Collectors.toList());
    }
}
