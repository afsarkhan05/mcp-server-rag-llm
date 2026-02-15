package com.mcp.rag.llm.module.llm;

import com.mcp.rag.llm.module.model.RagResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "ollama")
@Slf4j
public class OllamaLLMProvider implements LLMProvider {

    private final OllamaChatModel ollama;

    @Value("${spring.ai.ollama.chat.options.model}")
    private String ollamaModelName;


    public OllamaLLMProvider(OllamaChatModel ollama) {
        this.ollama = ollama;
    }

    @Override
    public RagResponse generateAnswer(String userQuery) {

        String[] myArgs = {"get_all_iabs", "get_iab_by_id", "search_iabs_by_name_using_qdrant","search_iabs_by_name","detail_about_developer","redis_stats"};

        // 1. Define the System instructions
        SystemMessage system = new SystemMessage("""
        You are a specialized RAG assistant for IAB Taxonomy and Ordering.
            You have access to the following tools:
            - 'search_iabs_by_name_using_qdrant': Use this for any questions about ad categories, taxonomy IDs, or IAB tiers with semantic search.
            - 'search_iabs_by_name': Use this if there is any question about getting by name.
            - 'detail_about_developer': Use this if there is any question about the developer.
            - 'redis_stats': Use if there is any question for redis or it's stats.
            - 'get_iab_by_id': Use this to iab category needs to find by id.
            - 'get_all_iabs': To get all list of iabs available.
            
            ALWAYS check the tools before answering. If a query is vague, use semantic search via 'search_iabs_by_name_using_qdrant'.
        """);

        // 2. Define the User question
        UserMessage user = new UserMessage(userQuery);

        // 3. Set the Options (This is where the Tool Handshake happens)
        OllamaChatOptions options = OllamaChatOptions.builder()
                .model(ollamaModelName) // Pull this: 'ollama pull qwen2.5:7b'
                // Pass the @Bean or @Tool names here
                .toolNames(myArgs)
                .build();

        // 4. Create the final Prompt
        Prompt prompt = new Prompt(List.of(system, user), options);

        // 5. Call the model directly
        ChatResponse response = ollama.call(prompt);

        String answer = response.getResult().getOutput().getText();

        return RagResponse.builder()
                .answer(answer)
                //.sources(sources)
                .modelUsed(ollamaModelName)
                .build();
    }
}

