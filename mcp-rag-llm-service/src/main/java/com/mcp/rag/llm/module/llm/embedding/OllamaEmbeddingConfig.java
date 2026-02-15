package com.mcp.rag.llm.module.llm.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "llm.provider", havingValue = "ollama")
public class OllamaEmbeddingConfig {

    @Bean
    @Primary
    EmbeddingModel embeddingModel(OllamaEmbeddingModel model) {
        return model;
    }
}

