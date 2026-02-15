package com.mcp.rag.llm.module.llm;

import com.mcp.rag.llm.module.model.RagResponse;
import com.mcp.rag.llm.module.model.RagSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "gemini")
@Slf4j
public class GeminiLLMProvider implements LLMProvider {

    private final GoogleGenAiChatModel chatModel;

    // Spring AI 2.0.0-M2 autoconfigures this bean based on your API key in YAML
    public GeminiLLMProvider(GoogleGenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public RagResponse generateAnswer(String userQuestion) {
        // 1. Build your custom instruction string
        String textPrompt = buildPromptForGemini(userQuestion);

        log.info("With SLFJ Inside Gemini LLM Provider");
        //return null;
        // 4. Send to Gemini
        ChatResponse response = chatModel.call(new Prompt(getUserMessage(textPrompt)));

        // In 2.0.x, AssistantMessage often uses .getText() or .getContent()
        // Depending on the specific interface implementation you see:
        String answer = response.getResult().getOutput().getText();
        return RagResponse.builder()
                .answer(answer)
                //.sources(sources)
                .modelUsed("gemini-1.5-flash")
                .build();
    }


}

