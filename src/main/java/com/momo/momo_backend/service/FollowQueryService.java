package com.momo.momo_backend.service;

import com.momo.momo_backend.dto.FollowResponse;
import com.momo.momo_backend.entity.User;
import com.momo.momo_backend.repository.FollowRepository;
import com.momo.momo_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowQueryService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    // 나를 팔로우하는 사용자 목록(팔로워) 조회
        // userNo: 조회 대상, myUserNo: 로그인한 사용자
    public List<FollowResponse> getFollowers(Long userNo, Long myUserNo) {
        if (!userRepository.existsById(userNo)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        List<User> followers = followRepository.findFollowersByUserNo(userNo);
        return followers.stream()
                .map(follower -> {
                    Boolean isFollowing = follower.getNo().equals(myUserNo) ? null :
                            followRepository.existsByFollower_NoAndFollowing_No(myUserNo, follower.getNo());
                    return FollowResponse.from(follower, isFollowing);
                })
                .collect(Collectors.toList());
    }

    // 내가 팔로우하는 사용자 목록(팔로잉) 조회
        // userNo: 조회 대상, myUserNo: 로그인한 사용자
    public List<FollowResponse> getFollowings(Long userNo, Long myUserNo) {
        if (!userRepository.existsById(userNo)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        List<User> followings = followRepository.findFollowingByUserNo(userNo);
        return followings.stream()
                .map(following -> {
                    Boolean isFollowing = following.getNo().equals(myUserNo) ? null :
                            followRepository.existsByFollower_NoAndFollowing_No(myUserNo, following.getNo());
                    return FollowResponse.from(following, isFollowing);
                })
                .collect(Collectors.toList());
    }
}