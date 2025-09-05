package com.momo.momo_backend.dto;

import com.momo.momo_backend.entity.Tag;
import com.momo.momo_backend.entity.Tip;
import com.momo.momo_backend.entity.TipTag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 꿀팁 및 북마크 랭킹 관련 DTO를 모아두는 컨테이너 클래스
 */
public final class TipDto {

    private TipDto() {}

    // AI 생성 요청 DTO
    @Getter
    @NoArgsConstructor
    public static class CreateRequest {
        private String url;
        private String title;
        private List<String> tags;
    }

    // AI 생성 완료 후 응답 DTO
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateResponse {
        private String url;
        private String title;
        private List<String> tags;
        private String summary;
        private String thumbnailImageUrl;
    }

    // 꿀팁 등록 요청 DTO
    @Getter
    @NoArgsConstructor
    public static class RegisterRequest {
        @NotEmpty
        private String url;
        @NotEmpty
        private String title;
        private String summary;
        private String thumbnailImageUrl;
        private List<String> tags;
        @NotNull
        private Long storageNo;
        @NotNull
        private Boolean isPublic;
    }

    // 꿀팁 수정 요청 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdateRequest {
        private String title;
        private String contentSummary;
        private Boolean isPublic;
        private List<String> tags;
    }

    // 꿀팁 상세 조회 응답 DTO
    @Getter
    @Builder
    public static class DetailResponse {
        private Long no;
        private String title;
        private String contentSummary;
        private String url;
        private Long userNo;
        private String nickname;
        private String thumbnailUrl;
        private Boolean isPublic;
        private List<String> tags;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long storageNo; // 꿀팁 등록 시에만 사용

        public static DetailResponse from(Tip tip) {
            return from(tip, null);
        }

        public static DetailResponse from(Tip tip, Long storageNo) {
            return DetailResponse.builder()
                    .no(tip.getNo())
                    .title(tip.getTitle())
                    .contentSummary(tip.getContentSummary())
                    .url(tip.getUrl())
                    .userNo(tip.getUser().getNo())
                    .nickname(tip.getUser().getNickname())
                    .thumbnailUrl(tip.getThumbnailUrl())
                    .isPublic(tip.getIsPublic())
                    .tags(tip.getTipTags().stream()
                            .map(TipTag::getTag)
                            .map(Tag::getName)
                            .collect(Collectors.toList()))
                    .createdAt(tip.getCreatedAt())
                    .updatedAt(tip.getUpdatedAt())
                    .storageNo(storageNo)
                    .build();
        }
    }

    // 이번 주 북마크 랭킹 응답 DTO
    @Getter
    @Builder
    public static class WeeklyRankingResponse {
        private Long tipNo;
        private String title;
        private String thumbnailUrl;
        private Long userNo;
        private String nickname;
        private List<String> tags;
        private Long weeklyBookmarkCount;

        public static WeeklyRankingResponse from(Tip tip, Long weeklyBookmarkCount) {
            List<String> tagNames = tip.getTipTags().stream()
                    .map(tipTag -> tipTag.getTag().getName())
                    .collect(Collectors.toList());

            return WeeklyRankingResponse.builder()
                    .tipNo(tip.getNo())
                    .title(tip.getTitle())
                    .thumbnailUrl(tip.getThumbnailUrl())
                    .userNo(tip.getUser().getNo())
                    .nickname(tip.getUser().getNickname())
                    .tags(tagNames)
                    .weeklyBookmarkCount(weeklyBookmarkCount)
                    .build();
        }
    }
}
