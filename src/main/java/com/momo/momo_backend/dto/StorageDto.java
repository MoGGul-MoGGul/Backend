package com.momo.momo_backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 보관함 관련 DTO를 모아두는 컨테이너 클래스
 */
public final class StorageDto {

    private StorageDto() {}

    // 보관함 생성 요청 DTO
    @Getter
    @NoArgsConstructor
    public static class CreateRequest {
        private String name;
        private Long groupNo;  // nullable
    }

    // 보관함 수정 요청 DTO
    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        private String name;
    }

    // 보관함 응답 DTO
    @Getter
    @Builder
    public static class Response {
        private Long storageNo;
        private String name;
        private Long userNo;
    }
}
