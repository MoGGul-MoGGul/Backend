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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository; // 사용자 정보 조회를 위해 추가
    private final GroupMemberRepository groupMemberRepository; // 그룹 멤버 저장을 위해 추가

    // 그룹 생성
    @Transactional // 트랜잭션 관리
    public Group createGroup(GroupDto.Request request, Long userNo) {
        // 1. 그룹 이름 중복 확인 (선택 사항이지만, 실제 서비스에서는 고려할 수 있음)
        // if (groupRepository.findByName(request.getName()).isPresent()) {
        //     throw new IllegalArgumentException("이미 존재하는 그룹 이름입니다.");
        // }

        // 2. 그룹 생성
        Group group = new Group();
        group.setName(request.getName());
        // createdAt은 @Column(name = "created_at", updatable = false) 및 @Builder.Default로 자동 설정됨

        Group savedGroup = groupRepository.save(group);

        // 3. 그룹 생성자를 그룹 멤버로 추가
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
        // 1. 그룹 존재 여부 확인
        Group group = groupRepository.findById(groupNo)
                .orElseThrow(() -> new IllegalArgumentException("그룹이 존재하지 않습니다."));

        // 2. 초대하는 사용자가 그룹 멤버인지 확인
        User inviter = userRepository.findById(inviterUserNo)
                .orElseThrow(() -> new IllegalArgumentException("초대하는 사용자가 존재하지 않습니다."));

        boolean inviterIsMember = groupMemberRepository.existsByGroupAndUser(group, inviter);
        if (!inviterIsMember) {
            throw new AccessDeniedException("그룹 멤버만 다른 사용자를 초대할 수 있습니다.");
        }

        List<String> results = new ArrayList<>();
        // userLoginIds 목록을 반복하며 초대 로직 수행
        Set<String> uniqueLoginIds = new LinkedHashSet<>(request.getUserLoginIds()); // 중복 아이디 제거

        for (String loginId : uniqueLoginIds) {
            try {
                // 로그인 아이디로 사용자 찾기
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
    @Transactional // 트랜잭션 관리
    public void leaveGroup(Long groupNo, Long userNo) {
        // 1. 그룹 존재 여부 확인
        Group group = groupRepository.findById(groupNo)
                .orElseThrow(() -> new IllegalArgumentException("그룹이 존재하지 않습니다."));

        // 2. 나갈 사용자 존재 여부 확인
        User userToLeave = userRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        // 3. 사용자가 해당 그룹의 멤버인지 확인
        GroupMember groupMember = groupMemberRepository.findByGroupAndUser(group, userToLeave)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자는 이 그룹의 멤버가 아닙니다."));

        // 4. 그룹 멤버 삭제
        groupMemberRepository.delete(groupMember);
    }

    // 그룹 멤버 조회
    @Transactional(readOnly = true)
    public List<User> getGroupMembers(Long groupNo, Long requestingUserNo) {
        // 1. 그룹 존재 여부 확인
        Group group = groupRepository.findById(groupNo)
                .orElseThrow(() -> new IllegalArgumentException("그룹이 존재하지 않습니다."));

        // 2. 요청하는 사용자가 그룹 멤버인지 확인
        User requestingUser = userRepository.findById(requestingUserNo)
                .orElseThrow(() -> new IllegalArgumentException("요청하는 사용자가 존재하지 않습니다.")); // 인증된 사용자이므로 보통 발생하지 않음

        if (!groupMemberRepository.existsByGroupAndUser(group, requestingUser)) {
            // 그룹 멤버가 아니면 AccessDeniedException 발생
            throw new AccessDeniedException("그룹 멤버만 그룹 멤버 목록을 조회할 수 있습니다.");
        }

        // 3. 해당 그룹의 모든 GroupMember 엔티티 조회
        List<GroupMember> groupMembers = groupMemberRepository.findByGroup(group);

        // 4. GroupMember에서 User 엔티티만 추출하여 반환
        return groupMembers.stream()
                .map(GroupMember::getUser)
                .collect(Collectors.toList());
    }

    // 그룹 목록 조회 (사용자가 속한 그룹)
    @Transactional(readOnly = true)
    public List<GroupDto.ListResponse> getGroupsForUser(Long userNo) {
        // 1. 사용자 존재 여부 확인 (인증된 사용자이므로 보통 발생하지 않음)

        // 2. 해당 사용자가 속한 모든 GroupMember 엔티티 조회
        List<GroupMember> groupMemberships = groupMemberRepository.findAllByUser_No(userNo);

        // 3. 각 GroupMember에서 Group 정보를 추출하고, 해당 그룹의 멤버 수를 계산하여 DTO로 변환
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
                .collect(Collectors.toList());
    }

    // 그룹명 수정
    @Transactional
    public Group updateGroupName(Long groupNo, GroupDto.Request request, Long requestingUserNo) {
        // 1. 그룹 존재 여부 확인
        Group group = groupRepository.findById(groupNo)
                .orElseThrow(() -> new IllegalArgumentException("그룹이 존재하지 않습니다."));

        // 2. 요청하는 사용자 존재 여부 확인
        User requestingUser = userRepository.findById(requestingUserNo)
                .orElseThrow(() -> new IllegalArgumentException("요청하는 사용자가 존재하지 않습니다."));

        // 3. 요청하는 사용자가 해당 그룹의 멤버인지 확인
        boolean isMember = groupMemberRepository.existsByGroupAndUser(group, requestingUser);
        if (!isMember) {
            throw new AccessDeniedException("그룹 멤버만 그룹명을 수정할 수 있습니다.");
        }

        // 4. 그룹명 업데이트
        group.setName(request.getName());
        return groupRepository.save(group);
    }
}
