package com.aiticketing.ai.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.aiticketing.ai.PromptLoader;
import com.aiticketing.ai.dto.DuplicateCandidate;
import com.aiticketing.ai.dto.DuplicateCheckOutcome;
import com.aiticketing.ai.dto.DuplicateCheckResult;
import com.aiticketing.ai.persistence.TicketEmbeddingJdbcRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DuplicateDetectionService {

    private static final Logger DUPLICATE_LOG = LoggerFactory.getLogger(DuplicateDetectionService.class);

    private final TicketEmbeddingJdbcRepository embeddingJdbcRepository;
    private final TicketEmbeddingGenerationService embeddingGenerationService;
    private final ChatClient chatClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;

    public DuplicateDetectionService(
            TicketEmbeddingJdbcRepository embeddingJdbcRepository,
            TicketEmbeddingGenerationService embeddingGenerationService,
            ChatClient chatClient,
            PromptLoader promptLoader,
            ObjectMapper objectMapper
    ) {
        this.embeddingJdbcRepository = embeddingJdbcRepository;
        this.embeddingGenerationService = embeddingGenerationService;
        this.chatClient = chatClient;
        this.promptLoader = promptLoader;
        this.objectMapper = objectMapper;
    }

    public DuplicateCheckOutcome checkDuplicate(long ticketId, int textVersion, String title, String description) {
    	DUPLICATE_LOG.info("DuplicateDetectionService :: in checkDuplicate :: ticketId={} textVersion={}}",
                ticketId, textVersion);
    	try {
    		//1. Fetch if the ticket's embedding already exists in tickets_embeddings table
            String embeddingVector = embeddingJdbcRepository.findEmbedding(ticketId, textVersion);

            //2. Embedding doesn't exist, use Spring AI's embedding model & insert the vector in tickets_embedding table
            if (embeddingVector == null) {
                embeddingVector = embeddingGenerationService.generateEmbeddingVector(title, description);
                embeddingJdbcRepository.insertEmbedding(ticketId, textVersion, embeddingVector);
            }

            //3. Finding top 5 similar tickets
            List<DuplicateCandidate> candidates =
                    embeddingJdbcRepository.findTopKCandidates(ticketId, embeddingVector, 5);
            
            DUPLICATE_LOG.info("DuplicateDetectionService :: in checkDuplicate :: Fetched duplicate candidates size={}",
                    candidates.size());
            
            //4. None of the tickets in system is worth evaluating for dupliacte check
            if (candidates.isEmpty()) {
                DuplicateCheckResult result = new DuplicateCheckResult();
                result.duplicateState = "NONE";
                result.primaryTicketId = null;
                result.confidence = BigDecimal.ZERO;
                result.similarity = null;
                result.threshold = null;
                result.reason = "No similar candidate tickets found";
                return DuplicateCheckOutcome.ok(result);
            }

            //5. Candidates exists, so we call LLM to give the DUPLICATE_CHECK response 
            String prompt = promptLoader.loadAndFormat(
                    "prompts/duplicate_check_v1.txt",
                    Map.of(
                            "ticketId", String.valueOf(ticketId),
                            "textVersion", String.valueOf(textVersion),
                            "title", safe(title),
                            "description", safe(description),
                            "candidatesJson", objectMapper.writeValueAsString(candidates)
                    )
            );

            String raw = chatClient.prompt().user(prompt).call().content();

            if (raw == null || raw.isBlank()) {
                return DuplicateCheckOutcome.fail("Empty duplicate-check LLM response");
            }

            JsonNode node = objectMapper.readTree(raw);

            //6. Set LLM returned response in DuplicateCheckResult
            DuplicateCheckResult result = new DuplicateCheckResult();
            result.duplicateState = node.path("duplicateState").asText(null);
            result.primaryTicketId = node.path("primaryTicketId").isNumber() ? node.path("primaryTicketId").asLong() : null;
            result.confidence = node.path("confidence").isNumber() ? node.path("confidence").decimalValue() : null;
            result.similarity = node.path("similarity").isNumber() ? node.path("similarity").decimalValue() : null;
            result.threshold = node.path("threshold").isNumber() ? node.path("threshold").decimalValue() : null;
            result.reason = jsonTextOrNull(node, "reason");

            //7. Set final DUPLICATE_CHECK AI pipeline response in DuplicateCheckOutcome
            if (result.duplicateState == null || result.duplicateState.isBlank()) {
                return DuplicateCheckOutcome.fail("Missing duplicateState from AI");
            }

            if (!List.of("NONE", "POTENTIAL", "CONFIRMED").contains(result.duplicateState)) {
                return DuplicateCheckOutcome.fail("Invalid duplicateState from AI: " + result.duplicateState);
            }

            if (!"NONE".equals(result.duplicateState) && result.primaryTicketId == null) {
                return DuplicateCheckOutcome.fail("Missing primaryTicketId for duplicate outcome");
            }

            return DuplicateCheckOutcome.ok(result);

        } catch (Exception ex) {
            DUPLICATE_LOG.error("DuplicateDetectionService :: checkDuplicate failed :: ticketId={} textVersion={} err={}",
                    ticketId, textVersion, ex.toString(), ex);
            return DuplicateCheckOutcome.fail("Duplicate detection failed: " + ex.getMessage());
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace("\n", " ").trim();
    }

    private static String jsonTextOrNull(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) {
            return null;
        }
        String value = node.get(field).asText(null);
        return value == null ? null : value.trim();
    }
}