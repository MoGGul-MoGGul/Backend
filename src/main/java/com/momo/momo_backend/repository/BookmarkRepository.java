package com.momo.momo_backend.repository;

import com.momo.momo_backend.entity.Bookmark;
import com.momo.momo_backend.entity.Tip;
import com.momo.momo_backend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // 즐겨찾기 중복 방지용 메서드
    boolean existsByUserAndTip(User user, Tip tip);

    // 즐겨찾기 취소 시 소유권 확인을 위해 userNo와 bookmarkNo로 찾는 메서드 추가
    Optional<Bookmark> findByNoAndUser_No(Long bookmarkNo, Long userNo);

    // 주간 랭킹 조회를 위한 쿼리 추가
    @Query("SELECT b.tip, COUNT(b.tip) as bookmarkCount " +
            "FROM Bookmark b " +
            "WHERE b.createdAt >= :startDate " +
            "GROUP BY b.tip " +
            "ORDER BY bookmarkCount DESC")
    List<Object[]> findWeeklyRanking(@Param("startDate") LocalDateTime startDate, Pageable pageable);

    // 특정 사용자가 작성한 모든 꿀팁의 총 즐겨찾기 수를 계산하는 메서드 추가
    long countByTip_User_No(Long userNo);
}