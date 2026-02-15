package com.mcp.rag.llm.module.llm;

import com.mcp.rag.llm.module.model.RagResponse;
import com.mcp.rag.llm.module.model.RagSource;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;

import java.util.List;
import java.util.Objects;

public interface LLMProvider {

    RagResponse generateAnswer(String prompt);

    default String buildPromptForGemini(String userQuestion) {
        StringBuilder contextBuilder = new StringBuilder();

        contextBuilder.append("## SYSTEM INSTRUCTIONS\n")
                .append("You are a helpful assistant. Use the provided context and attached media (images/videos/audio) to answer the question.\n")
                .append("If the answer is not in the context, say 'I don't know.'\n\n");

        contextBuilder.append("## CONTEXT DOCUMENTS\n");


        contextBuilder.append("## USER QUESTION\n")
                .append(userQuestion);

        return contextBuilder.toString();
    }

    default UserMessage getUserMessage(String textPrompt){
        // 3. Use the Builder (The 2.0.0-M2 Standard)
        return  UserMessage.builder()
                .text(textPrompt) // Sets the text
                //.media(mediaAttachments) // Accepts Collection<Media>
                .build();
    }
}

