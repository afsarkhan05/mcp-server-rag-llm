package com.mcp.rag.llm.module.service;

import com.mcp.rag.llm.module.entity.Iab;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IabSearchService {

    // Injected: RedisTemplate, QdrantVectorStore, IabRepository (H2)
    private final IabCategoriesService iabService;
    private final VectorStore vectorStore;

    private final ResourceLoader resourceLoader;

    private CacheService cacheService;

    @Value("${dataFileName:iab.csv}")
    private String dataFileName;

    @Autowired
    public IabSearchService(IabCategoriesService iabService, VectorStore vectorStore, ResourceLoader resourceLoader, CacheService cacheService) {
        this.iabService = iabService;
        this.vectorStore = vectorStore;
        this.resourceLoader = resourceLoader;
        this.cacheService = cacheService;
    }

    public List<Iab> performTripleTierSearch(String query) {
        String cacheKey = cacheService.generateCacheKey("SEARCH", query);
        // TIER 1: REDIS (Standard Cache or Semantic Cache)
        List<Iab> cached = cacheService.getCachedResult(cacheKey, List.class);
        if (cached != null) return cached;

        // TIER 2: QDRANT (Semantic/Vector Search)
        // This finds "Sneakers" even if the query was "Athletic Shoes"
        /*List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.query(query).withSimilarityThreshold(0.75).withTopK(5)
        );*/
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .similarityThreshold(0.50)
                        .topK(5)
                        .build()
        );

        if (!docs.isEmpty()) {
            List<Iab> results = mapToIab(docs);
            // Store in cache for next time
            cacheService.cacheResult(cacheKey, results); // Backfill Redis
            return results;
        }

        // TIER 3: H2 (Exact/Like Match)
        List<Iab> h2Results = iabService.searchByName(query);
        if (!h2Results.isEmpty()) {
            // Store in cache for next time
            cacheService.cacheResult(cacheKey, h2Results);
            return h2Results;
        }

        return Collections.emptyList();
    }

    private List<Iab> mapToIab(List<Document> documents) {
        return documents.stream().map(doc -> {
            Map<String, Object> meta = doc.getMetadata();

            return new Iab(
                    (Integer) meta.get("id"),
                    (Integer) meta.get("parentId"),
                    (String) meta.get("name"),
                    (String) meta.get("tier1"),
                    (String) meta.get("tier2"),
                    (String) meta.get("tier3"),
                    (String) meta.get("tier4")
            );
        }).collect(Collectors.toList());
    }
}