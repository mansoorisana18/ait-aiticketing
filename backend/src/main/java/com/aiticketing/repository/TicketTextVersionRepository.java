package com.aiticketing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aiticketing.entity.TicketTextVersion;

@Repository
public interface TicketTextVersionRepository extends JpaRepository<TicketTextVersion, Long>{
	List<TicketTextVersion> findByTicketIdOrderByVersionNoDesc(Long ticketId);
}
