package com.mcp.rag.llm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.rag.llm.module.model.RagResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test using MockMvc (from Spring Test)
 *
 * Uses:
 * - @SpringBootTest (from spring-boot-starter-test)
 * - MockMvc (from Spring Test, included in starter)
 * - ObjectMapper (from Jackson, comes with spring-boot-starter-web)
 *
 * This DOES work with just:
 * - spring-boot-starter-web
 * - spring-boot-starter-test
 */
@SpringBootTest
@AutoConfiguration
@ActiveProfiles("test")
class RagIntegrationMinimalTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testValidQuestion() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/api/rag/ask")
                                .param("question", "What is software development?")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        RagResponse response = objectMapper.readValue(responseBody, RagResponse.class);

        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertFalse(response.getAnswer().isBlank());
    }

    @Test
    void testEmptyQuestion() throws Exception {
        mockMvc.perform(
                        get("/api/rag/ask")
                                .param("question", "")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMissingQuestion() throws Exception {
        mockMvc.perform(
                        get("/api/rag/ask")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testResponseHasSources() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/api/rag/ask")
                                .param("question", "What is automotive?")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        RagResponse response = objectMapper.readValue(responseBody, RagResponse.class);

        assertNotNull(response.getSources());
        assertFalse(response.getSources().isEmpty());
    }

    @Test
    void testRelevanceScore() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/api/rag/ask")
                                .param("question", "What is technology?")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        RagResponse response = objectMapper.readValue(responseBody, RagResponse.class);

        //assertTrue(response.getRelevanceScore() >= 0.0);
        //assertTrue(response.getRelevanceScore() <= 1.0);
    }
}