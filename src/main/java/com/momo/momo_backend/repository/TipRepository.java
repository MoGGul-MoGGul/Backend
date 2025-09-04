package com.momo.momo_backend.repository;

import com.momo.momo_backend.entity.Tip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TipRepository extends JpaRepository<Tip, Long> {

    /* ====== 등록된 팁 조회 (StorageTip과 연결된 팁 - TipQueryService에서 사용) ====== */

    // 전체 공개 & 등록된 팁 조회
    @Query("""
           SELECT t
           FROM Tip t
           JOIN t.storageTips st
           WHERE t.isPublic = true
           ORDER BY t.createdAt DESC
           """)
    List<Tip> findAllPublicRegisteredTipsOrderByCreatedAtDesc();


    // 특정 사용자가 등록한 팁 조회
    @Query("""
           SELECT t
           FROM Tip t
           JOIN t.storageTips st
           WHERE t.user.no = :userNo
           ORDER BY t.createdAt DESC
           """)
    List<Tip> findRegisteredTipsByUserNo(@Param("userNo") Long userNo);


    /* ====== 그룹 보관함 (등록된 팁만) ====== */
    @Query("""
           SELECT t
           FROM   Tip t
           JOIN   t.storageTips st
           JOIN   st.storage s
           WHERE  s.group.no = :groupId
           ORDER  BY t.createdAt DESC
           """)
    List<Tip> findTipsByGroupId(@Param("groupId") Long groupId);

    /* ====== 내 보관함(전체) (등록된 팁만) ====== */
    @Query("""
           SELECT t
           FROM   Tip t
           JOIN   t.storageTips st
           JOIN   st.storage s
           WHERE  s.user.no = :userId
           ORDER  BY t.createdAt DESC
           """)
    List<Tip> findTipsByUserStorage(@Param("userId") Long userId);

    /* ====== 특정 보관함(ID) (등록된 팁만) ====== */
    @Query("""
           SELECT st.tip
           FROM   StorageTip st
           WHERE  st.storage.no = :storageId
           ORDER  BY st.tip.createdAt DESC
           """)
    List<Tip> findTipsByStorageId(@Param("storageId") Long storageId);
    Optional<Tip> findByNoAndUser_No(Long tipNo, Long userNo);

    // 미등록 상태이며 생성된 지 특정 시간(예: 24시간)이 지난 팁 조회
    @Query("""
           SELECT t
           FROM Tip t
           LEFT JOIN StorageTip st ON t.no = st.tip.no
           WHERE st.tip.no IS NULL
           AND t.createdAt < :cutoffTime
           """)
    List<Tip> findUnregisteredTipsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    // 특정 사용자가 작성한 공개 꿀팁 목록을 최신순으로 조회하는 메서드 추가
    List<Tip> findAllByUser_NoAndIsPublicTrueOrderByCreatedAtDesc(Long userNo);
}
