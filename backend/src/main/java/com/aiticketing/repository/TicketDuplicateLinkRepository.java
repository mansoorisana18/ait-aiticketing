package com.aiticketing.repository;

import java.util.List;
import java.util.Optional;

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

    List<TicketDuplicateLink> findByPrimaryTicket_TicketIdAndDuplicateTypeAndLinkStatus(
            Long primaryTicketId,
            String duplicateType,
            String linkStatus
    );

    Optional<TicketDuplicateLink> findFirstByDuplicateTicket_TicketIdAndDuplicateTypeAndLinkStatus(
            Long duplicateTicketId,
            String duplicateType,
            String linkStatus
    );
}