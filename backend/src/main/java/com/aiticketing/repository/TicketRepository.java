package com.aiticketing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aiticketing.entity.Ticket;
import com.aiticketing.entity.enums.TicketStatus;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>{

		@Query("""
	        SELECT t FROM Ticket t
	        JOIN FETCH t.createdBy cb
	        LEFT JOIN FETCH t.assignedTo at
	        WHERE cb.userId = :userId
	        ORDER BY t.createdAt DESC
	    """)
	    List<Ticket> findByCreatorWithUsers(@Param("userId") Long userId);

	    // Any: get ticket by id (needs assigned + creator)
	    @Query("""
	        SELECT t FROM Ticket t
	        JOIN FETCH t.createdBy cb
	        LEFT JOIN FETCH t.assignedTo at
	        WHERE t.ticketId = :ticketId
	    """)
	    Optional<Ticket> findByIdWithUsers(@Param("ticketId") Long ticketId);

	    //Admin: get all tickets
	    @Query("""
	        SELECT t FROM Ticket t
	        JOIN FETCH t.createdBy cb
	        LEFT JOIN FETCH t.assignedTo at
	        ORDER BY t.createdAt DESC
	    """)
	    List<Ticket> findAllWithUsers();

	    //Agent: get assigned tickets
	    @Query("""
	       SELECT t FROM Ticket t
	       JOIN FETCH t.createdBy cb
	       JOIN FETCH t.assignedTo at
	       WHERE at.userId = :agentUserId
	       ORDER BY t.createdAt DESC
	    """)
	    List<Ticket> findTicketsAssignedToAgent(@Param("userId") Long agentUserId);
	    
	    //To find workload (active tickets) of all agents of that department
	    //Returns list of [assignedToUserId, count]
	    @Query("""
            SELECT t.assignedTo.userId, COUNT(t)
            FROM Ticket t
            WHERE t.assignedTo.userId IN :agentIds
              AND t.status IN :statuses
            GROUP BY t.assignedTo.userId
        """)
        List<Object[]> countActiveByAgentIds(
                @Param("agentIds") List<Long> agentIds,
                @Param("statuses") List<TicketStatus> statuses
        );
}
