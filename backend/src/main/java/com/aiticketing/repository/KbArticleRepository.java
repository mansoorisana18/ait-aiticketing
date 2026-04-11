package com.aiticketing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aiticketing.entity.KbArticle;

@Repository
public interface KbArticleRepository extends JpaRepository<KbArticle, Long> {

    List<KbArticle> findByStatusOrderByUpdatedAtDesc(String status);
    Optional<KbArticle> findByKbIdAndStatus(Long kbId, String status);
    List<KbArticle> findAllByOrderByUpdatedAtDesc();
}