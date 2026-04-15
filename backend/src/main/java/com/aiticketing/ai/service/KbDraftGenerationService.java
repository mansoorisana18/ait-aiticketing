package com.aiticketing.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.aiticketing.ai.PromptLoader;
import com.aiticketing.ai.dto.KbDraftGenerationOutcome;
import com.aiticketing.ai.dto.KbDraftGenerationResult;
import com.aiticketing.entity.TicketComment;
import com.aiticketing.entity.enums.CommentVisibility;
import com.aiticketing.repository.TicketCommentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class KbDraftGenerationService {

    private static final Logger KB_DRAFT_LOG = LoggerFactory.getLogger(KbDraftGenerationService.class);

    private final TicketCommentRepository ticketCommentRepo;
    private final ChatClient chatClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;

    public KbDraftGenerationService(
            TicketCommentRepository ticketCommentRepo,
            ChatClient chatClient,
            PromptLoader promptLoader,
            ObjectMapper objectMapper
    ) {
        this.ticketCommentRepo = ticketCommentRepo;
        this.chatClient = chatClient;
        this.promptLoader = promptLoader;
        this.objectMapper = objectMapper;
    }

    public KbDraftGenerationOutcome generateDraft(
            long ticketId,
            int textVersion,
            String title,
            String description,
            String category,
            List<Long> selectedCommentIds
    ) {
        KB_DRAFT_LOG.info("KbDraftGenerationService :: in generateDraft() :: ticketId={} textVersion={} selectedCommentIds={}",
               ticketId, textVersion, selectedCommentIds);

        try {
            if (selectedCommentIds == null || selectedCommentIds.isEmpty()) {
                return KbDraftGenerationOutcome.fail("No selected public comments provided for KB draft generation");
            }

            List<TicketComment> ticketComments = ticketCommentRepo.findByTicketIdWithAuthor(ticketId);
            List<TicketComment> selectedPublicComments = resolveSelectedPublicComments(ticketComments, selectedCommentIds);

            if (selectedPublicComments.isEmpty()) {
                return KbDraftGenerationOutcome.fail("No valid PUBLIC comments found for KB draft generation");
            }

            String selectedCommentsText = buildSelectedCommentsText(selectedPublicComments);

            String prompt = promptLoader.loadAndFormat(
                    "prompts/kb_draft_generation_v1.txt",
                    Map.of(
                            "ticketId", String.valueOf(ticketId),
                            "textVersion", String.valueOf(textVersion),
                            "title", safe(title),
                            "description", safe(description),
                            "category", safe(category),
                            "selectedCommentsText", selectedCommentsText
                    )
            );

            KB_DRAFT_LOG.debug("KbDraftGenerationService :: generateDraft() promptBuilt :: ticketId={} promptChars={}",
                    ticketId, prompt.length());

            String raw = chatClient.prompt().user(prompt).call().content();

            KB_DRAFT_LOG.info("KbDraftGenerationService :: generateDraft() llmResponseReceived :: ticketId={} rawChars={}",
                    ticketId, raw == null ? 0 : raw.length());
            KB_DRAFT_LOG.debug("KbDraftGenerationService :: generateDraft() llmResponseRaw :: ticketId={} raw={}",
                    ticketId, raw);

            if (raw == null || raw.isBlank()) {
                return KbDraftGenerationOutcome.fail("Empty KB draft LLM response");
            }

            JsonNode node = objectMapper.readTree(raw);

            KbDraftGenerationResult result = new KbDraftGenerationResult();
            result.title = jsonTextOrNull(node, "title");
            result.body = jsonTextOrNull(node, "body");
            result.confidence = node.path("confidence").isNumber() ? node.path("confidence").decimalValue() : null;
            result.reason = jsonTextOrNull(node, "reason");
            result.rawOutputJson = raw;

            if (result.title == null || result.title.isBlank()) {
                return KbDraftGenerationOutcome.fail("KB draft title missing from AI response");
            }

            if (result.body == null || result.body.isBlank()) {
                return KbDraftGenerationOutcome.fail("KB draft body missing from AI response");
            }

            KB_DRAFT_LOG.info("KbDraftGenerationService :: exit generateDraft() :: ticketId={} draftTitle={}",
                    ticketId, result.title);

            return KbDraftGenerationOutcome.ok(result);

        } catch (Exception ex) {
            KB_DRAFT_LOG.error("KbDraftGenerationService :: exit generateDraft() failed :: ticketId={} err={}",
                    ticketId, ex.toString(), ex);
            return KbDraftGenerationOutcome.fail("KB draft generation failed: " + ex.getMessage());
        }
    }

    private List<TicketComment> resolveSelectedPublicComments(List<TicketComment> ticketComments, List<Long> selectedCommentIds) {
        Map<Long, TicketComment> commentsById = ticketComments.stream()
                .collect(Collectors.toMap(TicketComment::getCommentId, c -> c));

        List<TicketComment> selectedComments = new ArrayList<>();

        for (Long commentId : selectedCommentIds) {
            TicketComment comment = commentsById.get(commentId);

            if (comment == null) {
                throw new IllegalStateException("Selected comment does not belong to ticket: " + commentId);
            }

            if (comment.getVisibility() != CommentVisibility.PUBLIC) {
                throw new IllegalStateException("Only PUBLIC comments can be used for KB drafting: " + commentId);
            }

            selectedComments.add(comment);
        }

        return selectedComments;
    }

    private String buildSelectedCommentsText(List<TicketComment> selectedComments) {
        StringBuilder sb = new StringBuilder();

        for (TicketComment comment : selectedComments) {
            sb.append("- Comment ID: ").append(comment.getCommentId()).append("\n");
            sb.append("  Author: ").append(comment.getAuthor() != null ? safe(comment.getAuthor().getUsername()) : "Unknown").append("\n");
            sb.append("  Body: ").append(safe(comment.getBody())).append("\n");
        }

        return sb.toString().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\n", " ").trim();
    }

    private static String jsonTextOrNull(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) {
            return null;
        }
        String value = node.get(field).asText(null);
        return value == null ? null : value.trim();
    }
}