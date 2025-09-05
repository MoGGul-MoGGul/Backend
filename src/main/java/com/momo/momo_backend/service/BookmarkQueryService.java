package com.momo.momo_backend.service;

import com.momo.momo_backend.dto.TipDto;
import com.momo.momo_backend.entity.Tip;
import com.momo.momo_backend.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkQueryService {

    private final BookmarkRepository bookmarkRepository;

    // 주간 북마크 랭킹 조회
    public List<TipDto.WeeklyRankingResponse> getWeeklyBookmarkRanking() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Object[]> results = bookmarkRepository.findWeeklyRanking(startDate, pageRequest);

        if (results == null) {
            return Collections.emptyList();
        }

        return results.stream()
                .map(result -> {
                    if (result != null && result.length >= 2 &&
                            result[0] instanceof Tip tip && result[1] instanceof Long count) {
                        return TipDto.WeeklyRankingResponse.from(tip, count);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}