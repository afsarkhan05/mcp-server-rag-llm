package com.mcp.rag.llm.module.llm.embedding;

import org.springframework.ai.embedding.EmbeddingModel;

public interface EmbeddingProvider {
    EmbeddingModel embeddingModel();
}

