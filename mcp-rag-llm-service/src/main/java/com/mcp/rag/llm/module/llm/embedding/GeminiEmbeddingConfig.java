package com.mcp.rag.llm.module.llm.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;
import org.springframework.ai.model.google.genai.autoconfigure.embedding.GoogleGenAiTextEmbeddingProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
@Configuration
@ConditionalOnProperty(name = "llm.provider", havingValue = "gemini")
public class GeminiEmbeddingConfig {

    @Value("${spring.ai.google.genai.api-key}")
    private String apiKey;

    @Value("${spring.ai.google.genai.embedding.model}")
    private String model;

    private String defaultProject = "gen-lang-client-0437696354";



    @Bean
    @ConditionalOnProperty(name = "llm.provider", havingValue = "gemini")
    @Primary
    public EmbeddingModel embeddingModel(GoogleGenAiTextEmbeddingProperties properties) {
        // satisfying the 'must be set' requirement with a placeholder
        var connectionDetails = GoogleGenAiEmbeddingConnectionDetails.builder()
                .apiKey(apiKey)
                .projectId(defaultProject) // Satisfies Spring's internal Assert.hasText()
                .build();

        GoogleGenAiTextEmbeddingOptions props = properties.getOptions();
        props.setModel(model);
        // Pulling options from your YML properties for maximum flexibility
        return new GoogleGenAiTextEmbeddingModel(connectionDetails, props);
    }

    @Bean
    @ConditionalOnProperty(name = "llm.provider", havingValue = "gemini")
    public GoogleGenAiEmbeddingConnectionDetails googleGenAiEmbeddingConnectionDetails() {
        // We manually use the builder here.
        // We provide a placeholder for projectId to satisfy the internal Assert.hasText check.
        return GoogleGenAiEmbeddingConnectionDetails.builder()
                .apiKey(apiKey)
                .projectId(defaultProject)
                .build();
    }
}

