package com.aiticketing.ai.service;

import static com.aiticketing.ai.Taxonomy.CATEGORY_OPTIONS;
import static com.aiticketing.ai.Taxonomy.categoriesForPrompt;
import static com.aiticketing.ai.Taxonomy.normalize;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.aiticketing.ai.PromptLoader;
import com.aiticketing.ai.dto.TriageOutcome;
import com.aiticketing.ai.dto.TriageResult;
import com.aiticketing.entity.enums.TicketPriority;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AiTriageService {

	private static final Logger AI_TRIAGE_LOG = LoggerFactory.getLogger(AiTriageService.class);
	
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final PromptLoader promptLoader;

    public AiTriageService(ChatClient chatClient, ObjectMapper objectMapper, PromptLoader promptLoader) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
        this.promptLoader = promptLoader;
    }

    //Returns TriageOutcome.ok() on successful triage or TriageOutcome.fail() on any error so that the worker can retry/backoff  
    public TriageOutcome triage(long ticketId, int textVersion, String title, String description) {
        String prompt = null;
        String raw = null;
    	try {
    		AI_TRIAGE_LOG.info("AiTriageService :: in triage() start :: ticketId={} textVersion={}", ticketId, textVersion);

            prompt = promptLoader.loadAndFormat(
                    "prompts/triage_v1.txt",
                    Map.of(
                            "ticketId", String.valueOf(ticketId),
                            "textVersion", String.valueOf(textVersion),
                            "title", safe(title),
                            "description", safe(description),
                            "categoryList", categoriesForPrompt()
                    )
            );

            AI_TRIAGE_LOG.debug("AiTriageService :: in triage() promptBuilt :: ticketId={} promptChars={}",
                    ticketId, prompt.length());
            
            raw = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            AI_TRIAGE_LOG.info("AiTriageService :: in triage() llmResponseReceived :: ticketId={} rawChars={}",
                    ticketId, (raw == null ? 0 : raw.length()));
            AI_TRIAGE_LOG.debug("AiTriageService :: in triage() llmResponseReceived :: ticketId={} raw={}",
                    ticketId, raw);
            
            if (raw == null || raw.isBlank()) {
            	return TriageOutcome.fail("Empty LLM response");
            }
            
            JsonNode node = objectMapper.readTree(raw);

            String category = normalize(node.path("category").asText(null));
            String priorityStr = normalize(node.path("priority").asText(null));

            if (category == null || !CATEGORY_OPTIONS.contains(category)) {
            	AI_TRIAGE_LOG.warn("AiTriageService :: in triage() invalidCategory :: ticketId={} category={} raw={}",
                        ticketId, category, raw);
            	return TriageOutcome.fail("Invalid category from AI: " + category);
            }
            
            if (priorityStr == null) {
            	AI_TRIAGE_LOG.warn("AiTriageService :: in triage() missingPriority :: ticketId={} priority={} raw={}",
                        ticketId, priorityStr, raw);
                return TriageOutcome.fail("Missing priority from AI");
            }

            TicketPriority priority;
            try {
                priority = TicketPriority.valueOf(priorityStr);
            } catch (IllegalArgumentException ex) {
            	AI_TRIAGE_LOG.warn("AiTriageService :: in triage() invalidPriority :: ticketId={} priority={} raw={}",
                        ticketId, priorityStr, raw);
                return TriageOutcome.fail("Invalid priority from AI: " + priorityStr);
            }

            Boolean isVague = node.path("isVague").isBoolean() ? node.path("isVague").asBoolean() : null;
            if (isVague == null) {
                return TriageOutcome.fail("Missing isVague from AI");
            }

            String vagueReason = jsonTextOrNull(node, "vagueReason");
            String clarificationPrompt = jsonTextOrNull(node, "clarificationPrompt");

            if (Boolean.TRUE.equals(isVague)) {
                if (vagueReason == null || vagueReason.isBlank()) {
                    return TriageOutcome.fail("Missing vagueReason for vague ticket");
                }
                if (clarificationPrompt == null || clarificationPrompt.isBlank()) {
                    return TriageOutcome.fail("Missing clarificationPrompt for vague ticket");
                }
            } else {
                vagueReason = null;
                clarificationPrompt = null;
            }
            
            TriageResult r = new TriageResult();
            r.category = category;
            r.priority = priority;
            r.confidence = node.path("confidence").isNumber() ? node.path("confidence").decimalValue() : null;
            r.isVague = isVague;
            r.vagueReason = vagueReason;
            r.clarificationPrompt = clarificationPrompt;
            
            AI_TRIAGE_LOG.info("AiTriageService :: exit triage() success :: ticketId={} category={} priority={} confidence={} isVague={}",
                    ticketId, r.category, r.priority, r.confidence, r.isVague);
            
            return TriageOutcome.ok(r);

        } catch (Exception e) {
        	AI_TRIAGE_LOG.error("AiTriageService :: exit triage() failed :: ticketId={} textVersion={} err={}",
                    ticketId, textVersion, e.toString());
            return TriageOutcome.fail("AI triage failed: " + e.getMessage());
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