package com.aiticketing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aiticketing.entity.TicketDuplicateLink;

public interface TicketDuplicateLinkRepository extends JpaRepository<TicketDuplicateLink, Long> {

    List<TicketDuplicateLink> findByDuplicateTicket_TicketIdAndLinkStatus(Long duplicateTicketId, String linkStatus);

    Optional<TicketDuplicateLink> findByPrimaryTicket_TicketIdAndDuplicateTicket_TicketIdAndLinkStatus(
            Long primaryTicketId, Long duplicateTicketId, String linkStatus);
}