package com.aiticketing.ai.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.aiticketing.ai.dto.KbSuggestionCandidate;

@Repository
public class KbEmbeddingJdbcRepository {

    private static final Logger KB_EMBEDDING_REPO_LOG = LoggerFactory.getLogger(KbEmbeddingJdbcRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public KbEmbeddingJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String findEmbedding(Long kbId) {
        KB_EMBEDDING_REPO_LOG.debug("KbEmbeddingJdbcRepository :: in findEmbedding() :: kbId={}", kbId);

        String sql = """
            SELECT kbe_embedding::text
            FROM kb_embeddings
            WHERE kbe_kb_id = ?
        """;

        List<String> rows = jdbcTemplate.query(
                sql,
                ps -> ps.setLong(1, kbId),
                (rs, rowNum) -> rs.getString(1)
        );

        KB_EMBEDDING_REPO_LOG.info("KbEmbeddingJdbcRepository :: exit findEmbedding() :: kbId={} embeddingPresent={}",
                kbId, !rows.isEmpty());

        return rows.isEmpty() ? null : rows.get(0);
    }

    public void upsertEmbedding(Long kbId, String embeddingVector) {
        KB_EMBEDDING_REPO_LOG.info("KbEmbeddingJdbcRepository :: in upsertEmbedding() :: kbId={}", kbId);
        //if insert hits an existing primary key conflict on kbe_kb_id
        //then run the update part instead where we overwrite old embedding with the new one being inserted
        String sql = """
            INSERT INTO kb_embeddings (kbe_kb_id, kbe_embedding, kbe_created_at, kbe_updated_at)
            VALUES (?, CAST(? AS vector), ?, ?)
            ON CONFLICT (kbe_kb_id)
            DO UPDATE SET
                kbe_embedding = EXCLUDED.kbe_embedding,
                kbe_updated_at = EXCLUDED.kbe_updated_at
        """;
        
        OffsetDateTime now = OffsetDateTime.now();
        jdbcTemplate.update(sql, kbId, embeddingVector, now, now);

        KB_EMBEDDING_REPO_LOG.info("KbEmbeddingJdbcRepository :: exit upsertEmbedding() :: kbId={}", kbId);
    }

    public List<KbSuggestionCandidate> findTopKCandidates(String embeddingVector, int k) {
        KB_EMBEDDING_REPO_LOG.debug("KbEmbeddingJdbcRepository :: in findTopKCandidates() :: k={}", k);

        String sql = """
            SELECT
                ka.kba_kb_id,
                ka.kba_title,
                ka.kba_body,
                (1 - (ke.kbe_embedding <=> CAST(? AS vector))) AS similarity
            FROM kb_embeddings ke
            JOIN kb_articles ka
              ON ka.kba_kb_id = ke.kbe_kb_id
            WHERE ka.kba_status = 'PUBLISHED'
            ORDER BY ke.kbe_embedding <=> CAST(? AS vector)
            LIMIT ?
        """;

        List<KbSuggestionCandidate> candidates = jdbcTemplate.query(
                sql,
                ps -> {
                    ps.setString(1, embeddingVector);
                    ps.setString(2, embeddingVector);
                    ps.setInt(3, k);
                },
                (rs, rowNum) -> mapCandidate(rs)
        );

        KB_EMBEDDING_REPO_LOG.info("KbEmbeddingJdbcRepository :: exit findTopKCandidates() :: count={}",
                candidates.size());

        return candidates;
    }

    private KbSuggestionCandidate mapCandidate(ResultSet rs) throws SQLException {
        KbSuggestionCandidate candidate = new KbSuggestionCandidate();
        candidate.kbId = rs.getLong("kba_kb_id");
        candidate.title = rs.getString("kba_title");
        candidate.body = rs.getString("kba_body");
        candidate.similarity = rs.getBigDecimal("similarity");
        return candidate;
    }
}