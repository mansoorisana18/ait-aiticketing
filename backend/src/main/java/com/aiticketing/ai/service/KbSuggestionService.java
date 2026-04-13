package com.aiticketing.ai.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aiticketing.ai.PromptLoader;
import com.aiticketing.ai.dto.KbSuggestionCandidate;
import com.aiticketing.ai.dto.KbSuggestionOutcome;
import com.aiticketing.ai.dto.KbSuggestionResult;
import com.aiticketing.ai.persistence.KbEmbeddingJdbcRepository;
import com.aiticketing.ai.persistence.TicketEmbeddingJdbcRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class KbSuggestionService {

    private static final Logger KB_SUGGESTION_LOG = LoggerFactory.getLogger(KbSuggestionService.class);

    private final KbEmbeddingJdbcRepository kbEmbeddingJdbcRepository;
    private final TicketEmbeddingJdbcRepository ticketEmbeddingJdbcRepository;
    private final TicketEmbeddingGenerationService ticketEmbeddingGenerationService;
    private final ChatClient chatClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;
    private final BigDecimal retrievalThreshold;
    private final int topK;

    public KbSuggestionService(
            KbEmbeddingJdbcRepository kbEmbeddingJdbcRepository,
            TicketEmbeddingJdbcRepository ticketEmbeddingJdbcRepository,
            TicketEmbeddingGenerationService ticketEmbeddingGenerationService,
            ChatClient chatClient,
            PromptLoader promptLoader,
            ObjectMapper objectMapper,
            @Value("${aiticketing.ai.kb-suggestion.retrieval-threshold}") BigDecimal retrievalThreshold,
            @Value("${aiticketing.ai.kb-suggestion.top-k}") int topK
    ) {
        this.kbEmbeddingJdbcRepository = kbEmbeddingJdbcRepository;
        this.ticketEmbeddingJdbcRepository = ticketEmbeddingJdbcRepository;
        this.ticketEmbeddingGenerationService = ticketEmbeddingGenerationService;
        this.chatClient = chatClient;
        this.promptLoader = promptLoader;
        this.objectMapper = objectMapper;
        this.retrievalThreshold = retrievalThreshold;
        this.topK = topK;
    }

    public KbSuggestionOutcome suggestKb(long ticketId, int textVersion, String title, String description) {
        KB_SUGGESTION_LOG.info("KbSuggestionService :: in suggestKb() :: ticketId={} textVersion={}",
                ticketId, textVersion);

        try {
            //1. Reuse stored ticket embedding if present as duplicate detection should already have
            //created/reused the embedding before KB_SUGGESTION_REQUESTED stage is reached.
            String embeddingVector = ticketEmbeddingJdbcRepository.findEmbedding(ticketId, textVersion);

            //2. Defensive fallback only
            if (embeddingVector == null) {
                KB_SUGGESTION_LOG.warn(
                        "KbSuggestionService :: suggestKb() :: missing ticket embedding unexpectedly :: ticketId={} textVersion={} :: regenerating fallback",
                        ticketId, textVersion);

                embeddingVector = ticketEmbeddingGenerationService.generateEmbeddingVector(title, description);
                ticketEmbeddingJdbcRepository.insertEmbedding(ticketId, textVersion, embeddingVector);
            }

            //3. Retrieve top-K published KB article candidates
            List<KbSuggestionCandidate> candidates =
                    kbEmbeddingJdbcRepository.findTopKCandidates(embeddingVector, topK);

            KB_SUGGESTION_LOG.info(
                    "KbSuggestionService :: suggestKb() :: candidateSearchComplete :: ticketId={} candidateCount={}",
                    ticketId, candidates.size());

            //4. No candidates at all
            if (candidates.isEmpty()) {
                KbSuggestionResult result = new KbSuggestionResult();
                result.suggestionFound = false;
                result.kbId = null;
                result.kbTitle = null;
                result.kbPreview = null;
                result.confidence = null;
                result.similarity = null;
                result.threshold = retrievalThreshold;
                result.reason = "No published KB articles available for suggestion";
                return KbSuggestionOutcome.ok(result);
            }

            //5. Retrieval floor check before calling LLM
            KbSuggestionCandidate bestCandidate = candidates.get(0);
            if (bestCandidate.similarity == null || bestCandidate.similarity.compareTo(retrievalThreshold) < 0) {
                KbSuggestionResult result = new KbSuggestionResult();
                result.suggestionFound = false;
                result.kbId = null;
                result.kbTitle = null;
                result.kbPreview = null;
                result.confidence = null;
                result.similarity = bestCandidate.similarity;
                result.threshold = retrievalThreshold;
                result.reason = "Top KB candidate did not meet retrieval threshold";
                return KbSuggestionOutcome.ok(result);
            }
            
            KB_SUGGESTION_LOG.debug("KbSuggestionService :: suggestKb() :: top candidate's similarity is greater than thresohld :: ticketId={} bestCandidateSimilarity={}", ticketId, bestCandidate.similarity);
            
            //6. Send top-K candidates to LLM for final decision
            String prompt = promptLoader.loadAndFormat(
                    "prompts/kb_suggestion_rerank_v1.txt",
                    Map.of(
                            "ticketId", String.valueOf(ticketId),
                            "textVersion", String.valueOf(textVersion),
                            "title", safe(title),
                            "description", safe(description),
                            "candidatesJson", objectMapper.writeValueAsString(candidates)
                    )
            );

            KB_SUGGESTION_LOG.debug(
                    "KbSuggestionService :: suggestKb() :: rerankPromptBuilt :: ticketId={} promptChars={} prompt={}",
                    ticketId, prompt.length(), prompt);

            String raw = chatClient.prompt().user(prompt).call().content();

            KB_SUGGESTION_LOG.info(
                    "KbSuggestionService :: suggestKb() :: rerankResponseReceived :: ticketId={} rawChars={}",
                    ticketId, raw == null ? 0 : raw.length());
            KB_SUGGESTION_LOG.debug(
                    "KbSuggestionService :: suggestKb() :: rerankResponseRaw :: ticketId={} raw={}",
                    ticketId, raw);

            if (raw == null || raw.isBlank()) {
                return KbSuggestionOutcome.fail("Empty KB rerank LLM response");
            }

            JsonNode node = objectMapper.readTree(raw);

            boolean shouldSuggest = node.path("shouldSuggest").asBoolean(false);
            Long selectedKbId = node.path("selectedKbId").isNumber() ? node.path("selectedKbId").asLong() : null;
            BigDecimal confidence = node.path("confidence").isNumber() ? node.path("confidence").decimalValue() : null;
            String reason = jsonTextOrNull(node, "reason");

            if (shouldSuggest && selectedKbId == null) {
                return KbSuggestionOutcome.fail("LLM returned shouldSuggest=true but selectedKbId is missing");
            }

            if (!shouldSuggest) {
                KbSuggestionResult result = new KbSuggestionResult();
                result.suggestionFound = false;
                result.kbId = null;
                result.kbTitle = null;
                result.kbPreview = null;
                result.confidence = confidence;
                result.similarity = bestCandidate.similarity;
                result.threshold = retrievalThreshold;
                result.reason = reason == null ? "LLM determined no KB should be suggested" : reason;
                return KbSuggestionOutcome.ok(result);
            }

            KbSuggestionCandidate selectedCandidate = candidates.stream()
                    .filter(c -> c.kbId != null && c.kbId.equals(selectedKbId))
                    .findFirst()
                    .orElse(null);

            if (selectedCandidate == null) {
                return KbSuggestionOutcome.fail("LLM selected a KB not present in retrieved candidates");
            }

            KbSuggestionResult result = new KbSuggestionResult();
            result.suggestionFound = true;
            result.kbId = selectedCandidate.kbId;
            result.kbTitle = selectedCandidate.title;
            result.kbPreview = selectedCandidate.bodyPreview;
            result.confidence = confidence;
            result.similarity = selectedCandidate.similarity;
            result.threshold = retrievalThreshold;
            result.reason = reason == null ? "LLM selected KB as useful for self-resolution" : reason;

            KB_SUGGESTION_LOG.info(
                    "KbSuggestionService :: exit suggestKb() :: ticketId={} suggestionFound=true kbId={} similarity={} confidence={}",
                    ticketId, result.kbId, result.similarity, result.confidence);

            return KbSuggestionOutcome.ok(result);

        } catch (Exception ex) {
            KB_SUGGESTION_LOG.error(
                    "KbSuggestionService :: exit suggestKb() failed :: ticketId={} textVersion={} err={}",
                    ticketId, textVersion, ex.toString(), ex);
            return KbSuggestionOutcome.fail("KB suggestion failed: " + ex.getMessage());
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String jsonTextOrNull(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) {
            return null;
        }
        String value = node.get(field).asText(null);
        return value == null ? null : value.trim();
    }
}