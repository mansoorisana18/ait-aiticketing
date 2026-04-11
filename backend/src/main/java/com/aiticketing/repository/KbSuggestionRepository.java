package com.aiticketing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aiticketing.entity.KbSuggestion;

@Repository
public interface KbSuggestionRepository extends JpaRepository<KbSuggestion, Long> {

    Optional<KbSuggestion> findFirstByTicket_TicketIdOrderByCreatedAtDesc(Long ticketId);

    Optional<KbSuggestion> findFirstByTicket_TicketIdAndStatusOrderByCreatedAtDesc(Long ticketId, String status);

    boolean existsByTicket_TicketIdAndKbArticle_KbId(Long ticketId, Long kbId);

    boolean existsByKbArticle_KbIdAndTicket_CreatedBy_UserId(Long kbId, Long userId);
}