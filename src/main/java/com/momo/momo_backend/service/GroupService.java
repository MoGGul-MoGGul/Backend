package com.momo.momo_backend.service;

import com.momo.momo_backend.dto.GroupDto;
import com.momo.momo_backend.entity.Group;
import com.momo.momo_backend.entity.GroupMember; // GroupMember 임포트
import com.momo.momo_backend.entity.User; // User 임포트
import com.momo.momo_backend.repository.GroupRepository;
import com.momo.momo_backend.repository.GroupMemberRepository; // GroupMemberRepository 임포트
import com.momo.momo_backend.repository.UserRepository; // UserRepository 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Transactional 임포트

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository; // 사용자 정보 조회를 위해 추가
    private final GroupMemberRepository groupMemberRepository; // 그룹 멤버 저장을 위해 추가

    private static final String ERROR_MSG_GROUP_NOT_FOUND = "그룹이 존재하지 않습니다.";
    private static final String ERROR_MSG_USER_NOT_FOUND = "사용자가 존재하지 않습니다.";
    private static final String ERROR_MSG_INVITER_NOT_FOUND = "초대하는 사용자가 존재하지 않습니다.";
    private static final String ERROR_MSG_NOT_A_MEMBER = "그룹 멤버만 다른 사용자를 초대할 수 있습니다.";

    // 그룹 생성
    @Transactional
    public Group createGroup(GroupDto.Request request, Long userNo) {
        // SonarQube: 주석 처리된 코드 블록 제거
        Group group = new Group();
        group.setName(request.getName());
        Group savedGroup = groupRepository.save(group);

        User creator = userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("그룹 생성 사용자가 존재하지 않습니다."));

        GroupMember groupMember = GroupMember.builder()
                .group(savedGroup)
                .user(creator)
                .build();
        groupMemberRepository.save(groupMember);

        return savedGroup;
    }

    // 그룹에 사용자 초대
    @Transactional
    public List<String> inviteMembers(Long groupNo, GroupDto.InviteRequest request, Long inviterUserNo) {
        Group group = groupRepository.findById(groupNo)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_MSG_GROUP_NOT_FOUND));

        User inviter = userRepository.findById(inviterUserNo)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_MSG_INVITER_NOT_FOUND));

        if (!groupMemberRepository.existsByGroupAndUser(group, inviter)) {
            throw new AccessDeniedException(ERROR_MSG_NOT_A_MEMBER);
        }

        List<String> results = new ArrayList<>();
        Set<String> uniqueLoginIds = new LinkedHashSet<>(request.getUserLoginIds());

        for (String loginId : uniqueLoginIds) {
            try {
                User invitedUser = userRepository.findByLoginId(loginId)
                        .orElseThrow(() -> new IllegalArgumentException("사용자 아이디 '" + loginId + "'를 찾을 수 없습니다."));

                if (groupMemberRepository.existsByGroupAndUser(group, invitedUser)) {
                    results.add("사용자 아이디 '" + loginId + "'는 이미 그룹의 멤버입니다.");
                } else {
                    GroupMember newMember = GroupMember.builder()
                            .group(group)
                            .user(invitedUser)
                            .build();
                    groupMemberRepository.save(newMember);
                    results.add("사용자 아이디 '" + loginId + "'가 그룹에 성공적으로 초대되었습니다.");
                }
            } catch (IllegalArgumentException e) {
                results.add("사용자 아이디 '" + loginId + "' 초대 실패: " + e.getMessage());
            } catch (Exception e) {
                results.add("사용자 아이디 '" + loginId + "' 초대 중 알 수 없는 오류 발생: " + e.getMessage());
            }
        }
        return results;
    }

    // 그룹 멤버 나가기
    @Transactional
    public void leaveGroup(Long groupNo, Long userNo) {
        Group group = groupRepository.findById(groupNo)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_MSG_GROUP_NOT_FOUND));

        User userToLeave = userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_MSG_USER_NOT_FOUND));

        GroupMember groupMember = groupMemberRepository.findByGroupAndUser(group, userToLeave)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 이 그룹의 멤버가 아닙니다."));

        groupMemberRepository.delete(groupMember);
    }

    // 그룹 멤버 조회
    @Transactional(readOnly = true)
    public List<User> getGroupMembers(Long groupNo, Long requestingUserNo) {
        Group group = groupRepository.findById(groupNo)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_MSG_GROUP_NOT_FOUND));

        User requestingUser = userRepository.findById(requestingUserNo)
                .orElseThrow(() -> new IllegalArgumentException("요청하는 사용자가 존재하지 않습니다."));

        if (!groupMemberRepository.existsByGroupAndUser(group, requestingUser)) {
            throw new AccessDeniedException("그룹 멤버만 그룹 멤버 목록을 조회할 수 있습니다.");
        }

        List<GroupMember> groupMembers = groupMemberRepository.findByGroup(group);

        return groupMembers.stream()
                .map(GroupMember::getUser)
                .toList();
    }

    // 그룹 목록 조회 (사용자가 속한 그룹)
    @Transactional(readOnly = true)
    public List<GroupDto.ListResponse> getGroupsForUser(Long userNo) {
        List<GroupMember> groupMemberships = groupMemberRepository.findAllByUser_No(userNo);

        return groupMemberships.stream()
                .map(groupMember -> {
                    Group group = groupMember.getGroup();
                    int memberCount = groupMemberRepository.countByGroup(group);
                    return GroupDto.ListResponse.builder()
                            .groupNo(group.getNo())
                            .name(group.getName())
                            .memberCount(memberCount)
                            .build();
                })
                .toList();
    }

    // 그룹명 수정
    @Transactional
    public Group updateGroupName(Long groupNo, GroupDto.Request request, Long requestingUserNo) {
        Group group = groupRepository.findById(groupNo)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_MSG_GROUP_NOT_FOUND));

        User requestingUser = userRepository.findById(requestingUserNo)
                .orElseThrow(() -> new IllegalArgumentException("요청하는 사용자가 존재하지 않습니다."));

        if (!groupMemberRepository.existsByGroupAndUser(group, requestingUser)) {
            throw new AccessDeniedException("그룹 멤버만 그룹명을 수정할 수 있습니다.");
        }

        group.setName(request.getName());
        return groupRepository.save(group);
    }
}
