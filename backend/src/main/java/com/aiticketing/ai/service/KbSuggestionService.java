package com.aiticketing.ai.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aiticketing.ai.dto.KbSuggestionCandidate;
import com.aiticketing.ai.dto.KbSuggestionOutcome;
import com.aiticketing.ai.dto.KbSuggestionResult;
import com.aiticketing.ai.persistence.KbEmbeddingJdbcRepository;
import com.aiticketing.ai.persistence.TicketEmbeddingJdbcRepository;

@Service
public class KbSuggestionService {

    private static final Logger KB_SUGGESTION_LOG = LoggerFactory.getLogger(KbSuggestionService.class);

    private final KbEmbeddingJdbcRepository kbEmbeddingJdbcRepository;
    private final TicketEmbeddingJdbcRepository ticketEmbeddingJdbcRepository;
    private final TicketEmbeddingGenerationService ticketEmbeddingGenerationService;
    private final BigDecimal similarityThreshold;
    private final int topK;

    public KbSuggestionService(
            KbEmbeddingJdbcRepository kbEmbeddingJdbcRepository,
            TicketEmbeddingJdbcRepository ticketEmbeddingJdbcRepository,
            TicketEmbeddingGenerationService ticketEmbeddingGenerationService,
            @Value("${aiticketing.ai.kb-suggestion.threshold}") BigDecimal similarityThreshold,
            @Value("${aiticketing.ai.kb-suggestion.top-k}") int topK
    ) {
        this.kbEmbeddingJdbcRepository = kbEmbeddingJdbcRepository;
        this.ticketEmbeddingJdbcRepository = ticketEmbeddingJdbcRepository;
        this.ticketEmbeddingGenerationService = ticketEmbeddingGenerationService;
        this.similarityThreshold = similarityThreshold;
        this.topK = topK;
    }

    public KbSuggestionOutcome suggestKb(long ticketId, int textVersion, String title, String description) {
        KB_SUGGESTION_LOG.info("KbSuggestionService :: in suggestKb() :: ticketId={} textVersion={}",
                ticketId, textVersion);

        try {
            //1) Reuse stored ticket embedding if present as duplicate detection should already have
            //created/reused the embedding before KB_SUGGESTION_REQUESTED stage is reached.
            String embeddingVector = ticketEmbeddingJdbcRepository.findEmbedding(ticketId, textVersion);
           
            if (embeddingVector == null) {
                KB_SUGGESTION_LOG.warn(
                        "KbSuggestionService :: suggestKb() :: missing ticket embedding unexpectedly :: ticketId={} textVersion={} :: regenerating fallback",
                        ticketId, textVersion);

                embeddingVector = ticketEmbeddingGenerationService.generateEmbeddingVector(title, description);
                ticketEmbeddingJdbcRepository.insertEmbedding(ticketId, textVersion, embeddingVector);
            } else {
                KB_SUGGESTION_LOG.debug(
                        "KbSuggestionService :: suggestKb() :: reusing stored ticket embedding :: ticketId={} textVersion={}",
                        ticketId, textVersion);
            }

            //2) Search top-K published KB article candidates
            List<KbSuggestionCandidate> candidates =
                    kbEmbeddingJdbcRepository.findTopKCandidates(embeddingVector, topK);

            KB_SUGGESTION_LOG.info(
                    "KbSuggestionService :: suggestKb() :: candidateSearchComplete :: ticketId={} candidateCount={}",
                    ticketId, candidates.size());

            //3) No KBs available to suggest
            if (candidates.isEmpty()) {
                KbSuggestionResult result = new KbSuggestionResult();
                result.suggestionFound = false;
                result.kbId = null;
                result.kbTitle = null;
                result.kbPreview = null;
                result.similarity = null;
                result.threshold = similarityThreshold;
                result.reason = "No published KB articles available for suggestion";
                return KbSuggestionOutcome.ok(result);
            }

            //4) Take the best candidate (highest similarity)
            KbSuggestionCandidate best = candidates.get(0);

            //5) If the best match is below threshold, do not suggest anything
            if (best.similarity == null || best.similarity.compareTo(similarityThreshold) < 0) {
                KbSuggestionResult result = new KbSuggestionResult();
                result.suggestionFound = false;
                result.kbId = null;
                result.kbTitle = null;
                result.kbPreview = null;
                result.similarity = best.similarity == null ? null : scale(best.similarity);
                result.threshold = similarityThreshold;
                result.reason = "Top KB similarity did not meet threshold";
                return KbSuggestionOutcome.ok(result);
            }

            //6) Valid suggestion
            KbSuggestionResult result = new KbSuggestionResult();
            result.suggestionFound = true;
            result.kbId = best.kbId;
            result.kbTitle = safe(best.title);
            result.kbPreview = buildPreview(best.body);
            result.similarity = scale(best.similarity);
            result.threshold = similarityThreshold;
            result.reason = "Suggested based on embedding similarity threshold match";

            KB_SUGGESTION_LOG.info(
                    "KbSuggestionService :: exit suggestKb() :: ticketId={} suggestionFound=true kbId={} similarity={}",
                    ticketId, result.kbId, result.similarity);

            return KbSuggestionOutcome.ok(result);

        } catch (Exception ex) {
            KB_SUGGESTION_LOG.error(
                    "KbSuggestionService :: exit suggestKb() failed :: ticketId={} textVersion={} err={}",
                    ticketId, textVersion, ex.toString(), ex);
            return KbSuggestionOutcome.fail("KB suggestion failed: " + ex.getMessage());
        }
    }

    private String buildPreview(String body) {
        String safeBody = safe(body);
        int previewLength = 260;
        if (safeBody.length() <= previewLength) {
            return safeBody;
        }
        return safeBody.substring(0, previewLength).trim() + "...";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(5, RoundingMode.HALF_UP);
    }
}