package com.aiticketing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aiticketing.entity.TicketDuplicateLink;

public interface TicketDuplicateLinkRepository extends JpaRepository<TicketDuplicateLink, Long> {

	List<TicketDuplicateLink> findByDuplicateTicket_TicketIdAndLinkStatus(Long duplicateTicketId, String linkStatus);

	List<TicketDuplicateLink> findByPrimaryTicket_TicketIdAndDuplicateTypeAndLinkStatusAndPropagateResolution(
	        Long primaryTicketId,
	        String duplicateType,
	        String linkStatus,
	        Boolean propagateResolution
	);
}