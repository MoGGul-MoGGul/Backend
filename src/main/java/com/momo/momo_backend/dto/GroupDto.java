package com.momo.momo_backend.dto;

import com.momo.momo_backend.entity.Storage;
import com.momo.momo_backend.entity.User;
import lombok.*;

import java.util.List;

/**
 * 그룹 관련 DTO를 모아두는 컨테이너 클래스
 */
public final class GroupDto {

    private GroupDto() {}

    // 그룹 생성 요청 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        private String name; // 그룹 이름
    }

    // 그룹 생성 응답 DTO
    @Getter
    @Builder
    @AllArgsConstructor
    public static class CreateResponse {
        private String message;  // 성공 메시지
        private Long groupNo;    // 생성된 그룹의 식별 번호
    }

    // 그룹 사용자 초대 요청 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    public static class InviteRequest {
        private List<String> userLoginIds; // 초대할 사용자들의 로그인 아이디 목록
    }

    // 그룹 멤버 조회 응답 DTO
    @Getter
    @Builder
    @AllArgsConstructor
    public static class MemberResponse {
        private Long userNo;    // 사용자 식별 번호
        private String nickname; // 사용자 닉네임

        public static MemberResponse from(User user) {
            return MemberResponse.builder()
                    .userNo(user.getNo())
                    .nickname(user.getNickname())
                    .build();
        }
    }

    // 사용자가 속한 그룹 목록 조회 응답 DTO
    @Getter
    @Builder
    @AllArgsConstructor
    public static class ListResponse {
        private Long groupNo;       // 그룹 식별 번호
        private String name;        // 그룹 이름
        private int memberCount;    // 그룹 멤버 수
    }

    // 그룹 보관함 목록 조회 응답 DTO
    @Getter
    @Builder
    @AllArgsConstructor
    public static class StorageResponse {
        private Long storageNo;
        private String name;
        private Long userNo;

        public static StorageResponse from(Storage storage) {
            return StorageResponse.builder()
                    .storageNo(storage.getNo())
                    .name(storage.getName())
                    .userNo(storage.getUser().getNo())
                    .build();
        }
    }
}
