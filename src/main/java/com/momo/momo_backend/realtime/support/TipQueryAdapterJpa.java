package com.momo.momo_backend.realtime.support;

import com.momo.momo_backend.entity.Tag;
import com.momo.momo_backend.entity.Tip;
import com.momo.momo_backend.entity.TipTag;
import com.momo.momo_backend.entity.User;
import com.momo.momo_backend.repository.TipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TipQueryAdapterJpa implements TipQueryPort {

    private final TipRepository tipRepository;

    @Override
    @Transactional(readOnly = true)
    public TipSummaryView findSummaryById(Long tipId) {
        Tip tip = tipRepository.findById(tipId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팁입니다. id=" + tipId));

        String author = getAuthorDisplayName(tip.getUser());

        List<String> tags = Optional.ofNullable(tip.getTipTags()).orElse(List.of()).stream()
                .map(TipTag::getTag)
                .map(Tag::getName)
                .toList();

        Instant created = (tip.getCreatedAt() != null)
                ? tip.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
                : Instant.now();

        return new TipSummaryView(
                tip.getNo(),
                tip.getTitle(),
                author,
                tags,
                created,
                tip.getThumbnailUrl()
        );
    }

    private String getAuthorDisplayName(User user) {
        if (user == null) {
            return null;
        }

        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname();
        } else {
            return String.valueOf(user.getNo());
        }
    }
}
