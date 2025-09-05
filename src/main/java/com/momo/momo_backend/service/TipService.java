package com.momo.momo_backend.service;

import com.momo.momo_backend.dto.TipDto;
import com.momo.momo_backend.dto.ai.AiResultResponseDto;
import com.momo.momo_backend.dto.ai.AiTaskResponseDto;
import com.momo.momo_backend.entity.*;
import com.momo.momo_backend.enums.NotificationType;
import com.momo.momo_backend.exception.AiProcessingException;
import com.momo.momo_backend.realtime.events.NotificationCreatedEvent;
import com.momo.momo_backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TipService {

    @Value("${ai.server.url}")
    private String aiApiUrl;

    private final TipRepository tipRepository;
    private final TagRepository tagRepository;
    private final TipTagRepository tipTagRepository;
    private final UserRepository userRepository;
    private final StorageRepository storageRepository;
    private final FollowRepository followRepository;
    private final NotificationRepository notificationRepository;
    private final StorageTipRepository storageTipRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final RestTemplate restTemplate;
    private final ApplicationEventPublisher eventPublisher;

    private static final String DEFAULT_TITLE = "제목 없음";

    // 꿀팁 요약 생성 (AI API 연동)
    @Transactional
    public TipDto.CreateResponse createTip(TipDto.CreateRequest request) throws InterruptedException {
        String processUrl = aiApiUrl + "/async-index/";
        Map<String, String> body = new HashMap<>();
        body.put("url", request.getUrl());

        AiTaskResponseDto task = restTemplate.postForObject(processUrl, body, AiTaskResponseDto.class);
        String taskId = (task != null) ? task.getTaskId() : null;

        if (!StringUtils.hasText(taskId)) {
            throw new AiProcessingException("AI task 생성 실패");
        }

        String resultUrl = aiApiUrl + "/task-status/" + taskId;
        long start = System.currentTimeMillis();
        long timeoutMs = 120_000;

        while (System.currentTimeMillis() - start < timeoutMs) {
            AiResultResponseDto result = restTemplate.getForObject(resultUrl, AiResultResponseDto.class);
            if (result != null && "SUCCESS".equals(result.getStatus())) {
                AiResultResponseDto.ResultData r = result.getResult();
                String finalTitle = StringUtils.hasText(request.getTitle()) ? request.getTitle() : r.getTitle();
                List<String> finalTags = (request.getTags() != null && !request.getTags().isEmpty())
                        ? request.getTags() : r.getTags();
                return TipDto.CreateResponse.builder()
                        .url(request.getUrl())
                        .title(finalTitle)
                        .tags(finalTags)
                        .summary(r.getSummary())
                        .thumbnailImageUrl(r.getThumbnailUrl())
                        .build();
            }
            Thread.sleep(2000);
        }
        throw new AiProcessingException("AI processing timed out: " + taskId);
    }

    // 꿀팁 등록(저장)
    @Transactional
    public TipDto.DetailResponse registerTip(TipDto.RegisterRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Tip tip = Tip.builder()
                .title(StringUtils.hasText(request.getTitle()) ? request.getTitle() : DEFAULT_TITLE) // 상수 사용
                .url(request.getUrl())
                .contentSummary(request.getSummary())
                .thumbnailUrl(request.getThumbnailImageUrl())
                .isPublic(Boolean.TRUE.equals(request.getIsPublic()))
                .user(user)
                .build();
        tip.setCreatedAt(LocalDateTime.now());
        tip.setUpdatedAt(LocalDateTime.now());
        tipRepository.save(tip);

        List<String> tagNames = (request.getTags() != null) ? request.getTags() : Collections.emptyList();
        for (String tagName : new LinkedHashSet<>(tagNames)) {
            if (!StringUtils.hasText(tagName)) continue;
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
            TipTag link = TipTag.builder().tip(tip).tag(tag).build();
            tipTagRepository.save(link);
            tip.getTipTags().add(link);
        }

        Storage storage = storageRepository.findById(request.getStorageNo())
                .orElseThrow(() -> new RuntimeException("Storage not found with id: " + request.getStorageNo()));
        if (storage.getGroup() != null) {
            if (!groupMemberRepository.existsByGroupAndUser(storage.getGroup(), user)) {
                throw new AccessDeniedException("그룹 보관함에 등록하려면 해당 그룹의 멤버여야 합니다.");
            }
        } else if (!Objects.equals(storage.getUser().getNo(), userId)) {
            throw new AccessDeniedException("선택한 보관함은 현재 로그인한 사용자의 소유가 아닙니다.");
        }

        if (!storageTipRepository.existsByStorageAndTip(storage, tip)) {
            StorageTip storageTip = StorageTip.builder()
                    .storage(storage)
                    .tip(tip)
                    .build();
            storageTipRepository.save(storageTip);
            tip.getStorageTips().add(storageTip);
        }

        if (Boolean.TRUE.equals(tip.getIsPublic())) {
            notifyFollowers(tip);
            notifyGroupMembers(tip);
        }

        return TipDto.DetailResponse.from(tip, storage.getNo());
    }

    // 팁 수정
    @Transactional
    public TipDto.DetailResponse update(Long tipNo, Long userNo, TipDto.UpdateRequest req) {
        Tip tip = tipRepository.findByNoAndUser_No(tipNo, userNo)
                .orElseThrow(() -> new AccessDeniedException("수정 권한이 없습니다."));

        if (req.getTitle() != null) tip.setTitle(req.getTitle());
        if (req.getContentSummary() != null) tip.setContentSummary(req.getContentSummary());
        if (req.getIsPublic() != null) tip.setIsPublic(req.getIsPublic());

        if (req.getTags() != null) {
            tipTagRepository.deleteByTipNo(tipNo);
            tip.getTipTags().clear();
            for (String name : new LinkedHashSet<>(req.getTags())) {
                if (!StringUtils.hasText(name)) continue;
                Tag tag = tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));
                tip.getTipTags().add(TipTag.builder().tip(tip).tag(tag).build());
            }
        }

        Tip saved = tipRepository.save(tip);

        return TipDto.DetailResponse.from(saved);
    }

    // 팁 삭제
    @Transactional
    public void delete(Long tipNo, Long userNo) {
        Tip tip = tipRepository.findByNoAndUser_No(tipNo, userNo)
                .orElseThrow(() -> new AccessDeniedException("삭제 권한이 없습니다."));
        tipRepository.delete(tip);
    }

    private void notifyFollowers(Tip savedTip) {
        List<Follow> follows = followRepository.findByFollowing(savedTip.getUser());
        if (follows.isEmpty()) return;

        String actor = Optional.ofNullable(savedTip.getUser().getNickname()).orElse(savedTip.getUser().getLoginId());
        String title = StringUtils.hasText(savedTip.getTitle()) ? savedTip.getTitle() : DEFAULT_TITLE; // 상수 사용
        String message = actor + "님이 새 꿀팁을 등록했습니다: " + title;

        List<Notification> notis = follows.stream()
                .map(f -> Notification.builder()
                        .receiver(f.getFollower())
                        .tip(savedTip)
                        .type(NotificationType.FOLLOWING_TIP_UPLOAD)
                        .read(false)
                        .build())
                .toList();
        if (!notis.isEmpty()) notificationRepository.saveAll(notis);

        follows.forEach(f ->
                eventPublisher.publishEvent(
                        new NotificationCreatedEvent(
                                f.getFollower().getNo(),
                                savedTip.getNo(),
                                message,
                                Instant.now()
                        )
                )
        );
    }

    private void notifyGroupMembers(Tip savedTip) {
        Set<User> targets = savedTip.getStorageTips().stream()
                .map(StorageTip::getStorage)
                .filter(s -> s.getGroup() != null)
                .flatMap(s -> s.getGroup().getGroupMembers().stream())
                .map(GroupMember::getUser)
                .filter(u -> !u.getNo().equals(savedTip.getUser().getNo()))
                .collect(Collectors.toSet());
        if (targets.isEmpty()) return;

        String actor = Optional.ofNullable(savedTip.getUser().getNickname()).orElse(savedTip.getUser().getLoginId());
        String title = StringUtils.hasText(savedTip.getTitle()) ? savedTip.getTitle() : DEFAULT_TITLE; // 상수 사용
        String message = actor + "님이 그룹 보관함에 꿀팁을 등록했습니다: " + title;

        List<Notification> notis = targets.stream()
                .map(user -> Notification.builder()
                        .receiver(user)
                        .tip(savedTip)
                        .type(NotificationType.GROUP_TIP_UPLOAD)
                        .read(false)
                        .build())
                .toList();
        if (!notis.isEmpty()) notificationRepository.saveAll(notis);

        targets.forEach(u ->
                eventPublisher.publishEvent(
                        new NotificationCreatedEvent(
                                u.getNo(),
                                savedTip.getNo(),
                                message,
                                Instant.now()
                        )
                )
        );
    }
}
