package com.aiticketing.ai.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.aiticketing.ai.dto.DuplicateCandidate;

@Repository
public class TicketEmbeddingJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger TICKET_EMBEDDING_REPO_LOG = LoggerFactory.getLogger(TicketEmbeddingJdbcRepository.class);
    
    public TicketEmbeddingJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String findEmbedding(Long ticketId, Integer textVersion) {
    	TICKET_EMBEDDING_REPO_LOG.debug("TicketEmbeddingJdbcRepository :: in findEmbedding :: ticketId={} textVersion={}}",
                ticketId, textVersion);
    	String sql = """
            SELECT te_embedding::text
            FROM ticket_embeddings
            WHERE te_ticket_id = ? AND te_text_version = ?
        """;

        List<String> rows = jdbcTemplate.query(
                sql,
                ps -> {
                    ps.setLong(1, ticketId);
                    ps.setInt(2, textVersion);
                },
                (rs, rowNum) -> rs.getString(1)
        );
        
        TICKET_EMBEDDING_REPO_LOG.info("TicketEmbeddingJdbcRepository :: exit findEmbedding :: ticketId={} textVersion={} embeddingPresent={}}",
                ticketId, textVersion, !rows.isEmpty());
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void insertEmbedding(Long ticketId, Integer textVersion, String embeddingVector) {
        String sql = """
            INSERT INTO ticket_embeddings (te_ticket_id, te_text_version, te_embedding, te_created_at)
            VALUES (?, ?, CAST(? AS vector), ?)
        """;

        jdbcTemplate.update(sql, ticketId, textVersion, embeddingVector, OffsetDateTime.now());
    }

    public List<DuplicateCandidate> findTopKCandidates(Long currentTicketId, String embeddingVector, int k) {
        //<=> is the cosine-distance operator in pgvector. Lower the distance more similar are the vectors (tickets) here
    	String sql = """
            SELECT
                t.ticket_id,
                t.ticket_title,
                t.ticket_description,
                t.ticket_ai_category,
                t.ticket_status::text AS ticket_status,
                (1 - (te.te_embedding <=> CAST(? AS vector))) AS similarity
            FROM ticket_embeddings te
            JOIN tickets t
              ON t.ticket_id = te.te_ticket_id
            WHERE te.te_ticket_id <> ?
              AND t.ticket_status IN ('READY', 'IN_PROGRESS')
            ORDER BY te.te_embedding <=> CAST(? AS vector)
            LIMIT ?
        """;

        return jdbcTemplate.query(
                sql,
                ps -> {
                    ps.setString(1, embeddingVector);
                    ps.setLong(2, currentTicketId);
                    ps.setString(3, embeddingVector);
                    ps.setInt(4, k);
                },
                (rs, rowNum) -> mapCandidate(rs)
        );
    }

    private DuplicateCandidate mapCandidate(ResultSet rs) throws SQLException {
        DuplicateCandidate candidate = new DuplicateCandidate();
        candidate.ticketId = rs.getLong("ticket_id");
        candidate.title = rs.getString("ticket_title");
        candidate.description = rs.getString("ticket_description");
        candidate.category = rs.getString("ticket_ai_category");
        candidate.status = rs.getString("ticket_status");
        candidate.similarity = rs.getBigDecimal("similarity");
        return candidate;
    }
}