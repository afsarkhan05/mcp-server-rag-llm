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
                .append("You are a specialized RAG assistant for IAB Taxonomy and Ordering.\n" +
                        "            You have access to the following tools:\n" +
                        "            - 'search_iabs_by_name_using_qdrant': Use this for any questions about ad categories, taxonomy IDs, or IAB tiers with semantic search.\n" +
                        "            - 'search_iabs_by_name': Use this if there is any question about getting by name.\n" +
                        "            - 'detail_about_developer': Use this if there is any question about the developer.\n" +
                        "            - 'redis_stats': Use if there is any question for redis or it's stats.\n" +
                        "            - 'get_iab_by_id': Use this to iab category needs to find by id.\n" +
                        "            - 'get_all_iabs': To get all list of iabs available.\n" +
                        "            \n" +
                        "            ALWAYS check the tools before answering. If a query is vague, use semantic search via 'search_iabs_by_name_using_qdrant'.")
                .append("If the answer is not in the context, say 'I don't know.'\n\n");

        //contextBuilder.append("## CONTEXT DOCUMENTS\n");


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

