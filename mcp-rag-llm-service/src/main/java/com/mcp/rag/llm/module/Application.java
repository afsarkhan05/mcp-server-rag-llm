package com.mcp.rag.llm.module;

import com.mcp.rag.llm.module.tool.IabTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ToolCallbackProvider mcpToolProvider(IabTool iabTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(iabTool)
                .build();
    }
}