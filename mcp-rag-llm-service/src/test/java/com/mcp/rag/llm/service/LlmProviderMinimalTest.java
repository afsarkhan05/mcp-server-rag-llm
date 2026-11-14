package com.mcp.rag.llm.service;

import com.mcp.rag.llm.module.llm.OllamaLLMProvider;
import com.mcp.rag.llm.module.model.RagResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link OllamaLLMProvider}. No Spring context: only JUnit 5 + Mockito.
 */
@ExtendWith(MockitoExtension.class)
class LlmProviderMinimalTest {

    @Mock
    private OllamaChatModel ollamaChatModel;

    private OllamaLLMProvider llmProvider;

    @BeforeEach
    void setUp() {
        llmProvider = new OllamaLLMProvider(ollamaChatModel);
        // @Value field is not injected without a Spring context
        ReflectionTestUtils.setField(llmProvider, "ollamaModelName", "phi3:mini");
    }

    @Test
    void testGenerateAnswer_Success() {
        ChatResponse chatResponse = new ChatResponse(
                List.of(new Generation(new AssistantMessage("Software Development is..."))));
        when(ollamaChatModel.call(any(Prompt.class))).thenReturn(chatResponse);

        RagResponse response = llmProvider.generateAnswer("What is software development?");

        assertNotNull(response);
        assertEquals("Software Development is...", response.getAnswer());
        assertEquals("phi3:mini", response.getModelUsed());
        assertNotNull(response.getSources());
        assertTrue(response.getSources().isEmpty());
        assertFalse(response.getAnswer().isBlank());

        verify(ollamaChatModel, times(1)).call(any(Prompt.class));
    }

    @Test
    void testGenerateAnswer_ModelFailurePropagates() {
        when(ollamaChatModel.call(any(Prompt.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        assertThrows(RuntimeException.class, () -> llmProvider.generateAnswer("test"));
    }

    @Test
    void testGenerateAnswer_NullQuestion() {
        assertThrows(IllegalArgumentException.class, () -> llmProvider.generateAnswer(null));
        verify(ollamaChatModel, never()).call(any(Prompt.class));
    }

    @Test
    void testGenerateAnswer_EmptyQuestion() {
        assertThrows(IllegalArgumentException.class, () -> llmProvider.generateAnswer(""));
        verify(ollamaChatModel, never()).call(any(Prompt.class));
    }
}
