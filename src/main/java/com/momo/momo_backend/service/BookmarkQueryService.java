package com.momo.momo_backend.service;

import com.momo.momo_backend.dto.TipDto;
import com.momo.momo_backend.entity.Tip;
import com.momo.momo_backend.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkQueryService {

    private final BookmarkRepository bookmarkRepository;

    // 주간 북마크 랭킹 조회
    public List<TipDto.WeeklyRankingResponse> getWeeklyBookmarkRanking() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        // 상위 10개만 조회하도록 PageRequest 설정
        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Object[]> results = bookmarkRepository.findWeeklyRanking(startDate, pageRequest);

        return results.stream()
                .map(result -> {
                    Tip tip = (Tip) result[0];
                    Long count = (Long) result[1];
                    return TipDto.WeeklyRankingResponse.from(tip, count);
                })
                .collect(Collectors.toList());
    }

}