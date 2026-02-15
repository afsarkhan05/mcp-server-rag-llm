package com.mcp.rag.llm.module.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RagResponse {
    private String answer;
    private List<RagSource> sources;
    private String modelUsed;
    private long latencyMs;
}
