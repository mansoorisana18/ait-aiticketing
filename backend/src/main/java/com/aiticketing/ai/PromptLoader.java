package com.aiticketing.ai;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class PromptLoader {

	private static final Logger PROMPT_LOADER_LOG = LoggerFactory.getLogger(PromptLoader.class);
	
    public String loadAndFormat(String classpathLocation, Map<String, String> vars) {
        String template = readClasspathFile(classpathLocation);
        String out = template;
        for (var e : vars.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", e.getValue());
        }
        return out;
    }

    private String readClasspathFile(String path) {
    	try {
            ClassPathResource resource = new ClassPathResource(path);

            PROMPT_LOADER_LOG.info("PromptLoader :: in readClasspathFile() :: reading prompt from classpath path={}", path);

            if (!resource.exists()) {
                throw new IllegalStateException("Prompt file not found on classpath: " + path);
            }

            try (InputStream is = resource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read prompt: " + path, e);
        }
    }
}