package com.mcp.rag.llm.module.service;

import com.mcp.rag.llm.module.entity.Iab;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QdrantService {
    private final VectorStore vectorStore;

    @Autowired
    public QdrantService(VectorStore vectorStore){
        this.vectorStore = vectorStore;
    }

    public List<Iab> getQdrantSemanticData(String query){
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .similarityThreshold(0.50)
                        .topK(5)
                        .build()
        );
        log.info("getQdrantSemanticData: "+ docs.toString());
        if (!docs.isEmpty()) {
            List<Iab> results = null;
            try {
                results = mapToIab(docs);
                log.info("getQdrantSemanticData: " + docs.toString());
            }catch (Exception e){
                log.error("getQdrantSemanticData Error is : "+ e.getMessage());
            }
            return results;
        }
        return null;
    }

    private static List<Iab> mapToIab(List<Document> documents) {
        return documents.stream().map(doc -> {
            Map<String, Object> meta = doc.getMetadata();
            return new Iab(
                    meta.get("id") != null ? Long.valueOf(meta.get("id").toString()) : null,
                    meta.get("parentId") != null ? Long.valueOf(meta.get("parentId").toString()) : null,
                    (String) meta.get("name"),
                    (String) meta.get("tier1"),
                    (String) meta.get("tier2"),
                    (String) meta.get("tier3"),
                    (String) meta.get("tier4")
            );
        }).collect(Collectors.toList());
    }
}
