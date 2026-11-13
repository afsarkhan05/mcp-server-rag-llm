package com.mcp.rag.llm.service;

import com.mcp.rag.llm.module.llm.LLMProvider;
import com.mcp.rag.llm.module.llm.OllamaLLMProvider;
import com.mcp.rag.llm.module.model.RagResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Ultra-minimal unit test
 *
 * ONLY uses:
 * - JUnit 5 (from spring-boot-starter-test)
 * - Mockito (from spring-boot-starter-test)
 *
 * NO Spring context, NO web dependencies needed!
 */
@ExtendWith(MockitoExtension.class)
class LlmProviderMinimalTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private ChatModel chatModel;

    @InjectMocks
    private OllamaLLMProvider llmProvider;

    private List<Document> mockDocuments;

    @BeforeEach
    void setUp() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", "469");
        metadata.put("name", "Software Development");

        mockDocuments = Arrays.asList(
                new Document("Software Development content", metadata)
        );
    }

    @Test
    void testGenerateAnswer_Success() {
        // Arrange
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(mockDocuments);
        when(chatModel.call(anyString()))
                .thenReturn("Software Development is...");

        // Act
        RagResponse response = llmProvider.generateAnswer("What is software development?");

        // Assert - using plain JUnit assertions
        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertFalse(response.getAnswer().isBlank());
        assertEquals(1, response.getSources().size());

        verify(vectorStore, times(1)).similaritySearch(any(SearchRequest.class));
        verify(chatModel, times(1)).call(anyString());
    }

    @Test
    void testGenerateAnswer_EmptyResults() {
        // Arrange
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of());

        // Act
        RagResponse response = llmProvider.generateAnswer("test question");

        // Assert
        assertNotNull(response);
        assertTrue(response.getAnswer().contains("don't have enough information"));
        assertTrue(response.getSources().isEmpty());

        verify(chatModel, never()).call(anyString());
    }

    @Test
    void testGenerateAnswer_Exception() {
        // Arrange
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            llmProvider.generateAnswer("test");
        });
    }

    @Test
    void testGenerateAnswer_NullQuestion() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            llmProvider.generateAnswer(null);
        });
    }

    @Test
    void testGenerateAnswer_EmptyQuestion() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            llmProvider.generateAnswer("");
        });
    }
}
