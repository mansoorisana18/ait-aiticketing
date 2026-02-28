package com.aiticketing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aiticketing.entity.CommentVisibility;
import com.aiticketing.entity.TicketComment;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

	//USER: only PUBLIC comments
	@Query(value = """
		    SELECT tc.* FROM ticket_comments tc
		    JOIN users u ON u.user_id = tc.tc_author_id
		    WHERE tc.tc_ticket_id = :ticketId
		      AND tc.tc_visibility = CAST(:visibility AS comment_visibility)
		    ORDER BY tc.tc_created_at ASC
		""", nativeQuery = true)
    List<TicketComment> findByTicketIdAndVisibilityWithAuthor(
            @Param("ticketId") Long ticketId,
            @Param("visibility") String visibility
    );

    //AGENT/ADMIN: PUBLIC+INTERNAL comments
    @Query("""
        SELECT c FROM TicketComment c
        JOIN FETCH c.author a
        WHERE c.ticket.ticketId = :ticketId
        ORDER BY c.createdAt ASC
    """)
    List<TicketComment> findByTicketIdWithAuthor(@Param("ticketId") Long ticketId);
}