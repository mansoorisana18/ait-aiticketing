package com.aiticketing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aiticketing.entity.AiDecision;

@Repository
public interface AiDecisionRepository extends JpaRepository<AiDecision, Long>{
	
	Optional<AiDecision> findFirstByTicketIdAndDecisionTypeOrderByCreatedAtDesc(Long ticketId, String decisionType);
}
