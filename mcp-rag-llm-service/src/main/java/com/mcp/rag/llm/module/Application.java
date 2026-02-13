package com.mcp.rag.llm.module;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // Remove the ToolCallbackProvider bean - not needed for MCP WebMVC!
}