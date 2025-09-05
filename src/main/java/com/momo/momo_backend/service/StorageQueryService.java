package com.momo.momo_backend.service;

import com.momo.momo_backend.entity.Group;
import com.momo.momo_backend.entity.GroupMember;
import com.momo.momo_backend.entity.Storage;
import com.momo.momo_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.momo.momo_backend.entity.User;


// 보관함 조회 전문 서비스

@Service
@RequiredArgsConstructor
public class StorageQueryService {

    private final StorageRepository storageRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public List<Storage> findByUser(Long userNo) {

        // 2. 사용자가 존재하면 해당 사용자의 보관함 목록 조회
        // 개인 보관함만 조회하도록 필터링 조건 추가
        return storageRepository.findAllByUser_NoAndGroupIsNull(userNo);
    }

    // 그룹 보관함 목록 조회
    public List<Storage> findGroupStoragesByGroup(Long groupNo, Long requestingUserNo) {
        // 1. 그룹 존재 여부 확인
        Group group = groupRepository.findById(groupNo)
                .orElseThrow(() -> new IllegalArgumentException("그룹이 존재하지 않습니다."));

        // 2. 요청하는 사용자 존재 여부 확인
        User requestingUser = userRepository.findById(requestingUserNo)
                .orElseThrow(() -> new IllegalArgumentException("요청하는 사용자가 존재하지 않습니다."));

        // 3. 요청하는 사용자가 해당 그룹의 멤버인지 확인
        boolean isMember = groupMemberRepository.existsByGroupAndUser(group, requestingUser);
        if (!isMember) {
            throw new AccessDeniedException("그룹 멤버만 그룹 보관함 목록을 조회할 수 있습니다.");
        }

        // 4. 해당 그룹에 속한 모든 보관함 조회
        return storageRepository.findAllByGroup_No(groupNo);
    }

    // 사용자가 속한 모든 그룹의 보관함 조회
    public List<Storage> findStoragesForUserGroups(Long userNo) {
        // 1. 사용자 존재 여부 확인
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 사용자가 멤버로 속한 모든 그룹을 조회합니다.
        List<Group> userGroups = groupMemberRepository.findAllByUser_No(user.getNo())
                .stream()
                .map(GroupMember::getGroup)
                .toList();

        // 3. 사용자가 속한 각 그룹의 보관함을 조회합니다.
        return userGroups.stream()
                .flatMap(group -> storageRepository.findAllByGroup_No(group.getNo()).stream())
                .toList();
    }
}