package com.momo.momo_backend.service;

import com.momo.momo_backend.dto.StorageDto;
import com.momo.momo_backend.entity.Storage;
import com.momo.momo_backend.entity.User;
import com.momo.momo_backend.entity.Group;
import com.momo.momo_backend.repository.GroupMemberRepository;
import com.momo.momo_backend.repository.GroupRepository;
import com.momo.momo_backend.repository.StorageRepository;
import com.momo.momo_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageRepository storageRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    private static final String ERROR_MSG_USER_NOT_FOUND = "사용자가 존재하지 않습니다.";
    private static final String ERROR_MSG_STORAGE_NOT_FOUND = "보관함이 존재하지 않습니다.";
    private static final String ERROR_MSG_GROUP_NOT_FOUND = "그룹이 존재하지 않습니다.";

    // 보관함 생성
    public Storage create(StorageDto.CreateRequest request, Long loginUserNo) {
        User user = userRepository.findById(loginUserNo)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_MSG_USER_NOT_FOUND));

        Storage storage = Storage.builder()
                .user(user)
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (request.getGroupNo() != null) {
            Group group = groupRepository.findById(request.getGroupNo())
                    .orElseThrow(() -> new IllegalArgumentException(ERROR_MSG_GROUP_NOT_FOUND));

            boolean isMember = groupMemberRepository.existsByGroupAndUser(group, user);
            if (!isMember) {
                throw new AccessDeniedException("그룹 보관함은 해당 그룹의 멤버만 생성할 수 있습니다.");
            }
            storage.setGroup(group);
        }

        return storageRepository.save(storage);
    }

    // 보관함 수정
    @Transactional
    public Storage update(Long storageNo, Long loginUserNo, StorageDto.UpdateRequest request) {
        Storage storage = storageRepository.findById(storageNo)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_MSG_STORAGE_NOT_FOUND));

        User loginUser = userRepository.findById(loginUserNo)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_MSG_USER_NOT_FOUND));

        if (storage.getGroup() != null) {
            boolean isGroupMember = groupMemberRepository.existsByGroupAndUser(storage.getGroup(), loginUser);
            if (!isGroupMember) {
                throw new AccessDeniedException("그룹 보관함은 해당 그룹의 멤버만 수정할 수 있습니다.");
            }
        } else {
            if (!storage.getUser().getNo().equals(loginUserNo)) {
                throw new AccessDeniedException("개인 보관함은 소유자만 수정할 수 있습니다.");
            }
        }

        storage.setName(request.getName());
        return storageRepository.save(storage);
    }

    // 보관함 삭제
    public void delete(Long storageNo, Long loginUserNo) {
        Storage storage = storageRepository.findById(storageNo)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_MSG_STORAGE_NOT_FOUND));

        User loginUser = userRepository.findById(loginUserNo)
                .orElseThrow(() -> new IllegalArgumentException(ERROR_MSG_USER_NOT_FOUND));

        if (storage.getGroup() != null) {
            boolean isGroupMember = groupMemberRepository.existsByGroupAndUser(storage.getGroup(), loginUser);
            if (!isGroupMember) {
                throw new AccessDeniedException("그룹 보관함은 해당 그룹의 멤버만 삭제할 수 있습니다.");
            }
        } else {
            if (!storage.getUser().getNo().equals(loginUserNo)) {
                throw new AccessDeniedException("개인 보관함은 소유자만 삭제할 수 있습니다.");
            }
        }

        storageRepository.delete(storage);
    }
}