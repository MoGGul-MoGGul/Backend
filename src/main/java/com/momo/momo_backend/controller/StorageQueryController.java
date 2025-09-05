package com.momo.momo_backend.controller;

import com.momo.momo_backend.dto.GroupDto;
import com.momo.momo_backend.dto.StorageDto;
import com.momo.momo_backend.dto.ErrorResponse;
import com.momo.momo_backend.entity.Storage;
import com.momo.momo_backend.security.CustomUserDetails;
import com.momo.momo_backend.service.StorageQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/query/storage")
@RequiredArgsConstructor
@Slf4j // 로그 사용을 위해 추가
public class StorageQueryController {

    private final StorageQueryService storageQueryService;

    // 특정 사용자의 보관함 목록 조회 API
    @GetMapping("/{userNo}")
    public ResponseEntity<Object> getStoragesByUser(@PathVariable Long userNo) {
        log.info("보관함 목록 조회 요청 - userNo: {}", userNo);
        try {
            List<Storage> storages = storageQueryService.findByUser(userNo);

            // StorageNameResponse DTO 리스트로 변환하며 필드 추가
            List<StorageDto.Response> responseList = storages.stream()
                    .map(storage -> StorageDto.Response.builder()
                            .storageNo(storage.getNo())
                            .name(storage.getName())
                            .userNo(storage.getUser().getNo())
                            .build())
                    .toList();

            log.info("보관함 목록 조회 성공 - userNo: {}, 조회된 보관함 수: {}", userNo, responseList.size());
            return ResponseEntity.ok(responseList);
        } catch (IllegalArgumentException e) {
            log.error("보관함 목록 조회 실패: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // 특정 그룹의 보관함 목록 조회 API
    @GetMapping("/group/{groupNo}")
    public ResponseEntity<Object> getGroupStorages(
            @PathVariable Long groupNo,
            @AuthenticationPrincipal CustomUserDetails userDetails) { // 인증된 사용자 정보 필요
        log.info("그룹 보관함 목록 조회 요청 - 그룹 ID: {}, 요청 사용자: {}", groupNo, userDetails.getUsername());
        try {
            Long requestingUserNo = userDetails.getUser().getNo(); // 현재 로그인한 사용자 번호
            List<Storage> groupStorages = storageQueryService.findGroupStoragesByGroup(groupNo, requestingUserNo);

            // Storage 엔티티 리스트를 GroupStorageResponse DTO 리스트로 변환
            List<GroupDto.StorageResponse> responseList = groupStorages.stream()
                    .map(GroupDto.StorageResponse::from)
                    .toList();

            log.info("그룹 보관함 목록 조회 성공 - 그룹 ID: {}, 조회된 보관함 수: {}", groupNo, responseList.size());
            return ResponseEntity.ok(responseList); // 200 OK 응답
        } catch (IllegalArgumentException e) {
            log.error("그룹 보관함 목록 조회 실패: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value()) // 400 Bad Request (그룹 없음 등)
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (AccessDeniedException e) { // AccessDeniedException 처리 추가
            log.error("그룹 보관함 목록 조회 권한 없음: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.FORBIDDEN.value()) // 403 Forbidden
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        } catch (Exception e) {
            log.error("그룹 보관함 목록 조회 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value()) // 500 Internal Server Error
                    .message("그룹 보관함 목록 조회 중 오류가 발생했습니다.")
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 사용자가 속한 모든 그룹의 보관함 조회 API
    @GetMapping("/group-all")
    public ResponseEntity<Object> getStoragesForUserGroups(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("사용자의 모든 그룹 보관함 조회 요청 - 요청 사용자: {}", userDetails.getUsername());
        try {
            Long userNo = userDetails.getUser().getNo();

            List<Storage> storages = storageQueryService.findStoragesForUserGroups(userNo);

            List<GroupDto.StorageResponse> responseList = storages.stream()
                    .map(GroupDto.StorageResponse::from)
                    .toList();

            log.info("사용자 그룹 보관함 목록 조회 성공 - 사용자: {}, 조회된 보관함 수: {}", userNo, responseList.size());
            return ResponseEntity.ok(responseList);
        } catch (IllegalArgumentException e) {
            log.error("사용자 그룹 보관함 목록 조회 실패: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (AccessDeniedException e) {
            log.error("사용자 그룹 보관함 목록 조회 권한 없음: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.FORBIDDEN.value())
                    .message(e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
    }
}