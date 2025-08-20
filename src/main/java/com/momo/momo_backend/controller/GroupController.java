package com.momo.momo_backend.controller;

import com.momo.momo_backend.dto.*;
import com.momo.momo_backend.entity.Group;
import com.momo.momo_backend.entity.User;
import com.momo.momo_backend.security.CustomUserDetails;
import com.momo.momo_backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Slf4j 임포트
import org.springframework.http.HttpStatus; // HttpStatus 임포트
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups") // 그룹 관련 API의 기본 경로
@RequiredArgsConstructor
@Slf4j // 로그 사용을 위해 추가
public class GroupController {

    private final GroupService groupService;

    // 그룹 생성 API
    @PostMapping
    public ResponseEntity<?> createGroup(
            @RequestBody GroupCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("그룹 생성 요청 - 그룹명: {}, 사용자: {}", request.getName(), userDetails.getUsername());
        try {
            Long userNo = userDetails.getUser().getNo(); // 현재 로그인한 사용자 번호
            Group createdGroup = groupService.createGroup(request, userNo);

            GroupCreateResponse response = GroupCreateResponse.builder()
                    .message("그룹 생성 완료!")
                    .groupNo(createdGroup.getNo())
                    .build();

            log.info("그룹 생성 성공 - 그룹 번호: {}", createdGroup.getNo());
            return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created 응답
        } catch (IllegalArgumentException e) {
            log.error("그룹 생성 실패: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value()) // 400 Bad Request
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("그룹 생성 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value()) // 500 Internal Server Error
                    .message("그룹 생성 중 오류가 발생했습니다.")
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 그룹 멤버 초대 API (여러 명 동시 초대 가능)
    @PostMapping("/{groupNo}/invite")
    public ResponseEntity<?> inviteGroupMember(
            @PathVariable Long groupNo,
            @RequestBody GroupInviteRequest request, // userLoginIds 목록을 받음
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("그룹 멤버 초대 요청 - 그룹 ID: {}, 초대할 사용자 아이디 목록: {}, 초대하는 사용자: {}",
                groupNo, request.getUserLoginIds(), userDetails.getUsername());
        try {
            Long inviterUserNo = userDetails.getUser().getNo();
            List<String> results = groupService.inviteMembers(groupNo, request, inviterUserNo);

            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            log.error("그룹 멤버 초대 실패: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            log.error("그룹 멤버 초대 권한 없음: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.FORBIDDEN.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            log.error("그룹 멤버 초대 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("그룹 멤버 초대 중 오류가 발생했습니다.")
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 그룹 멤버 나가기 API
    @DeleteMapping("/{groupNo}/leave") // 엔드포인트: /api/groups/{groupId}/leave
    public ResponseEntity<?> leaveGroup(
            @PathVariable Long groupNo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("그룹 멤버 나가기 요청 - 그룹 ID: {}, 사용자: {}", groupNo, userDetails.getUsername());
        try {
            Long userNo = userDetails.getUser().getNo(); // 현재 로그인한 사용자 번호
            groupService.leaveGroup(groupNo, userNo);

            MessageResponse response = MessageResponse.builder()
                    .message("그룹에서 나갔습니다.")
                    .build();

            log.info("그룹 멤버 나가기 성공 - 그룹 ID: {}, 사용자: {}", groupNo, userNo);
            return ResponseEntity.ok(response); // 200 OK 응답
        } catch (IllegalArgumentException e) {
            log.error("그룹 멤버 나가기 실패: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value()) // 400 Bad Request (그룹 없음, 멤버 아님 등)
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("그룹 멤버 나가기 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value()) // 500 Internal Server Error
                    .message("그룹 멤버 나가기 중 오류가 발생했습니다.")
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 그룹 멤버 조회 API
    @GetMapping("/{groupNo}/members")
    public ResponseEntity<?> getGroupMembers(
            @PathVariable Long groupNo,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("그룹 멤버 조회 요청 - 그룹 ID: {}, 요청 사용자: {}", groupNo, userDetails.getUsername());
        try {
            Long requestingUserNo = userDetails.getUser().getNo(); // 요청하는 사용자 번호
            List<User> members = groupService.getGroupMembers(groupNo, requestingUserNo);

            List<GroupMemberResponse> responseList = members.stream()
                    .map(GroupMemberResponse::from)
                    .collect(Collectors.toList());

            log.info("그룹 멤버 조회 성공 - 그룹 ID: {}, 조회된 멤버 수: {}", groupNo, responseList.size());
            return ResponseEntity.ok(responseList);
        } catch (IllegalArgumentException e) {
            log.error("그룹 멤버 조회 실패: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) { // AccessDeniedException 처리 추가
            log.error("그룹 멤버 조회 권한 없음: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            log.error("그룹 멤버 조회 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("그룹 멤버 조회 중 오류가 발생했습니다.")
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 그룹 목록 조회 API (사용자가 속한 그룹)
    @GetMapping("/check") // 엔드포인트: /api/groups/check
    public ResponseEntity<?> getGroupsForUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("그룹 목록 조회 요청 - 사용자: {}", userDetails.getUsername());
        try {
            Long userNo = userDetails.getUser().getNo();
            List<GroupListResponse> responseList = groupService.getGroupsForUser(userNo);

            log.info("그룹 목록 조회 성공 - 사용자: {}, 조회된 그룹 수: {}", userNo, responseList.size());
            return ResponseEntity.ok(responseList); // 200 OK 응답
        } catch (IllegalArgumentException e) {
            log.error("그룹 목록 조회 실패: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value()) // 400 Bad Request (사용자 없음 등)
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("그룹 목록 조회 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("그룹 목록 조회 중 오류가 발생했습니다.")
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 그룹명 수정 API
    @PutMapping("/{groupNo}/name") // 엔드포인트: /api/groups/{groupId}/name
    public ResponseEntity<?> updateGroupName(
            @PathVariable Long groupNo,
            @RequestBody GroupUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("그룹명 수정 요청 - 그룹 ID: {}, 새 그룹명: {}, 요청 사용자: {}",
                groupNo, request.getName(), userDetails.getUsername());
        try {
            Long requestingUserNo = userDetails.getUser().getNo();
            Group updatedGroup = groupService.updateGroupName(groupNo, request, requestingUserNo);

            MessageResponse response = MessageResponse.builder()
                    .message("그룹명이 성공적으로 수정되었습니다: " + updatedGroup.getName())
                    .build();

            log.info("그룹명 수정 성공 - 그룹 ID: {}, 새 그룹명: {}", groupNo, updatedGroup.getName());
            return ResponseEntity.ok(response); // 200 OK 응답
        } catch (IllegalArgumentException e) {
            log.error("그룹명 수정 실패: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) {
            log.error("그룹명 수정 권한 없음: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.FORBIDDEN.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            log.error("그룹명 수정 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("그룹명 수정 중 오류가 발생했습니다.")
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
