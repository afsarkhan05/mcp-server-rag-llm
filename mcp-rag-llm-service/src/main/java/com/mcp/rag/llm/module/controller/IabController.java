package com.mcp.rag.llm.module.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IabController {
    @GetMapping("/")
    public String index() {
        return "MCP Server is Running. Use /sse for protocol access.";
    }
}
