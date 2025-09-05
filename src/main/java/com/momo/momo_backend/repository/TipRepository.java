package com.momo.momo_backend.repository;

import com.momo.momo_backend.entity.Tip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TipRepository extends JpaRepository<Tip, Long> {
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


    /* ====== 특정 보관함(ID) (등록된 팁만) ====== */
    @Query("""
           SELECT st.tip
           FROM   StorageTip st
           WHERE  st.storage.no = :storageId
           ORDER  BY st.tip.createdAt DESC
           """)
    List<Tip> findTipsByStorageId(@Param("storageId") Long storageId);
    Optional<Tip> findByNoAndUser_No(Long tipNo, Long userNo);


    // 특정 사용자가 작성한 공개 꿀팁 목록을 최신순으로 조회하는 메서드 추가
    List<Tip> findAllByUser_NoAndIsPublicTrueOrderByCreatedAtDesc(Long userNo);
}
