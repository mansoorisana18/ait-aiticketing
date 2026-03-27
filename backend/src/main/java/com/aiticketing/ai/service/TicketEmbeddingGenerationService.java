package com.aiticketing.ai.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

@Service
public class TicketEmbeddingGenerationService {

    private final EmbeddingModel embeddingModel;
    private static final Logger TICKET_EMBEDDING_GEN_LOG = LoggerFactory.getLogger(TicketEmbeddingGenerationService.class);
    
    public TicketEmbeddingGenerationService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public String generateEmbeddingVector(String title, String description) {
    	TICKET_EMBEDDING_GEN_LOG.debug("TicketEmbeddingGenerationService :: in generateEmbeddingVector");
    	String text = buildEmbeddingText(title, description);

        EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            throw new IllegalStateException("Embedding model returned empty response");
        }

        float[] vector = response.getResults().get(0).getOutput();
        if (vector == null || vector.length == 0) {
            throw new IllegalStateException("Embedding model returned empty vector");
        }

        return toPgVectorLiteral(vector);
    }

    private String buildEmbeddingText(String title, String description) {
        String safeTitle = title == null ? "" : title.trim();
        String safeDescription = description == null ? "" : description.trim();
        return "Title: " + safeTitle + "\nDescription: " + safeDescription;
    }

    private String toPgVectorLiteral(float[] vector) {
        return "[" + java.util.Arrays.stream(toDoubleArray(vector))
                .mapToObj(Double::toString)
                .collect(Collectors.joining(",")) + "]";
    }

    private double[] toDoubleArray(float[] input) {
        double[] result = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = input[i];
        }
        return result;
    }
}