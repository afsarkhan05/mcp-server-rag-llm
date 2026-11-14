package com.mcp.rag.llm.module.controller;

import com.mcp.rag.llm.module.llm.LLMProvider;
import com.mcp.rag.llm.module.model.RagResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rag")
public class AIController {
    private final LLMProvider llmProvider;

    public AIController(LLMProvider llmProvider) {
        this.llmProvider = llmProvider;
    }

    @GetMapping("/ask")
    public ResponseEntity<RagResponse> askQuestion(@RequestParam String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be empty");
        }
        return ResponseEntity.ok(llmProvider.generateAnswer(question));
    }
}
