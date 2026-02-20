package com.aiticketing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aiticketing.entity.Ticket;
import com.aiticketing.entity.User;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>{

		@Query("""
	        SELECT t FROM Ticket t
	        JOIN FETCH t.createdBy cb
	        left join fetch t.assignedTo at
	        WHERE cb.userId = :userId
	        ORDER BY t.createdAt desc
	    """)
	    List<Ticket> findByCreatorWithUsers(@Param("userId") Long userId);

	    // Any: get ticket by id (needs assigned + creator)
	    @Query("""
	        SELECT t FROM Ticket t
	        JOIN FETCH t.createdBy cb
	        left join fetch t.assignedTo at
	        WHERE t.ticketId = :ticketId
	    """)
	    Optional<Ticket> findByIdWithUsers(@Param("ticketId") Long ticketId);

	    //Admin: get all tickets
	    @Query("""
	        SELECT t FROM Ticket t
	        JOIN FETCH t.createdBy cb
	        left join fetch t.assignedTo at
	        ORDER BY t.createdAt desc
	    """)
	    List<Ticket> findAllWithUsers();

	    //Agent: get assigned tickets
	    @Query("""
	       SELECT t FROM Ticket t
	       JOIN FETCH t.assignedTo
	       WHERE t.assignedTo.userId = :userId
	       ORDER BY t.createdAt DESC
	       """)
	    List<Ticket> findTicketsAssignedToAgent(@Param("userId") Long userId);

}
